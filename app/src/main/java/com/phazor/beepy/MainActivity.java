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

//@Keep
public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
	private GoogleApiClient mGoogleApiClient;
	private boolean hasLocation = false;
	
	/*
	 * Stuff that happens after connecting to Google Play Services
	 * 
	 * Specifically: Retrieving location and subsequently triggering the UI to update
	 */
	@Override
	public void onConnected(Bundle bundle) {
		TextView passTimeText = (TextView) findViewById(R.id.passTimeText);
		//passTimeText.setText("Sup!!");
		// TODO: Only check location once, per app run
		if (!hasLocation) {
			Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
				mGoogleApiClient);
			if (mLastLocation != null) {
				hasLocation = true;

				RequestQueue queue = Volley.newRequestQueue(this);
				
				// Trigger the request to the ISS Pass Times API
				fireISSPassTimesRequest(queue, mLastLocation);
				// Trigger the request to the Sunrise Sunset API
				fireSunsetSunriseRequest(queue, mLastLocation);
				
				// doYetMoreStuff(mLastLocation);
			}
		}
	}
	
	// TODO: Refactor into a separate class
	private void fireISSPassTimesRequest(RequestQueue queue, Location mLastLocation) {
		StringBuilder url = new StringBuilder("http://api.open-notify.org/iss-pass.json");
		url.append("?lat=");
		url.append(Math.round(mLastLocation.getLatitude()));
		url.append("&lon=");
		url.append(Math.round(mLastLocation.getLongitude()));
		
		GsonRequest<ISSPassTimes> myReq = new GsonRequest<ISSPassTimes>(url.toString(),
																		ISSPassTimes.class,
																		null,
																		createISSPassTimesSuccessListener(),
																		createMyReqErrorListener());
		queue.add(myReq);
	}
	
	// TODO: Refactor into a separate class
	private void fireSunsetSunriseRequest(RequestQueue queue, Location mLastLocation) {

		// http://api.sunrise-sunset.org/json?lat=36.7201600&lng=-4.4203400
		StringBuilder url = new StringBuilder("http://api.sunrise-sunset.org/json");
		url.append("?lat=");
		url.append(Math.round(mLastLocation.getLatitude()));
		url.append("&lng=");
		url.append(Math.round(mLastLocation.getLongitude()));

		GsonRequest<SunriseSunset> myReq = new GsonRequest<SunriseSunset>(url.toString(),
																		SunriseSunset.class,
																		null,
																		createSunriseSunsetSuccessListener(),
																		createMyReqErrorListener());
		queue.add(myReq);
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
				TextView passTimeText = (TextView) findViewById(R.id.passTimeText);
				passTimeText.append(response.getResponse()[0].getRisetime());
				passTimeText.append(" pass-times success!! ");
				showNextPassTime(response);
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
				TextView passTimeText = (TextView) findViewById(R.id.passTimeText);
				passTimeText.append(response.getResults().getSunrise());
				passTimeText.append(" sunrise-sunset success!! ");
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
	
	private void showNextPassTime(ISSPassTimes issPassTime) {
		Log.w("showNextPassTime", "hello");
		if (issPassTime.getResponse() != null && issPassTime.getResponse().length > 0) {
			Log.w("PassTimes", "No. Risetimes" + issPassTime.getResponse().length);
			Date date = new Date(Long.parseLong(issPassTime.getResponse()[0].getRisetime())*1000);
			Log.w("risetime", "risetime: " + date.toLocaleString());
			
			TextView passTimeText = (TextView) findViewById(R.id.passTimeText);
			passTimeText.append(" next pass time: " + date.toLocaleString());
			passTimeText.setTextColor(Color.WHITE);
		} else {
			if (issPassTime.toString().length() < 0) {
				Log.w("error - passTimes", "msg: " + issPassTime.toString());	
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
