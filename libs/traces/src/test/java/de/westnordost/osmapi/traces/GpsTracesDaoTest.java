package de.westnordost.osmapi.traces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.westnordost.osmapi.ConnectionTestFactory;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.errors.OsmAuthorizationException;
import de.westnordost.osmapi.common.errors.OsmNotFoundException;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.traces.GpsTraceDetails.Visibility;
import junit.framework.TestCase;

public class GpsTracesDaoTest extends TestCase
{
	/* the time is chosen relatively arbitrary. Basically, if it is off by more than half an hour,
	   there might be a problem with parsing the correct timezone. But ten minutes is suspicious
	   enough to let the test fail, imo */
	private static final int TEN_MINUTES = 1000 * 60 * 10;
	
	private static final int NONEXISTING_TRACE = 0;

	private static final int PRIVATE_TRACE_OF_OTHER_USER = 928;
	private static final int PUBLIC_TRACE = 927;

	
	private GpsTracesDao privilegedDao;
	private GpsTracesDao unprivilegedDao;
	
	@Override
	protected void setUp()
	{
		privilegedDao = new GpsTracesDao(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_EVERYTHING));
		unprivilegedDao = new GpsTracesDao(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_NOTHING));
	}
	
	public void testAccessPrivateTraceOfOtherUserResultsInFailure()
	{
		try
		{
			privilegedDao.get(PRIVATE_TRACE_OF_OTHER_USER);
			fail();
		}
		catch(OsmAuthorizationException ignore) { }

		try
		{
			privilegedDao.delete(PRIVATE_TRACE_OF_OTHER_USER);
			fail();
		}
		catch(OsmAuthorizationException ignore) { }

		try
		{
			privilegedDao.getData(PRIVATE_TRACE_OF_OTHER_USER, null);
			fail();
		}
		catch(OsmAuthorizationException ignore) { }

		try
		{
			privilegedDao.update(PRIVATE_TRACE_OF_OTHER_USER, Visibility.PUBLIC, "test", null);
			fail();
		}
		catch(OsmAuthorizationException ignore) { }
	}
	
	public void testAccessTraceWithoutPrivilegesResultsInFailure()
	{
		try
		{
			unprivilegedDao.create("bla", Visibility.PUBLIC, "desc", Collections.<GpsTrackpoint> emptyList());
			fail();
		}
		catch(OsmAuthorizationException ignore) { }
		
		try
		{
			unprivilegedDao.get(PUBLIC_TRACE);
			fail();
		}
		catch(OsmAuthorizationException ignore) { }

		try
		{
			unprivilegedDao.delete(PUBLIC_TRACE);
			fail();
		}
		catch(OsmAuthorizationException ignore) { }

		try
		{
			unprivilegedDao.getData(PUBLIC_TRACE, null);
			fail();
		}
		catch(OsmAuthorizationException ignore) { }

		try
		{
			unprivilegedDao.update(PUBLIC_TRACE, Visibility.PUBLIC, "test", null);
			fail();
		}
		catch(OsmAuthorizationException ignore) { }
		
		try
		{
			unprivilegedDao.getMine(new Handler<GpsTraceDetails>() { public void handle(GpsTraceDetails tea) {}} );
			fail();
		}
		catch(OsmAuthorizationException ignore) { }
	}
	
	
	public void testAccessNonexistingTraceFails()
	{
		try
		{
			privilegedDao.getData(NONEXISTING_TRACE, null);
			fail();
		}
		catch(OsmNotFoundException ignore) {}
		
		try
		{
			privilegedDao.update(NONEXISTING_TRACE, Visibility.TRACKABLE, "desc", null);
			fail();
		}
		catch(OsmNotFoundException ignore) {}
	}
	
	public void testGetNonexistingTrace()
	{
		assertNull(privilegedDao.get(NONEXISTING_TRACE));
	}
	
	public void testTooLongDescription()
	{
		try
		{
			privilegedDao.update(PUBLIC_TRACE, Visibility.TRACKABLE, tooLong(), null);
			fail();
		}
		catch(IllegalArgumentException ignore) { }
		
		try
		{
			privilegedDao.create("xxx", Visibility.TRACKABLE, tooLong(), null,
					Collections.<GpsTrackpoint> emptyList());
			fail();
		}
		catch(IllegalArgumentException ignore) { }
	}
	
	public void testTooLongTags()
	{
		List<String> tags = new ArrayList<>();
		tags.add("abc");
		tags.add(tooLong());
		
		try
		{
			privilegedDao.create("abc", Visibility.TRACKABLE, "desc", tags,
					Collections.<GpsTrackpoint> emptyList());
			fail();
		}
		catch(IllegalArgumentException ignore) { }
		
		try
		{
			privilegedDao.update(PUBLIC_TRACE, Visibility.TRACKABLE, "test", tags);
			fail();
		}
		catch(IllegalArgumentException ignore) { }
	}
	
	public void testCreateGetUpdateDelete()
	{
		List<String> tags = new ArrayList<>();
		tags.add("a tag, another");
		List<GpsTrackpoint> points = new ArrayList<>();
		points.add(new GpsTrackpoint(new OsmLatLon(1.23,3.45)));

		long id = privilegedDao.create("test case", Visibility.PRIVATE, "test case desc", tags,
				points);

		GpsTraceDetails trace = privilegedDao.get(id);
		assertEquals("test_case", trace.name);
		assertEquals(Visibility.PRIVATE, trace.visibility);
		assertEquals("test case desc", trace.description);
		assertTrue(trace.tags.contains("a tag"));
		assertTrue(trace.tags.contains("another"));
		assertEquals("osmagent-test-allow-everything", trace.userName);
		assertTrue(Math.abs(new Date().getTime() - trace.date.getTime()) < TEN_MINUTES);

		privilegedDao.update(id, Visibility.TRACKABLE, "desc", null);
		trace = privilegedDao.get(id);
		assertEquals(Visibility.TRACKABLE, trace.visibility);
		assertEquals("desc",trace.description);
		assertNull(trace.tags);

		privilegedDao.delete(id);
		trace = privilegedDao.get(id);
		assertNull(trace);
	}
	
	private static String tooLong()
	{
		StringBuilder result = new StringBuilder();
		for(int i = 0; i<=256; ++i) result.append("x");
		return result.toString();
	}
	
}
