package de.westnordost.osmapi.changesets;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.SingleElementHandler;
import de.westnordost.osmapi.common.errors.OsmAuthorizationException;
import de.westnordost.osmapi.common.errors.OsmConflictException;
import de.westnordost.osmapi.common.errors.OsmNotFoundException;
import de.westnordost.osmapi.map.MapDataFactory;
import de.westnordost.osmapi.map.OsmMapDataFactory;
import de.westnordost.osmapi.map.changes.MapDataChangesHandler;
import de.westnordost.osmapi.map.changes.MapDataChangesParser;

/** Gets information for, searches for and shows changeset discussions.
 *  All interactions with this class require an OsmConnection with a logged in user. */
public class ChangesetsApi
{
	private static final String CHANGESET = "changeset";

	private final OsmConnection osm;

	public ChangesetsApi(OsmConnection osm)
	{
		this.osm = osm;
	}

	/** Get the changeset information with the given id. Always includes the changeset discussion.
	 *
	 * @param id changeset id
	 * @return info for the given changeset. Null if it does not exist. */
	public ChangesetInfo get(long id)
	{
		SingleElementHandler<ChangesetInfo> handler = new SingleElementHandler<>();
		String query = CHANGESET + "/" + id + "?include_discussion=true";
		try
		{
			boolean authenticate = osm.getOAuthAccessToken() != null;
			osm.makeRequest(query, authenticate, new ChangesetParser(handler));
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
	 *                 new QueryChangesetsFilters().byUser(123).onlyClosed()
	 *
	 *	@throws OsmAuthorizationException if not logged in */
	public void find(Handler<ChangesetInfo> handler, QueryChangesetsFilters filters)
	{
		String query = filters != null ? "?" + filters.toParamString() : "";
		try
		{
			boolean authenticate = osm.getOAuthAccessToken() != null;
			osm.makeRequest(CHANGESET + "s" + query, authenticate, new ChangesetParser(handler));
		}
		catch(OsmNotFoundException e)
		{
			// ok, we are done (ignore the exception)
		}
	}

	/**
	 * Add a comment to the given changeset. The changeset must already be closed. Adding a comment
	 * to a changeset automatically subscribes the user to it.
	 * 
	 * TODO monitor https://github.com/openstreetmap/openstreetmap-website/issues/1165
	 *
	 * @param id id of the changeset
	 * @param text text to add to the changeset. Must not be empty
	 * 
	 * @return the updated changeset
	 *
	 * @throws OsmAuthorizationException if this application is not authorized to modify the map
	 *                                    (Permission.MODIFY_MAP)
	 * @throws OsmConflictException if the changeset is not yet closed. (Only closed changesets can
	 *                              be commented
	 */
	public ChangesetInfo comment(long id, String text)
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
			return URLEncoder.encode(text, OsmConnection.CHARSET);
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
	 * @throws OsmNotFoundException if the given changeset does not exist
	 */
	public ChangesetInfo subscribe(long id)
	{
		SingleElementHandler<ChangesetInfo> handler = new SingleElementHandler<>();
		ChangesetInfo result;
		try
		{
			String apiCall = CHANGESET + "/" + id + "/subscribe";
			osm.makeAuthenticatedRequest(apiCall, "POST", new ChangesetParser(handler));
			result = handler.get();
		}
		catch(OsmConflictException ignore)
		{
			/* ignore this exception which occurs when the user already subscribed to the changeset */
			result = get(id);
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
	 * @throws OsmNotFoundException if the given changeset does not exist
	 *
	 */
	public ChangesetInfo unsubscribe(long id)
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

		   TODO monitor https://github.com/openstreetmap/openstreetmap-website/issues/1199
		   */
			result = get(id);
			if(result == null) throw e;
		}

		return result;
	}

	/**
	 * Get map data changes associated with the given changeset, using the default OsmMapDataFactory
	 * 
	 * @see #getData(long, MapDataChangesHandler, MapDataFactory)
	 */
	public void getData(long id, MapDataChangesHandler handler)
	{
		getData(id, handler, new OsmMapDataFactory());
	}

	/**
	 * Get map data changes associated with the given changeset.
	 *
	 * @param id id of the changeset to get the data for
	 * @param handler handler to feed the map data changes to
	 * @param factory factory that creates the elements
	 *
	 * @throws OsmNotFoundException if changeset with the given id does not exist
	 */
	public void getData(long id, MapDataChangesHandler handler, MapDataFactory factory)
	{
		boolean authenticate = osm.getOAuthAccessToken() != null;
		osm.makeRequest(CHANGESET + "/" + id + "/download", authenticate, new MapDataChangesParser(handler, factory));
	}
}
