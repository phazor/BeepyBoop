package com.phazor.beepy.position.json;
import com.google.gson.annotations.*;

public class ISSCurrentPos
{
	private String message = null;
	private String timetamp = null;
	@SerializedName("iss_position")
	private Position position = null;
	
	ISSCurrentPos() {	
	}

	public void setPosition(Position position)
	{
		this.position = position;
	}

	public Position getPosition()
	{
		return position;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getMessage()
	{
		return message;
	}

	public void setTimetamp(String timetamp)
	{
		this.timetamp = timetamp;
	}

	public String getTimetamp()
	{
		return timetamp;
	}
	
	public class Position {
		private String latitude;
		private String longitude;

		public void setLongitude(String longitude)
		{
			this.longitude = longitude;
		}

		public String getLongitude()
		{
			return longitude;
		}


		public void setLatitude(String latitude)
		{
			this.latitude = latitude;
		}

		public String getLatitude()
		{
			return latitude;
		}
	}
	
}
