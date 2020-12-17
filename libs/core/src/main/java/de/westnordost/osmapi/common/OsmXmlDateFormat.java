package de.westnordost.osmapi.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/** Format used to represent dates within the OSM Api 0.6 (except notes)*/
public class OsmXmlDateFormat
{
	private final SimpleDateFormat dateFormat;

	public OsmXmlDateFormat()
	{
		dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	public Date parse(String source) throws ParseException
	{
		return dateFormat.parse(source);
	}
	
	public String format(Date date)
	{
		return dateFormat.format(date);
	}
}
