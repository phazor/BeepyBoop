package com.phazor.beepy.position.json;

public class ISSPassTimes
{
	private String message;
	private Request request;
	private Response[] response;

	public void setResponse(Response[] response)
	{
		this.response = response;
	}

	public Response[] getResponse()
	{
		return response;
	}

	public void setRequest(Request request)
	{
		this.request = request;
	}

	public Request getRequest()
	{
		return request;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getMessage()
	{
		return message;
	}
	
	private class Request {
		private int latitude;
		private int longitude;
		private int altitude;
		private int passes;
		private String dateTime;

		public Request(int latitude)
		{
			this.latitude = latitude;
		}

		public void setDateTime(String dateTime)
		{
			this.dateTime = dateTime;
		}

		public String getDateTime()
		{
			return dateTime;
		}

		public void setPasses(int passes)
		{
			this.passes = passes;
		}

		public int getPasses()
		{
			return passes;
		}

		public void setAltitude(int altitude)
		{
			this.altitude = altitude;
		}

		public int getAltitude()
		{
			return altitude;
		}

		public void setLongitude(int longitude)
		{
			this.longitude = longitude;
		}

		public int getLongitude()
		{
			return longitude;
		}
	}
	
	private class Response {
		private String risetime;
		private String timestamp;

		public void setTimestamp(String timestamp)
		{
			this.timestamp = timestamp;
		}

		public String getTimestamp()
		{
			return timestamp;
		}


		public void setRisetime(String risetime)
		{
			this.risetime = risetime;
		}

		public String getRisetime()
		{
			return risetime;
		}}
	
	// {"risetime": TIMESTAMP, "duration": DURATION},
}
