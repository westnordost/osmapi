package de.westnordost.osmapi.traces;

import org.junit.Test;

import java.io.IOException;
import java.time.Instant;

import de.westnordost.osmapi.TestUtils;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.SingleElementHandler;

import static org.junit.Assert.*;

public class GpsTracesParserTest
{
	@Test public void fields()
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
		assertEquals(51.68812, result.position.getLatitude(), 1e-7);
		assertEquals(-3.0294167, result.position.getLongitude(), 1e-7);
		assertEquals("testo", result.userName);
		assertEquals(false, result.pending);
		assertEquals(null, result.tags);
		assertEquals(null, result.description);
		assertEquals(null, result.createdAt);
	}
	
	@Test public void date()
	{
		String xml =
				"<osm>" + 
				  "<gpx_file id=\"123\" visibility=\"public\" timestamp=\"2006-12-12T15:20:32Z\">" +
				  "</gpx_file>"+
				"</osm>";
		
		GpsTraceDetails result = parseOne(xml);
		assertEquals(Instant.parse("2006-12-12T15:20:32Z"), result.createdAt);
	}
	
	@Test public void parseVisibility() throws IOException
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
	
	@Test public void tags()
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
	
	@Test public void description()
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
		try
		{
			SingleElementHandler<GpsTraceDetails> handler = new SingleElementHandler<>();
			new GpsTracesParser(handler).parse(TestUtils.asInputStream(xml));
			return handler.get();
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

}
