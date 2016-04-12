package de.westnordost.osmapi.changesets;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import de.westnordost.osmapi.Handler;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.SingleElementHandler;
import de.westnordost.osmapi.errors.OsmAuthorizationException;
import de.westnordost.osmapi.errors.OsmConflictException;
import de.westnordost.osmapi.errors.OsmNotFoundException;
import de.westnordost.osmapi.map.changes.MapDataChangesHandler;
import de.westnordost.osmapi.map.changes.MapDataChangesParser;

/** Gets information for, searches for and shows changeset discussions. */
public class ChangesetsDao
{
	private static final String CHANGESET = "changeset";

	private final OsmConnection osm;

	public ChangesetsDao(OsmConnection osm)
	{
		this.osm = osm;
	}

	/** Get the changeset information with the given id. Always includes the changeset discussion.
	 *
	 * @param id changeset id
	 * @return info for the given changeset. Null if it does not exist. */
	public ChangesetInfo getChangeset(long id)
	{
		SingleElementHandler<ChangesetInfo> handler = new SingleElementHandler<>();
		String query = CHANGESET + "/" + id + "?include_discussion=true";
		try
		{
			osm.makeRequest(query, new ChangesetParser(handler));
		}
		catch(OsmNotFoundException e)
		{
			return null;
		}
		return handler.get();
	}

	/** Get a number of changesets that match the given filters.
	 *
	 *  @param handler The handler which is fed the incoming changeset infos
	 *  @param filters what to search for. I.e.
	 *                 new QueryChangesetsFilters().byUser(123).onlyClosed() */
	public void getChangesets(Handler<ChangesetInfo> handler, QueryChangesetsFilters filters)
	{
		String query = filters != null ? "?" + filters.toParamString() : "";
		try
		{
			osm.makeRequest(CHANGESET + "s" + query, new ChangesetParser(handler));
		}
		catch(OsmNotFoundException e)
		{
			// ok, we are done (ignore the exception)
		}
	}

	/**
	 * Add a comment to the given changeset. The changeset must already be closed. Adding a comment
	 * to a changeset automatically subscribes the user to it.
	 * Note: as of February 2016, the OSM API 0.6 returns the updated changesetInfo, however
	 * <b>without</b> the changeset discussion you were contributing to.
	 * See https://github.com/openstreetmap/openstreetmap-website/issues/1165
	 * TODO monitor/contribute to the issue
	 *
	 * @param id id of the changeset
	 * @param text text to add to the changeset. Must not be empty
	 * @return the updated changeset
	 *
	 * @throws OsmAuthorizationException if this application is not authorized to modify the map
	 *                                    (Permission.MODIFY_MAP)
	 * @throws OsmConflictException if the changeset is not yet closed. (Only closed changesets can
	 *                              be commented
	 */
	public ChangesetInfo commentChangeset(long id, String text)
	{
		if(text.isEmpty())
		{
			throw new IllegalArgumentException("Text must not be empty");
		}

		SingleElementHandler<ChangesetInfo> handler = new SingleElementHandler<>();

		String apiCall = CHANGESET + "/" + id + "/comment?text=" + urlEncodeText(text);
		osm.makeAuthenticatedRequest(apiCall, "POST", new ChangesetParser(handler));
		return handler.get();
	}

	private String urlEncodeText(String text)
	{
		try
		{
			return URLEncoder.encode(text, osm.getCharset());
		}
		catch (UnsupportedEncodingException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Subscribe the user to a changeset discussion. The changeset must be closed already.
	 *
	 * @param id id of the changeset
	 * @return the changeset
	 *
	 * @throws OsmAuthorizationException if this application is not authorized to modify the map
	 *                                    (Permission.MODIFY_MAP)
	 * @throws OsmConflictException the changeset has not been closed yet
	 * @throws OsmNotFoundException if the given changeset does not exist
	 */
	public ChangesetInfo subscribeToChangeset(long id)
	{
		SingleElementHandler<ChangesetInfo> handler = new SingleElementHandler<>();
		ChangesetInfo result;
		try
		{
			String apiCall = CHANGESET + "/" + id + "/subscribe";
			osm.makeAuthenticatedRequest(apiCall, "POST", new ChangesetParser(handler));
			result = handler.get();
		}
		catch(OsmConflictException e)
		{
			/* either this is because the changeset has not been closed yet or the user already
			   subscribed to the changeset. We want to ignore the latter */
			result = getChangeset(id);
			if(result.isOpen()) throw e;
		}
		return result;
	}

	/**
	 * Unsubscribe the user from a changeset discussion. The changeset must be closed already.
	 *
	 * @param id id of the changeset
	 * @return the changeset
	 *
	 * @throws OsmAuthorizationException if this application is not authorized to modify the map
	 *                                    (Permission.MODIFY_MAP)
	 * @throws OsmConflictException the changeset has not been closed yet
	 * @throws OsmNotFoundException if the given changeset does not exist
	 *
	 */
	// TODO or return void?
	public ChangesetInfo unsubscribeFromChangeset(long id)
	{
		SingleElementHandler<ChangesetInfo> handler = new SingleElementHandler<>();
		ChangesetInfo result;

		try
		{
			String apiCall = CHANGESET + "/" + id + "/unsubscribe";
			osm.makeAuthenticatedRequest(apiCall, "POST", new ChangesetParser(handler));
			result = handler.get();
		}
		catch(OsmNotFoundException e)
		{
		/* the API is inconsistent here when compared to the "subscribe" command: It returns a 404
		   if the changeset does not exist OR if the user did not subscribe to the changeset. We
		   want to rethrow it only if the changeset really does not exist

		   https://github.com/openstreetmap/openstreetmap-website/issues/1199
		   TODO monitor this issue
		   */
			result = getChangeset(id);
			if(result == null) throw e;
		}

		return result;
	}

	/**
	 * @throws OsmNotFoundException if changeset with the given id does not exist
	 */
	public void getChanges(long id, MapDataChangesHandler handler)
	{
		osm.makeRequest(CHANGESET + "/" + id + "/download", new MapDataChangesParser(handler));
	}

}
