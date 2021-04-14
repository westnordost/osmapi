package de.westnordost.osmapi.notes;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** The date format for the notes API is a little different: The 'T' literal is missing between 
 *  time and date */
public class NotesDateFormat
{
	private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss z");

	public Instant parse(String source) throws DateTimeParseException
	{
		return ZonedDateTime.parse(source, FORMATTER).toInstant();
	}
	
	public String format(Instant instant)
	{
		return FORMATTER.format(instant);
	}
}
