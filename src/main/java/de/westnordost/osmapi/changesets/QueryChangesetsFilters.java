package de.westnordost.osmapi.changesets;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.westnordost.osmapi.common.OsmXmlDateFormat;
import de.westnordost.osmapi.map.data.BoundingBox;

public class QueryChangesetsFilters
{
	private static final String CHARSET = "UTF-8";

	private final OsmXmlDateFormat dateFormat = new OsmXmlDateFormat();
	
	private Map<String, String> params = new HashMap<>();

	/**
	 * @param displayName limit search to only the given user name
	 * @throws IllegalArgumentException if a user has already been specified by id
	 */
	public QueryChangesetsFilters byUser(String displayName)
	{
		if(params.containsKey("user"))
		{
			throw new IllegalArgumentException("already provided a user ID");
		}
		try
		{
			params.put("display_name", URLEncoder.encode(displayName, CHARSET));
		}
		catch (UnsupportedEncodingException e) { }
		return this;
	}

	/**
	 * @param userId limit search to only the given user id
	 * @throws IllegalArgumentException if a user has already been specified by user name
	 */
	public QueryChangesetsFilters byUser(long userId)
	{
		if(params.containsKey("display_name"))
		{
			throw new IllegalArgumentException("already provided a user name");
		}
		params.put("user", String.valueOf(userId));
		return this;
	}

	/**
	 * @param bounds limit search by these bounds
	 * @throws IllegalArgumentException if the bounds do cross the 180th meridian
	 */
	public QueryChangesetsFilters byBounds(BoundingBox bounds)
	{
		if(bounds.crosses180thMeridian())
		{
			throw new IllegalArgumentException("The bounds must not cross the 180th meridian");
		}

		params.put("bbox",bounds.getAsLeftBottomRightTopString());
		return this;
	}

	public QueryChangesetsFilters onlyClosed()
	{
		params.put("closed", "true");
		return this;
	}

	public QueryChangesetsFilters onlyOpen()
	{
		params.put("open", "true");
		return this;
	}

	/** @param changesetIds at least one changeset id to search for
	 *  @throws IllegalArgumentException if the collection is empty  */
	public QueryChangesetsFilters byChangesets( Collection<Long> changesetIds )
	{
		long[] changesetIdArray = new long[changesetIds.size()];
		int i = 0;
		for(Long id : changesetIds) changesetIdArray[i++] = id;
		
		return byChangesets(changesetIdArray);
	}

	/** @param changesetIds at least one changeset id to search for
	 *  @throws IllegalArgumentException if the collection is empty  */
	public QueryChangesetsFilters byChangesets( long... changesetIds )
	{
		if(changesetIds.length == 0)
		{
			throw new IllegalArgumentException("Must at least supply one changeset id");
		}

		StringBuilder cids = new StringBuilder();
		for(int i = 0; i < changesetIds.length; ++i)
		{
			if(i > 0) cids.append(",");
			cids.append(changesetIds[i]);
		}
		params.put("changesets", cids.toString());
		return this;
	}

	/**
	 * @param closedAfter limit search to changesets that have been closed after this date
	 */
	public QueryChangesetsFilters byClosedAfter(Date closedAfter)
	{
		params.put("time", dateFormat.format(closedAfter));
		return this;
	}

	/**
	 * Filter by changesets that have at one time been open during the given time range
	 * 
	 * @param closedAfter limit search to changesets that have been closed after this date
	 * @param createdBefore limit search to changesets that have been created before this date
	 */
	public QueryChangesetsFilters byOpenSomeTimeBetween(Date createdBefore, Date closedAfter)
	{
		params.put("time", dateFormat.format(closedAfter) + "," + dateFormat.format(createdBefore));
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
