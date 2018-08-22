package de.westnordost.osmapi.map;

import java.net.HttpURLConnection;

import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.errors.OsmApiException;
import de.westnordost.osmapi.common.errors.OsmNotFoundException;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.Way;
import de.westnordost.osmapi.map.handler.MapDataHandler;
import de.westnordost.osmapi.map.handler.SingleOsmElementHandler;
import de.westnordost.osmapi.map.handler.WrapperOsmElementHandler;

/** Get old versions of map data.
 *
 *  Note that the (complete) history of certain elements in the OSM database can be extremely large
 *  and may take minutes to load. */
public class MapDataHistoryDao
{
	private static final String NODE = "node";
	private static final String WAY = "way";
	private static final String RELATION = "relation";
	private static final String HISTORY = "history";

	private final OsmConnection osm;
	private final MapDataFactory factory;
	
	public MapDataHistoryDao(OsmConnection osm, MapDataFactory factory)
	{
		this.osm = osm;
		this.factory = factory;
	}
	
	public MapDataHistoryDao(OsmConnection osm)
	{
		this(osm, new OsmMapDataFactory());
	}

	/** Feeds all versions of the given node to the handler. The elements are sorted by version,
	 *  the oldest version is the first, the newest version is the last element.<br>
	 *  If not logged in, the Changeset for each returned element will be null
	 *
	 * @throws OsmNotFoundException if the node has not been found. */
	public void getNodeHistory(long id, Handler<Node> handler)
	{
		MapDataHandler mapDataHandler = new WrapperOsmElementHandler<>(Node.class, handler);
		boolean authenticate = osm.getOAuth() != null;
		osm.makeRequest(NODE + "/" + id + "/" + HISTORY, authenticate,
				new MapDataParser(mapDataHandler, factory));
	}

	/** Feeds all versions of the given way to the handler. The elements are sorted by version,
	 *  the oldest version is the first, the newest version is the last element.<br>
	 *  If not logged in, the Changeset for each returned element will be null
	 *
	 * @throws OsmNotFoundException if the node has not been found. */
	public void getWayHistory(long id, Handler<Way> handler)
	{
		MapDataHandler mapDataHandler = new WrapperOsmElementHandler<>(Way.class, handler);
		boolean authenticate = osm.getOAuth() != null;
		osm.makeRequest(WAY + "/" + id + "/" + HISTORY, authenticate,
				new MapDataParser(mapDataHandler, factory));
	}

	/** Feeds all versions of the given relation to the handler. The elements are sorted by version,
	 *  the oldest version is the first, the newest version is the last element.<br>
	 *  If not logged in, the Changeset for each returned element will be null
	 *
	 * @throws OsmNotFoundException if the node has not been found. */
	public void getRelationHistory(long id, Handler<Relation> handler)
	{
		MapDataHandler mapDataHandler = new WrapperOsmElementHandler<>(Relation.class, handler);
		boolean authenticate = osm.getOAuth() != null;
		osm.makeRequest(RELATION + "/" + id + "/" + HISTORY, authenticate,
				new MapDataParser(mapDataHandler, factory));
	}

	/** Note that if not logged in, the Changeset for each returned element will be null
	 *
	 *  @return the given version of the given element or null if either the node or given the version
	 *          of the node does not exist (anymore).  */
	public Node getNodeVersion(long id, int version)
	{
		return getElementVersion(NODE + "/" + id + "/" + version, Node.class);
	}

	/** Note that if not logged in, the Changeset for each returned element will be null
	 *
	 *  @return the given version of the given element or null if either the node or given the version
	 *          of the node does not exist (anymore).  */
	public Way getWayVersion(long id, int version)
	{
		return getElementVersion(WAY + "/" + id + "/" + version, Way.class);
	}

	/** Note that if not logged in, the Changeset for each returned element will be null
	 *
	 *  @return the given version of the given element or null if either the node or given the version
	 *          of the node does not exist (anymore).  */
	public Relation getRelationVersion(long id, int version)
	{
		return getElementVersion(RELATION + "/" + id + "/" + version, Relation.class);
	}

	private <T extends Element> T getElementVersion(String call, Class<T> tClass)
	{
		SingleOsmElementHandler<T> handler = new SingleOsmElementHandler<>(tClass);
		try
		{
			boolean authenticate = osm.getOAuth() != null;
			osm.makeRequest(call, authenticate, new MapDataParser(handler, factory));
		}
		catch(OsmApiException e)
		{
			/* if a version of an element has been redacted, the api will send back a 403
			 *  forbidden error instead of a 404. Since for the user, this is just a "it's not
			 *  there" because he has no way to acquire redacted versions, it should just return
			 *  null. The error code between "version does not exist" and "element does not exist"
			 *  does not differ, so we cannot make a distinction here */
			switch(e.getErrorCode())
			{
				case HttpURLConnection.HTTP_NOT_FOUND:
				case HttpURLConnection.HTTP_GONE:
				case HttpURLConnection.HTTP_FORBIDDEN:
					return null;
			}
			throw e;
		}
		return handler.get();
	}
}
