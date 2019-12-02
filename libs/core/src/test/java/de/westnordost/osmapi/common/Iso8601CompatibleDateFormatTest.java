package de.westnordost.osmapi.common;

import java.text.ParseException;
import java.util.Date;

import junit.framework.TestCase;

public class Iso8601CompatibleDateFormatTest extends TestCase
{
	public void testColon() throws ParseException
	{
		Iso8601CompatibleDateFormat timezoneFormat = new Iso8601CompatibleDateFormat("Z");
		Date x = timezoneFormat.parse("+0300");
		Date y = timezoneFormat.parse("+03:00");
		assertEquals(x,y);
	}
	
	public void testColonWithMinutes() throws ParseException
	{
		Iso8601CompatibleDateFormat timezoneFormat = new Iso8601CompatibleDateFormat("Z");
		Date x = timezoneFormat.parse("+0130");
		Date y = timezoneFormat.parse("+01:30");
		assertEquals(x,y);
	}
	
	public void testShortForm() throws ParseException
	{
		Iso8601CompatibleDateFormat timezoneFormat = new Iso8601CompatibleDateFormat("Z");
		Date x = timezoneFormat.parse("+0100");
		Date y = timezoneFormat.parse("+01");
		assertEquals(x,y);
	}
	
	public void testZulu() throws ParseException
	{
		Iso8601CompatibleDateFormat timezoneFormat = new Iso8601CompatibleDateFormat("Z");
		Date x = timezoneFormat.parse("+0000");
		Date y = timezoneFormat.parse("Z");
		assertEquals(x,y);
	}
	
	public void testUTC() throws ParseException
	{
		Iso8601CompatibleDateFormat timezoneFormat = new Iso8601CompatibleDateFormat("Z");
		Date x = timezoneFormat.parse("+0000");
		Date y = timezoneFormat.parse("UTC");
		assertEquals(x,y);
	}
}
