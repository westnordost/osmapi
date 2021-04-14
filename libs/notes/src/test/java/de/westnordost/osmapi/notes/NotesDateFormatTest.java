package de.westnordost.osmapi.notes;

import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import static org.junit.Assert.assertEquals;

public class NotesDateFormatTest
{
	@Test public void date() throws DateTimeParseException
	{
		assertEquals(
			ZonedDateTime.of(2020, 10, 26, 19, 33, 14, 0, ZoneId.of("UTC")).toInstant(),
			new NotesDateFormat().parse("2020-10-26 19:33:14 UTC")
		);
	}
}
