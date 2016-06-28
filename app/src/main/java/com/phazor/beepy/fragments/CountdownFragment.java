package com.phazor.beepy.fragments;

import android.app.*;
import android.content.*;
import android.location.*;
import android.os.*;
import android.provider.*;
import android.support.v4.app.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.google.android.gms.common.*;
import com.google.android.gms.common.api.*;
import com.google.android.gms.location.*;
import com.phazor.beepy.*;
import com.phazor.beepy.network.*;
import com.phazor.beepy.position.json.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;

import android.support.v4.app.Fragment;
import com.phazor.beepy.R;

public class CountdownFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
	// Static variable for tracking number of requests made
	public static int mRequestCount;
	// Static variable for tracking no. of completed requests
	public static CountDownLatch mCountDownLatch;
	// TODO: Figure out a way of putting these in a callback
	public static ISSPassTimes mISSPassTimes;
	public static SunriseSunset mSunriseSunset;
	public Date mNextVisiblePass;
	// TODO: Refactor all references into the main activity
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
				// TODO: Verify that it is safe to access the context via the activity
				RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
				queue.add(ISSPassTimesRequestCreator.createISSPassTimesRequest(mLastLocation));
				queue.add(SunsetSunriseRequestCreator.createSunsetSunriseRequest(mLastLocation));
				
				/* Slightly janky code to run a thread when x number of requests finish. Retrofit
				 * and RXJava make this more elegant but cannot be used because parameter 
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
											
											setNextPassTime(mISSPassTimes, mSunriseSunset);
											showNextPassTime();
											showCountdownTimer();
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
		TextView passTimeText = (TextView) getView().findViewById(R.id.passTimeText);
		passTimeText.setText("Urgghh: " + result.getResolution() + " " + result.getErrorMessage() + " " + result.getErrorCode());
		// TODO: Stub
	}
	
	// Code that executes when the connection to Google Play Services is suspended
	@Override
	public void onConnectionSuspended(int val) {
		TextView passTimeText = (TextView) getView().findViewById(R.id.passTimeText);
		passTimeText.setText("Suspended :D");
		// TODO: Stub
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		// TODO: Confirm no longer required in Fragments
        //setContentView(R.layout.activity_main);
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
								 
		Log.w("beepy", "creating the Fragment View");
		
        // Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_countdown, container, false);
		
		view.findViewById(R.id.fab_alarm).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if (mNextVisiblePass != null) {
					Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);
					i.putExtra(AlarmClock.EXTRA_DAYS, mNextVisiblePass.getDay());
					i.putExtra(AlarmClock.EXTRA_HOUR, mNextVisiblePass.getHours());
					i.putExtra(AlarmClock.EXTRA_MINUTES, mNextVisiblePass.getMinutes() - 5);
					i.putExtra(AlarmClock.EXTRA_MESSAGE, "Next ISS sighting");
					startActivity(i);
				}
			}
		});
		
        return view;
    }

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		
		Log.w("beepy", "attaching the Fragment to the Activity");

		// TODO Verify that it is safe to access the context here								 
		// Create an instance of GoogleAPIClient.
		// This allows us to later query Google Play Services for location
		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
		}
	}
	
	

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		
		Log.w("beepy", "creating the Fragment (onActivityCreated)");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		Log.w("beepy", "resuming the fragment");
	}
	
	@Override
	public void onStart() {
		mGoogleApiClient.connect();
		super.onStart();
	}

	@Override
	public void onStop() {
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
	private void setNextPassTime(ISSPassTimes issPassTimes, SunriseSunset sunriseSunset) {
		Log.w("showNextPassTime", "hello");
		try {
			/* It would be nice to write this using functional methods
			 * However AIDE doesn't support Java 8, hence no .filter()
			 * Refactor this if Guava gets pulled in, and apply the 
			 * appropriate Pro-Guard settings.
			 *
			 * /sadface
			 */
			// Initially set Next Visible Pass to be the next pass
			mNextVisiblePass = new Date(Long.parseLong(issPassTimes.getResponse().get(0).getRisetime())*1000);
			// Are we currently in day-time?
			if (isDayTime(sunriseSunset)) {
				// If yes, when is the next pass?
				Log.w("beepy", "we're in night time!!");
			} else {
				// If no, when is the next night-time pass?
				Log.w("beepy", "we're in day time!!");
				for (ISSPassTimes.Response response : issPassTimes.getResponse()) {
					Date thisDate = new Date(Long.parseLong(response.getRisetime())*1000);
					Log.w(" pass timez: ", thisDate.toLocaleString());
					Date sunsetDate = sdf.parse(sunriseSunset.getResults().getSunset());
					if (thisDate.after(sunsetDate)) {
						mNextVisiblePass = thisDate;
						break;
					}
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	// TODO: Guard against mNextPassTime not being set
	private void showCountdownTimer() {
		final TextView countdownText = (TextView) getView().findViewById(R.id.countdownText);
		new CountDownTimer((Math.abs(new Date().getTime() - mNextVisiblePass.getTime())), 1000) {
			
			public void onTick(long millisUntilFinished) {
				SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
				//countdownText.setText(String.valueOf(millisUntilFinished / 1000) + "s");
				countdownText.setText(f.format(millisUntilFinished));
			}
			
			public void onFinish() {
				countdownText.setText("Loading...");
				// TODO: Trigger re-calculation of nextPassTime
			}
			
		}.start();
	}
	
	// TODO: Guard against mNextPassTime not being set
	private void showNextPassTime() {
			TextView passTimeText = (TextView) getView().findViewById(R.id.passTimeText);
			Log.w("beepy", "next visible pass time: " + mNextVisiblePass.toLocaleString());
			passTimeText.setText(mNextVisiblePass.toLocaleString());
		
	}
	
}
