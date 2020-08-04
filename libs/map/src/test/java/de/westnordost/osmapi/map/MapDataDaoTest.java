package de.westnordost.osmapi.map;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import de.westnordost.osmapi.ConnectionTestFactory;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.changesets.ChangesetsDao;
import de.westnordost.osmapi.common.SingleElementHandler;
import de.westnordost.osmapi.common.errors.OsmAuthorizationException;
import de.westnordost.osmapi.common.errors.OsmConflictException;
import de.westnordost.osmapi.common.errors.OsmNotFoundException;
import de.westnordost.osmapi.common.errors.OsmQueryTooBigException;
import de.westnordost.osmapi.map.changes.DiffElement;
import de.westnordost.osmapi.map.changes.SimpleMapDataChangesHandler;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.LatLons;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.map.data.OsmNode;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.Way;
import de.westnordost.osmapi.map.handler.DefaultMapDataHandler;
import de.westnordost.osmapi.map.handler.MapDataHandler;
import de.westnordost.osmapi.map.handler.OneElementTypeHandler;

public class MapDataDaoTest extends TestCase
{
	private OsmConnection privilegedConnection;
	private OsmConnection unprivilegedConnection;
	private OsmConnection connection;
	private OsmConnection liveConnection;

	@Override
	protected void setUp()
	{
		// Create the different connections...
		connection = ConnectionTestFactory.createConnection(null);
		privilegedConnection = ConnectionTestFactory
				.createConnection(ConnectionTestFactory.User.ALLOW_EVERYTHING);
		unprivilegedConnection = ConnectionTestFactory
				.createConnection(ConnectionTestFactory.User.ALLOW_NOTHING);

		// we need to test a few things with live data, because we have no clue
		// how the test data looks like :-(
		// i.e. where to expect data
		liveConnection = ConnectionTestFactory.createLiveConnection();
	}

	public void testGetMapTooBigBounds()
	{
		try
		{
			// this must surely be too big, regardless of the server preferences
			// - it's the whole world!
			new MapDataDao(connection).getMap(
					new BoundingBox(LatLons.MIN_VALUE, LatLons.MAX_VALUE),
					new DefaultMapDataHandler());
			fail();
		}
		catch(OsmQueryTooBigException ignore) {}
	}

	public void testGetMapBoundsCross180thMeridian()
	{
		try
		{
			// using here the smallest possible query area, so it cannot be too
			// big
			new MapDataDao(connection).getMap(new BoundingBox(0, 180, 0.0000001, -179.9999999),
					new DefaultMapDataHandler());
			fail();
		}
		catch(IllegalArgumentException ignore) {}
	}

	public void testGetMapBounds()
	{
		final BoundingBox verySmallBounds = new BoundingBox(31, 31, 31.0000001, 31.0000001);

		new MapDataDao(connection).getMap(verySmallBounds, new DefaultMapDataHandler()
		{
			@Override
			public void handle(BoundingBox bounds)
			{
				assertEquals(verySmallBounds, bounds);
			}
		});
	}

	public void testSomeElements()
	{
		// there is not so much we can test regarding the validity of the
		// returned data, but
		// querying something in the middle of Hamburg should at least make
		// handle(X) be called for
		// every type of element once
		final BoundingBox hamburg = new BoundingBox(53.579, 9.939, 53.580, 9.940);

		CountMapDataHandler counter = new CountMapDataHandler();
		new MapDataDao(liveConnection).getMap(hamburg, counter);

		assertEquals(1, counter.bounds);
		assertTrue(counter.ways > 0);
		assertTrue(counter.nodes > 0);
		assertTrue(counter.relations > 0);
	}

	public void testDownloadMapIsReallyStreamed()
	{
		// should be >1MB of data
		final BoundingBox bigHamburg = new BoundingBox(53.585, 9.945, 53.59, 9.95);

		CheckFirstHandleCallHandler handler = new CheckFirstHandleCallHandler();

		long startTime = System.currentTimeMillis();
		new MapDataDao(liveConnection).getMap(bigHamburg, handler);
		long timeToFirstData = handler.firstCallTime - startTime;
		long totalTime = System.currentTimeMillis() - startTime;

		// a bit of a cheap test, but I had no better idea how to test this:

		assertTrue(totalTime *3/4 > timeToFirstData);
		/*
		 * = the first data can be processed at least after 3/4 of the total
		 * time of transmission. If the processing of the data would only be
		 * started after the whole data had been transferred, the
		 * 'timeToFirstData' would be much closer to 'totalTime' (than
		 * 'startTime')
		 */
	}

