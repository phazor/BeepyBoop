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

//@Keep
public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
	private GoogleApiClient mGoogleApiClient;
	private boolean hasLocation = false;
	
	@Override
	public void onConnected(Bundle bundle) {
		TextView passTimeText = (TextView) findViewById(R.id.passTimeText);
		//passTimeText.setText("Sup!!");
		// Only check location once, per app run
		if (!hasLocation) {
			Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
				mGoogleApiClient);
			if (mLastLocation != null) {
				hasLocation = true;
				doYetMoreStuff(mLastLocation);
			}
		}
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO: Prompt the user to update google play services, if it is out of date
		TextView passTimeText = (TextView) findViewById(R.id.passTimeText);
		passTimeText.setText("Urgghh: " + result.getResolution() + " " + result.getErrorMessage() + " " + result.getErrorCode());
		// TODO: Stub
	}
	
	@Override
	public void onConnectionSuspended(int val) {
		TextView passTimeText = (TextView) findViewById(R.id.passTimeText);
		passTimeText.setText("Suspended :D");
		// TODO: Stub
	}
	
	private Response.Listener<ISSPassTimes> createMyReqSuccessListener() {
		return new Response.Listener<ISSPassTimes>() {
			@Override
			public void onResponse(ISSPassTimes response) {
				TextView passTimeText = (TextView) findViewById(R.id.passTimeText);
				passTimeText.setText(response.getResponse()[0].getRisetime());
				// Do whatever you want to do with response;
				// Like response.tags.getListing_count(); etc. etc.
			}
		};
	}
	
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
		
		RequestQueue queue = Volley.newRequestQueue(this);
		/*GsonRequest<ISSPassTimes> myReq = new GsonRequest<ISSPassTimes>(Request.Method.GET,
															  "http://JSONURL/",
															  5,
															  ISSPassTimes.class,
															  createMyReqSuccessListener(),
															  createMyReqErrorListener());
		*/
		GsonRequest<ISSPassTimes> myReq = new GsonRequest<ISSPassTimes>("http://api.open-notify.org/iss-pass.json?lat=16&lon=106",
																		ISSPassTimes.class,
																		null,
																		createMyReqSuccessListener(),
																		createMyReqErrorListener());
		// public GsonRequest(String url, Class<T> clazz, Map<String, String> headers, Listener<T> listener, ErrorListener errorListener) {
		queue.add(myReq);
		
		/*
		// Instantiate the RequestQueue.
		RequestQueue queue = Volley.newRequestQueue(this);
		String url ="http://www.google.com";

		// Request a string response from the provided URL.
		StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
			new Response.Listener<String>() {
				@Override
				public void onResponse(String response) {
					TextView passTimeText = (TextView) findViewById(R.id.passTimeText);
					// Display the first 500 characters of the response string.
					passTimeText.setText("Response is: "+ response.substring(0,500));
				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					TextView passTimeText = (TextView) findViewById(R.id.passTimeText);
					passTimeText.setText("That didn't work!");
				}
			});
		// Add the request to the RequestQueue.
		queue.add(stringRequest);
		*/
		
		// Create an instance of GoogleAPIClient.
		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
		}
		doStuff();
		
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
	
	/*
	 * doStuff
	 * 
	 * This method does exactly what it says on the tin
	 * TODO: Come up with a less foolish name
	 */
	public void doStuff() {
	/*	Retrofit retrofit = new Retrofit.Builder()
			.baseUrl("http://api.open-notify.org/")
			.addConverterFactory(GsonConverterFactory.create())
			.build();
			
		ISSNowRetrieverService issService = retrofit.create(ISSNowRetrieverService.class);
		
		Call<ISSCurrentPos> stuff = issService.getISSPos();
		stuff.enqueue(new Callback<ISSCurrentPos>() {
			@Override
			public void onResponse(Call<ISSCurrentPos> c, Response<ISSCurrentPos> response) {
				try {
					Log.w("blah", "blah");
					Log.w("blah", response.body().getMessage());
					doMoreStuff(response.body());
				} catch(Exception ex) {
					Log.w("blah", "cripes");
				}
			}
			
			@Override
			public void onFailure(Call<ISSCurrentPos> c, Throwable t) {
				// TODO: Handle when no internet connection is present
				Log.w("blah", "faill");
				if (t.getMessage().length() > 0) {
					Log.w("error", t.getMessage());
				}
				// Ruh roh, no internet!
			}
		});
		Log.w("Retrofit", stuff.toString());
		*/
	}
	
	public void doMoreStuff(ISSCurrentPos issNow) {
		TextView latitude = (TextView) findViewById(R.id.latitudeText);
		TextView longitude = (TextView) findViewById(R.id.longitudeText);
		// TODO: Use String Buffer and convert location to human redable form
		latitude.setText(issNow.getPosition().getLatitude());
		latitude.setTextColor(Color.WHITE);
		longitude.setText(issNow.getPosition().getLongitude());
		longitude.setTextColor(Color.WHITE);
	}
	
	public void doYetMoreStuff(final Location currentLocation) {
		// mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
		// mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
		/*
		// TODO: Refactor this out
		Retrofit retrofit = new Retrofit.Builder()
			.baseUrl("http://api.open-notify.org/")
			.addConverterFactory(GsonConverterFactory.create())
			.build();

		ISSPassTimesRetrieverService issPassTimesService = retrofit.create(ISSPassTimesRetrieverService.class);

		// For now, we've gotta hardcode the location since there seems to
		// be something funny going on between AIDE, Proguard that strips
		// method argument annotations on Retrofit		
		
		Call<ISSPassTimes> stuff = issPassTimesService.getISSPassTimes(16, 106);
		//Call<ISSPassTimes> stuff = issPassTimesService.getISSPassTimes();
		
		stuff.enqueue(new Callback<ISSPassTimes>() {
				@Override
				public void onResponse(Call<ISSPassTimes> c, Response<ISSPassTimes> response) {
					try {
						Log.w("PassTimes", "blah");
						Log.w("PassTimes", "msg: " + response.body().getMessage());
						showNextPassTime(response.body(), currentLocation);
					} catch(Exception ex) {
						Log.w("PassTimes", "cripes");
						if (ex != null && ex.getMessage().length() > 0) {							
							ex.printStackTrace();
							if (ex.getCause() != null) {
								ex.getCause().printStackTrace();
							}
						}
					}
				}

				@Override
				public void onFailure(Call<ISSPassTimes> c, Throwable t) {
					// TODO: Handle when no internet connection is present
					Log.w("PassTimes", "faill");
					if (t.getMessage().length() > 0) {
						Log.w("error - passTimes", t.getMessage());
					}
					// Ruh roh, no internet!
				}
			});
		Log.w("Retrofit", stuff.toString());
		*/
	}
	
	private void showNextPassTime(ISSPassTimes issPassTime, Location currentLocation) {
		Log.w("showNextPassTime", "hello");
		if (issPassTime.getResponse() != null && issPassTime.getResponse().length > 0) {
			Log.w("PassTimes", "No. Risetimes" + issPassTime.getResponse().length);
			Date date = new Date(Long.parseLong(issPassTime.getResponse()[0].getRisetime())*1000);
			Log.w("risetime", "risetime: " + date.toLocaleString());
			
			TextView passTimeText = (TextView) findViewById(R.id.passTimeText);
			//passTimeText.setText(date.toLocaleString());
			passTimeText.setText(String.valueOf(currentLocation.getAltitude()));
			passTimeText.setTextColor(Color.WHITE);
		} else {
			if (issPassTime.toString().length() < 0) {
				Log.w("error - passTimes", "msg: " + issPassTime.toString());	
			}
			Log.w("error - passTimes", "isPassTime.toString().length() = 0");
		}
	}
	
	public interface ISSNowRetrieverService {
		/*
		// http://api.open-notify.org/iss-now.json
		@GET("iss-now.json")
		Call<ISSCurrentPos> getISSPos();
		*/
	}
	
	public interface ISSPassTimesRetrieverService {
		/*
		// http://api.open-notify.org/iss-pass.json?lat=LAT&lon=LON
		@GET("/iss-pass.json")
		Call<ISSPassTimes> getISSPassTimes(@retrofit2.http.Query("lat") int lat, @retrofit2.http.Query("lon") int lon);
		
		@GET("iss-pass.json?lat=16&lon=106")
		Call<ISSPassTimes> getISSPassTimes();
		*/
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
	
	/*
	public class GsonRequest<T> extends Request<T> {
		private final Gson gson = new Gson();
		private final Class<T> clazz;
		private final Map<String, String> headers;
		private final Listener<T> listener;

		/**
		 * Make a GET request and return a parsed object from JSON.
		 *
		 * @param url URL of the request to make
		 * @param clazz Relevant class object, for Gson's reflection
		 * @param headers Map of request headers
		 */
		/*public GsonRequest(String url, Class<T> clazz, Map<String, String> headers,
						   Listener<T> listener, ErrorListener errorListener) {
			super(Method.GET, url, errorListener);
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
	} */
	
	/*
	public class ISSPassTimesRequest extends Request<ISSPassTimes>
	{

		@Override
		protected Response<ISSPassTimes> parseNetworkResponse(NetworkResponse response)
		{
			// TODO: Implement this method
			return null;
		}

		@Override
		protected void deliverResponse(ISSPassTimes response)
		{
			// TODO: Implement this method
		}
		
	}
	*/
}
