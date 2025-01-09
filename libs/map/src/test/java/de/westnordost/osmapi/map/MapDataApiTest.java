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
	private MapDataApi privilegedApi;
	private MapDataApi anonymousApi;
	private MapDataApi unprivilegedApi;
	private ChangesetsApi privilegedChangesetApi;

	private MapDataApi liveApi;

	@Before public void setUp()
	{
		OsmConnection privilegedConnection =
				ConnectionTestFactory.createConnection(ConnectionTestFactory.User.ALLOW_EVERYTHING);
		// Create the different connections...
		anonymousApi = new MapDataApi(ConnectionTestFactory.createConnection(null));
		privilegedApi = new MapDataApi(privilegedConnection);
		unprivilegedApi = new MapDataApi(
				ConnectionTestFactory.createConnection(ConnectionTestFactory.User.ALLOW_NOTHING)
		);
		privilegedChangesetApi = new ChangesetsApi(privilegedConnection);

		// we need to test a few things with live data, because we have no clue
		// how the test data looks like :-(
		// i.e. where to expect data
		liveApi = new MapDataApi(ConnectionTestFactory.createLiveConnection());
	}

	@Test public void getMapTooBigBounds()
	{
		assertThrows(
				OsmQueryTooBigException.class,
				() -> {
					// this must surely be too big, regardless of the server preferences
					// - it's the whole world!
					anonymousApi.getMap(
							new BoundingBox(LatLons.MIN_VALUE, LatLons.MAX_VALUE),
							new DefaultMapDataHandler());
				}
		);
	}

	@Test public void getMapBoundsCross180thMeridian()
	{
		assertThrows(
				IllegalArgumentException.class,
				() -> {
					// using here the smallest possible query area, so it cannot be too
					// big
					anonymousApi.getMap(new BoundingBox(0, 180, 0.0000001, -179.9999999),
							new DefaultMapDataHandler());
				}
		);
	}

	@Test public void getMapBounds()
	{
		final BoundingBox verySmallBounds = new BoundingBox(31, 31, 31.0000001, 31.0000001);

		anonymousApi.getMap(verySmallBounds, new DefaultMapDataHandler()
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
		liveApi.getMap(hamburg, counter);

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
		liveApi.getMap(bigHamburg, handler);
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
		assertThrows(
				OsmAuthorizationException.class,
				() -> anonymousApi.updateMap("test", "test", Collections.emptyList(), null)
		);
	}

	@Test public void uploadAsUnprivilegedUserFails()
	{
		assertThrows(
				OsmAuthorizationException.class,
				() -> unprivilegedApi.updateMap("test", "test", Collections.emptyList(), null)
		);
	}

	@Test public void readDiff()
	{
		final LatLon POS = new OsmLatLon(55.42313, 50.13221);
		final long PLACEHOLDER_ID = -33;

		Element node = new OsmNode(PLACEHOLDER_ID, 1, POS, null);
		SingleElementHandler<DiffElement> handlerCreated = new SingleElementHandler<>();
		privilegedApi.updateMap("test", "test", Arrays.asList(node), handlerCreated);
		DiffElement diffCreated = handlerCreated.get();

		// update id and delete again... (clean up before asserting)
		OsmNode deleteNode = new OsmNode(diffCreated.serverId, diffCreated.serverVersion, POS,
				null);
		deleteNode.setDeleted(true);
		SingleElementHandler<DiffElement> handlerDeleted = new SingleElementHandler<>();
		privilegedApi.updateMap("clean up test", "test", Arrays.asList(deleteNode), handlerDeleted);

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
		assertThrows(OsmNotFoundException.class, () -> anonymousApi.getWayComplete(Long.MAX_VALUE, new DefaultMapDataHandler()));
		assertThrows(OsmNotFoundException.class, () -> anonymousApi.getRelationComplete(Long.MAX_VALUE, new DefaultMapDataHandler()));
	}

	// we do not test the validity of the data here, just they should not throw
	// exceptions

	@Test public void wayComplete()
	{
		liveApi.getWayComplete(27308882, new DefaultMapDataHandler());
	}

	@Test public void relationComplete()
	{
		liveApi.getRelationComplete(3301989, new DefaultMapDataHandler());
	}

	@Test public void relationsForRelation()
	{
		liveApi.getRelationsForRelation(3218689);
	}

	@Test public void relationsForWay()
	{
		liveApi.getRelationsForWay(244179625);
	}

	@Test public void relationsForNode()
	{
		liveApi.getRelationsForNode(3375377736L);
	}

	@Test public void waysForNode()
	{
		liveApi.getWaysForNode(3668931466L);
	}

	@Test public void emptySomeElementsForX()
	{
		assertTrue(anonymousApi.getRelationsForRelation(Long.MAX_VALUE).isEmpty());
		assertTrue(anonymousApi.getRelationsForNode(Long.MAX_VALUE).isEmpty());
		assertTrue(anonymousApi.getRelationsForWay(Long.MAX_VALUE).isEmpty());
		assertTrue(anonymousApi.getWaysForNode(Long.MAX_VALUE).isEmpty());
	}

	@Test public void getNode()
	{
		assertNotNull(liveApi.getNode(ElementShouldExist.NODE));
		assertNull(anonymousApi.getNode(Long.MAX_VALUE));
	}

	@Test public void getWay()
	{
		assertNotNull(liveApi.getWay(ElementShouldExist.WAY));
		assertNull(anonymousApi.getWay(Long.MAX_VALUE));
	}

	@Test public void getRelation()
	{
		assertNotNull(liveApi.getRelation(ElementShouldExist.RELATION));
		assertNull(anonymousApi.getRelation(Long.MAX_VALUE));
	}

	@Test public void getNodes()
	{
		// test if a non-existing element does "poison the well"
		// this test will fail if both Yangon and New York place=city nodes do
		// not exist anymore ;-)
		assertThrows(
				OsmNotFoundException.class,
				() -> {
					List<Long> places = Arrays.asList(ElementShouldExist.NODE, Long.MAX_VALUE);
					liveApi.getNodes(places);
				}
		);

		List<Long> place = Arrays.asList(ElementShouldExist.NODE);
		assertFalse(liveApi.getNodes(place).isEmpty());
	}

	@Test public void getRelations()
	{
		// test if a non-existing element does "poison the well"
		// this test will fail if Germany type=boundary does not exist anymore
		assertThrows(
				OsmNotFoundException.class,
				() -> {
					List<Long> places = Arrays.asList(ElementShouldExist.RELATION, Long.MAX_VALUE);
					liveApi.getRelations(places);
				}
		);

		List<Long> place = Arrays.asList(ElementShouldExist.RELATION);
		assertFalse(liveApi.getRelations(place).isEmpty());
	}

	@Test public void getWays()
	{
		// test if a non-existing element does "poison the well"
		// this test will fail if some harbor in Hamburg does not exist
		// anymore...
		assertThrows(
				OsmNotFoundException.class,
				() -> {
					List<Long> places = Arrays.asList(ElementShouldExist.WAY, Long.MAX_VALUE);
					liveApi.getWays(places);
				}
		);

		List<Long> place = Arrays.asList(ElementShouldExist.WAY);
		assertFalse(liveApi.getWays(place).isEmpty());
	}

	@Test public void getElementsEmpty()
	{
		MapDataApi api = anonymousApi;
		assertTrue(api.getWays(Collections.emptyList()).isEmpty());
		assertTrue(api.getNodes(Collections.emptyList()).isEmpty());
		assertTrue(api.getRelations(Collections.emptyList()).isEmpty());
	}

	@Test public void closeUnopenedChangesetFails()
	{
		assertThrows(
				OsmNotFoundException.class,
				() -> privilegedApi.closeChangeset(Long.MAX_VALUE-1)
		);
	}
	
	@Test public void closeClosedChangesetFails()
	{
		Map<String,String> tags = new HashMap<>();
		tags.put("comment", "test case");
		long changesetId = privilegedApi.openChangeset(tags);
		privilegedApi.closeChangeset(changesetId);

		assertThrows(OsmConflictException.class, () -> privilegedApi.closeChangeset(changesetId));
	}
	
	@Test public void multipleChangesInChangeset()
	{
		Map<String,String> tags = new HashMap<>();
		tags.put("comment", "test case");

		long changesetId = privilegedApi.openChangeset(tags);
		try
		{
            assertTrue(privilegedChangesetApi.get(changesetId).isOpen);
			assertChangesetHasElementCount(changesetId, 0,0,0);

			assertThrows(
					OsmConflictException.class,
					() -> privilegedChangesetApi.comment(changesetId, "Trying to comment on a non-closed changeset")
			);

			// upload first change
			Element node1 = new OsmNode(-33, 1, new OsmLatLon(10.42313, 65.13221), null);
			privilegedApi.uploadChanges(changesetId, Arrays.asList(node1), null);

			assertChangesetHasElementCount(changesetId,1,0,0);

			// delete a non-existing element: -> not found
			OsmNode delNode = new OsmNode(Long.MAX_VALUE-1, 1, new OsmLatLon(0.11111, 1.565467), null);
			delNode.setDeleted(true);
			assertThrows(
					OsmNotFoundException.class,
					() -> privilegedApi.uploadChanges(changesetId, Arrays.asList(delNode), null)
			);

			assertChangesetHasElementCount(changesetId,1,0,0);

			Element node2 = new OsmNode(-34, 1, new OsmLatLon(10.42314, 65.13220), null);
			privilegedApi.uploadChanges(changesetId, Arrays.asList(node2), null);
			assertChangesetHasElementCount(changesetId,2,0,0);
		}
		finally
		{
			privilegedApi.closeChangeset(changesetId);
            assertFalse(privilegedChangesetApi.get(changesetId).isOpen);
		}
	}
	
	@Test public void updateClosedChangesetFails()
	{
		Map<String,String> tags = new HashMap<>();
		tags.put("comment", "test case");
		long changesetId = privilegedApi.openChangeset(tags);
		privilegedApi.closeChangeset(changesetId);

		assertThrows(OsmConflictException.class, () -> privilegedApi.updateChangeset(changesetId, tags));
	}
	
	@Test public void updateUnopenedChangesetFails()
	{
		Map<String,String> tags = new HashMap<>();
		tags.put("comment", "test case");
		assertThrows(
				OsmNotFoundException.class,
				() -> privilegedApi.updateChangeset(Long.MAX_VALUE-1, tags)
		);
	}

	@Test public void updateChangesetOverwritesOldTags()
	{
		Map<String,String> tags1 = new HashMap<>();
		tags1.put("comment", "test case");
		long changesetId = privilegedApi.openChangeset(tags1);

		assertEquals(tags1, privilegedChangesetApi.get(changesetId).tags);

		Map<String,String> tags2 = new HashMap<>();
		tags2.put("comment2", "test case2");

		privilegedApi.updateChangeset(changesetId, tags2);

		assertEquals(tags2, privilegedChangesetApi.get(changesetId).tags);

		// just cleaning up
		privilegedApi.closeChangeset(changesetId);
	}

	private void assertChangesetHasElementCount(long changesetId, int creations, int modifications, int deletions)
	{
		SimpleMapDataChangesHandler h = new SimpleMapDataChangesHandler();
		privilegedChangesetApi.getData(changesetId, h);
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
