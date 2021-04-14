package de.westnordost.osmapi.traces;

import org.junit.Before;
import org.junit.Test;

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

	private static final int PRIVATE_TRACE_OF_OTHER_USER = 928;
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
		try
		{
			privilegedApi.get(PRIVATE_TRACE_OF_OTHER_USER);
			fail();
		}
		catch(OsmAuthorizationException ignore) { }

		try
		{
			privilegedApi.delete(PRIVATE_TRACE_OF_OTHER_USER);
			fail();
		}
		catch(OsmAuthorizationException ignore) { }

		try
		{
			privilegedApi.getData(PRIVATE_TRACE_OF_OTHER_USER, null);
			fail();
		}
		catch(OsmAuthorizationException ignore) { }

		try
		{
			privilegedApi.update(PRIVATE_TRACE_OF_OTHER_USER, Visibility.PUBLIC, "test", null);
			fail();
		}
		catch(OsmAuthorizationException ignore) { }
	}

	@Test public void accessTraceWithoutPrivilegesResultsInFailure()
	{
		try
		{
			unprivilegedApi.create("bla", Visibility.PUBLIC, "desc", Collections.<GpsTrackpoint> emptyList());
			fail();
		}
		catch(OsmAuthorizationException ignore) { }
		
		try
		{
			unprivilegedApi.get(PUBLIC_TRACE);
			fail();
		}
		catch(OsmAuthorizationException ignore) { }

		try
		{
			unprivilegedApi.delete(PUBLIC_TRACE);
			fail();
		}
		catch(OsmAuthorizationException ignore) { }

		try
		{
			unprivilegedApi.getData(PUBLIC_TRACE, null);
			fail();
		}
		catch(OsmAuthorizationException ignore) { }

		try
		{
			unprivilegedApi.update(PUBLIC_TRACE, Visibility.PUBLIC, "test", null);
			fail();
		}
		catch(OsmAuthorizationException ignore) { }
		
		try
		{
			unprivilegedApi.getMine(new Handler<GpsTraceDetails>() { public void handle(GpsTraceDetails tea) {}} );
			fail();
		}
		catch(OsmAuthorizationException ignore) { }
	}
	
	
	@Test public void accessNonexistingTraceFails()
	{
		try
		{
			privilegedApi.getData(NONEXISTING_TRACE, null);
			fail();
		}
		catch(OsmNotFoundException ignore) {}
		
		try
		{
			privilegedApi.update(NONEXISTING_TRACE, Visibility.TRACKABLE, "desc", null);
			fail();
		}
		catch(OsmNotFoundException ignore) {}
	}
	
	@Test public void getNonexistingTrace()
	{
		assertNull(privilegedApi.get(NONEXISTING_TRACE));
	}
	
	@Test public void tooLongDescription()
	{
		try
		{
			privilegedApi.update(PUBLIC_TRACE, Visibility.TRACKABLE, tooLong(), null);
			fail();
		}
		catch(IllegalArgumentException ignore) { }
		
		try
		{
			privilegedApi.create("xxx", Visibility.TRACKABLE, tooLong(), null,
					Collections.<GpsTrackpoint> emptyList());
			fail();
		}
		catch(IllegalArgumentException ignore) { }
	}
	
	@Test public void tooLongTags()
	{
		List<String> tags = new ArrayList<>();
		tags.add("abc");
		tags.add(tooLong());
		
		try
		{
			privilegedApi.create("abc", Visibility.TRACKABLE, "desc", tags,
					Collections.<GpsTrackpoint> emptyList());
			fail();
		}
		catch(IllegalArgumentException ignore) { }
		
		try
		{
			privilegedApi.update(PUBLIC_TRACE, Visibility.TRACKABLE, "test", tags);
			fail();
		}
		catch(IllegalArgumentException ignore) { }
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
		assertEquals("osmagent-test-allow-everything", trace.userName);
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
