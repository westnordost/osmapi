package de.westnordost.osmapi.map;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.ConnectionTestFactory;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.changesets.ChangesetsApi;
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

import static org.junit.Assert.*;

public class MapDataApiTest
{
	private OsmConnection privilegedConnection;
	private OsmConnection unprivilegedConnection;
	private OsmConnection connection;
	private OsmConnection liveConnection;

	@Before public void setUp()
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

	@Test public void getMapTooBigBounds()
	{
		try
		{
			// this must surely be too big, regardless of the server preferences
			// - it's the whole world!
			new MapDataApi(connection).getMap(
					new BoundingBox(LatLons.MIN_VALUE, LatLons.MAX_VALUE),
					new DefaultMapDataHandler());
			fail();
		}
		catch(OsmQueryTooBigException ignore) {}
	}

	@Test public void getMapBoundsCross180thMeridian()
	{
		try
		{
			// using here the smallest possible query area, so it cannot be too
			// big
			new MapDataApi(connection).getMap(new BoundingBox(0, 180, 0.0000001, -179.9999999),
					new DefaultMapDataHandler());
			fail();
		}
		catch(IllegalArgumentException ignore) {}
	}

	@Test public void getMapBounds()
	{
		final BoundingBox verySmallBounds = new BoundingBox(31, 31, 31.0000001, 31.0000001);

		new MapDataApi(connection).getMap(verySmallBounds, new DefaultMapDataHandler()
		{
			@Override
			public void handle(BoundingBox bounds)
			{
				assertEquals(verySmallBounds, bounds);
			}
		});
	}

	@Test public void someElements()
	{
		// there is not so much we can test regarding the validity of the
		// returned data, but
		// querying something in the middle of Hamburg should at least make
		// handle(X) be called for
		// every type of element once
		final BoundingBox hamburg = new BoundingBox(53.579, 9.939, 53.580, 9.940);

		CountMapDataHandler counter = new CountMapDataHandler();
		new MapDataApi(liveConnection).getMap(hamburg, counter);

		assertEquals(1, counter.bounds);
		assertTrue(counter.ways > 0);
		assertTrue(counter.nodes > 0);
		assertTrue(counter.relations > 0);
	}

