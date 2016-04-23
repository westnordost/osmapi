package de.westnordost.osmapi.traces;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import de.westnordost.osmapi.Handler;
import de.westnordost.osmapi.SingleElementHandler;
import de.westnordost.osmapi.TestUtils;
import de.westnordost.osmapi.traces.GpsTraceDetails;
import de.westnordost.osmapi.traces.GpsTracesParser;
import junit.framework.TestCase;

public class GpsTracesParserTest extends TestCase
{
	public void testFields()
	{
		String xml =
				"<osm>" + 
				  "<gpx_file id=\"123\" name=\"näim\" lat=\"51.68812\" lon=\"-3.0294167\" user=\"testo\" visibility=\"public\" pending=\"false\">" +
				  "<description/>" +
				  "</gpx_file>"+
				"</osm>";

		GpsTraceDetails result = parseOne(xml);
		
		assertEquals(123, result.id);
		assertEquals("näim", result.name);
		assertEquals(51.68812, result.position.getLatitude());
		assertEquals(-3.0294167, result.position.getLongitude());
		assertEquals("testo", result.userName);
		assertEquals(false, result.pending);
		assertEquals(null, result.tags);
		assertEquals(null, result.description);
		assertEquals(null, result.date);
	}
	
	public void testDate()
	{
		String xml =
				"<osm>" + 
				  "<gpx_file id=\"123\" visibility=\"public\" timestamp=\"2006-12-12T15:20:32Z\">" +
				  "</gpx_file>"+
				"</osm>";
		
		GpsTraceDetails result = parseOne(xml);
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
		c.set(2006, Calendar.DECEMBER, 12, 15, 20, 32);
		assertEquals(c.getTimeInMillis() / 1000, result.date.getTime() / 1000);
	}
	
	public void testParseVisibility()
	{
		String xml =
				"<osm>" + 
				  "<gpx_file id=\"1\" visibility=\"public\">" +
				  "</gpx_file>"+
				  "<gpx_file id=\"2\" visibility=\"private\">" +
				  "</gpx_file>"+
				  "<gpx_file id=\"3\" visibility=\"identifiable\">" +
				  "</gpx_file>"+
				  "<gpx_file id=\"4\" visibility=\"trackable\">" +
				  "</gpx_file>"+
				"</osm>";
		
		Handler<GpsTraceDetails> handler = new Handler<GpsTraceDetails>()
		{
			@Override
			public void handle(GpsTraceDetails trace) {
				assertEquals(getVisibility((int) trace.id), trace.visibility);
			}
			
			private GpsTraceDetails.Visibility getVisibility(int id)
			{
				switch(id)
				{
					case 1: return GpsTraceDetails.Visibility.PUBLIC;
					case 2: return GpsTraceDetails.Visibility.PRIVATE;
					case 3: return GpsTraceDetails.Visibility.IDENTIFIABLE;
					case 4: return GpsTraceDetails.Visibility.TRACKABLE;
				}
				return null;
			}
		};
		new GpsTracesParser(handler).parse(TestUtils.asInputStream(xml));
	}
	
	public void testTags()
	{
		String xml =
				"<osm>" + 
				  "<gpx_file id=\"123\" visibility=\"public\">" +
				  "<tag>yoyoyo</tag>" +
				  "<tag>hihihi</tag>" +
				  "</gpx_file>"+
				"</osm>";

		GpsTraceDetails result = parseOne(xml);
		
		assertEquals(2,result.tags.size());
		assertEquals("yoyoyo",result.tags.get(0));
		assertEquals("hihihi",result.tags.get(1));
	}
	
	public void testDescription()
	{
		String xml =
				"<osm>" + 
				  "<gpx_file id=\"123\" visibility=\"public\">" +
				  "<description>hiho</description>" +
				  "</gpx_file>"+
				"</osm>";

		GpsTraceDetails result = parseOne(xml);
		
		assertEquals("hiho",result.description);
	}
	
	private GpsTraceDetails parseOne(String xml)
	{
		SingleElementHandler<GpsTraceDetails> handler = new SingleElementHandler<GpsTraceDetails>();
		new GpsTracesParser(handler).parse(TestUtils.asInputStream(xml));
		return handler.get();
	}

}
