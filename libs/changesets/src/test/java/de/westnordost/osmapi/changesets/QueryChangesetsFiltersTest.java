package de.westnordost.osmapi.changesets;

import junit.framework.TestCase;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.westnordost.osmapi.common.OsmXmlDateFormat;
import de.westnordost.osmapi.map.data.BoundingBox;

public class QueryChangesetsFiltersTest extends TestCase
{
	private final OsmXmlDateFormat dateFormat = new OsmXmlDateFormat();
	
	public void testByBounds()
	{
		QueryChangesetsFilters filters = new QueryChangesetsFilters();
		filters.byBounds(new BoundingBox(0, 5, 10, 15));
		
		assertEquals("5,0,15,10", getParam(filters.toParamString(), "bbox"));
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
		Date validDate = dateFormat.parse("2222-11-22T22:11:00Z");
		filters.byClosedAfter(validDate);

		assertEquals(validDate, dateFormat.parse(getParam(filters.toParamString(), "time")));
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
			new QueryChangesetsFilters().byBounds(new BoundingBox(0, 15, 10, 5));
			fail();
		}
		catch(IllegalArgumentException ignore) {}
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
		catch(IllegalArgumentException ignore) {}
		
		QueryChangesetsFilters filters2 = new QueryChangesetsFilters();

		try
		{
			filters2.byUser("hans");
			filters2.byUser(1);
			fail();
		}
		catch(IllegalArgumentException ignore) {}
	}
	
	public void testIllegalChangesetCount()
	{
		try
		{
			new QueryChangesetsFilters().byChangesets();
			fail();
		}
		catch(IllegalArgumentException ignore) {}
		
		try
		{
			new QueryChangesetsFilters().byChangesets(new ArrayList<Long>());
			fail();
		}
		catch(IllegalArgumentException ignore) {}
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
