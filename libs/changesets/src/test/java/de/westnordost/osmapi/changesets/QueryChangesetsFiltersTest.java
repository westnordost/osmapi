package de.westnordost.osmapi.changesets;

import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import de.westnordost.osmapi.map.data.BoundingBox;

import static org.junit.Assert.*;

public class QueryChangesetsFiltersTest
{
	@Test public void byBounds()
	{
		QueryChangesetsFilters filters = new QueryChangesetsFilters();
		filters.byBounds(new BoundingBox(0, 5, 10, 15));
		
		assertEquals("5,0,15,10", getParam(filters.toParamString(), "bbox"));
	}
	
	@Test public void byChangesets()
	{
		QueryChangesetsFilters filters = new QueryChangesetsFilters();
		filters.byChangesets(1, 2, 3);
		
		assertEquals("1,2,3", getParam(filters.toParamString(), "changesets"));
	}
	
	@Test public void byUserId()
	{
		QueryChangesetsFilters filters = new QueryChangesetsFilters();
		filters.byUser(4);
		
		assertEquals("4", getParam(filters.toParamString(), "user"));
	}
	
	@Test public void onlyClosed()
	{
		QueryChangesetsFilters filters = new QueryChangesetsFilters();
		filters.onlyClosed();

		assertEquals("true", getParam(filters.toParamString(), "closed"));
	}
	
	@Test public void onlyOpen()
	{
		QueryChangesetsFilters filters = new QueryChangesetsFilters();
		filters.onlyOpen();

		assertEquals("true", getParam(filters.toParamString(), "open"));
	}
	
	@Test public void byClosedAfter()
	{
		QueryChangesetsFilters filters = new QueryChangesetsFilters();
		Instant validInstant = Instant.parse("2222-11-22T22:11:00Z");
		filters.byClosedAfter(validInstant);

		assertEquals(validInstant, Instant.parse(getParam(filters.toParamString(), "time")));
	}
	
	@Test public void twoDates()
	{
		QueryChangesetsFilters filters = new QueryChangesetsFilters();
		filters.byOpenSomeTimeBetween(Instant.now(), Instant.now());

		String result = filters.toParamString();

		assertTrue(getParam(result, "time").contains(","));
	}
	
	@Test public void byChangesetIds()
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
	
	@Test public void byUserName()
	{
		QueryChangesetsFilters filters = new QueryChangesetsFilters();
		filters.byUser("hans");

		assertEquals("hans", getParam(filters.toParamString(), "display_name"));
	}

	@Test public void illegalBounds()
	{
		try
		{
			new QueryChangesetsFilters().byBounds(new BoundingBox(0, 15, 10, 5));
			fail();
		}
		catch(IllegalArgumentException ignore) {}
	}

	@Test public void illegalUserSelection()
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
	
	@Test public void illegalChangesetCount()
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
