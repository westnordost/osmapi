package de.westnordost.osmapi;

import java.text.ParseException;
import java.util.Date;

public class OsmXmlDateFormat
{
	private static final Iso8601CompatibleDateFormat
		DEFAULT = new Iso8601CompatibleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	
	public Date parse(String source) throws ParseException
	{
		return DEFAULT.parse(source);
	}
	
	public String format(Date date)
	{
		return DEFAULT.format(date);
	}
}
