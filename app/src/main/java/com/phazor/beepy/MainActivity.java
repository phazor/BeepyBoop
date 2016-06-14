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
import com.phazor.beepy.network.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.xml.transform.*;
import java.util.concurrent.*;
import java.text.*;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
	// Static variable for tracking number of requests made
	public static int mRequestCount;
	// Static variable for tracking no. of completed requests
	public static CountDownLatch mCountDownLatch;
	// TODO: Figure out a way of putting these in a callback
	public static ISSPassTimes mISSPassTimes;
	public static SunriseSunset mSunriseSunset;
	
	private GoogleApiClient mGoogleApiClient;
	private boolean hasLocation = false;
	// The 'specific' ISO-8601 that Sunrise Sunset uses
	// Java 8 Dates would be great for this type of parsing :)
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	
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
				
				Log.w("beepy", "Queueing HTTP requests");
				this.mRequestCount = 0;
				RequestQueue queue = Volley.newRequestQueue(this);
				queue.add(ISSPassTimesRequestCreator.createISSPassTimesRequest(mLastLocation));
				queue.add(SunsetSunriseRequestCreator.createSunsetSunriseRequest(mLastLocation));
				
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
											Log.w("beepy", "All requests finished!! ");
											
											// Get ISSPassTimes
											Log.w("beepy", "first pass-time:");
											Log.w("beepy", mISSPassTimes.getResponse().get(0).getRisetime());
											
											// Get SunriseSunset
											Log.w("beepy", "sunrise-sunset:");
											Log.w("beepy", mSunriseSunset.getResults().getSunrise());
											
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
	
	private boolean isDayTime(SunriseSunset sunriseSunset) throws ParseException {
		Date now = new Date();
		Date sunrise = sdf.parse(sunriseSunset.getResults().getSunrise());
		Date sunset = sdf.parse(sunriseSunset.getResults().getSunset());
		return ((now.compareTo(sunrise) < 0) || (now.compareTo(sunset) > 0));
	}
	
	// Do some math to determine when the next night-time pass-time is going to be
	private void showNextPassTime(ISSPassTimes issPassTimes, SunriseSunset sunriseSunset) {
		Log.w("showNextPassTime", "hello");
		TextView passTimeText = (TextView) findViewById(R.id.passTimeText);
		try {
			/* It would be nice to write this using functional methods
			 * However AIDE doesn't support Java 8, hence no .filter()
			 * Refactor this if Guava gets pulled in, and apply the 
			 * appropriate Pro-Guard settings.
			 *
			 * /sadface
			 */
			// Initially set Next Visible Pass to be the next pass
			Date nextVisiblePass = new Date(Long.parseLong(issPassTimes.getResponse().get(0).getRisetime())*1000);
			// Are we currently in day-time?
			if (isDayTime(sunriseSunset)) {
				// If yes, when is the next pass?
				Log.w("beepy", "we're in night time!!");
			} else {
				// If no, when is the next night-time pass?
				Log.w("beepy", "we're in day time!!");
				Iterator<ISSPassTimes.Response> i = issPassTimes.getResponse().iterator();
				for (ISSPassTimes.Response response : issPassTimes.getResponse()) {
					Date thisDate = new Date(Long.parseLong(response.getRisetime())*1000);
					passTimeText.append(" pass timez: " + thisDate.toLocaleString());
					Date sunsetDate = sdf.parse(sunriseSunset.getResults().getSunset());
					if (thisDate.after(sunsetDate)) {
						nextVisiblePass = thisDate;
						break;
					}
				}
			}
			Log.w("beepy", "next visible pass time: " + nextVisiblePass.toLocaleString());
			passTimeText.setText(nextVisiblePass.toLocaleString());
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
}
