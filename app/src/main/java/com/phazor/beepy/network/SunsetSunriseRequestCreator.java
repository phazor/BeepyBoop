package com.phazor.beepy.network;

import android.location.*;
import com.android.volley.*;
import com.phazor.beepy.fragments.*;
import com.phazor.beepy.position.json.*;

public class SunsetSunriseRequestCreator
{
	public static GsonRequest<SunriseSunset> createSunsetSunriseRequest(Location mLastLocation) {
		CountdownFragment.mRequestCount++;
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

	/*
	 * This code executes when recieving a successful HTTP response from the Sunrise Sunset API
	 */
	private static Response.Listener<SunriseSunset> createSunriseSunsetSuccessListener() {
		return new Response.Listener<SunriseSunset>() {
			@Override
			public void onResponse(SunriseSunset response) {
				CountdownFragment.mCountDownLatch.countDown();
				CountdownFragment.mSunriseSunset = response;

				// Do whatever you want to do with response;
				// Like response.tags.getListing_count(); etc. etc.
			}
		};
	}
	
	/*
	 * Handle all HTTP Request errors here
	 */
	private static Response.ErrorListener createMyReqErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				// Do whatever you want to do with error.getMessage();
			}
		};
	}
}
