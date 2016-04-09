package de.westnordost.osmapi.map;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.westnordost.osmapi.ConnectionTestFactory;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.errors.OsmAuthenticationException;
import de.westnordost.osmapi.errors.OsmNotFoundException;
import de.westnordost.osmapi.errors.OsmQueryTooBigException;
import de.westnordost.osmapi.map.data.Bounds;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
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

	private long changesetId;

	@Override
	protected void setUp() {
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

	public void testGetMapTooBigBounds() {
		try {
			// this must surely be too big, regardless of the server preferences
			// - it's the whole world!
			new MapDataDao(connection).getMap(new Bounds(LatLon.MIN_VALUE,
					LatLon.MAX_VALUE), new DefaultMapDataHandler());
			fail();
		} catch (OsmQueryTooBigException e) {
		}
	}

	public void testGetMapBoundsCross180thMeridian() {
		try {
			// using here the smallest possible query area, so it cannot be too
			// big
			new MapDataDao(connection).getMap(
					new Bounds(OsmLatLon.parseLatLon("0", "180"), OsmLatLon
							.parseLatLon("0.0000001", "-179.9999999")),
					new DefaultMapDataHandler());
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	public void testGetMapBounds() {
		final Bounds verySmallBounds = new Bounds(OsmLatLon.parseLatLon("31",
				"31"), OsmLatLon.parseLatLon("31.0000001", "31.0000001"));

		new MapDataDao(connection).getMap(verySmallBounds,
				new DefaultMapDataHandler() {
					@Override
					public void handle(Bounds bounds) {
						assertEquals(verySmallBounds, bounds);
					}
				});
	}

	public void testSomeElements() {
		// there is not so much we can test regarding the validity of the
		// returned data, but
		// querying something in the middle of Hamburg should at least make
		// handle(X) be called for
		// every type of element once
		final Bounds hamburg = new Bounds(OsmLatLon.parseLatLon("53.579",
				"9.939"), OsmLatLon.parseLatLon("53.580", "9.940"));

		CountMapDataHandler counter = new CountMapDataHandler();
		new MapDataDao(liveConnection).getMap(hamburg, counter);

		assertEquals(1, counter.bounds);
		assertTrue(counter.ways > 0);
		assertTrue(counter.nodes > 0);
		assertTrue(counter.relations > 0);
	}

	public void testDownloadMapIsReallyStreamed() {
		// should be >1MB of data
		final Bounds bigHamburg = new Bounds(OsmLatLon.parseLatLon("53.585",
				"9.945"), OsmLatLon.parseLatLon("53.59", "9.95"));

		CheckFirstHandleCallHandler handler = new CheckFirstHandleCallHandler();

		long startTime = System.currentTimeMillis();
		new MapDataDao(liveConnection).getMap(bigHamburg, handler);
		long timeToFirstData = handler.firstCallTime - startTime;
		long totalTime = System.currentTimeMillis() - startTime;

		// a bit of a cheap test, but I had no better idea how to test this:

		assertTrue(totalTime / 2 > timeToFirstData);
		/*
		 * = the first data can be processed at least after 50% of the total
		 * time of transmission. If the processing of the data would only be
		 * started after the whole data had been transferred, the
		 * 'timeToFirstData' would be much closer to 'totalTime' (than
		 * 'startTime')
		 */
	}

	public void testUploadAsAnonymousFails() {
		try {
			new MapDataDao(connection).updateMap("test", "test",
					Collections.EMPTY_LIST, null);
			fail();
		} catch (OsmAuthenticationException e) {
		}
	}

	public void testUploadAsUnprivilegedUserFails() {
		try {
			new MapDataDao(unprivilegedConnection).updateMap("test", "test",
					Collections.EMPTY_LIST, null);
			fail();
		} catch (OsmAuthenticationException e) {
		}
	}

	public void testNotFound() {
		MapDataDao dao = new MapDataDao(connection);
		MapDataHandler h = new DefaultMapDataHandler();

		try {
			dao.getWayComplete(Long.MAX_VALUE, h);
			fail();
		} catch (OsmNotFoundException e) {
		}

		try {
			dao.getRelationComplete(Long.MAX_VALUE, h);
			fail();
		} catch (OsmNotFoundException e) {
		}
	}

	// we do not test the validity of the data here, just they should not throw
	// exceptions

	public void testWayComplete() {
		new MapDataDao(liveConnection).getWayComplete(27308882,
				new DefaultMapDataHandler());
	}

	public void testRelationComplete() {
		new MapDataDao(liveConnection).getRelationComplete(3301989,
				new DefaultMapDataHandler());
	}

	public void testRelationsForRelation() {
		new MapDataDao(liveConnection).getRelationsForRelation(3218689);
	}

	public void testRelationsForWay() {
		new MapDataDao(liveConnection).getRelationsForWay(244179625);
	}

	public void testRelationsForNode() {
		new MapDataDao(liveConnection).getRelationsForNode(3375377736L);
	}

	public void testWaysForNode() {
		new MapDataDao(liveConnection).getWaysForNode(3668931466L);
	}

	public void testEmptySomeElementsForX() {
		MapDataDao dao = new MapDataDao(connection);

		assertEquals(0, dao.getRelationsForRelation(Long.MAX_VALUE).size());
		assertEquals(0, dao.getRelationsForNode(Long.MAX_VALUE).size());
		assertEquals(0, dao.getRelationsForWay(Long.MAX_VALUE).size());
		assertEquals(0, dao.getWaysForNode(Long.MAX_VALUE).size());
	}

	public void testGetNode() {
		new MapDataDao(liveConnection).getNode(ElementShouldExist.NODE);
		assertNull(new MapDataDao(connection).getNode(Long.MAX_VALUE));
	}

	public void testGetWay() {
		new MapDataDao(liveConnection).getWay(ElementShouldExist.WAY);
		assertNull(new MapDataDao(connection).getWay(Long.MAX_VALUE));
	}

	public void testGetRelation() {
		new MapDataDao(liveConnection).getRelation(ElementShouldExist.RELATION);
		assertNull(new MapDataDao(connection).getRelation(Long.MAX_VALUE));
	}

	public void testGetNodes() {
		// test if a non-existing element does "poison the well"
		// this test will fail if both Yangon and New York place=city nodes do
		// not exist anymore ;-)
		try {
			List<Long> places = Arrays.asList(ElementShouldExist.NODE,
					Long.MAX_VALUE);
			new MapDataDao(liveConnection).getNodes(places);
			fail();
		} catch (OsmNotFoundException e) {
		}

		List<Long> place = Arrays.asList(ElementShouldExist.NODE);
		assertFalse(new MapDataDao(liveConnection).getNodes(place).isEmpty());
	}

	public void testGetRelations() {
		// test if a non-existing element does "poison the well"
		// this test will fail if Germany type=boundary does not exist anymore
		try {
			List<Long> places = Arrays.asList(ElementShouldExist.RELATION,
					Long.MAX_VALUE);
			new MapDataDao(liveConnection).getRelations(places);
			fail();
		} catch (OsmNotFoundException e) {
		}

		List<Long> place = Arrays.asList(ElementShouldExist.RELATION);
		assertFalse(new MapDataDao(liveConnection).getRelations(place)
				.isEmpty());
	}

	public void testGetWays() {
		// test if a non-existing element does "poison the well"
		// this test will fail if some harbor in Hamburg does not exist
		// anymore...
		try {
			List<Long> places = Arrays.asList(ElementShouldExist.WAY,
					Long.MAX_VALUE);
			List<Way> ways = new MapDataDao(liveConnection).getWays(places);
			ways.isEmpty();
			fail();
		} catch (OsmNotFoundException e) {
		}

		List<Long> place = Arrays.asList(ElementShouldExist.WAY);
		assertFalse(new MapDataDao(liveConnection).getWays(place).isEmpty());
	}

	private class CountMapDataHandler implements MapDataHandler {
		public int bounds;
		public int nodes;
		public int ways;
		public int relations;

		@Override
		public void handle(Bounds bbox) {
			bounds++;
		}

		@Override
		public void handle(Node node) {
			nodes++;
		}

		@Override
		public void handle(Way way) {
			ways++;
		}

		@Override
		public void handle(Relation relation) {
			relations++;
		}
	}

	private class CheckFirstHandleCallHandler extends
			OneElementTypeHandler<Element> {
		public long firstCallTime = -1;

		public CheckFirstHandleCallHandler() {
			super(Element.class);
		}

		@Override
		public void handleElement(Element element) {
			if (firstCallTime == -1)
				firstCallTime = System.currentTimeMillis();
		}

	}
}
