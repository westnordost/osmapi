package de.westnordost.osmapi.map;

import org.junit.Before;
import org.junit.Test;

import de.westnordost.osmapi.ConnectionTestFactory;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.errors.OsmNotFoundException;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.Way;

import static org.junit.Assert.*;

public class MapDataHistoryApiTest
{
	private MapDataHistoryApi liveApi;
	private MapDataHistoryApi api;

	@Before public void setUp()
	{
		liveApi = new MapDataHistoryApi(ConnectionTestFactory.createLiveConnection());
		api = new MapDataHistoryApi(ConnectionTestFactory.createConnection(null));
	}

	@Test public void getUnknownNodeHistory()
	{
		try
		{
			api.getNodeHistory(Long.MAX_VALUE, new NullHandler<Node>());
			fail();
		}
		catch(OsmNotFoundException ignore) { }

		assertNull(api.getNodeVersion(Long.MAX_VALUE,1));
	}

	@Test public void getUnknownWayHistory()
	{
		try
		{
			api.getWayHistory(Long.MAX_VALUE, new NullHandler<Way>());
			fail();
		}
		catch(OsmNotFoundException ignore) { }
		assertNull(api.getWayVersion(Long.MAX_VALUE, 1));
	}

	@Test public void getUnknownRelationHistory()
	{
		try
		{
			api.getRelationHistory(Long.MAX_VALUE, new NullHandler<Relation>());
			fail();
		}
		catch(OsmNotFoundException ignore) { }
		assertNull(api.getRelationVersion(Long.MAX_VALUE, 1));
	}

	@Test public void getUnknownNodeVersion()
	{
		assertNull(liveApi.getNodeVersion(ElementShouldExist.NODE, Integer.MAX_VALUE));
	}

	@Test public void getUnknownWayVersion()
	{
		assertNull(liveApi.getWayVersion(ElementShouldExist.WAY, Integer.MAX_VALUE));
	}

	@Test public void getUnknownRelationVersion()
	{
		assertNull(liveApi.getRelationVersion(ElementShouldExist.RELATION, Integer.MAX_VALUE));
	}

	@Test public void getNodeHistory()
	{
		CountHandler<Node> handler = new CountHandler<>();
		liveApi.getNodeHistory(ElementShouldExist.NODE, handler);
		assertTrue(handler.count > 0);
		assertNotNull(liveApi.getNodeVersion(ElementShouldExist.NODE, 1));
	}

	@Test public void getWayHistory()
	{
		CountHandler<Way> handler = new CountHandler<>();
		liveApi.getWayHistory(ElementShouldExist.WAY, handler);
		assertTrue(handler.count > 0);
		assertNotNull(liveApi.getWayVersion(ElementShouldExist.WAY, 1));
	}

	@Test public void getRelationHistory()
	{
		CountHandler<Relation> handler = new CountHandler<>();
		liveApi.getRelationHistory(ElementShouldExist.RELATION, handler);
		assertTrue(handler.count > 0);
		assertNotNull(liveApi.getRelationVersion(ElementShouldExist.RELATION, 1));
	}

	private static class NullHandler<T> implements Handler<T>
	{
		public void handle(T tea) { }
	}

	private static class CountHandler<T> implements Handler<T>
	{
		int count;

		public void handle(T tea) { count++; }
	}
}
