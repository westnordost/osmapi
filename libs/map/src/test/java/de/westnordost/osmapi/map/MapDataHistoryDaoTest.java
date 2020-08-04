package de.westnordost.osmapi.map;

import junit.framework.TestCase;
import de.westnordost.osmapi.ConnectionTestFactory;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.errors.OsmNotFoundException;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.Way;

public class MapDataHistoryDaoTest extends TestCase
{
	private MapDataHistoryDao liveDao;
	private MapDataHistoryDao dao;

	@Override
	protected void setUp()
	{
		liveDao = new MapDataHistoryDao(ConnectionTestFactory.createLiveConnection());
		dao = new MapDataHistoryDao(ConnectionTestFactory.createConnection(null));
	}

	public void testGetUnknownNodeHistory()
	{
		try
		{
			dao.getNodeHistory(Long.MAX_VALUE, new NullHandler<Node>());
			fail();
		}
		catch(OsmNotFoundException ignore) { }

		assertNull(dao.getNodeVersion(Long.MAX_VALUE,1));
	}

	public void testGetUnknownWayHistory()
	{
		try
		{
			dao.getWayHistory(Long.MAX_VALUE, new NullHandler<Way>());
			fail();
		}
		catch(OsmNotFoundException ignore) { }
		assertNull(dao.getWayVersion(Long.MAX_VALUE, 1));
	}

	public void testGetUnknownRelationHistory()
	{
		try
		{
			dao.getRelationHistory(Long.MAX_VALUE, new NullHandler<Relation>());
			fail();
		}
		catch(OsmNotFoundException ignore) { }
		assertNull(dao.getRelationVersion(Long.MAX_VALUE, 1));
	}

	public void testGetUnknownNodeVersion()
	{
		assertNull(liveDao.getNodeVersion(ElementShouldExist.NODE, Integer.MAX_VALUE));
	}

	public void testGetUnknownWayVersion()
	{
		assertNull(liveDao.getWayVersion(ElementShouldExist.WAY, Integer.MAX_VALUE));
	}

	public void testGetUnknownRelationVersion()
	{
		assertNull(liveDao.getRelationVersion(ElementShouldExist.RELATION, Integer.MAX_VALUE));
	}

	public void testGetNodeHistory()
	{
		CountHandler<Node> handler = new CountHandler<>();
		liveDao.getNodeHistory(ElementShouldExist.NODE, handler);
		assertTrue(handler.count > 0);
		assertNotNull(liveDao.getNodeVersion(ElementShouldExist.NODE, 1));
	}

	public void testGetWayHistory()
	{
		CountHandler<Way> handler = new CountHandler<>();
		liveDao.getWayHistory(ElementShouldExist.WAY, handler);
		assertTrue(handler.count > 0);
		assertNotNull(liveDao.getWayVersion(ElementShouldExist.WAY, 1));
	}

	public void testGetRelationHistory()
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
