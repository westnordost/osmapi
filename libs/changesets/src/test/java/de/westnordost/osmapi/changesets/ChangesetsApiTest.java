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
import de.westnordost.osmapi.map.MapDataApi;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.map.data.OsmNode;
import de.westnordost.osmapi.map.changes.SimpleMapDataChangesHandler;

import static org.junit.Assert.*;

public class ChangesetsApiTest
{
	private ChangesetsApi privilegedApi;
	private ChangesetsApi anonymousApi;
	private ChangesetsApi unprivilegedApi;

	private MapDataApi mapDataApi;

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

		mapDataApi = new MapDataApi(userConnection);

		anonymousApi = new ChangesetsApi(ConnectionTestFactory.createConnection(null));
		privilegedApi = new ChangesetsApi(userConnection);
		unprivilegedApi = new ChangesetsApi(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_NOTHING));
	}

	@Test public void subscribe()
	{
		ChangesetInfo info = privilegedApi.subscribe(changesetId);
		privilegedApi.unsubscribe(changesetId);
		assertEquals(changesetId, info.id);
	}

	@Test public void read()
	{
		List<Element> elements = new ArrayList<>();
		long myChangesetId = mapDataApi.updateMap("unit test", "", elements, null);

		ChangesetInfo infos = unprivilegedApi.get(myChangesetId);
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
		assertNull(unprivilegedApi.get(0));
	}

	@Test public void comment()
	{
		List<Element> elements = new ArrayList<>();
		long myChangesetId = mapDataApi.updateMap("unit test", "", elements, null);

		ChangesetInfo infos = privilegedApi.comment(myChangesetId, "test comment");
		assertEquals(1, infos.notesCount);

		List<ChangesetNote> comments = privilegedApi.get(myChangesetId).discussion;
		assertNotNull(comments);
		long now = Instant.now().toEpochMilli();
		assertTrue(Math.abs(now - comments.get(0).createdAt.toEpochMilli()) < TEN_MINUTES);
		assertEquals("test comment", comments.get(0).text);
	}

	@Test public void emptyComment()
	{
		try
		{
			privilegedApi.comment(changesetId, "");
			fail();
		}
		catch(IllegalArgumentException ignore) {}
	}

	@Test public void authFail()
	{
		try
		{
			unprivilegedApi.subscribe(changesetId);
			fail();
		}
		catch(OsmAuthorizationException ignore) {}

		try
		{
			unprivilegedApi.unsubscribe(changesetId);
			fail();
		}
		catch(OsmAuthorizationException ignore) {}

		try
		{
			unprivilegedApi.comment(changesetId, "test comment");
			fail();
		}
		catch(OsmAuthorizationException ignore) {}
	}

	@Test public void alreadySubscribedDoesNotFail()
	{
		// ...which is different from the "raw" API response

		assertNotNull(privilegedApi.subscribe(changesetId));
		assertNotNull(privilegedApi.subscribe(changesetId));
	}

	@Test public void notSubscribedDoesNotFail()
	{
		// ...which is different from the "raw" API response
		assertNotNull(privilegedApi.unsubscribe(changesetId));
	}

	@Test public void subscribeNonExistingChangesetFails()
	{
		try { privilegedApi.subscribe(0); fail(); } catch(OsmNotFoundException ignore) {}
		try { privilegedApi.unsubscribe(0); fail(); } catch(OsmNotFoundException ignore) {}
	}

	@Test public void getAsAnonymousDoesNotFail()
	{
		assertNotNull(anonymousApi.get(changesetId));
	}


	@Test public void getDataAsAnonymousDoesNotFail()
	{
		anonymousApi.getData(123, new SimpleMapDataChangesHandler());
	}

	@Test public void findAsAnonymousDoesNotFail()
	{
		anonymousApi.find(new Handler<ChangesetInfo>()
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
			anonymousApi.subscribe(changesetId);
			fail();
		}
		catch(OsmAuthorizationException ignore) {}

		try
		{
			anonymousApi.unsubscribe(changesetId);
			fail();
		}
		catch(OsmAuthorizationException ignore) {}

		try
		{
			anonymousApi.comment(changesetId, "test comment");
			fail();
		}
		catch(OsmAuthorizationException ignore) {}
	}

	@Test public void getChangesets()
	{
		unprivilegedApi.find(new Handler<ChangesetInfo>()
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
		unprivilegedApi.find(new Handler<ChangesetInfo>()
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
		long changesetId = mapDataApi.updateMap("test", "test", elements, null);

		SimpleMapDataChangesHandler handler = new SimpleMapDataChangesHandler();
		unprivilegedApi.getData(changesetId, handler);

		List<Element> createdElements = handler.getCreations();

		assertEquals(1, createdElements.size());

		OsmNode createdNode = (OsmNode) createdElements.get(0);

		// delete again... (clean up before asserting)
		createdNode.setDeleted(true);
		mapDataApi.updateMap("clean up test", "test", createdElements, null);

		assertNotSame(node.getId(), createdNode.getId());
		assertEquals(node.getVersion(), createdNode.getVersion());
		assertEquals(node.getPosition(), createdNode.getPosition());
		assertEquals(changesetId, createdNode.getChangeset().id);
	}
}
