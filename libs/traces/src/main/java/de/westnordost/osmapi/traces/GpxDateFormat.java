package de.westnordost.osmapi.traces;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

import de.westnordost.osmapi.common.OsmXmlDateFormat;

/** Gpx timestamps can optionally include milliseconds */
public class GpxDateFormat extends OsmXmlDateFormat
{
	private final SimpleDateFormat dateFormatMillis;
	public GpxDateFormat()
	{
		dateFormatMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		dateFormatMillis.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	private static final Pattern MILLIS_PATTERN = Pattern.compile("\\.[0-9]{3}");
	private static boolean hasMillis(String source)
	{
		return MILLIS_PATTERN.matcher(source).find();
	}
	
	public Date parse(String source) throws ParseException
	{
		// optional: parse milliseconds
		if(hasMillis(source))
		{
			return dateFormatMillis.parse(source);
		}
		return super.parse(source);
	}
	
	public String format(Date date)
	{
		if(date.getTime() % 1000 > 0)
		{
			return dateFormatMillis.format(date);
		}
		return super.format(date);
	}
}
