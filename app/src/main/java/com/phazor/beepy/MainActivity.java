package com.phazor.beepy;

import android.app.*;
import android.graphics.*;
import android.location.*;
import android.os.*;
import android.util.*;
import android.widget.*;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.google.android.gms.common.*;
import com.google.android.gms.common.api.*;
import com.google.android.gms.location.*;
import com.google.gson.*;
import com.phazor.beepy.position.json.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.xml.transform.*;
import java.util.concurrent.*;
import java.text.*;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
	private GoogleApiClient mGoogleApiClient;
	private static boolean hasLocation = false;
	// Static variable for tracking number of requests made
	private static int mRequestCount;
	// Static variable for tracking no. of completed requests
	private static CountDownLatch mCountDownLatch;
	
	// ISO-8601
	private static SimpleDateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
	// The 'specific' ISO-8601 that Sunrise Sunset uses
	// Java 8 Dates would be great for this type of parsing :)
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	
	// Global
	// TODO: Figure out a way of putting these in a callback
	private ISSPassTimes mISSPassTimes;
	private SunriseSunset mSunriseSunset;
	
	/*
	 * Stuff that happens after connecting to Google Play Services
	 * 
	 * Specifically: Retrieving location and subsequently triggering the UI to update
	 */
	@Override
	public void onConnected(Bundle bundle) {
		
		// TODO: Have a better way of signalling the main commands to run
		if (!hasLocation) {
			Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
				mGoogleApiClient);
			if (mLastLocation != null) {
				hasLocation = true;
				
				this.mRequestCount = 0;
				RequestQueue queue = Volley.newRequestQueue(this);
				GsonRequest<ISSPassTimes> request = createISSPassTimesRequest(mLastLocation);
				queue.add(request);
				queue.add(createSunsetSunriseRequest(mLastLocation));
				
				/* Slightly janky code to run a thread when x number of requests finish.
				 * Retrofit and RXJava make this easy but cannot be used because parameter
				 * annotations aren't supported by the mobile IDE I'm using - AIDE.
				 *
				 * /sadface
				 */
				mCountDownLatch = new CountDownLatch(mRequestCount);
				final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
				new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								mCountDownLatch.await();
								mainThreadHandler.post(new Runnable() {
										@Override
										public void run() {
											TextView passTimeText = (TextView) findViewById(R.id.passTimeText);
											passTimeText.append(" All requests finished!! ");
											
											// Get ISSPassTimes
											passTimeText.append(mISSPassTimes.getResponse()[0].getRisetime());
											passTimeText.append(" pass-times success!! ");
											
											// Get SunriseSunset
											passTimeText.append(mSunriseSunset.getResults().getSunrise());
											passTimeText.append(" sunrise-sunset success!! ");
											
											showNextPassTime(mISSPassTimes, mSunriseSunset);
										}
									});
							}
							catch (InterruptedException e) {
								// TODO: Handle this exception
								e.printStackTrace();
							}
						}
					}).start();
				
			}
		}
	}
	
	// TODO: Refactor into a separate class
	private GsonRequest<ISSPassTimes> createISSPassTimesRequest(Location mLastLocation) {
		this.mRequestCount++;
		StringBuilder url = new StringBuilder("http://api.open-notify.org/iss-pass.json");
		url.append("?lat=");
		url.append(Math.round(mLastLocation.getLatitude()));
		url.append("&lon=");
		url.append(Math.round(mLastLocation.getLongitude()));
		
		return new GsonRequest<ISSPassTimes>(url.toString(),
											 ISSPassTimes.class,
											 null,
											 createISSPassTimesSuccessListener(),
											 createMyReqErrorListener());
	}
	
	// TODO: Refactor into a separate class
	private GsonRequest<SunriseSunset> createSunsetSunriseRequest(Location mLastLocation) {
		this.mRequestCount++;
		StringBuilder url = new StringBuilder("http://api.sunrise-sunset.org/json");
		url.append("?lat=");
		url.append(Math.round(mLastLocation.getLatitude()));
		url.append("&lng=");
		url.append(Math.round(mLastLocation.getLongitude()));
		url.append("&formatted=0");

		return new GsonRequest<SunriseSunset>(url.toString(),
											  SunriseSunset.class,
											  null,
											  createSunriseSunsetSuccessListener(),
											  createMyReqErrorListener());
	}
	
	// Code that executes when the connection to Google Play Services fails
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO: Prompt the user to update google play services, if it is out of date
		TextView passTimeText = (TextView) findViewById(R.id.passTimeText);
		passTimeText.setText("Urgghh: " + result.getResolution() + " " + result.getErrorMessage() + " " + result.getErrorCode());
		// TODO: Stub
	}
	
	// Code that executes when the connection to Google Play Services is suspended
	@Override
	public void onConnectionSuspended(int val) {
		TextView passTimeText = (TextView) findViewById(R.id.passTimeText);
		passTimeText.setText("Suspended :D");
		// TODO: Stub
	}
	
	/*
	 * This code executes when recieving a successful HTTP response from the Pass Times API
	 */
	private Response.Listener<ISSPassTimes> createISSPassTimesSuccessListener() {
		return new Response.Listener<ISSPassTimes>() {
			@Override
			public void onResponse(ISSPassTimes response) {
				mCountDownLatch.countDown();
				mISSPassTimes = response;
				
				// Do whatever you want to do with response;
				// Like response.tags.getListing_count(); etc. etc.
			}
		};
	}
	
	/*
	 * This code executes when recieving a successful HTTP response from the Sunrise Sunset API
	 */
	private Response.Listener<SunriseSunset> createSunriseSunsetSuccessListener() {
		return new Response.Listener<SunriseSunset>() {
			@Override
			public void onResponse(SunriseSunset response) {
				mCountDownLatch.countDown();
				mSunriseSunset = response;
				
				// Do whatever you want to do with response;
				// Like response.tags.getListing_count(); etc. etc.
			}
		};
	}
	
	/*
	 * Handle all HTTP Request errors here
	 *
	 * Note: For granularity of error handling this method could be
	 * newed during creation of each request
	 */
	private Response.ErrorListener createMyReqErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				// Do whatever you want to do with error.getMessage();
			}
		};
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		// Create an instance of GoogleAPIClient.
		// This allows us to later query Google Play Services for location
		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
		}
    }
	
	protected void onStart() {
		mGoogleApiClient.connect();
		super.onStart();
	}

	protected void onStop() {
		mGoogleApiClient.disconnect();
		super.onStop();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		
	}

	@Override
	protected void onPause()
	{
		super.onPause();
	}
	
	public void doStuff(ISSCurrentPos issNow) {
		TextView latitude = (TextView) findViewById(R.id.latitudeText);
		TextView longitude = (TextView) findViewById(R.id.longitudeText);
		// TODO: Use String Buffer and convert location to human redable form
		latitude.setText(issNow.getPosition().getLatitude());
		latitude.setTextColor(Color.WHITE);
		longitude.setText(issNow.getPosition().getLongitude());
		longitude.setTextColor(Color.WHITE);
	}
	
	// Do some math to determine when the next night-time pass-time is going to be
	private void showNextPassTime(ISSPassTimes issPassTimes, SunriseSunset sunriseSunset) {
		Log.w("showNextPassTime", "hello");
		TextView passTimeText = (TextView) findViewById(R.id.passTimeText);
		// Current Time (US Time, to match the other APIs)
		//Calendar now = Calendar.getInstance(Locale.US);
		Date now = new Date();
		
		
		try {
			// Current Sunrise Time
			Date sunrise = sdf.parse(sunriseSunset.getResults().getSunrise());
			// Current Sunset Time
			Date sunset = sdf.parse(sunriseSunset.getResults().getSunset());
			
			/* It would be great to write this using functional methods
			 * However AIDE doesn't support Java 8, hence no .filter()
			 * TODO: Convert to Guava, use appropriate pro-guard settings
			 *
			 * /sadface
			 */
			// Are we currently in day-time?
			if ((now.compareTo(sunrise) < 0) || (now.compareTo(sunset) > 0)) {
				passTimeText.append(" we're in night time!!");
			} else {
				passTimeText.append(" we're in day time!!");
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		
		
			
		// If yes, when is the next pass?
		
		// If no, when is the next night-time pass?
		
		// End
		if (issPassTimes.getResponse() != null && issPassTimes.getResponse().length > 0) {
			Log.w("PassTimes", "No. Risetimes" + issPassTimes.getResponse().length);
			Date date = new Date(Long.parseLong(issPassTimes.getResponse()[0].getRisetime())*1000);
			Log.w("risetime", "risetime: " + date.toLocaleString());
			passTimeText.append(" next pass time: " + date.toLocaleString());
			passTimeText.setTextColor(Color.WHITE);
		} else {
			if (issPassTimes.toString().length() < 0) {
				Log.w("error - passTimes", "msg: " + issPassTimes.toString());	
			}
			Log.w("error - passTimes", "isPassTime.toString().length() = 0");
		}
	}
	
	public class GsonRequest<T> extends Request<T> {
		private final Gson gson = new Gson();
		private final Class<T> clazz;
		private final Map<String, String> headers;
		private final Response.Listener<T> listener;

		/**
		 * Make a GET request and return a parsed object from JSON.
		 *
		 * @param url URL of the request to make
		 * @param clazz Relevant class object, for Gson's reflection
		 * @param headers Map of request headers
		 */
		public GsonRequest(String url, Class<T> clazz, Map<String, String> headers,
						   Response.Listener<T> listener, Response.ErrorListener errorListener) {
			super(Request.Method.GET, url, errorListener);
			this.clazz = clazz;
			this.headers = headers;
			this.listener = listener;
		}

		@Override
		public Map<String, String> getHeaders() throws AuthFailureError {
			return headers != null ? headers : super.getHeaders();
		}

		@Override
		protected void deliverResponse(T response) {
			listener.onResponse(response);
		}

		@Override
		protected Response<T> parseNetworkResponse(NetworkResponse response) {
			try {
				String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));
				return Response.success(
                    gson.fromJson(json, clazz),
                    HttpHeaderParser.parseCacheHeaders(response));
			} catch (UnsupportedEncodingException e) {
				return Response.error(new ParseError(e));
			} catch (JsonSyntaxException e) {
				return Response.error(new ParseError(e));
			}
		}
	}
	
}
