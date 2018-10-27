package de.westnordost.osmapi.notes;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.westnordost.osmapi.common.OsmXmlDateFormat;

public class QueryNotesFilters
{
	private static final String CHARSET = "UTF-8";

	private final OsmXmlDateFormat dateFormat = new OsmXmlDateFormat();
	
	private Map<String, String> params = new HashMap<>();

	/**
	 * @param term search for a certain string
	 */
	public QueryNotesFilters byTerm(String term)
	{
		params.put("q", term);
		return this;
	}

	/**
	 * @param displayName limit search to only the given user name
	 * @throws IllegalArgumentException if a user has already been specified by id
	 */
	public QueryNotesFilters byUser(String displayName)
	{
		if(params.containsKey("user"))
		{
			throw new IllegalArgumentException("already provided a user ID");
		}
		try
		{
			params.put("display_name", URLEncoder.encode(displayName, CHARSET));
		}
		catch (UnsupportedEncodingException ignore) { }
		return this;
	}

	/**
	 * @param userId limit search to only the given user id
	 * @throws IllegalArgumentException if a user has already been specified by user name
	 */
	public QueryNotesFilters byUser(long userId)
	{
		if(params.containsKey("display_name"))
		{
			throw new IllegalArgumentException("already provided a user name");
		}
		params.put("user", String.valueOf(userId));
		return this;
	}

	/**
	 * @param date include notes only created after the given date
	 */
	public QueryNotesFilters createdAfter(Date date)
	{
		params.put("from", dateFormat.format(date));
		return this;
	}

	/**
	 * @param date include notes only created after the given date. If not specified, now
	 */
	public QueryNotesFilters createdBefore(Date date)
	{
		params.put("to", dateFormat.format(date));
		return this;
	}

	/**
	 * @param days include closed notes in the search closed not more than x days ago. Default is
	 *             7 days. A value of -1 means all notes are returned
	 */
	public QueryNotesFilters hideClosedNotesAfter(int days)
	{
		params.put("closed", String.valueOf(days));
		return this;
	}

	/**
	 * @param count return at most this many notes. Default is 100. Must be within 1 and 10000.
	 * @throws IllegalArgumentException if the given limit is not within 1 and 10000
	 */
	public QueryNotesFilters limit(int count)
	{
		if(count <= 0 || count > 10000)
		{
			throw new IllegalArgumentException("limit must be within 1 and 10000");
		}
		params.put("limit", String.valueOf(count));
		return this;
	}

	public String toParamString()
	{
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for(Map.Entry<String, String> entry : params.entrySet())
		{
			if(first) first = false;
			else      result.append("&");
			result.append(entry.getKey());
			result.append("=");

			result.append(entry.getValue());
		}
		return result.toString();
	}
}