	@Test public void downloadMapIsReallyStreamed()
	{
		// should be >1MB of data
		final BoundingBox bigHamburg = new BoundingBox(53.585, 9.945, 53.59, 9.95);

		CheckFirstHandleCallHandler handler = new CheckFirstHandleCallHandler();

		long startTime = System.currentTimeMillis();
		new MapDataApi(liveConnection).getMap(bigHamburg, handler);
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

	@Test public void uploadAsAnonymousFails()
	{
		try
		{
			new MapDataApi(connection).updateMap("test", "test", Collections.<Element> emptyList(),
					null);
			fail();
		}
		catch(OsmAuthorizationException ignore) {}
	}

	@Test public void uploadAsUnprivilegedUserFails()
	{
		try
		{
			new MapDataApi(unprivilegedConnection).updateMap("test", "test",
					Collections.<Element> emptyList(), null);
			fail();
		}
		catch(OsmAuthorizationException ignore) {}
	}

	@Test public void readDiff()
	{
		MapDataApi mapDataApi = new MapDataApi(privilegedConnection);

		final LatLon POS = new OsmLatLon(55.42313, 50.13221);
		final long PLACEHOLDER_ID = -33;

		Element node = new OsmNode(PLACEHOLDER_ID, 1, POS, null);
		SingleElementHandler<DiffElement> handlerCreated = new SingleElementHandler<>();
		mapDataApi.updateMap("test", "test", Arrays.asList(node), handlerCreated);
		DiffElement diffCreated = handlerCreated.get();

		// update id and delete again... (clean up before asserting)
		OsmNode deleteNode = new OsmNode(diffCreated.serverId, diffCreated.serverVersion, POS,
				null);
		deleteNode.setDeleted(true);
		SingleElementHandler<DiffElement> handlerDeleted = new SingleElementHandler<>();
		mapDataApi.updateMap("clean up test", "test", Arrays.asList((Element) deleteNode),
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

	@Test public void notFound()
	{
		MapDataApi api = new MapDataApi(connection);
		MapDataHandler h = new DefaultMapDataHandler();

		try
		{
			api.getWayComplete(Long.MAX_VALUE, h);
			fail();
		}
		catch(OsmNotFoundException ignore) {}

		try
		{
			api.getRelationComplete(Long.MAX_VALUE, h);
			fail();
		}
		catch(OsmNotFoundException ignore) {}
	}

	// we do not test the validity of the data here, just they should not throw
	// exceptions

	@Test public void wayComplete()
	{
		new MapDataApi(liveConnection).getWayComplete(27308882, new DefaultMapDataHandler());
	}

	@Test public void relationComplete()
	{
		new MapDataApi(liveConnection).getRelationComplete(3301989, new DefaultMapDataHandler());
	}

	@Test public void relationsForRelation()
	{
		new MapDataApi(liveConnection).getRelationsForRelation(3218689);
	}

	@Test public void relationsForWay()
	{
		new MapDataApi(liveConnection).getRelationsForWay(244179625);
	}

	@Test public void relationsForNode()
	{
		new MapDataApi(liveConnection).getRelationsForNode(3375377736L);
	}

	@Test public void waysForNode()
	{
		new MapDataApi(liveConnection).getWaysForNode(3668931466L);
	}

	@Test public void emptySomeElementsForX()
	{
		MapDataApi api = new MapDataApi(connection);

		assertEquals(0, api.getRelationsForRelation(Long.MAX_VALUE).size());
		assertEquals(0, api.getRelationsForNode(Long.MAX_VALUE).size());
		assertEquals(0, api.getRelationsForWay(Long.MAX_VALUE).size());
		assertEquals(0, api.getWaysForNode(Long.MAX_VALUE).size());
	}

	@Test public void getNode()
	{
		new MapDataApi(liveConnection).getNode(ElementShouldExist.NODE);
		assertNull(new MapDataApi(connection).getNode(Long.MAX_VALUE));
	}

	@Test public void getWay()
	{
		new MapDataApi(liveConnection).getWay(ElementShouldExist.WAY);
		assertNull(new MapDataApi(connection).getWay(Long.MAX_VALUE));
	}

	@Test public void getRelation()
	{
		new MapDataApi(liveConnection).getRelation(ElementShouldExist.RELATION);
		assertNull(new MapDataApi(connection).getRelation(Long.MAX_VALUE));
	}

	@Test public void getNodes()
	{
		// test if a non-existing element does "poison the well"
		// this test will fail if both Yangon and New York place=city nodes do
		// not exist anymore ;-)
		try
		{
			List<Long> places = Arrays.asList(ElementShouldExist.NODE, Long.MAX_VALUE);
			new MapDataApi(liveConnection).getNodes(places);
			fail();
		}
		catch(OsmNotFoundException ignore) {}

		List<Long> place = Arrays.asList(ElementShouldExist.NODE);
		assertFalse(new MapDataApi(liveConnection).getNodes(place).isEmpty());
	}

	@Test public void getRelations()
	{
		// test if a non-existing element does "poison the well"
		// this test will fail if Germany type=boundary does not exist anymore
		try
		{
			List<Long> places = Arrays.asList(ElementShouldExist.RELATION, Long.MAX_VALUE);
			new MapDataApi(liveConnection).getRelations(places);
			fail();
		}
		catch(OsmNotFoundException ignore) {}

		List<Long> place = Arrays.asList(ElementShouldExist.RELATION);
		assertFalse(new MapDataApi(liveConnection).getRelations(place).isEmpty());
	}

	@Test public void getWays()
	{
		// test if a non-existing element does "poison the well"
		// this test will fail if some harbor in Hamburg does not exist
		// anymore...
		try
		{
			List<Long> places = Arrays.asList(ElementShouldExist.WAY, Long.MAX_VALUE);
			List<Way> ways = new MapDataApi(liveConnection).getWays(places);
			ways.isEmpty();
			fail();
		}
		catch(OsmNotFoundException ignore) {}

		List<Long> place = Arrays.asList(ElementShouldExist.WAY);
		assertFalse(new MapDataApi(liveConnection).getWays(place).isEmpty());
	}

	@Test public void getElementsEmpty()
	{
		MapDataApi api = new MapDataApi(connection);
		assertTrue(api.getWays(Collections.<Long> emptyList()).isEmpty());
		assertTrue(api.getNodes(Collections.<Long> emptyList()).isEmpty());
		assertTrue(api.getRelations(Collections.<Long> emptyList()).isEmpty());
	}

	@Test public void closeUnopenedChangesetFails()
	{
		try
		{
			new MapDataApi(privilegedConnection).closeChangeset(Long.MAX_VALUE-1);
			fail();
		}
		catch(OsmNotFoundException ignore) {}
	}
	
	@Test public void closeClosedChangesetFails()
	{
		MapDataApi api = new MapDataApi(privilegedConnection);
		Map<String,String> tags = new HashMap<>();
		tags.put("comment", "test case");
		long changesetId = api.openChangeset(tags);
		api.closeChangeset(changesetId);
		
		try
		{
			api.closeChangeset(changesetId);
			fail();
		}
		catch(OsmConflictException ignore) { }
	}
	
	@Test public void multipleChangesInChangeset()
	{
		MapDataApi mapDataApi = new MapDataApi(privilegedConnection);
		ChangesetsApi changesetApi = new ChangesetsApi(privilegedConnection);

		Map<String,String> tags = new HashMap<>();
		tags.put("comment", "test case");

		long changesetId = mapDataApi.openChangeset(tags);
		try
		{
			assertEquals(true, changesetApi.get(changesetId).isOpen);
			assertChangesetHasElementCount(changesetId, 0,0,0);

			try
			{
				changesetApi.comment(changesetId, "Trying to comment on a non-closed changeset");
				fail();
			}
			catch(OsmConflictException ignore) {}

			// upload first change
			Element node1 = new OsmNode(-33, 1, new OsmLatLon(10.42313, 65.13221), null);
			mapDataApi.uploadChanges(changesetId, Arrays.asList(node1), null);

			assertChangesetHasElementCount(changesetId,1,0,0);

			// delete a non-existing element: -> not found
			try
			{
				OsmNode delNode = new OsmNode(Long.MAX_VALUE-1, 1, new OsmLatLon(0.11111, 1.565467), null);
				delNode.setDeleted(true);
				Element delElement = delNode;
				mapDataApi.uploadChanges(changesetId, Arrays.asList(delElement), null);
				fail();
			}
			catch(OsmNotFoundException ignore) {}

			assertChangesetHasElementCount(changesetId,1,0,0);

			Element node2 = new OsmNode(-34, 1, new OsmLatLon(10.42314, 65.13220), null);
			mapDataApi.uploadChanges(changesetId, Arrays.asList(node2), null);
			assertChangesetHasElementCount(changesetId,2,0,0);
		}
		finally
		{
			mapDataApi.closeChangeset(changesetId);
			assertEquals(false, changesetApi.get(changesetId).isOpen);
		}
	}
	
	@Test public void updateClosedChangesetFails()
	{
		MapDataApi api = new MapDataApi(privilegedConnection);
		Map<String,String> tags = new HashMap<>();
		tags.put("comment", "test case");
		long changesetId = api.openChangeset(tags);
		api.closeChangeset(changesetId);
		
		try
		{
			api.updateChangeset(changesetId, tags);
			fail();
		}
		catch(OsmConflictException ignore)
		{
		}
	}
	
	@Test public void updateUnopenedChangesetFails()
	{
		Map<String,String> tags = new HashMap<>();
		tags.put("comment", "test case");
		try
		{
			new MapDataApi(privilegedConnection).updateChangeset(Long.MAX_VALUE-1, tags);
			fail();
		}
		catch(OsmNotFoundException ignore)
		{
		}
	}

	@Test public void updateChangesetOverwritesOldTags()
	{
		MapDataApi api = new MapDataApi(privilegedConnection);
		ChangesetsApi changesetApi = new ChangesetsApi(privilegedConnection);

		Map<String,String> tags1 = new HashMap<>();
		tags1.put("comment", "test case");
		long changesetId = api.openChangeset(tags1);

		assertEquals(tags1, changesetApi.get(changesetId).tags);

		Map<String,String> tags2 = new HashMap<>();
		tags2.put("comment2", "test case2");

		api.updateChangeset(changesetId, tags2);

		assertEquals(tags2, changesetApi.get(changesetId).tags);

		// just cleaning up
		api.closeChangeset(changesetId);
	}

	private void assertChangesetHasElementCount(long changesetId, int creations, int modifications, int deletions)
	{
		ChangesetsApi changesetApi = new ChangesetsApi(unprivilegedConnection);
		SimpleMapDataChangesHandler h = new SimpleMapDataChangesHandler();
		changesetApi.getData(changesetId, h);
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
