package com.phazor.beepy;

import android.app.*;
import android.graphics.*;
import android.location.*;
import android.os.*;
import android.util.*;
import android.widget.*;
import com.google.android.gms.common.*;
import com.google.android.gms.common.api.*;
import com.google.android.gms.location.*;
import com.phazor.beepy.position.json.*;
import java.util.*;
import retrofit2.*;
import retrofit2.converter.gson.*;
import retrofit2.http.*;

//@Keep
public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
	private GoogleApiClient mGoogleApiClient;
	private boolean hasLocation = false;
	
	@Override
	public void onConnected(Bundle bundle) {
		TextView passTimeText = (TextView) findViewById(R.id.passTimeText);
		passTimeText.setText("Sup!!");
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
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
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
		// TODO: Implement this method
		super.onResume();
		
	}

	@Override
	protected void onPause()
	{
		// TODO: Implement this method
		super.onPause();
	}
	
	/*
	 * doStuff
	 * 
	 * This method does exactly what it says on the tin
	 * TODO: Come up with a less foolish name
	 */
	public void doStuff() {
		Retrofit retrofit = new Retrofit.Builder()
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
	}
	
	public void doMoreStuff(ISSCurrentPos issNow) {
		TextView latitude = (TextView) findViewById(R.id.latitudeText);
		TextView longitude = (TextView) findViewById(R.id.longitudeText);
		// TODO: Use String Buffer
		latitude.setText(issNow.getPosition().getLatitude());
		latitude.setTextColor(Color.WHITE);
		longitude.setText(issNow.getPosition().getLongitude());
		longitude.setTextColor(Color.WHITE);
	}
	
	public void doYetMoreStuff(final Location currentLocation) {
		// mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
		// mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
		
		// TODO: Refactor this out
		Retrofit retrofit = new Retrofit.Builder()
			.baseUrl("http://api.open-notify.org/")
			.addConverterFactory(GsonConverterFactory.create())
			.build();

		ISSPassTimesRetrieverService issPassTimesService = retrofit.create(ISSPassTimesRetrieverService.class);

		Map<String, String> queryMap = new HashMap<String, String>();

		// For now, we've gotta hardcode the location since there seems to
		// be something funny going on between AIDE, Proguard that strips
		// method argument annotations on Retrofit		
		
		//Call<ISSPassTimes> stuff = issPassTimesService.getISSPassTimes("16", "106");
		Call<ISSPassTimes> stuff = issPassTimesService.getISSPassTimes();
		
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
		// http://api.open-notify.org/iss-now.json
		@GET("iss-now.json")
		Call<ISSCurrentPos> getISSPos();
	}
	
	public interface ISSPassTimesRetrieverService {
		// http://api.open-notify.org/iss-pass.json?lat=LAT&lon=LON
		@GET("iss-pass.json")
		Call<ISSPassTimes> getISSPassTimes(@Query("lat") String lat, @Query("lon") String lon);
		
		@GET("iss-pass.json?lat=16&lon=106")
		Call<ISSPassTimes> getISSPassTimes();
	}
}
