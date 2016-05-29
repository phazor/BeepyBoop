package com.phazor.beepy;

import android.app.*;
import android.os.*;
import android.util.*;
import android.widget.*;
import com.phazor.beepy.position.json.*;
import java.util.*;
import retrofit2.*;
import retrofit2.converter.gson.*;
import retrofit2.http.*;




//@Keep
public class MainActivity extends Activity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		doStuff();
		
    }

	@Override
	protected void onResume()
	{
		// TODO: Implement this method
		super.onResume();
		doStuff();
		doYetMoreStuff();
		// timerHandler.post(mUpdateTimer);
		
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
		// Sup Goobers!!
		TextView top = (TextView) findViewById(R.id.topTextView);
		top.setText("Sup Goobers!!");
		top.setText(issNow.getMessage());
			
		TextView bottom = (TextView) findViewById(R.id.bottomTextView);
		bottom.append("Latitude:");
		bottom.append("\n");
		bottom.append("Longitude:");
		bottom.append("\n");
	}
	
	public void doYetMoreStuff() {
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
		
		// queryMap.put("lat", "16");
		//queryMap.put("lon", "106");
		//Call<ISSPassTimes> stuff = issPassTimesService.getISSPassTimes(queryMap);
		Call<ISSPassTimes> stuff = issPassTimesService.getISSPassTimes();
		
		stuff.enqueue(new Callback<ISSPassTimes>() {
				@Override
				public void onResponse(Call<ISSPassTimes> c, Response<ISSPassTimes> response) {
					try {
						Log.w("PassTimes", "blah");
						Log.w("PassTimes", "msg: " + response.body().getMessage());
						showNextPassTime(response.body());
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
					Log.w("PassTimes", "faill");
					if (t.getMessage().length() > 0) {
						Log.w("error - passTimes", t.getMessage());
					}
					// Ruh roh, no internet!
				}
			});
		Log.w("Retrofit", stuff.toString());
	}
	
	private void showNextPassTime(ISSPassTimes issPassTime) {
		TextView bottom = (TextView) findViewById(R.id.bottomTextView);
		Log.w("showNextPassTime", "hello");
		if (issPassTime.getResponse() != null && issPassTime.getResponse().length > 0) {
			Log.w("PassTimes", "No. Risetimes" + issPassTime.getResponse().length);
			Date date = new Date(Long.parseLong(issPassTime.getResponse()[0].getRisetime())*1000);
			Log.w("risetime", "risetime: " + date.toLocaleString());
			bottom.append("\n");
			bottom.append(issPassTime.getResponse()[0].getRisetime());
			bottom.append("\n");
			bottom.append(date.toLocaleString());
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
		Call<ISSPassTimes> getISSPassTimes(@QueryMap Map<String, String> queryMap);
		
		@GET("iss-pass.json?lat=16&lon=106")
		Call<ISSPassTimes> getISSPassTimes();
	}
}
