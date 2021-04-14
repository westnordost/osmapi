package de.westnordost.osmapi.notes;

import java.time.Instant;
import java.util.List;

import junit.framework.TestCase;
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

public class NotesDaoTest extends TestCase
{
	private NotesDao privilegedDao;
	private NotesDao anonymousDao;
	private NotesDao unprivilegedDao;

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

	@Override
	protected void setUp()
	{
		anonymousDao = new NotesDao(ConnectionTestFactory.createConnection(null));
		privilegedDao = new NotesDao(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_EVERYTHING));
		unprivilegedDao = new NotesDao(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_NOTHING));

		// create one note to work with...
		note = privilegedDao.create(POINT, TEXT);
	}

	@Override
	protected void tearDown()
	{
		privilegedDao.close(note.id);
	}

	public void testCreateNote()
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

	public void testCreateNoteInsufficientPrivileges()
	{
		try
		{
			unprivilegedDao.create(POINT, TEXT);
			fail();
		}
		catch(OsmAuthorizationException ignore) {}
	}

	public void testCreateNoteAsAnonymousAllowed()
	{
		Note note = anonymousDao.create(POINT, TEXT);

		assertEquals(POINT, note.position);
		assertEquals(TEXT, note.comments.get(0).text);
	}

	public void testCommentNoteInsufficientPrivileges()
	{
		try
		{
			unprivilegedDao.comment(note.id, TEXT);
			fail();
		}
		catch(OsmAuthorizationException ignore) {}
	}

	public void testCommentNoteAsAnonymousFails()
	{
		try
		{
			anonymousDao.comment(note.id, TEXT);
			fail();
		}
		catch(OsmAuthorizationException ignore) {}
	}

	public void testReopenNoteInsufficientPrivileges()
	{
		try
		{
			unprivilegedDao.reopen(note.id, TEXT);
			fail();
		}
		catch(OsmAuthorizationException ignore) {}
	}

	public void testCloseNoteInsufficientPrivileges()
	{
		try
		{
			unprivilegedDao.close(note.id, TEXT);
			fail();
		}
		catch(OsmAuthorizationException ignore) {}
	}

	public void testReopenNoteAsAnonymousFails()
	{
		try
		{
			anonymousDao.reopen(note.id, TEXT);
			fail();
		}
		catch(OsmAuthorizationException e)
		{
			assertTrue(e.getCause() instanceof OAuthExpectationFailedException);
		}
	}

	public void testCloseNoteAsAnonymousFails()
	{
		try
		{
			anonymousDao.close(note.id, TEXT);
			fail();
		}
		catch(OsmAuthorizationException e)
		{
			assertTrue(e.getCause() instanceof OAuthExpectationFailedException);
		}
	}

	public void testCreateNoteWithoutTextFails()
	{
		try
		{
			privilegedDao.create(POINT, "");
			fail();
		}
		catch(IllegalArgumentException ignore) {}
	}

	public void testCommentNoteWithoutTextFails()
	{
		try
		{
			privilegedDao.comment(note.id, "");
			fail();
		}
		catch(IllegalArgumentException ignore) {}
	}

	public void testCommentNoteWithNullTextFails()
	{
		try
		{
			privilegedDao.comment(note.id, null);
			fail();
		}
		catch(NullPointerException ignore) {}
	}

	public void testCloseAndReopenNoteWithoutTextDoesNotFail()
	{
		Note myNote = privilegedDao.create(POINT3, TEXT);

		myNote = privilegedDao.close(myNote.id, "");
		assertEquals(2, myNote.comments.size());
		assertNull(myNote.comments.get(1).text);

		myNote = privilegedDao.reopen(myNote.id, "");
		assertEquals(3, myNote.comments.size());
		assertNull(myNote.comments.get(2).text);

		myNote = privilegedDao.close(myNote.id);
		assertEquals(4, myNote.comments.size());
		assertNull(myNote.comments.get(3).text);

		myNote = privilegedDao.reopen(myNote.id);
		assertEquals(5, myNote.comments.size());
		assertNull(myNote.comments.get(4).text);

		privilegedDao.close(myNote.id);
	}

	public void testCommentNote()
	{
		List<NoteComment> comments;
		long now, commentTime;

		Note myNote = privilegedDao.create(POINT4, TEXT);

		comments = privilegedDao.comment(myNote.id, TEXT + 1).comments;
		assertEquals(2, comments.size());
		assertEquals(TEXT + 1, comments.get(1).text);
		assertEquals(NoteComment.Action.COMMENTED, comments.get(1).action);
		assertNotNull(comments.get(1).user);
		assertFalse(comments.get(1).isAnonymous());

		now = Instant.now().toEpochMilli();
		commentTime = comments.get(1).date.toEpochMilli();
		assertTrue(Math.abs(now - commentTime) < TEN_MINUTES);

		comments = privilegedDao.comment(myNote.id, TEXT + 2).comments;
		assertEquals(3, comments.size());
		assertEquals(TEXT + 2, comments.get(2).text);
		assertEquals(NoteComment.Action.COMMENTED, comments.get(2).action);
		assertNotNull(comments.get(2).user);
		assertFalse(comments.get(2).isAnonymous());

		now = Instant.now().toEpochMilli();
		commentTime = comments.get(2).date.toEpochMilli();
		assertTrue(Math.abs(now - commentTime) < TEN_MINUTES);

		privilegedDao.close(myNote.id);
	}

	public void testCloseAndReopenNote()
	{
		List<NoteComment> comments;

		Note myNote = privilegedDao.create(POINT5, TEXT);

		myNote = privilegedDao.close(myNote.id, TEXT + 1);

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

		myNote = privilegedDao.reopen(myNote.id, TEXT + 2);

		assertNull(myNote.closedAt);

		comments = myNote.comments;
		assertEquals(3, comments.size());
		assertEquals(TEXT + 2, comments.get(2).text);
		assertEquals(NoteComment.Action.REOPENED, comments.get(2).action);
		assertNotNull(comments.get(2).user);
		assertFalse(comments.get(2).isAnonymous());

		privilegedDao.close(myNote.id);
	}

	public void testNoteNotFound()
	{
		try { privilegedDao.comment(0, TEXT); fail(); } catch(OsmNotFoundException ignore) {}
		try { privilegedDao.reopen(0); fail(); } catch(OsmNotFoundException ignore) {}
		try { privilegedDao.close(0); fail(); } catch(OsmNotFoundException ignore) {}
	}

	public void testConflict()
	{
		Note myNote = privilegedDao.create(POINT6, TEXT);

		try
		{
			privilegedDao.reopen(myNote.id);
			fail();
		}
		catch(OsmConflictException ignore) {}

		privilegedDao.close(myNote.id);

		try
		{
			privilegedDao.close(myNote.id);
			fail();
		}
		catch(OsmConflictException ignore) {}

		try
		{
			privilegedDao.comment(myNote.id, TEXT);
			fail();
		}
		catch(OsmConflictException ignore) {}
	}

	public void testGetNote()
	{
		Note note2 = unprivilegedDao.get(note.id);
		assertEquals(note.id, note2.id);
		assertEquals(note.status, note2.status);
		assertEquals(note.comments.size(), note2.comments.size());
		assertEquals(note.createdAt, note2.createdAt);
		assertEquals(note.position, note2.position);
	}

	public void testGetNoNote()
	{
		assertNull(unprivilegedDao.get(0));
	}

	public void testGetNoteAsAnonymous()
	{
		assertNotNull(anonymousDao.get(note.id));
	}

	public void testQueryTooBig()
	{
		try
		{
			// try to download the whole world...
			unprivilegedDao.getAll(WHOLE_WORLD, new FailIfCalled(), 10000, -1);
			fail();
		}
		catch (OsmQueryTooBigException ignore) {}
	}

	public void testWrongLimit()
	{
		try
		{
			unprivilegedDao.getAll(WHOLE_WORLD, new FailIfCalled(), 0, -1);
			fail();
		}
		catch (IllegalArgumentException ignore) {}

		try
		{
			unprivilegedDao.getAll(WHOLE_WORLD, new FailIfCalled(), 0, 10001);
			fail();
		}
		catch (IllegalArgumentException ignore) {}
	}

	public void testCrosses180thMeridian()
	{
		try
		{
			unprivilegedDao.getAll(CROSS_180TH_MERIDIAN, new FailIfCalled(), 10000, -1);
			fail();
		}
		catch (IllegalArgumentException ignore) {}
	}

	public void testGetNotes()
	{
		Counter counter = new Counter();
		unprivilegedDao.getAll(MY_AREA, counter, 100, -1);
		assertTrue(counter.count > 0);
	}

	public void testGetNotesAsAnonymous()
	{
		Counter counter = new Counter();
		anonymousDao.getAll(MY_AREA, counter, 100, -1);
		assertTrue(counter.count > 0);
	}

	public void testFindNotes()
	{
		Counter counter = new Counter();
		unprivilegedDao.find(counter, null);
		assertTrue(counter.count > 0);
	}

	public void testFindNotesWithParameters()
	{
		Counter counter = new Counter();
		unprivilegedDao.find(counter,
				new QueryNotesFilters().hideClosedNotesAfter(-1).limit(10));
		assertTrue(counter.count > 0);
	}

	public void testFindNotesWithoutQueryDoesNotFail()
	{
		Counter counter = new Counter();
		unprivilegedDao.find(counter, null);
		assertTrue(counter.count > 0);
	}

	public void testFindNotesAsAnonymous()
	{
		Counter counter = new Counter();
		anonymousDao.find(counter, null);
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
