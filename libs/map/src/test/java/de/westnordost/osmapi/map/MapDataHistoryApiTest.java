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
	private MapDataHistoryApi liveDao;
	private MapDataHistoryApi dao;

	@Before public void setUp()
	{
		liveDao = new MapDataHistoryApi(ConnectionTestFactory.createLiveConnection());
		dao = new MapDataHistoryApi(ConnectionTestFactory.createConnection(null));
	}

	@Test public void getUnknownNodeHistory()
	{
		try
		{
			dao.getNodeHistory(Long.MAX_VALUE, new NullHandler<Node>());
			fail();
		}
		catch(OsmNotFoundException ignore) { }

		assertNull(dao.getNodeVersion(Long.MAX_VALUE,1));
	}

	@Test public void getUnknownWayHistory()
	{
		try
		{
			dao.getWayHistory(Long.MAX_VALUE, new NullHandler<Way>());
			fail();
		}
		catch(OsmNotFoundException ignore) { }
		assertNull(dao.getWayVersion(Long.MAX_VALUE, 1));
	}

	@Test public void getUnknownRelationHistory()
	{
		try
		{
			dao.getRelationHistory(Long.MAX_VALUE, new NullHandler<Relation>());
			fail();
		}
		catch(OsmNotFoundException ignore) { }
		assertNull(dao.getRelationVersion(Long.MAX_VALUE, 1));
	}

	@Test public void getUnknownNodeVersion()
	{
		assertNull(liveDao.getNodeVersion(ElementShouldExist.NODE, Integer.MAX_VALUE));
	}

	@Test public void getUnknownWayVersion()
	{
		assertNull(liveDao.getWayVersion(ElementShouldExist.WAY, Integer.MAX_VALUE));
	}

	@Test public void getUnknownRelationVersion()
	{
		assertNull(liveDao.getRelationVersion(ElementShouldExist.RELATION, Integer.MAX_VALUE));
	}

	@Test public void getNodeHistory()
	{
		CountHandler<Node> handler = new CountHandler<>();
		liveDao.getNodeHistory(ElementShouldExist.NODE, handler);
		assertTrue(handler.count > 0);
		assertNotNull(liveDao.getNodeVersion(ElementShouldExist.NODE, 1));
	}

	@Test public void getWayHistory()
	{
		CountHandler<Way> handler = new CountHandler<>();
		liveDao.getWayHistory(ElementShouldExist.WAY, handler);
		assertTrue(handler.count > 0);
		assertNotNull(liveDao.getWayVersion(ElementShouldExist.WAY, 1));
	}

	@Test public void getRelationHistory()
	{
		CountHandler<Relation> handler = new CountHandler<>();
		liveDao.getRelationHistory(ElementShouldExist.RELATION, handler);
		assertTrue(handler.count > 0);
		assertNotNull(liveDao.getRelationVersion(ElementShouldExist.RELATION, 1));
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
