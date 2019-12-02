package de.westnordost.osmapi.common;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Extended SimpleDateFormat with extended timezone parsing so that it is compatible to Iso 8601
 */
public class Iso8601CompatibleDateFormat extends SimpleDateFormat
{
	private static final long serialVersionUID = 1L;

	public Iso8601CompatibleDateFormat(String pattern)
	{
		super(pattern, Locale.UK);
	}

	@Override
	public final Date parse(String source, ParsePosition pos)
	{
		return super.parse(convertToSimpleDateFormatCompatible(source), pos);
	}
	
	protected static String convertToSimpleDateFormatCompatible(String source)
	{
		// SimpleDateFormat does not understand that 
		// * the 'Z' literal implies UTC (= +00:00)
		source = source.replaceAll("Z$", "+0000");
		// * "+01:30" is a synonym for "+0130"
		source = source.replaceAll("([0-9]{2}):([0-9]{2})$","$1$2");
		// * "+01" is a synonym for "+0100"
		source = source.replaceAll("(\\+|\\-)([0-9]{2})$", "$1$200");
		
		return source;
		// see http://stackoverflow.com/questions/2201925/converting-iso-8601-compliant-string-to-java-util-date
	}
}
