package de.westnordost.osmapi.traces;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import de.westnordost.osmapi.TestUtils;
import de.westnordost.osmapi.common.ListHandler;
import de.westnordost.osmapi.map.data.OsmLatLon;

public class GpxTrackWriterTest extends TestCase
{
	public void testEmpty() throws IOException
	{
		List<GpsTrackpoint> elements = new ArrayList<>();
		checkElementsEqual(elements, writeAndRead(elements));
	}
	
	public void testMinimal() throws IOException
	{
		List<GpsTrackpoint> elements = new ArrayList<>();
		GpsTrackpoint point = new GpsTrackpoint(new OsmLatLon(1.234, 2.345));
		elements.add(point);
		
		checkElementsEqual(elements, writeAndRead(elements));
	}
	
	public void testProps() throws IOException
	{
		List<GpsTrackpoint> elements = new ArrayList<>();
		GpsTrackpoint point = new GpsTrackpoint(new OsmLatLon(1.234, 2.345));
		point.elevation = 789.1f;
		point.horizontalDilutionOfPrecision = 42.1f;
		point.time = Instant.now();
		point.isFirstPointInTrackSegment = false; // will be ignored and set to true
		elements.add(point);
		
		checkElementsEqual(elements, writeAndRead(elements));
	}
	
	public void testOneSegment() throws IOException
	{
		List<GpsTrackpoint> elements = new ArrayList<>();
		elements.add( new GpsTrackpoint(new OsmLatLon(1.234, 2.345)));
		elements.add( new GpsTrackpoint(new OsmLatLon(2.234, 3.567)));
		
		checkElementsEqual(elements, writeAndRead(elements));
	}
	
	public void testTwoSegments() throws IOException
	{
		List<GpsTrackpoint> elements = new ArrayList<>();
		elements.add( new GpsTrackpoint(new OsmLatLon(1.234, 2.345)));
		elements.add( new GpsTrackpoint(new OsmLatLon(2.234, 3.567)));
		
		GpsTrackpoint newSegmentPoint = new GpsTrackpoint(new OsmLatLon(3.234, 4.567));
		newSegmentPoint.isFirstPointInTrackSegment = true;
		
		elements.add( newSegmentPoint );
		
		checkElementsEqual(elements, writeAndRead(elements));
	}
	
	public void testRoundFloatsToOneDecimal() throws IOException
	{
		List<GpsTrackpoint> elements = new ArrayList<>();
		GpsTrackpoint point = new GpsTrackpoint(new OsmLatLon(1.234, 2.345));
		point.elevation = 789.113524654646f;
		point.horizontalDilutionOfPrecision = 42.191655223465f;
		elements.add(point);
		
		GpsTrackpoint pointNew = writeAndRead(elements).get(0);
		assertEquals(789.1f, pointNew.elevation);
		assertEquals(42.2f, pointNew.horizontalDilutionOfPrecision);
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
