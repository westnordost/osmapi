package de.westnordost.osmapi.changesets;

import junit.framework.TestCase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.westnordost.osmapi.map.data.Bounds;

public class QueryChangesetsFiltersTest extends TestCase
{
	private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.UK);
	private static final String VALID_DATE = "2222-11-22T22:11:00+0100";
	
	public void testByBounds()
	{
		QueryChangesetsFilters filters = new QueryChangesetsFilters();
		filters.byBounds(new Bounds(0, 5, 10, 15));
		
		assertEquals("5.0,0.0,15.0,10.0", getParam(filters.toParamString(), "bbox"));
	}
	
	public void testByChangesets()
	{
		QueryChangesetsFilters filters = new QueryChangesetsFilters();
		filters.byChangesets(1, 2, 3);
		
		assertEquals("1,2,3", getParam(filters.toParamString(), "changesets"));
	}
	
	public void testByUserId()
	{
		QueryChangesetsFilters filters = new QueryChangesetsFilters();
		filters.byUser(4);
		
		assertEquals("4", getParam(filters.toParamString(), "user"));
	}
	
	public void testOnlyClosed()
	{
		QueryChangesetsFilters filters = new QueryChangesetsFilters();
		filters.onlyClosed();

		assertEquals("true", getParam(filters.toParamString(), "closed"));
	}
	
	public void testOnlyOpen()
	{
		QueryChangesetsFilters filters = new QueryChangesetsFilters();
		filters.onlyOpen();

		assertEquals("true", getParam(filters.toParamString(), "open"));
	}
	
	public void testByClosedAfter() throws ParseException
	{
		QueryChangesetsFilters filters = new QueryChangesetsFilters();
		filters.byClosedAfter(df.parse(VALID_DATE));
		
		assertEquals(VALID_DATE, getParam(filters.toParamString(), "time"));
	}
	
	public void testTwoDates()
	{
		QueryChangesetsFilters filters = new QueryChangesetsFilters();
		filters.byOpenSomeTimeBetween(new Date(), new Date());

		String result = filters.toParamString();

		assertTrue(getParam(result, "time").contains(","));
	}
	
	public void testByChangesetIds()
	{
		QueryChangesetsFilters filters = new QueryChangesetsFilters();
		List<Long> ids = new ArrayList<>();
		ids.add(1L);
		ids.add(2L);
		ids.add(3L);
		filters.byChangesets(ids);

		assertEquals("1,2,3", getParam(filters.toParamString(), "changesets"));
		
		filters.byChangesets(1,2,3);
		
		assertEquals("1,2,3", getParam(filters.toParamString(), "changesets"));
	}
	
	public void testByUserName()
	{
		QueryChangesetsFilters filters = new QueryChangesetsFilters();
		filters.byUser("hans");

		assertEquals("hans", getParam(filters.toParamString(), "display_name"));
	}

	public void testIllegalBounds()
	{
		try
		{
			new QueryChangesetsFilters().byBounds(new Bounds(0, 15, 10, 5));
			fail();
		}
		catch(IllegalArgumentException e) {}
	}

	public void testIllegalUserSelection()
	{
		QueryChangesetsFilters filters = new QueryChangesetsFilters();
		try
		{
			filters.byUser(1);
			filters.byUser("hans");
			fail();
		}
		catch(IllegalArgumentException e) {}
		
		QueryChangesetsFilters filters2 = new QueryChangesetsFilters();

		try
		{
			filters2.byUser("hans");
			filters2.byUser(1);
			fail();
		}
		catch(IllegalArgumentException e) {}
	}
	
	public void testIllegalChangesetCount()
	{
		try
		{
			new QueryChangesetsFilters().byChangesets();
			fail();
		}
		catch(IllegalArgumentException e) {}
		
		try
		{
			new QueryChangesetsFilters().byChangesets(new ArrayList<Long>());
			fail();
		}
		catch(IllegalArgumentException e) {}
	}

	private String getParam(String params, String paramName)
	{
		int startsAt = params.indexOf(paramName);
		if(startsAt == -1)
		{
			throw new RuntimeException(paramName + " not found in " + params);
		}

		int valueStartsAt = startsAt + paramName.length() + 1;
		int valueEndsAt = params.indexOf("&", valueStartsAt);
		if(valueEndsAt == -1)
			return params.substring(valueStartsAt);
		else
			return params.substring(valueStartsAt, valueEndsAt);
	}
}
