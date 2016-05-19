package com.phazor.beepy;

import android.app.*;
import android.os.*;
import android.widget.TextView;
import com.google.gson.Gson;
import retrofit2.*;
import retrofit2.http.GET;
import retrofit2.BuiltInConverters;
import retrofit2.Converter;
import okhttp3.ResponseBody;

import android.widget.*;
import java.net.*;
import java.io.*;
import android.util.*;

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
		// timerHandler.post(mUpdateTimer);
		
	}

	@Override
	protected void onPause()
	{
		// TODO: Implement this method
		super.onPause();
	}
	
	private final Runnable mUpdateTimer = new Runnable() {
		public void run() {
			
			doStuff();
			
			/*
			
			TextView t = (TextView) findViewById(R.id.topTextView);
			t.append("Blahh");
			HttpURLConnection connection = null;
			try {
				// Retrieve the response
				String issPosJsonAPI = "http://api.open-notify.org/iss-now.json";
				Log.w("BeepyBoop", "a " + issPosJsonAPI.toString());
				URL url = new URL(issPosJsonAPI);
				Log.w("BeepyBoop", "b " + url.toString());
				connection = (HttpURLConnection) url.openConnection();
				Log.w("BeepyBoop", "c " + connection.toString());
				InputStream in = new BufferedInputStream(connection.getInputStream());
				Log.w("BeepyBoop", "d " + in.toString());
				BufferedReader r = new BufferedReader(new InputStreamReader(in));
				Log.w("BeepyBoop", "e");
				// Write the response to a variable
				StringBuilder sb = new StringBuilder();
				Log.w("BeepyBoop", "f");
				String line;
				Log.w("BeepyBoop", "g");
				while ((line = r.readLine()) != null) {
					sb.append(line).append('\n');
				}
				Log.w("BeepyBoop", sb.toString());
			} catch (MalformedURLException ex) {
				Log.w("BeepyBoop", ex);
			} catch(IOException ex) {
				Log.w("BeepyBoop", ex);
			} catch(Exception ex) {
				Log.w("BeepyBoop", ex);
			} finally {
				Log.w("Beepy Boop", "Harrohh!");
				if (connection != null) {
					connection.disconnect();
				}
			}
			
			*/
		}
	};
	
	/*
	 BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
	 StringBuilder total = new StringBuilder();
	 String line;
	 while ((line = r.readLine()) != null) {
	 total.append(line).append('\n');
	 }
	 */
	
	/*
	 URL url = new URL("http://www.android.com/");
	 HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
	 try {
     InputStream in = new BufferedInputStream(urlConnection.getInputStream());
     readStream(in);
	 finally {
     urlConnection.disconnect();
	 }
	 }
	*/
	
	/*
	 public interface GitHubService {
	 @GET("users/{user}/repos")
	 Call<List<Repo>> listRepos(@Path("user") String user);
	 }
	 The Retrofit class generates an implementation of the GitHubService interface.

	 Retrofit retrofit = new Retrofit.Builder()
	 .baseUrl("https://api.github.com/")
	 .build();

	 GitHubService service = retrofit.create(GitHubService.class);
	 Each Call from the created GitHubService can make a synchronous or asynchronous HTTP request to the remote webserver.

	 Call<List<Repo>> repos = service.listRepos("octocat");
	*/
	
	/*
	public interface GitHubService {
    @GET("/users/{user}")
    Call<ResponseBody> listRepos(@Path("user") String user);
}
then you can create and execute your call --

GitHubService service = retrofit.create(GitHubService.class);
Call<ResponseBody> result = service.listRepos(username);
result.enqueue(new Callback<ResponseBody>() {
    @Override
    public void onResponse(Response<ResponseBody> response) {
        try {
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(Throwable t) {
        e.printStackTrace();
    }
	*/
	
	/*
	 * doStuff
	 * 
	 * This method does exactly what it says on the tin
	 */
	public void doStuff() {
		Retrofit retrofit = new Retrofit.Builder()
			.baseUrl("http://api.open-notify.org/")
			.build();
			
		ISSRetrieverService issService = retrofit.create(ISSRetrieverService.class);
		
		Call<ResponseBody> stuff = issService.getISSPos();
		stuff.enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(Call<ResponseBody> c, Response<ResponseBody> response) {
				try {
					Log.w("blah", "blah");
					Log.w("blah", response.body().string());
					doMoreStuff();
				} catch(Exception ex) {
					Log.w("blah", "cripes");
				}
			}
			
			@Override
			public void onFailure(Call<ResponseBody> c, Throwable t) {
				Log.w("blah", "faill");
				Log.w("error", t.getMessage());
				// Ruh roh, no internet!
			}
		});
		Log.w("Retrofit", stuff.toString());
	}
	
	public void doMoreStuff() {
		// Sup Goobers!!
		TextView top = (TextView) findViewById(R.id.topTextView);
		top.setText("Sup Goobers!!");
		
		TextView bottom = (TextView) findViewById(R.id.bottomTextView);
		bottom.append("Latitude:");
		bottom.append("\n");
		bottom.append("Longitude:");
		bottom.append("\n");
	}
	
	private final Handler timerHandler = new Handler();
	
	public interface ISSRetrieverService {
		@GET("iss-now.json")
		Call<ResponseBody> getISSPos();
	}
}
