package de.westnordost.osmapi.changesets;

import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
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

import static org.junit.Assert.*;

public class ChangesetsApiTest
{
	private ChangesetsApi privilegedDao;
	private ChangesetsApi anonymousDao;
	private ChangesetsApi unprivilegedDao;

	private MapDataDao mapDataDao;

	private long changesetId = 1;

	/* the time is chosen relatively arbitrary. Basically, if it is off by more than half an hour,
	   there might be a problem with parsing the correct timezone. But ten minutes is suspicious
	   enough to let the test fail, imo */
	private static final int TEN_MINUTES = 1000 * 60 * 10;

	private static final int A_USER_WITH_CHANGESETS = 3630;
	private static final int A_USER_WITHOUT_CHANGESETS = 3632;
	
	@Before public void setUp()
	{
		OsmConnection userConnection = ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_EVERYTHING);

		mapDataDao = new MapDataDao(userConnection);

		anonymousDao = new ChangesetsApi(ConnectionTestFactory.createConnection(null));
		privilegedDao = new ChangesetsApi(userConnection);
		unprivilegedDao = new ChangesetsApi(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_NOTHING));
	}

	@Test public void subscribe()
	{
		ChangesetInfo info = privilegedDao.subscribe(changesetId);
		privilegedDao.unsubscribe(changesetId);
		assertEquals(changesetId, info.id);
	}

	@Test public void read()
	{
		List<Element> elements = new ArrayList<>();
		long myChangesetId = mapDataDao.updateMap("unit test", "", elements, null);

		ChangesetInfo infos = unprivilegedDao.get(myChangesetId);
		assertEquals(myChangesetId, infos.id);
		assertEquals(ConnectionTestFactory.USER_AGENT, infos.getGenerator());
		assertEquals("unit test",infos.getChangesetComment());
		assertNull(infos.boundingBox);
		assertFalse(infos.isOpen);
		assertNotNull(infos.closedAt);
		long now = Instant.now().toEpochMilli();
		assertTrue(Math.abs(now - infos.closedAt.toEpochMilli()) < TEN_MINUTES);
		assertTrue(Math.abs(now - infos.createdAt.toEpochMilli()) < TEN_MINUTES);
	}

	@Test public void readInvalidReturnsNull()
	{
		assertNull(unprivilegedDao.get(0));
	}

	@Test public void comment()
	{
		List<Element> elements = new ArrayList<>();
		long myChangesetId = mapDataDao.updateMap("unit test", "", elements, null);

		ChangesetInfo infos = privilegedDao.comment(myChangesetId, "test comment");
		assertEquals(1, infos.notesCount);

		List<ChangesetNote> comments = privilegedDao.get(myChangesetId).discussion;
		assertNotNull(comments);
		long now = Instant.now().toEpochMilli();
		assertTrue(Math.abs(now - comments.get(0).createdAt.toEpochMilli()) < TEN_MINUTES);
		assertEquals("test comment", comments.get(0).text);
	}

	@Test public void emptyComment()
	{
		try
		{
			privilegedDao.comment(changesetId, "");
			fail();
		}
		catch(IllegalArgumentException ignore) {}
	}

	@Test public void authFail()
	{
		try
		{
			unprivilegedDao.subscribe(changesetId);
			fail();
		}
		catch(OsmAuthorizationException ignore) {}

		try
		{
			unprivilegedDao.unsubscribe(changesetId);
			fail();
		}
		catch(OsmAuthorizationException ignore) {}

		try
		{
			unprivilegedDao.comment(changesetId, "test comment");
			fail();
		}
		catch(OsmAuthorizationException ignore) {}
	}

	@Test public void alreadySubscribedDoesNotFail()
	{
		// ...which is different from the "raw" API response

		assertNotNull(privilegedDao.subscribe(changesetId));
		assertNotNull(privilegedDao.subscribe(changesetId));
	}

	@Test public void notSubscribedDoesNotFail()
	{
		// ...which is different from the "raw" API response
		assertNotNull(privilegedDao.unsubscribe(changesetId));
	}

	@Test public void subscribeNonExistingChangesetFails()
	{
		try { privilegedDao.subscribe(0); fail(); } catch(OsmNotFoundException ignore) {}
		try { privilegedDao.unsubscribe(0); fail(); } catch(OsmNotFoundException ignore) {}
	}

	@Test public void getAsAnonymousDoesNotFail()
	{
		assertNotNull(anonymousDao.get(changesetId));
	}


	@Test public void getDataAsAnonymousDoesNotFail()
	{
		anonymousDao.getData(123, new SimpleMapDataChangesHandler());
	}

	@Test public void findAsAnonymousDoesNotFail()
	{
		anonymousDao.find(new Handler<ChangesetInfo>()
		{
			@Override
			public void handle(ChangesetInfo tea)
			{
				// nothing...
			}
		}, new QueryChangesetsFilters().byUser(A_USER_WITH_CHANGESETS));
	}

	@Test public void anonymousFail()
	{
		try
		{
			anonymousDao.subscribe(changesetId);
			fail();
		}
		catch(OsmAuthorizationException ignore) {}

		try
		{
			anonymousDao.unsubscribe(changesetId);
			fail();
		}
		catch(OsmAuthorizationException ignore) {}

		try
		{
			anonymousDao.comment(changesetId, "test comment");
			fail();
		}
		catch(OsmAuthorizationException ignore) {}
	}

	@Test public void getChangesets()
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

	@Test public void getChangesetsEmptyDoesNotFail()
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
	
	@Test public void getChanges()
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
