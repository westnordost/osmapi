package de.westnordost.osmapi.traces;

import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import de.westnordost.osmapi.TestUtils;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.ListHandler;
import de.westnordost.osmapi.common.SingleElementHandler;

import static org.junit.Assert.*;

public class GpxTrackParserTest
{
	@Test public void parseEmptyTrack()
	{
		String xml = "<gpx><trk><trkseg /></trk></gpx>";
		assertNull(parseOne(xml));
	}
		
	@Test public void parseSingleTrackpointWithExtras()
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
		assertEquals(12.3, trackpoint.position.getLatitude(), 1e-7);
		assertEquals(45.6, trackpoint.position.getLongitude(), 1e-7);
		assertEquals(2.12f, trackpoint.horizontalDilutionOfPrecision, 1e-7);
		assertEquals(789.1f, trackpoint.elevation, 1e-7);

		assertEquals(Instant.parse("2016-04-17T16:41:02Z"), trackpoint.time);
	}
	
	@Test public void parseSingleTrackpointWithMillis()
	{
		String xml =
				"<trkseg>" +
					"<trkpt lat=\"12.3\" lon=\"45.6\">" +
						"<time>2016-04-17T16:41:02.654Z</time>" +
					"</trkpt>" +
				"</trkseg>";
		
		GpsTrackpoint trackpoint = parseOne(xml);

		assertEquals(Instant.parse("2016-04-17T16:41:02.654Z"), trackpoint.time);
		assertEquals(654, trackpoint.time.toEpochMilli() % 1000);
	}
	
	@Test public void parseMultipleSegments()
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
