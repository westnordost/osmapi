package de.westnordost.osmapi.changesets;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.westnordost.osmapi.ConnectionTestFactory;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.errors.OsmAuthorizationException;
import de.westnordost.osmapi.common.errors.OsmNotFoundException;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.map.data.OsmNode;
import de.westnordost.osmapi.map.changes.SimpleMapDataChangesHandler;


public class ChangesetsDaoTest extends TestCase
{
	private ChangesetsDao privilegedDao;
	private ChangesetsDao anonymousDao;
	private ChangesetsDao unprivilegedDao;

	private MapDataDao mapDataDao;

	private long changesetId = 1;

	/* the time is chosen relatively arbitrary. Basically, if it is off by more than half an hour,
	   there might be a problem with parsing the correct timezone. But ten minutes is suspicious
	   enough to let the test fail, imo */
	private static final int TEN_MINUTES = 1000 * 60 * 10;

	private static final int A_USER_WITH_CHANGESETS = 3630;
	private static final int A_USER_WITHOUT_CHANGESETS = 3632;
	
	@Override
	protected void setUp()
	{
		OsmConnection userConnection = ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_EVERYTHING);

		mapDataDao = new MapDataDao(userConnection);

		anonymousDao = new ChangesetsDao(ConnectionTestFactory.createConnection(null));
		privilegedDao = new ChangesetsDao(userConnection);
		unprivilegedDao = new ChangesetsDao(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_NOTHING));
	}

	public void testSubscribe()
	{
		ChangesetInfo info = privilegedDao.subscribe(changesetId);
		privilegedDao.unsubscribe(changesetId);
		assertEquals(changesetId, info.id);
	}

	public void testRead()
	{
		List<Element> elements = new ArrayList<>();
		long myChangesetId = mapDataDao.updateMap("unit test", "", elements, null);

		ChangesetInfo infos = unprivilegedDao.get(myChangesetId);
		assertEquals(myChangesetId, infos.id);
		assertEquals(ConnectionTestFactory.USER_AGENT, infos.getGenerator());
		assertEquals("unit test",infos.getChangesetComment());
		assertNull(infos.boundingBox);
		assertFalse(infos.isOpen);
		assertEquals(infos.date, infos.dateCreated);
		assertNotNull(infos.dateClosed);
		assertTrue(Math.abs(new Date().getTime() - infos.dateClosed.getTime()) < TEN_MINUTES);
		assertTrue(Math.abs(new Date().getTime() - infos.dateCreated.getTime()) < TEN_MINUTES);
	}

	public void testReadInvalidReturnsNull()
	{
		assertNull(unprivilegedDao.get(0));
	}

	public void testComment()
	{
		List<Element> elements = new ArrayList<>();
		long myChangesetId = mapDataDao.updateMap("unit test", "", elements, null);

		ChangesetInfo infos = privilegedDao.comment(myChangesetId, "test comment");
		assertEquals(1, infos.notesCount);

		List<ChangesetNote> comments = privilegedDao.get(myChangesetId).discussion;
		assertNotNull(comments);
		assertTrue(Math.abs(new Date().getTime() - comments.get(0).date.getTime()) < TEN_MINUTES);
		assertEquals("test comment", comments.get(0).text);
	}

	public void testEmptyComment()
	{
		try
		{
			privilegedDao.comment(changesetId, "");
			fail();
		}
		catch(IllegalArgumentException e) {}
	}

	public void testAuthFail()
	{
		try
		{
			unprivilegedDao.subscribe(changesetId);
			fail();
		}
		catch(OsmAuthorizationException e) {}

		try
		{
			unprivilegedDao.unsubscribe(changesetId);
			fail();
		}
		catch(OsmAuthorizationException e) {}

		try
		{
			unprivilegedDao.comment(changesetId, "test comment");
			fail();
		}
		catch(OsmAuthorizationException e) {}
	}

	public void testAlreadySubscribedDoesNotFail()
	{
		// ...which is different from the "raw" API response

		assertNotNull(privilegedDao.subscribe(changesetId));
		assertNotNull(privilegedDao.subscribe(changesetId));
	}

	public void testNotSubscribedDoesNotFail()
	{
		// ...which is different from the "raw" API response
		assertNotNull(privilegedDao.unsubscribe(changesetId));
	}

	public void testSubscribeNonExistingChangesetFails()
	{
		try { privilegedDao.subscribe(0); fail(); } catch(OsmNotFoundException e) {}
		try { privilegedDao.unsubscribe(0); fail(); } catch(OsmNotFoundException e) {}
	}

	public void testAnonymousFail()
	{
		try
		{
			anonymousDao.subscribe(changesetId);
			fail();
		}
		catch(OsmAuthorizationException e) {}

		try
		{
			anonymousDao.unsubscribe(changesetId);
			fail();
		}
		catch(OsmAuthorizationException e) {}

		try
		{
			anonymousDao.comment(changesetId, "test comment");
			fail();
		}
		catch(OsmAuthorizationException e) {}

		try
		{
			anonymousDao.get(changesetId);
			fail();
		}
		catch(OsmAuthorizationException e) {}

		try
		{
			anonymousDao.find(new Handler<ChangesetInfo>()
			{
				@Override
				public void handle(ChangesetInfo tea)
				{
					// nothing...
				}
			}, new QueryChangesetsFilters().byUser(A_USER_WITH_CHANGESETS));
			fail();
		}
		catch(OsmAuthorizationException e) {}
	}

	public void testGetChangesets()
	{
		unprivilegedDao.find(new Handler<ChangesetInfo>()
		{
			@Override
			public void handle(ChangesetInfo tea)
			{
				// nothing...
			}
		}, new QueryChangesetsFilters().byUser(A_USER_WITH_CHANGESETS));

		// whether the result is consistent with the query parameters is not tested here
		// this just should not throw an exception
	}

	public void testGetChangesetsEmptyDoesNotFail()
	{
		unprivilegedDao.find(new Handler<ChangesetInfo>()
		{
			@Override
			public void handle(ChangesetInfo tea)
			{
				// nothing...
			}
		}, new QueryChangesetsFilters().byUser(A_USER_WITHOUT_CHANGESETS));
	}
	
	public void testGetChanges()
	{
		Node node = new OsmNode(-1, 1, new OsmLatLon(55.12313,50.13221), null);
		List<Element> elements = new ArrayList<>();
		elements.add(node);
		long changesetId = mapDataDao.updateMap("test", "test", elements, null);

		SimpleMapDataChangesHandler handler = new SimpleMapDataChangesHandler();
		unprivilegedDao.getData(changesetId, handler);

		List<Element> createdElements = handler.getCreations();

		assertEquals(1, createdElements.size());

		OsmNode createdNode = (OsmNode) createdElements.get(0);

		// delete again... (clean up before asserting)
		createdNode.setDeleted(true);
		mapDataDao.updateMap("clean up test", "test", createdElements, null);

		assertNotSame(node.getId(), createdNode.getId());
		assertEquals(node.getVersion(), createdNode.getVersion());
		assertEquals(node.getPosition(), createdNode.getPosition());
		assertEquals(changesetId, createdNode.getChangeset().id);
	}
}
