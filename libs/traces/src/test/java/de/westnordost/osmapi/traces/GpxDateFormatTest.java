package de.westnordost.osmapi.traces;

import junit.framework.TestCase;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class GpxDateFormatTest extends TestCase {

    public void testDate() throws ParseException
    {
        GpxDateFormat timezoneFormat = new GpxDateFormat();
        Date x = timezoneFormat.parse("2015-05-07T09:58:16Z");
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.set(2015, Calendar.MAY, 7, 9, 58, 16);
        assertEquals(c.getTime().getTime() / 1000, x.getTime() / 1000);
    }

    public void testDateWithMilliseconds() throws ParseException
    {
        GpxDateFormat timezoneFormat = new GpxDateFormat();
        Date x = timezoneFormat.parse("2015-05-07T09:58:16.123Z");
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.set(2015, Calendar.MAY, 7, 9, 58, 16);
        long expectedTimeInSeconds = c.getTimeInMillis() / 1000;
        long expectedTimeInMilliseconds = expectedTimeInSeconds * 1000 + 123;
        assertEquals(expectedTimeInMilliseconds, x.getTime());
    }
}