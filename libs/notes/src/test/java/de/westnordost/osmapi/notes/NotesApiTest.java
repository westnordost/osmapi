package de.westnordost.osmapi.notes;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.List;

import oauth.signpost.exception.OAuthExpectationFailedException;
import de.westnordost.osmapi.ConnectionTestFactory;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.errors.OsmAuthorizationException;
import de.westnordost.osmapi.common.errors.OsmConflictException;
import de.westnordost.osmapi.common.errors.OsmNotFoundException;
import de.westnordost.osmapi.common.errors.OsmQueryTooBigException;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.LatLons;
import de.westnordost.osmapi.map.data.OsmLatLon;

import static org.junit.Assert.*;

public class NotesApiTest
{
	private NotesApi privilegedApi;
	private NotesApi anonymousApi;
	private NotesApi unprivilegedApi;

	private Note note;

	// different positions for the different tests
	private static final LatLon POINT = OsmLatLon.parseLatLon("-21", "94");
	private static final LatLon POINT2 = OsmLatLon.parseLatLon("-21","94.00001");
	private static final LatLon POINT3 = OsmLatLon.parseLatLon("-21","94.00002");
	private static final LatLon POINT4 = OsmLatLon.parseLatLon("-21.00001","94");
	private static final LatLon POINT5 = OsmLatLon.parseLatLon("-21.00002","94");
	private static final LatLon POINT6 = OsmLatLon.parseLatLon("-21.00001","94.00001");

	/* the time is chosen relatively arbitrary. Basically, if it off by more than half an hour,
	   there might be a problem with parsing the correct timezone. But ten minutes is suspicious
	   enough to let the test fail, imo */
	private static final int TEN_MINUTES = 1000 * 60 * 10;

	private static final String TEXT = "test case";

	private static final BoundingBox WHOLE_WORLD = new BoundingBox(LatLons.MIN_VALUE, LatLons.MAX_VALUE);

	// the area in which this test spawns all those notes
	private static final BoundingBox MY_AREA = new BoundingBox(
			OsmLatLon.parseLatLon("-21.00002", "94"),
			OsmLatLon.parseLatLon("-21", "94.00002"));

	private static final BoundingBox CROSS_180TH_MERIDIAN = new BoundingBox(
			OsmLatLon.parseLatLon("0", "180"),
			OsmLatLon.parseLatLon("0.0000001", "-179.9999999"));

