package de.westnordost.osmapi.notes;

import junit.framework.TestCase;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class NotesDateFormatTest extends TestCase
{
	public void testDate() throws ParseException
	{
		NotesDateFormat timezoneFormat = new NotesDateFormat();
		Date x = timezoneFormat.parse("2020-10-26 19:33:14 UTC");
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c.set(2020, Calendar.OCTOBER, 26, 19, 33, 14);
		assertEquals(c.getTime().getTime() / 1000, x.getTime() / 1000);
	}
}