	public void testUploadAsAnonymousFails()
	{
		try
		{
			new MapDataDao(connection).updateMap("test", "test", Collections.<Element> emptyList(),
					null);
			fail();
		}
		catch(OsmAuthorizationException ignore) {}
	}

	public void testUploadAsUnprivilegedUserFails()
	{
		try
		{
			new MapDataDao(unprivilegedConnection).updateMap("test", "test",
					Collections.<Element> emptyList(), null);
			fail();
		}
		catch(OsmAuthorizationException ignore) {}
	}

	public void testReadDiff()
	{
		MapDataDao mapDataDao = new MapDataDao(privilegedConnection);

		final LatLon POS = new OsmLatLon(55.42313, 50.13221);
		final long PLACEHOLDER_ID = -33;

		Element node = new OsmNode(PLACEHOLDER_ID, 1, POS, null);
		SingleElementHandler<DiffElement> handlerCreated = new SingleElementHandler<>();
		mapDataDao.updateMap("test", "test", Collections.singletonList(node), handlerCreated);
		DiffElement diffCreated = handlerCreated.get();

		// update id and delete again... (clean up before asserting)
		OsmNode deleteNode = new OsmNode(diffCreated.serverId, diffCreated.serverVersion, POS,
				null);
		deleteNode.setDeleted(true);
		SingleElementHandler<DiffElement> handlerDeleted = new SingleElementHandler<>();
		mapDataDao.updateMap("clean up test", "test", Collections.singletonList((Element) deleteNode),
				handlerDeleted);

		DiffElement diffDeleted = handlerDeleted.get();

		assertEquals(PLACEHOLDER_ID, diffCreated.clientId);
		assertEquals(Element.Type.NODE, diffCreated.type);
		assertNotNull(diffCreated.serverVersion);
		assertEquals(1, (int) diffCreated.serverVersion);

		assertEquals((long) diffCreated.serverId, diffDeleted.clientId);
		assertEquals(Element.Type.NODE, diffDeleted.type);
		assertNull(diffDeleted.serverId);
		assertNull(diffDeleted.serverVersion);
	}

	public void testNotFound()
	{
		MapDataDao dao = new MapDataDao(connection);
		MapDataHandler h = new DefaultMapDataHandler();

		try
		{
			dao.getWayComplete(Long.MAX_VALUE, h);
			fail();
		}
		catch(OsmNotFoundException ignore) {}

		try
		{
			dao.getRelationComplete(Long.MAX_VALUE, h);
			fail();
		}
		catch(OsmNotFoundException ignore) {}
	}

	// we do not test the validity of the data here, just they should not throw
	// exceptions

	public void testWayComplete()
	{
		new MapDataDao(liveConnection).getWayComplete(27308882, new DefaultMapDataHandler());
	}

	public void testRelationComplete()
	{
		new MapDataDao(liveConnection).getRelationComplete(3301989, new DefaultMapDataHandler());
	}

	public void testRelationsForRelation()
	{
		new MapDataDao(liveConnection).getRelationsForRelation(3218689);
	}

	public void testRelationsForWay()
	{
		new MapDataDao(liveConnection).getRelationsForWay(244179625);
	}

	public void testRelationsForNode()
	{
		new MapDataDao(liveConnection).getRelationsForNode(3375377736L);
	}

	public void testWaysForNode()
	{
		new MapDataDao(liveConnection).getWaysForNode(3668931466L);
	}

	public void testEmptySomeElementsForX()
	{
		MapDataDao dao = new MapDataDao(connection);

		assertEquals(0, dao.getRelationsForRelation(Long.MAX_VALUE).size());
		assertEquals(0, dao.getRelationsForNode(Long.MAX_VALUE).size());
		assertEquals(0, dao.getRelationsForWay(Long.MAX_VALUE).size());
		assertEquals(0, dao.getWaysForNode(Long.MAX_VALUE).size());
	}

