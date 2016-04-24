package de.westnordost.osmapi.traces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.westnordost.osmapi.ConnectionTestFactory;
import de.westnordost.osmapi.Handler;
import de.westnordost.osmapi.errors.OsmAuthorizationException;
import de.westnordost.osmapi.errors.OsmNotFoundException;
import de.westnordost.osmapi.traces.GpsTraceDetails.Visibility;
import junit.framework.TestCase;

public class GpsTracesDaoTest extends TestCase
{
	/* the time is chosen relatively arbitrary. Basically, if it is off by more than half an hour,
	   there might be a problem with parsing the correct timezone. But ten minutes is suspicious
	   enough to let the test fail, imo */
	private static final int TEN_MINUTES = 1000 * 60 * 10;
	
	private static final int NONEXISTING_TRACE = 0;
// TODO blocked by https://github.com/openstreetmap/chef/pull/31
//	private static final int PRIVATE_TRACE_OF_OTHER_USER = 0; // TODO upload test traces!
//	private static final int TRACE_OF_UNPRIVILEGED_USER = 0;// TODO upload test traces!
//	private static final int TRACE_OF_PRIVILEGED_USER = 0;// TODO upload test traces!
	
	
	private GpsTracesDao privilegedDao;
	private GpsTracesDao unprivilegedDao;
	
	@Override
	protected void setUp() throws Exception
	{
		privilegedDao = new GpsTracesDao(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_EVERYTHING));
		unprivilegedDao = new GpsTracesDao(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_NOTHING));
	}
	
//	public void testAccessPrivateTraceOfOtherUserResultsInFailure()
//	{
//		try
//		{
//			privilegedDao.get(PRIVATE_TRACE_OF_OTHER_USER);
//			fail();
//		}
//		catch(OsmAuthorizationException e) { }
//		
//		try
//		{
//			privilegedDao.delete(PRIVATE_TRACE_OF_OTHER_USER);
//			fail();
//		}
//		catch(OsmAuthorizationException e) { }
//		
//		try
//		{
//			privilegedDao.getData(PRIVATE_TRACE_OF_OTHER_USER, null);
//			fail();
//		}
//		catch(OsmAuthorizationException e) { }
//		
//		try
//		{
//			privilegedDao.update(PRIVATE_TRACE_OF_OTHER_USER, Visibility.PUBLIC, null, null);
//			fail();
//		}
//		catch(OsmAuthorizationException e) { }
//	}
	
	public void testAccessTraceWithoutPrivilegesResultsInFailure()
	{
		try
		{
			unprivilegedDao.create("bla", Visibility.PUBLIC, "desc", Collections.<GpsTrackpoint> emptyList());
			fail();
		}
		catch(OsmAuthorizationException e) { }
		
//		try
//		{
//			unprivilegedDao.get(TRACE_OF_UNPRIVILEGED_USER);
//			fail();
//		}
//		catch(OsmAuthorizationException e) { }
//		
//		try
//		{
//			unprivilegedDao.delete(TRACE_OF_UNPRIVILEGED_USER);
//			fail();
//		}
//		catch(OsmAuthorizationException e) { }
//		
//		try
//		{
//			unprivilegedDao.getData(TRACE_OF_UNPRIVILEGED_USER, null);
//			fail();
//		}
//		catch(OsmAuthorizationException e) { }
//		
//		try
//		{
//			unprivilegedDao.update(TRACE_OF_UNPRIVILEGED_USER, Visibility.PUBLIC, null, null);
//			fail();
//		}
//		catch(OsmAuthorizationException e) { }
		
		try
		{
			unprivilegedDao.getMine(new Handler<GpsTraceDetails>() { public void handle(GpsTraceDetails tea) {}} );
			fail();
		}
		catch(OsmAuthorizationException e) { }
	}
	
	
	public void testAccessNonexistingTraceFails()
	{
		try
		{
			privilegedDao.getData(NONEXISTING_TRACE, null);
			fail();
		}
		catch(OsmNotFoundException e) {}
		
		try
		{
			privilegedDao.update(NONEXISTING_TRACE, Visibility.TRACKABLE, "desc", null);
			fail();
		}
		catch(OsmNotFoundException e) {}
	}
	
	public void testGetNonexistingTrace()
	{
		assertNull(privilegedDao.get(NONEXISTING_TRACE));
	}
	
	public void testTooLongDescription()
	{
//		try
//		{
//			privilegedDao.update(TRACE_OF_PRIVILEGED_USER, Visibility.TRACKABLE, tooLong(), null);
//			fail();
//		}
//		catch(IllegalArgumentException e) { }
		
		try
		{
			privilegedDao.create("xxx", Visibility.TRACKABLE, tooLong(), null,
					Collections.<GpsTrackpoint> emptyList());
			fail();
		}
		catch(IllegalArgumentException e) { }
	}
	
	public void testTooLongTags()
	{
		List<String> tags = new ArrayList<String>();
		tags.add("abc");
		tags.add(tooLong());
		
		try
		{
			privilegedDao.create("abc", Visibility.TRACKABLE, "desc", tags,
					Collections.<GpsTrackpoint> emptyList());
			fail();
		}
		catch(IllegalArgumentException e) { }
		
//		try
//		{
//			privilegedDao.update(TRACE_OF_PRIVILEGED_USER, Visibility.TRACKABLE, null, tags);
//			fail();
//		}
//		catch(IllegalArgumentException e) { }
	}
	
//	public void testCreateGetUpdateDelete()
//	{
//		List<String> tags = new ArrayList<>();
//		tags.add("a tag");
//		List<GpsTrackpoint> points = new ArrayList<>();
//		points.add(new GpsTrackpoint(new OsmLatLon(1.23,3.45)));
//		
//		long id = privilegedDao.create("test case", Visibility.PRIVATE, "test case desc", tags,
//				points);
//		
//		GpsTraceDetails trace = privilegedDao.get(id);
//		assertEquals("test case", trace.name);
//		assertEquals(Visibility.PRIVATE, trace.visibility);
//		assertEquals("test case desc", trace.description);
//		assertEquals("a tag", trace.tags.get(0));
//		assertEquals("osmagent-test-allow-everything", trace.userName);
//		assertTrue(Math.abs(new Date().getTime() - trace.date.getTime()) < TEN_MINUTES);
//		
//		privilegedDao.update(id, Visibility.TRACKABLE, null, null);
//		trace = privilegedDao.get(id);
//		assertEquals(Visibility.TRACKABLE, trace.visibility);
//		assertEquals(null, trace.description);
//		assertEquals(null, trace.tags);
//		
//		privilegedDao.delete(id);
//		trace = privilegedDao.get(id);
//		assertNull(trace);
//	}
	
	private static String tooLong()
	{
		String result = "";
		for(int i = 0; i<=256; ++i) result += "x";
		return result;
	}
	
}
