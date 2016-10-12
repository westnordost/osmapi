package de.westnordost.osmapi.notes;

import java.text.ParseException;
import java.util.Date;

import de.westnordost.osmapi.common.Iso8601CompatibleDateFormat;

/** The date format for the notes API is a little different: The 'T' literal is missing between 
 *  time and date */
public class NotesDateFormat
{
	private static final Iso8601CompatibleDateFormat
		DEFAULT = new Iso8601CompatibleDateFormat("yyyy-MM-dd HH:mm:ss z");

	public Date parse(String source) throws ParseException
	{
		return DEFAULT.parse(source);
	}
	
	public String format(Date date)
	{
		return DEFAULT.format(date);
	}
}
