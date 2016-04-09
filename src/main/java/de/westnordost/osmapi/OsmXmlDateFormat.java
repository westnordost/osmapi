package de.westnordost.osmapi;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * The format in which the OSM Api 0.6 represents dates in XML replies. (Except in the Notes Api)
 */
public class OsmXmlDateFormat extends SimpleDateFormat
{
	public OsmXmlDateFormat()
	{
		super("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK);
		/* I wonder if it is a bug that the last letter is always "Z" in those timestamps instead of
		   a proper timezone. Anyway, we always assume UTC then
		  */
		setTimeZone(TimeZone.getTimeZone("UTC"));
	}
}
