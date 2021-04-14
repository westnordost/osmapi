package de.westnordost.osmapi.notes;

import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.*;

public class QueryNotesFiltersTest
{
	@Test public void byTerm()
	{
		QueryNotesFilters filters = new QueryNotesFilters();
		filters.byTerm("hallo");

		assertEquals("hallo", getParam(filters.toParamString(), "q"));
	}

	@Test public void byUserId()
	{
		QueryNotesFilters filters = new QueryNotesFilters();
		filters.byUser(4);
		
		assertEquals("4", getParam(filters.toParamString(), "user"));
	}
	
	@Test public void byUserName()
	{
		QueryNotesFilters filters = new QueryNotesFilters();
		filters.byUser("hans");

		assertEquals("hans", getParam(filters.toParamString(), "display_name"));
	}

	@Test public void illegalUserSelection()
	{
		QueryNotesFilters filters = new QueryNotesFilters();
		try
		{
			filters.byUser(1);
			filters.byUser("hans");
			fail();
		}
		catch(IllegalArgumentException ignore) {}
		
		QueryNotesFilters filters2 = new QueryNotesFilters();

		try
		{
			filters2.byUser("hans");
			filters2.byUser(1);
			fail();
		}
		catch(IllegalArgumentException ignore) {}
	}

	@Test public void limit()
	{
		QueryNotesFilters filters = new QueryNotesFilters();
		filters.limit(10);

		assertEquals("10", getParam(filters.toParamString(), "limit"));
	}

	@Test public void illegalLimit()
	{
		QueryNotesFilters filters = new QueryNotesFilters();
		try
		{
			filters.limit(0);
			fail();
		}
		catch (IllegalArgumentException ignore) {}

		try
		{
			filters.limit(10001);
			fail();
		}
		catch (IllegalArgumentException ignore) {}
	}

	@Test public void createdBefore()
	{
		QueryNotesFilters filters = new QueryNotesFilters();
		Instant validDate = Instant.parse("2222-11-22T22:11:00Z");
		filters.createdBefore(validDate);

		assertEquals(validDate, Instant.parse(getParam(filters.toParamString(), "to")));
	}

	@Test public void createdAfter()
	{
		QueryNotesFilters filters = new QueryNotesFilters();
		Instant validDate = Instant.parse("2222-11-22T22:11:00Z");
		filters.createdAfter(validDate);

		assertEquals(validDate, Instant.parse(getParam(filters.toParamString(), "from")));
	}

	@Test public void closedNotesAfter()
	{
		QueryNotesFilters filters = new QueryNotesFilters();
		filters.hideClosedNotesAfter(4);

		assertEquals("4", getParam(filters.toParamString(), "closed"));
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
