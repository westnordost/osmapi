package de.westnordost.osmapi.notes;

import junit.framework.TestCase;

import java.text.ParseException;
import java.util.Date;

import de.westnordost.osmapi.common.OsmXmlDateFormat;

public class QueryNotesFiltersTest extends TestCase
{
	private final OsmXmlDateFormat dateFormat = new OsmXmlDateFormat();

	public void testByTerm()
	{
		QueryNotesFilters filters = new QueryNotesFilters();
		filters.byTerm("hallo");

		assertEquals("hallo", getParam(filters.toParamString(), "q"));
	}

	public void testByUserId()
	{
		QueryNotesFilters filters = new QueryNotesFilters();
		filters.byUser(4);
		
		assertEquals("4", getParam(filters.toParamString(), "user"));
	}
	
	public void testByUserName()
	{
		QueryNotesFilters filters = new QueryNotesFilters();
		filters.byUser("hans");

		assertEquals("hans", getParam(filters.toParamString(), "display_name"));
	}

	public void testIllegalUserSelection()
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

	public void testLimit()
	{
		QueryNotesFilters filters = new QueryNotesFilters();
		filters.limit(10);

		assertEquals("10", getParam(filters.toParamString(), "limit"));
	}

	public void testIllegalLimit()
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

	public void testCreatedBefore() throws ParseException
	{
		QueryNotesFilters filters = new QueryNotesFilters();
		Date validDate = dateFormat.parse("2222-11-22T22:11:00Z");
		filters.createdBefore(validDate);

		assertEquals(validDate, dateFormat.parse(getParam(filters.toParamString(), "to")));
	}

	public void testCreatedAfter() throws ParseException
	{
		QueryNotesFilters filters = new QueryNotesFilters();
		Date validDate = dateFormat.parse("2222-11-22T22:11:00Z");
		filters.createdAfter(validDate);

		assertEquals(validDate, dateFormat.parse(getParam(filters.toParamString(), "from")));
	}

	public void testClosedNotesAfter()
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
