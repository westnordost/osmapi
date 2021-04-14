package de.westnordost.osmapi.notes;

import junit.framework.TestCase;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

public class NotesDateFormatTest extends TestCase
{
	public void testDate() throws DateTimeParseException
	{
		assertEquals(
			ZonedDateTime.of(2020, 10, 26, 19, 33, 14, 0, ZoneId.of("UTC")).toInstant(),
			new NotesDateFormat().parse("2020-10-26 19:33:14 UTC")
		);
	}
}
