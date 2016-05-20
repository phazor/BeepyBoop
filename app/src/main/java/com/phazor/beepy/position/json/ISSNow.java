package com.phazor.beepy.position.json;
import com.google.gson.annotations.*;

public class ISSNow
{
	private String message = null;
	private String timetamp = null;
	@SerializedName("iss_position")
	private Position position = null;
	
	ISSNow() {	
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
	
	
	
}
