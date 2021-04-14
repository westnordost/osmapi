package de.westnordost.osmapi.traces;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import de.westnordost.osmapi.TestUtils;
import de.westnordost.osmapi.common.ListHandler;
import de.westnordost.osmapi.map.data.OsmLatLon;

import static org.junit.Assert.*;

public class GpxTrackWriterTest
{
	@Test public void empty() throws IOException
	{
		List<GpsTrackpoint> elements = new ArrayList<>();
		checkElementsEqual(elements, writeAndRead(elements));
	}
	
	@Test public void minimal() throws IOException
	{
		List<GpsTrackpoint> elements = new ArrayList<>();
		elements.add(new GpsTrackpoint(
			new OsmLatLon(1.234, 2.345),
			Instant.now()
		));
		
		checkElementsEqual(elements, writeAndRead(elements));
	}
	
	@Test public void props() throws IOException
	{
		List<GpsTrackpoint> elements = new ArrayList<>();
		elements.add(new GpsTrackpoint(
			new OsmLatLon(1.234, 2.345),
			Instant.now(),
			false,
			42.1f,
			789.1f
		));
		
		checkElementsEqual(elements, writeAndRead(elements));
	}
	
	@Test public void oneSegment() throws IOException
	{
		List<GpsTrackpoint> elements = new ArrayList<>();
		elements.add( new GpsTrackpoint(new OsmLatLon(1.234, 2.345), Instant.now()));
		elements.add( new GpsTrackpoint(new OsmLatLon(2.234, 3.567), Instant.now()));
		
		checkElementsEqual(elements, writeAndRead(elements));
	}
	
	@Test public void twoSegments() throws IOException
	{
		List<GpsTrackpoint> elements = new ArrayList<>();
		elements.add( new GpsTrackpoint(new OsmLatLon(1.234, 2.345), Instant.now()));
		elements.add( new GpsTrackpoint(new OsmLatLon(2.234, 3.567), Instant.now()));
		
		GpsTrackpoint newSegmentPoint = new GpsTrackpoint(new OsmLatLon(3.234, 4.567), Instant.now(), true, null, null);

		elements.add( newSegmentPoint);
		
		checkElementsEqual(elements, writeAndRead(elements));
	}
	
	@Test public void roundFloatsToOneDecimal() throws IOException
	{
		List<GpsTrackpoint> elements = new ArrayList<>();
		elements.add(new GpsTrackpoint(
			new OsmLatLon(1.234, 2.345),
			Instant.now(),
			false,
			42.191655223465f,
			789.113524654646f
		));
		
		GpsTrackpoint pointNew = writeAndRead(elements).get(0);
		assertEquals(789.1f, pointNew.elevation, 1e-7);
		assertEquals(42.2f, pointNew.horizontalDilutionOfPrecision, 1e-7);
	}
	
	private void checkElementsEqual(List<GpsTrackpoint> expect, List<GpsTrackpoint> actual)
	{
		assertEquals(expect.size(), actual.size());
		for(int i = 0; i<expect.size(); ++i)
		{
			GpsTrackpoint e = expect.get(i);
			GpsTrackpoint a = actual.get(i);
						
			assertEquals(e.position, a.position);
			assertEquals(e.horizontalDilutionOfPrecision, a.horizontalDilutionOfPrecision);
			assertEquals(e.elevation, a.elevation);
			assertEquals(e.time, a.time);
			// first point in actual is always set to isFirstPointInTrackSegment=true
			if(i > 0)
				assertEquals(e.isFirstPointInTrackSegment, a.isFirstPointInTrackSegment);
			else
				assertTrue(a.isFirstPointInTrackSegment);
		}
	}

	private List<GpsTrackpoint> writeAndRead(Iterable<GpsTrackpoint> elements)
			throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new GpxTrackWriter("user agent test", elements).write(out);
		String xml = TestUtils.asString(out);
		ListHandler<GpsTrackpoint> handler = new ListHandler<>();
		new GpxTrackParser(handler).parse(TestUtils.asInputStream(xml));
		return handler.get();
	}
}
