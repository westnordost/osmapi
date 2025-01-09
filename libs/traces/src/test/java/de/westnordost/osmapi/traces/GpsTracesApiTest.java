package de.westnordost.osmapi.traces;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.westnordost.osmapi.ConnectionTestFactory;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.errors.OsmAuthorizationException;
import de.westnordost.osmapi.common.errors.OsmNotFoundException;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.traces.GpsTraceDetails.Visibility;

import static org.junit.Assert.*;

public class GpsTracesApiTest
{
	/* the time is chosen relatively arbitrary. Basically, if it is off by more than half an hour,
	   there might be a problem with parsing the correct timezone. But ten minutes is suspicious
	   enough to let the test fail, imo */
	private static final int TEN_MINUTES = 1000 * 60 * 10;
	
	private static final int NONEXISTING_TRACE = 0;

	private static final int PRIVATE_TRACE_OF_OTHER_USER = 23;
	private static final int PUBLIC_TRACE = 927;

	
	private GpsTracesApi privilegedApi;
	private GpsTracesApi unprivilegedApi;
	
	@Before public void setUp()
	{
		privilegedApi = new GpsTracesApi(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_EVERYTHING));
		unprivilegedApi = new GpsTracesApi(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_NOTHING));
	}
	
	@Test public void accessPrivateTraceOfOtherUserResultsInFailure()
	{
		Class<OsmAuthorizationException> e = OsmAuthorizationException.class;
		assertThrows(e, () -> privilegedApi.get(PRIVATE_TRACE_OF_OTHER_USER));
		assertThrows(e, () -> privilegedApi.delete(PRIVATE_TRACE_OF_OTHER_USER));
		assertThrows(e, () -> privilegedApi.getData(PRIVATE_TRACE_OF_OTHER_USER, null));
		assertThrows(e, () -> privilegedApi.update(PRIVATE_TRACE_OF_OTHER_USER, Visibility.PUBLIC, "test", null));
	}

	@Test public void accessTraceWithoutPrivilegesResultsInFailure()
	{
		Class<OsmAuthorizationException> e = OsmAuthorizationException.class;
		assertThrows(e, () -> unprivilegedApi.create("bla", Visibility.PUBLIC, "desc", Collections.emptyList()));
		assertThrows(e, () -> unprivilegedApi.get(PUBLIC_TRACE));
		assertThrows(e, () -> unprivilegedApi.delete(PUBLIC_TRACE));
		assertThrows(e, () -> unprivilegedApi.getData(PUBLIC_TRACE, null));
		assertThrows(e, () -> unprivilegedApi.update(PUBLIC_TRACE, Visibility.PUBLIC, "test", null));
		assertThrows(e, () -> unprivilegedApi.getMine(tea -> {}));
	}
	
	
	@Test public void accessNonExistingTraceFails()
	{
		assertThrows(
				OsmNotFoundException.class,
				() -> privilegedApi.getData(NONEXISTING_TRACE, null)
		);
		assertThrows(
				OsmNotFoundException.class,
				() -> privilegedApi.update(NONEXISTING_TRACE, Visibility.TRACKABLE, "desc", null)
		);
	}
	
	@Test public void getNonExistingTrace()
	{
		assertNull(privilegedApi.get(NONEXISTING_TRACE));
	}
	
	@Test public void tooLongDescription()
	{
		assertThrows(
				IllegalArgumentException.class,
				() -> privilegedApi.update(PUBLIC_TRACE, Visibility.TRACKABLE, tooLong(), null)
		);
		assertThrows(
				IllegalArgumentException.class,
				() -> privilegedApi.create("xxx", Visibility.TRACKABLE, tooLong(), null, Collections.emptyList())
		);
	}
	
	@Test public void tooLongTags()
	{
		List<String> tags = new ArrayList<>();
		tags.add("abc");
		tags.add(tooLong());

		assertThrows(
				IllegalArgumentException.class,
				() -> privilegedApi.create("abc", Visibility.TRACKABLE, "desc", tags, Collections.emptyList())
		);

		assertThrows(
				IllegalArgumentException.class,
				() -> privilegedApi.update(PUBLIC_TRACE, Visibility.TRACKABLE, "test", tags)
		);
	}
	
	@Test public void createGetUpdateDelete()
	{
		List<String> tags = new ArrayList<>();
		tags.add("a tag, another");
		List<GpsTrackpoint> points = new ArrayList<>();
		points.add(new GpsTrackpoint(new OsmLatLon(1.23,3.45), Instant.now()));

		long id = privilegedApi.create("test case", Visibility.PRIVATE, "test case desc", tags,
				points);

		GpsTraceDetails trace = privilegedApi.get(id);
		assertEquals("test_case", trace.name);
		assertEquals(Visibility.PRIVATE, trace.visibility);
		assertEquals("test case desc", trace.description);
		assertTrue(trace.tags.contains("a tag"));
		assertTrue(trace.tags.contains("another"));
		assertEquals("westnordost", trace.userName);
		assertTrue(Math.abs(Instant.now().toEpochMilli() - trace.createdAt.toEpochMilli()) < TEN_MINUTES);

		privilegedApi.update(id, Visibility.TRACKABLE, "desc", null);
		trace = privilegedApi.get(id);
		assertEquals(Visibility.TRACKABLE, trace.visibility);
		assertEquals("desc",trace.description);
		assertNull(trace.tags);

		privilegedApi.delete(id);
		trace = privilegedApi.get(id);
		assertNull(trace);
	}

	@Test public void createFromGpxFileGetUpdateDelete() throws IOException
	{
		List<String> tags = new ArrayList<>();
		tags.add("a tag, another");
		InputStream is = getClass().getClassLoader().getResourceAsStream("track.gpx");

		long id = privilegedApi.create("gpx file test case", Visibility.PRIVATE, "test case from gpx file desc",
				tags, is);
		is.close();

		GpsTraceDetails trace = privilegedApi.get(id);
		assertEquals("gpx_file_test_case", trace.name);
		assertEquals(Visibility.PRIVATE, trace.visibility);
		assertEquals("test case from gpx file desc", trace.description);
		assertTrue(trace.tags.contains("a tag"));
		assertTrue(trace.tags.contains("another"));
		assertEquals("westnordost", trace.userName);
		assertTrue(Math.abs(Instant.now().toEpochMilli() - trace.createdAt.toEpochMilli()) < TEN_MINUTES);

		privilegedApi.update(id, Visibility.TRACKABLE, "desc", null);
		trace = privilegedApi.get(id);
		assertEquals(Visibility.TRACKABLE, trace.visibility);
		assertEquals("desc",trace.description);
		assertNull(trace.tags);

		privilegedApi.delete(id);
		trace = privilegedApi.get(id);
		assertNull(trace);
	}
	
	private static String tooLong()
	{
		StringBuilder result = new StringBuilder();
		for(int i = 0; i<=256; ++i) result.append("x");
		return result.toString();
	}
	
}
