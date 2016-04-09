package de.westnordost.osmapi.changesets;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.westnordost.osmapi.ConnectionTestFactory;
import de.westnordost.osmapi.Handler;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.errors.OsmAuthenticationException;
import de.westnordost.osmapi.errors.OsmNotFoundException;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.map.data.OsmNode;
import de.westnordost.osmapi.map.changes.SimpleMapDataChangesHandler;


public class ChangesetsDaoTest extends TestCase
{
	private OsmConnection connection;
	private OsmConnection userConnection;
	private OsmConnection badUserConnection;

	private long changesetId = 1;

	/* the time is chosen relatively arbitrary. Basically, if it is off by more than half an hour,
	   there might be a problem with parsing the correct timezone. But ten minutes is suspicious
	   enough to let the test fail, imo */
	private static final int TEN_MINUTES = 1000 * 60 * 10;

	@Override
	protected void setUp() throws Exception
	{
		// Create the different connections...
		connection = ConnectionTestFactory.createConnection(null);
		userConnection = ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_EVERYTHING);
		badUserConnection = ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_NOTHING
		);
	}

	public void testSubscribe()
	{
		ChangesetsDao infoDao = new ChangesetsDao(userConnection);

		ChangesetInfo info = infoDao.subscribeToChangeset(changesetId);
		infoDao.unsubscribeFromChangeset(changesetId);
		assertEquals(changesetId, info.getId());
	}

	public void testRead()
	{
		List<Element> elements = new ArrayList<>();
		long myChangesetId = new MapDataDao(userConnection).updateMap("unit test", "", elements, null);

		ChangesetInfo infos = new ChangesetsDao(connection).getChangeset(myChangesetId);
		assertEquals(myChangesetId, infos.getId());
		assertEquals(ConnectionTestFactory.USER_AGENT, infos.getGenerator());
		assertEquals("unit test",infos.getChangesetComment());
		assertNull(infos.getBounds());
		assertFalse(infos.isOpen());
		assertEquals(infos.getDate(), infos.getDateCreated());
		assertNotNull(infos.getDateClosed());
		assertTrue(Math.abs(new Date().getTime() - infos.getDateClosed().getTime()) < TEN_MINUTES);
		assertTrue(Math.abs(new Date().getTime() - infos.getDateCreated().getTime()) < TEN_MINUTES);
	}

	public void testReadInvalidReturnsNull()
	{
		assertNull(new ChangesetsDao(connection).getChangeset(0));
	}

	public void testComment()
	{
		List<Element> elements = new ArrayList<>();
		long myChangesetId = new MapDataDao(userConnection).updateMap("unit test", "", elements, null);

		ChangesetsDao dao = new ChangesetsDao(userConnection);

		ChangesetInfo infos = dao.commentChangeset(myChangesetId, "test comment");
		assertEquals(1, infos.getCommentsCount());

		List<ChangesetComment> comments = dao.getChangeset(myChangesetId).getDiscussion();
		assertNotNull(comments);
		assertTrue(Math.abs(new Date().getTime() - comments.get(0).getDate().getTime()) < TEN_MINUTES);
		assertEquals("test comment", comments.get(0).getText());
	}

	public void testEmptyComment()
	{
		ChangesetsDao dao = new ChangesetsDao(userConnection);

		try
		{
			dao.commentChangeset(changesetId, "");
			fail();
		}
		catch(IllegalArgumentException e) {}
	}

	public void testAuthFail()
	{
		ChangesetsDao infoDao = new ChangesetsDao(badUserConnection);

		try
		{
			infoDao.subscribeToChangeset(changesetId);
			fail();
		}
		catch(OsmAuthenticationException e) {}

		try
		{
			infoDao.unsubscribeFromChangeset(changesetId);
			fail();
		}
		catch(OsmAuthenticationException e) {}

		try
		{
			infoDao.commentChangeset(changesetId, "test comment");
			fail();
		}
		catch(OsmAuthenticationException e) {}
	}

	public void testAlreadySubscribedDoesNotFail()
	{
		// ...which is different from the "raw" API response

		ChangesetsDao infoDao = new ChangesetsDao(userConnection);
		assertNotNull(infoDao.subscribeToChangeset(changesetId));
		assertNotNull(infoDao.subscribeToChangeset(changesetId));
	}

	public void testNotSubscribedDoesNotFail()
	{
		// ...which is different from the "raw" API response
		ChangesetsDao infoDao = new ChangesetsDao(userConnection);
		assertNotNull(infoDao.unsubscribeFromChangeset(changesetId));
	}

	public void testSubscribeNonExistingChangesetFails()
	{
		ChangesetsDao infoDao = new ChangesetsDao(userConnection);
		try { infoDao.subscribeToChangeset(0); fail(); } catch(OsmNotFoundException e) {}
		try { infoDao.unsubscribeFromChangeset(0); fail(); } catch(OsmNotFoundException e) {}
	}

	public void testAnonymousFail()
	{
		ChangesetsDao infoDao = new ChangesetsDao(connection);

		try
		{
			infoDao.subscribeToChangeset(changesetId);
			fail();
		}
		catch(OsmAuthenticationException e) {}

		try
		{
			infoDao.unsubscribeFromChangeset(changesetId);
			fail();
		}
		catch(OsmAuthenticationException e) {}

		try
		{
			infoDao.commentChangeset(changesetId, "test comment");
			fail();
		}
		catch(OsmAuthenticationException e) {}
	}

	public void testGetChangesets()
	{
		ChangesetsDao dao = new ChangesetsDao(connection);
		dao.getChangesets(new Handler<ChangesetInfo>()
		{
			@Override
			public void handle(ChangesetInfo tea)
			{
				// nothing...
			}
		}, new QueryChangesetsFilters().byUser(3630));

		// whether the result is consistent with the query parameters is not tested here
		// this just should not throw an exception
	}

	public void testGetChanges()
	{
		MapDataDao mapDataDao = new MapDataDao(userConnection);

		Node node = new OsmNode(-1, 1, new OsmLatLon(55.12313,50.13221), null, null);
		List<Element> elements = new ArrayList<>();
		elements.add(node);
		long changesetId = mapDataDao.updateMap("test", "test", elements, null);

		ChangesetsDao changesetsDao = new ChangesetsDao(userConnection);
		SimpleMapDataChangesHandler handler = new SimpleMapDataChangesHandler();
		changesetsDao.getChanges(changesetId, handler);

		List<Element> createdElements = handler.getCreations();

		assertEquals(1, createdElements.size());

		Node createdNode = (Node) createdElements.get(0);

		// delete again... (clean up before asserting)
		createdNode.setDeleted(true);
		mapDataDao.updateMap("clean up test", "test", createdElements, null);

		assertNotSame(node.getId(), createdNode.getId());
		assertEquals(node.getVersion(), createdNode.getVersion());
		assertEquals(node.getPosition(), createdNode.getPosition());
		assertEquals(changesetId, createdNode.getChangeset().getId());
	}
}
