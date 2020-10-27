package de.westnordost.osmapi.notes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/** The date format for the notes API is a little different: The 'T' literal is missing between 
 *  time and date */
public class NotesDateFormat
{
	private static final SimpleDateFormat DEFAULT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

	public Date parse(String source) throws ParseException
	{
		return DEFAULT.parse(source);
	}
	
	public String format(Date date)
	{
		return DEFAULT.format(date);
	}
}
