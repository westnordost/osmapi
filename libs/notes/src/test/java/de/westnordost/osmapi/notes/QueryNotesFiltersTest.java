package de.westnordost.osmapi.notes;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.notes.QueryNotesFilters.NoteProperty;
import de.westnordost.osmapi.notes.QueryNotesFilters.Order;
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

	@Test public void byBoundingBox()
	{
		QueryNotesFilters filters = new QueryNotesFilters();
		filters.byBoundingBox(new BoundingBox(-5.0, -10.0, 6.0, 7.0));

		assertEquals("-10,-5,7,6", getParam(filters.toParamString(), "bbox"));
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

	@Test public void orderByAscending()
	{
		QueryNotesFilters filters = new QueryNotesFilters();
		filters.orderBy(NoteProperty.CREATION_DATE, Order.ASCENDING);

		assertEquals("created_at", getParam(filters.toParamString(), "sort"));
		assertEquals("oldest", getParam(filters.toParamString(), "order"));
	}

	@Test public void orderByNewest()
	{
		QueryNotesFilters filters = new QueryNotesFilters();
		filters.orderBy(NoteProperty.UPDATE_DATE, Order.DESCENDING);

		assertEquals("updated_at", getParam(filters.toParamString(), "sort"));
		assertEquals("newest", getParam(filters.toParamString(), "order"));
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
