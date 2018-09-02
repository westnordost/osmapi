package de.westnordost.osmapi.traces;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import junit.framework.TestCase;
import de.westnordost.osmapi.TestUtils;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.ListHandler;
import de.westnordost.osmapi.common.SingleElementHandler;

public class GpxTrackParserTest extends TestCase
{
	public void testParseEmptyTrack()
	{
		String xml = "<gpx><trk><trkseg /></trk></gpx>";
		assertNull(parseOne(xml));
	}
		
	public void testParseSingleTrackpointWithExtras()
	{
		String xml =
				"<trkseg>" +
					"<trkpt lat=\"12.3\" lon=\"45.6\">" +
						"<ele>789.1</ele>" +
						"<time>2016-04-17T16:41:02Z</time>" +
						"<hdop>2.12</hdop>" +
					"</trkpt>" +
				"</trkseg>";
		
		GpsTrackpoint trackpoint = parseOne(xml);
		assertTrue(trackpoint.isFirstPointInTrackSegment);
		assertEquals(12.3, trackpoint.position.getLatitude());
		assertEquals(45.6, trackpoint.position.getLongitude());
		assertEquals(2.12f, trackpoint.horizontalDilutionOfPrecision);
		assertEquals(789.1f, trackpoint.elevation);
		
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
		c.set(2016, Calendar.APRIL, 17, 16, 41, 2);
		assertEquals(c.getTimeInMillis() / 1000, trackpoint.time.getTime() / 1000);
	}
	
	public void testParseSingleTrackpointWithMillis()
	{
		String xml =
				"<trkseg>" +
					"<trkpt lat=\"12.3\" lon=\"45.6\">" +
						"<time>2016-04-17T16:41:02.654Z</time>" +
					"</trkpt>" +
				"</trkseg>";
		
		GpsTrackpoint trackpoint = parseOne(xml);
		
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
		c.set(2016, Calendar.APRIL, 17, 16, 41, 2);
		// I cannot set the milliseconds with the calendar.... :-(
		assertEquals(c.getTimeInMillis() / 1000, trackpoint.time.getTime() / 1000);
		assertEquals(654, trackpoint.time.getTime() % 1000);
	}
	
	public void testParseMultipleSegments()
	{
		String xml =
				"<trkseg>" +
					"<trkpt lat=\"12.3\" lon=\"45.6\" />" +
					"<trkpt lat=\"11.1\" lon=\"44.4\" />" +
				"</trkseg>" +
				"<trkseg>" +
					"<trkpt lat=\"1.2\" lon=\"3.4\" />" +
				"</trkseg>";
		
		List<GpsTrackpoint> list = parseList(xml);
		assertEquals(3, list.size());
		assertTrue(list.get(0).isFirstPointInTrackSegment);
		assertFalse(list.get(1).isFirstPointInTrackSegment);
		assertTrue(list.get(2).isFirstPointInTrackSegment);
	}
	
	private List<GpsTrackpoint> parseList(String xml)
	{
		ListHandler<GpsTrackpoint> handler = new ListHandler<>();
		parse(xml, handler);
		return handler.get();
	}
	
	private GpsTrackpoint parseOne(String xml)
	{
		SingleElementHandler<GpsTrackpoint> handler = new SingleElementHandler<>();
		parse(xml, handler);
		return handler.get();
	}
	
	private void parse(String xml, Handler<GpsTrackpoint> handler)
	{
		try
		{
			new GpxTrackParser(handler).parse(TestUtils.asInputStream(xml));
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
