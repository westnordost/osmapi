package de.westnordost.osmapi.traces;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.westnordost.osmapi.TestUtils;
import de.westnordost.osmapi.common.SingleElementHandler;

import static org.junit.Assert.assertEquals;

public class GpsTracesWriterTest
{
	@Test public void writeAll() throws IOException
	{
		List<String> tags = new ArrayList<>();
		tags.add("abc");
		tags.add("def");
		
		writeAndReadTest(123, GpsTraceDetails.Visibility.TRACKABLE, "xyz", tags);
	}
	
	@Test public void writeMin() throws IOException
	{
		writeAndReadTest(456, GpsTraceDetails.Visibility.IDENTIFIABLE, null, null);
	}
	
	private GpsTraceDetails writeAndRead(long id, GpsTraceDetails.Visibility visibility, String description, List<String> tags)
			throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new GpsTraceWriter(id, visibility, description, tags).write(out);
		String xml = TestUtils.asString(out);
		SingleElementHandler<GpsTraceDetails> handler = new SingleElementHandler<>();
		new GpsTracesParser(handler).parse(TestUtils.asInputStream(xml));
		return handler.get();
	}
	
	private void writeAndReadTest(long id, GpsTraceDetails.Visibility visibility, String description, List<String> tags)
			throws IOException
	{
		GpsTraceDetails trace = writeAndRead(id, visibility, description, tags);
		assertEquals(id, trace.id);
		assertEquals(visibility, trace.visibility);
		assertEquals(description, trace.description);
		assertEquals(tags, trace.tags);
	}
}