	public void testGetNode()
	{
		new MapDataDao(liveConnection).getNode(ElementShouldExist.NODE);
		assertNull(new MapDataDao(connection).getNode(Long.MAX_VALUE));
	}

	public void testGetWay()
	{
		new MapDataDao(liveConnection).getWay(ElementShouldExist.WAY);
		assertNull(new MapDataDao(connection).getWay(Long.MAX_VALUE));
	}

	public void testGetRelation()
	{
		new MapDataDao(liveConnection).getRelation(ElementShouldExist.RELATION);
		assertNull(new MapDataDao(connection).getRelation(Long.MAX_VALUE));
	}

	public void testGetNodes()
	{
		// test if a non-existing element does "poison the well"
		// this test will fail if both Yangon and New York place=city nodes do
		// not exist anymore ;-)
		try
		{
			List<Long> places = Arrays.asList(ElementShouldExist.NODE, Long.MAX_VALUE);
			new MapDataDao(liveConnection).getNodes(places);
			fail();
		}
		catch(OsmNotFoundException ignore) {}

		List<Long> place = Collections.singletonList(ElementShouldExist.NODE);
		assertFalse(new MapDataDao(liveConnection).getNodes(place).isEmpty());
	}

	public void testGetRelations()
	{
		// test if a non-existing element does "poison the well"
		// this test will fail if Germany type=boundary does not exist anymore
		try
		{
			List<Long> places = Arrays.asList(ElementShouldExist.RELATION, Long.MAX_VALUE);
			new MapDataDao(liveConnection).getRelations(places);
			fail();
		}
		catch(OsmNotFoundException ignore) {}

		List<Long> place = Collections.singletonList(ElementShouldExist.RELATION);
		assertFalse(new MapDataDao(liveConnection).getRelations(place).isEmpty());
	}

	public void testGetWays()
	{
		// test if a non-existing element does "poison the well"
		// this test will fail if some harbor in Hamburg does not exist
		// anymore...
		try
		{
			List<Long> places = Arrays.asList(ElementShouldExist.WAY, Long.MAX_VALUE);
			List<Way> ways = new MapDataDao(liveConnection).getWays(places);
			ways.isEmpty();
			fail();
		}
		catch(OsmNotFoundException ignore) {}

		List<Long> place = Collections.singletonList(ElementShouldExist.WAY);
		assertFalse(new MapDataDao(liveConnection).getWays(place).isEmpty());
	}

	public void testGetElementsEmpty()
	{
		MapDataDao dao = new MapDataDao(connection);
		assertTrue(dao.getWays(Collections.<Long> emptyList()).isEmpty());
		assertTrue(dao.getNodes(Collections.<Long> emptyList()).isEmpty());
		assertTrue(dao.getRelations(Collections.<Long> emptyList()).isEmpty());
	}

	public void testCloseUnopenedChangesetFails()
	{
		try
		{
			new MapDataDao(privilegedConnection).closeChangeset(Long.MAX_VALUE-1);
			fail();
		}
		catch(OsmNotFoundException ignore) {}
	}
	
	public void testCloseClosedChangesetFails()
	{
		MapDataDao dao = new MapDataDao(privilegedConnection);
		Map<String,String> tags = new HashMap<>();
		tags.put("comment", "test case");
		long changesetId = dao.openChangeset(tags);
		dao.closeChangeset(changesetId);
		
		try
		{
			dao.closeChangeset(changesetId);
			fail();
		}
		catch(OsmConflictException ignore) { }
	}
	
