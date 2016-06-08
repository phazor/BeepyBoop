package com.phazor.beepy.network;

import android.location.*;
import com.android.volley.*;
import com.phazor.beepy.*;
import com.phazor.beepy.position.json.*;

public class ISSPassTimesRequestCreator
{
	public static GsonRequest<ISSPassTimes> createISSPassTimesRequest(Location mLastLocation) {
		MainActivity.mRequestCount++;
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
	
	/*
	 * This code executes when recieving a successful HTTP response from the Pass Times API
	 */
	private static Response.Listener<ISSPassTimes> createISSPassTimesSuccessListener() {
		return new Response.Listener<ISSPassTimes>() {
			@Override
			public void onResponse(ISSPassTimes response) {
				MainActivity.mCountDownLatch.countDown();
				MainActivity.mISSPassTimes = response;

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