	@Before public void setUp()
	{
		anonymousApi = new NotesApi(ConnectionTestFactory.createConnection(null));
		privilegedApi = new NotesApi(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_EVERYTHING));
		unprivilegedApi = new NotesApi(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_NOTHING));

		// create one note to work with...
		note = privilegedApi.create(POINT, TEXT);
	}

	@After public void tearDown()
	{
		privilegedApi.close(note.id);
	}

	@Test public void createNote()
	{
		// was already created it in setUp
		assertTrue(note.isOpen());
		assertEquals(POINT, note.position);
		assertEquals(Note.Status.OPEN, note.status);
		assertEquals(1, note.comments.size());

		NoteComment firstComment = note.comments.get(0);
		assertEquals(TEXT, firstComment.text);
		assertEquals(NoteComment.Action.OPENED, firstComment.action);
		assertFalse(firstComment.isAnonymous());

		assertTrue(Math.abs(Instant.now().toEpochMilli() - note.createdAt.toEpochMilli()) < TEN_MINUTES);
	}

	@Test public void createNoteInsufficientPrivileges()
	{
		Note note = unprivilegedApi.create(POINT, TEXT);
		assertTrue(note.comments.get(0).isAnonymous());
	}

	@Test public void createNoteAsAnonymousAllowed()
	{
		Note note = anonymousApi.create(POINT, TEXT);

		assertEquals(POINT, note.position);
		assertEquals(TEXT, note.comments.get(0).text);
	}

	@Test public void commentNoteInsufficientPrivileges()
	{
		try
		{
			unprivilegedApi.comment(note.id, TEXT);
			fail();
		}
		catch(OsmAuthorizationException ignore) {}
	}

	@Test public void commentNoteAsAnonymousFails()
	{
		try
		{
			anonymousApi.comment(note.id, TEXT);
			fail();
		}
		catch(OsmAuthorizationException ignore) {}
	}

	@Test public void reopenNoteInsufficientPrivileges()
	{
		try
		{
			unprivilegedApi.reopen(note.id, TEXT);
			fail();
		}
		catch(OsmAuthorizationException ignore) {}
	}

	@Test public void closeNoteInsufficientPrivileges()
	{
		try
		{
			unprivilegedApi.close(note.id, TEXT);
			fail();
		}
		catch(OsmAuthorizationException ignore) {}
	}

	@Test public void reopenNoteAsAnonymousFails()
	{
		try
		{
			anonymousApi.reopen(note.id, TEXT);
			fail();
		}
		catch(OsmAuthorizationException e)
		{
			assertTrue(e.getCause() instanceof OAuthExpectationFailedException);
		}
	}

	@Test public void closeNoteAsAnonymousFails()
	{
		try
		{
			anonymousApi.close(note.id, TEXT);
			fail();
		}
		catch(OsmAuthorizationException e)
		{
			assertTrue(e.getCause() instanceof OAuthExpectationFailedException);
		}
	}

	@Test public void createNoteWithoutTextFails()
	{
		try
		{
			privilegedApi.create(POINT, "");
			fail();
		}
		catch(IllegalArgumentException ignore) {}
	}

	@Test public void commentNoteWithoutTextFails()
	{
		try
		{
			privilegedApi.comment(note.id, "");
			fail();
		}
		catch(IllegalArgumentException ignore) {}
	}

	@Test public void commentNoteWithNullTextFails()
	{
		try
		{
			privilegedApi.comment(note.id, null);
			fail();
		}
		catch(NullPointerException ignore) {}
	}

	@Test public void closeAndReopenNoteWithoutTextDoesNotFail()
	{
		Note myNote = privilegedApi.create(POINT3, TEXT);

		myNote = privilegedApi.close(myNote.id, "");
		assertEquals(2, myNote.comments.size());
		assertNull(myNote.comments.get(1).text);

		myNote = privilegedApi.reopen(myNote.id, "");
		assertEquals(3, myNote.comments.size());
		assertNull(myNote.comments.get(2).text);

		myNote = privilegedApi.close(myNote.id);
		assertEquals(4, myNote.comments.size());
		assertNull(myNote.comments.get(3).text);

		myNote = privilegedApi.reopen(myNote.id);
		assertEquals(5, myNote.comments.size());
		assertNull(myNote.comments.get(4).text);

		privilegedApi.close(myNote.id);
	}

	@Test public void commentNote()
	{
		List<NoteComment> comments;
		long now, commentTime;

		Note myNote = privilegedApi.create(POINT4, TEXT);

		comments = privilegedApi.comment(myNote.id, TEXT + 1).comments;
		assertEquals(2, comments.size());
		assertEquals(TEXT + 1, comments.get(1).text);
		assertEquals(NoteComment.Action.COMMENTED, comments.get(1).action);
		assertNotNull(comments.get(1).user);
		assertFalse(comments.get(1).isAnonymous());

		now = Instant.now().toEpochMilli();
		commentTime = comments.get(1).date.toEpochMilli();
		assertTrue(Math.abs(now - commentTime) < TEN_MINUTES);

		comments = privilegedApi.comment(myNote.id, TEXT + 2).comments;
		assertEquals(3, comments.size());
		assertEquals(TEXT + 2, comments.get(2).text);
		assertEquals(NoteComment.Action.COMMENTED, comments.get(2).action);
		assertNotNull(comments.get(2).user);
		assertFalse(comments.get(2).isAnonymous());

		now = Instant.now().toEpochMilli();
		commentTime = comments.get(2).date.toEpochMilli();
		assertTrue(Math.abs(now - commentTime) < TEN_MINUTES);

		privilegedApi.close(myNote.id);
	}

	@Test public void closeAndReopenNote()
	{
		List<NoteComment> comments;

		Note myNote = privilegedApi.create(POINT5, TEXT);

		myNote = privilegedApi.close(myNote.id, TEXT + 1);

		assertNotNull(myNote.closedAt);
		long now = Instant.now().toEpochMilli();
		long closedDate = myNote.closedAt.toEpochMilli();
		assertTrue(Math.abs(now - closedDate) < TEN_MINUTES);

		comments = myNote.comments;
		assertEquals(2, comments.size());
		assertEquals(TEXT + 1, comments.get(1).text);
		assertEquals(NoteComment.Action.CLOSED, comments.get(1).action);
		assertNotNull(comments.get(1).user);
		assertFalse(comments.get(1).isAnonymous());

		myNote = privilegedApi.reopen(myNote.id, TEXT + 2);

		assertNull(myNote.closedAt);

		comments = myNote.comments;
		assertEquals(3, comments.size());
		assertEquals(TEXT + 2, comments.get(2).text);
		assertEquals(NoteComment.Action.REOPENED, comments.get(2).action);
		assertNotNull(comments.get(2).user);
		assertFalse(comments.get(2).isAnonymous());

		privilegedApi.close(myNote.id);
	}

	@Test public void noteNotFound()
	{
		try { privilegedApi.comment(0, TEXT); fail(); } catch(OsmNotFoundException ignore) {}
		try { privilegedApi.reopen(0); fail(); } catch(OsmNotFoundException ignore) {}
		try { privilegedApi.close(0); fail(); } catch(OsmNotFoundException ignore) {}
	}

	@Test public void conflict()
	{
		Note myNote = privilegedApi.create(POINT6, TEXT);

		try
		{
			privilegedApi.reopen(myNote.id);
			fail();
		}
		catch(OsmConflictException ignore) {}

		privilegedApi.close(myNote.id);

		try
		{
			privilegedApi.close(myNote.id);
			fail();
		}
		catch(OsmConflictException ignore) {}

		try
		{
			privilegedApi.comment(myNote.id, TEXT);
			fail();
		}
		catch(OsmConflictException ignore) {}
	}

	@Test public void getNote()
	{
		Note note2 = unprivilegedApi.get(note.id);
		assertEquals(note.id, note2.id);
		assertEquals(note.status, note2.status);
		assertEquals(note.comments.size(), note2.comments.size());
		assertEquals(note.createdAt, note2.createdAt);
		assertEquals(note.position, note2.position);
	}

	@Test public void getNoNote()
	{
		assertNull(unprivilegedApi.get(0));
	}

	@Test public void getNoteAsAnonymous()
	{
		assertNotNull(anonymousApi.get(note.id));
	}

	@Test public void queryTooBig()
	{
		try
		{
			// try to download the whole world...
			unprivilegedApi.getAll(WHOLE_WORLD, new FailIfCalled(), 10000, -1);
			fail();
		}
		catch (OsmQueryTooBigException ignore) {}
	}

	@Test public void wrongLimit()
	{
		try
		{
			unprivilegedApi.getAll(WHOLE_WORLD, new FailIfCalled(), 0, -1);
			fail();
		}
		catch (IllegalArgumentException ignore) {}

		try
		{
			unprivilegedApi.getAll(WHOLE_WORLD, new FailIfCalled(), 0, 10001);
			fail();
		}
		catch (IllegalArgumentException ignore) {}
	}

	@Test public void crosses180thMeridian()
	{
		try
		{
			unprivilegedApi.getAll(CROSS_180TH_MERIDIAN, new FailIfCalled(), 10000, -1);
			fail();
		}
		catch (IllegalArgumentException ignore) {}
	}

	@Test public void getNotes()
	{
		Counter counter = new Counter();
		unprivilegedApi.getAll(MY_AREA, counter, 100, -1);
		assertTrue(counter.count > 0);
	}

	@Test public void getNotesAsAnonymous()
	{
		Counter counter = new Counter();
		anonymousApi.getAll(MY_AREA, counter, 100, -1);
		assertTrue(counter.count > 0);
	}

	@Test public void findNotes()
	{
		Counter counter = new Counter();
		unprivilegedApi.find(counter, null);
		assertTrue(counter.count > 0);
	}

	@Test public void findNotesWithParameters()
	{
		Counter counter = new Counter();
		unprivilegedApi.find(counter,
				new QueryNotesFilters().hideClosedNotesAfter(-1).limit(10));
		assertTrue(counter.count > 0);
	}

	@Test public void findNotesWithoutQueryDoesNotFail()
	{
		Counter counter = new Counter();
		unprivilegedApi.find(counter, null);
		assertTrue(counter.count > 0);
	}

	@Test public void findNotesAsAnonymous()
	{
		Counter counter = new Counter();
		anonymousApi.find(counter, null);
		assertTrue(counter.count > 0);
	}

	private static class Counter implements Handler<Note>
	{
		int count;

		@Override
		public void handle(Note tea)
		{
			count++;
		}
	}

	private static class FailIfCalled implements Handler<Note>
	{
		@Override
		public void handle(Note tea)
		{
			fail();
		}
	}
}