	public void testMultipleChangesInChangeset()
	{
		MapDataDao mapDataDao = new MapDataDao(privilegedConnection);
		ChangesetsDao changesetDao = new ChangesetsDao(privilegedConnection);

		Map<String,String> tags = new HashMap<>();
		tags.put("comment", "test case");

		long changesetId = mapDataDao.openChangeset(tags);
		try
		{
			assertEquals(true, changesetDao.get(changesetId).isOpen);
			assertChangesetHasElementCount(changesetId, 0,0,0);

			try
			{
				changesetDao.comment(changesetId, "Trying to comment on a non-closed changeset");
				fail();
			}
			catch(OsmConflictException ignore) {}

			// upload first change
			Element node1 = new OsmNode(-33, 1, new OsmLatLon(10.42313, 65.13221), null);
			mapDataDao.uploadChanges(changesetId, Collections.singletonList(node1), null);

			assertChangesetHasElementCount(changesetId,1,0,0);

			// delete a non-existing element: -> not found
			try
			{
				OsmNode delNode = new OsmNode(Long.MAX_VALUE-1, 1, new OsmLatLon(0.11111, 1.565467), null);
				delNode.setDeleted(true);
				Element delElement = delNode;
				mapDataDao.uploadChanges(changesetId, Collections.singletonList(delElement), null);
				fail();
			}
			catch(OsmNotFoundException ignore) {}

			assertChangesetHasElementCount(changesetId,1,0,0);

			Element node2 = new OsmNode(-34, 1, new OsmLatLon(10.42314, 65.13220), null);
			mapDataDao.uploadChanges(changesetId, Collections.singletonList(node2), null);
			assertChangesetHasElementCount(changesetId,2,0,0);
		}
		finally
		{
			mapDataDao.closeChangeset(changesetId);
			assertEquals(false, changesetDao.get(changesetId).isOpen);
		}
	}
	
	public void testUpdateClosedChangesetFails()
	{
		MapDataDao dao = new MapDataDao(privilegedConnection);
		Map<String,String> tags = new HashMap<>();
		tags.put("comment", "test case");
		long changesetId = dao.openChangeset(tags);
		dao.closeChangeset(changesetId);
		
		try
		{
			dao.updateChangeset(changesetId, tags);
			fail();
		}
		catch(OsmConflictException ignore)
		{
		}
	}
	
	public void testUpdateUnopenedChangesetFails()
	{
		Map<String,String> tags = new HashMap<>();
		tags.put("comment", "test case");
		try
		{
			new MapDataDao(privilegedConnection).updateChangeset(Long.MAX_VALUE-1, tags);
			fail();
		}
		catch(OsmNotFoundException ignore)
		{
		}
	}

	public void testUpdateChangesetOverwritesOldTags()
	{
		MapDataDao dao = new MapDataDao(privilegedConnection);
		ChangesetsDao changesetDao = new ChangesetsDao(privilegedConnection);

		Map<String,String> tags1 = new HashMap<>();
		tags1.put("comment", "test case");
		long changesetId = dao.openChangeset(tags1);

		assertEquals(tags1, changesetDao.get(changesetId).tags);

		Map<String,String> tags2 = new HashMap<>();
		tags2.put("comment2", "test case2");

		dao.updateChangeset(changesetId, tags2);

		assertEquals(tags2, changesetDao.get(changesetId).tags);

		// just cleaning up
		dao.closeChangeset(changesetId);
	}

	private void assertChangesetHasElementCount(long changesetId, int creations, int modifications, int deletions)
	{
		ChangesetsDao changesetDao = new ChangesetsDao(unprivilegedConnection);
		SimpleMapDataChangesHandler h = new SimpleMapDataChangesHandler();
		changesetDao.getData(changesetId, h);
		assertEquals(creations, h.getCreations().size());
		assertEquals(modifications, h.getModifications().size());
		assertEquals(deletions, h.getDeletions().size());
	}

	private static class CountMapDataHandler implements MapDataHandler
	{
		int bounds;
		int nodes;
		int ways;
		int relations;

		@Override
		public void handle(BoundingBox bbox)
		{
			bounds++;
		}

		@Override
		public void handle(Node node)
		{
			nodes++;
		}

		@Override
		public void handle(Way way)
		{
			ways++;
		}

		@Override
		public void handle(Relation relation)
		{
			relations++;
		}
	}

	private static class CheckFirstHandleCallHandler extends OneElementTypeHandler<Element>
	{
		long firstCallTime = -1;

		CheckFirstHandleCallHandler()
		{
			super(Element.class);
		}

		@Override
		public void handleElement(Element element)
		{
			if(firstCallTime == -1)
				firstCallTime = System.currentTimeMillis();
		}

	}
}
