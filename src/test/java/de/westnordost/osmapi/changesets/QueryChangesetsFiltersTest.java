package de.westnordost.osmapi.changesets;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.westnordost.osmapi.map.data.Bounds;
import de.westnordost.osmapi.map.data.OsmLatLon;

public class QueryChangesetsFiltersTest extends TestCase
{
	public void testOne()
	{

		QueryChangesetsFilters filters = new QueryChangesetsFilters();
		filters.byBounds(
				new Bounds(OsmLatLon.parseLatLon("0", "5"), OsmLatLon.parseLatLon("10", "15"))
		);
		filters.byChangesets(1, 2, 3);
		filters.byUser(4);
		filters.onlyClosed();
		filters.onlyOpen();
		filters.byTime(new Date());

		String result = filters.toParamString();

		assertEquals("5.0,0.0,15.0,10.0", getParam(result, "bbox"));
		assertEquals("1,2,3", getParam(result, "changesets"));
		assertEquals("4", getParam(result, "user"));
		assertEquals("true", getParam(result, "open"));
		assertEquals("true", getParam(result, "closed"));
		assertNotNull(getParam(result, "time"));
	}

	public void testTwo()
	{
		QueryChangesetsFilters filters = new QueryChangesetsFilters();
		List<Long> ids = new ArrayList<>();
		ids.add((long) 1);
		ids.add((long) 2);
		ids.add((long) 3);
		filters.byChangesets(ids);
		filters.byUser("hans");
		filters.byTime(new Date(), new Date());

		String result = filters.toParamString();

		assertEquals("1,2,3", getParam(result, "changesets"));
		assertEquals("hans", getParam(result, "display_name"));
		assertTrue(getParam(result, "time").contains(","));
	}

	public void testIlegalValues()
	{
		QueryChangesetsFilters filters = new QueryChangesetsFilters();
		try
		{
			filters.byBounds(
					new Bounds(OsmLatLon.parseLatLon("0", "15"),OsmLatLon.parseLatLon("10", "5"))
			);
			fail();
		}
		catch(IllegalArgumentException e) {}

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
