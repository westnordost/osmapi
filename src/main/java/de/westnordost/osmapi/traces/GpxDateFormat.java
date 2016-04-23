package de.westnordost.osmapi.traces;

import java.text.ParseException;
import java.util.Date;
import java.util.regex.Pattern;

import de.westnordost.osmapi.Iso8601CompatibleDateFormat;
import de.westnordost.osmapi.OsmXmlDateFormat;

/** Gpx timestamps can optionally include milliseconds */
public class GpxDateFormat extends OsmXmlDateFormat
{
	private static final Iso8601CompatibleDateFormat
		MILLIS = new Iso8601CompatibleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
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
			return MILLIS.parse(source);
		}
		return super.parse(source);
	}
	
	public String format(Date date)
	{
		if(date.getTime() % 1000 > 0)
		{
			return MILLIS.format(date);
		}
		return super.format(date);
	}
}
