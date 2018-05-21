package de.westnordost.osmapi.notes;

import java.util.Date;
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
	protected void setUp() throws Exception
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
	protected void tearDown() throws Exception
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

		long now = new Date().getTime();
		long creationTime = note.dateCreated.getTime();
		assertTrue(Math.abs(now - creationTime) < TEN_MINUTES);
	}

	public void testCreateNoteInsufficientPrivileges()
	{
		try
		{
			unprivilegedDao.create(POINT, TEXT, false);
			fail();
		}
		catch(OsmAuthorizationException e) {}

		try
		{
			unprivilegedDao.create(POINT, TEXT);
			fail();
		}
		catch(OsmAuthorizationException e) {}

	}

	public void testCommentNoteInsufficientPrivileges()
	{
		try
		{
			unprivilegedDao.comment(note.id, TEXT);
			fail();
		}
		catch(OsmAuthorizationException e) {}
	}

	public void testReopenNoteInsufficientPrivileges()
	{
		try
		{
			unprivilegedDao.reopen(note.id, TEXT);
			fail();
		}
		catch(OsmAuthorizationException e) {}
	}

	public void testCloseNoteInsufficientPrivileges()
	{
		try
		{
			unprivilegedDao.close(note.id, TEXT);
			fail();
		}
		catch(OsmAuthorizationException e) {}
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
		catch(IllegalArgumentException e) {}

		try
		{
			privilegedDao.create(POINT, "", false);
			fail();
		}
		catch(IllegalArgumentException e) {}
	}

	public void testCommentNoteWithoutTextFails()
	{
		try
		{
			privilegedDao.comment(note.id, "");
			fail();
		}
		catch(IllegalArgumentException e) {}
	}

	public void testCommentNoteWithNullTextFails()
	{
		try
		{
			privilegedDao.comment(note.id, null);
			fail();
		}
		catch(NullPointerException e) {}
	}

	public void testCloseAndReopenNoteWithoutTextDoesNotFail()
	{
		Note myNote = privilegedDao.create(POINT3, TEXT);

		myNote = privilegedDao.close(myNote.id, "");
		assertEquals(2, myNote.comments.size());
		assertEquals(null, myNote.comments.get(1).text);

		myNote = privilegedDao.reopen(myNote.id, "");
		assertEquals(3, myNote.comments.size());
		assertEquals(null, myNote.comments.get(2).text);

		myNote = privilegedDao.close(myNote.id);
		assertEquals(4, myNote.comments.size());
		assertEquals(null, myNote.comments.get(3).text);

		myNote = privilegedDao.reopen(myNote.id);
		assertEquals(5, myNote.comments.size());
		assertEquals(null, myNote.comments.get(4).text);

		privilegedDao.close(myNote.id);
	}

	public void testCreateNoteAsAnonymousWorks()
	{
		Note myNote = anonymousDao.create(POINT2, TEXT);
		assertTrue(myNote.isOpen());
		assertEquals(POINT2, myNote.position);
		assertEquals(Note.Status.OPEN, myNote.status);
		assertEquals(1, myNote.comments.size());

		Note closedNote = privilegedDao.close(myNote.id);
		assertFalse(closedNote.isOpen());
		assertEquals(POINT2, closedNote.position);
		assertEquals(Note.Status.CLOSED, closedNote.status);
	}

	public void testCommentNote()
	{
		List<NoteComment> comments;
		long now, commentTime;

		Note myNote = anonymousDao.create(POINT4, TEXT);

		comments = anonymousDao.comment(myNote.id, TEXT + 1).comments;
		assertEquals(2, comments.size());
		assertEquals(TEXT + 1, comments.get(1).text);
		assertEquals(NoteComment.Action.COMMENTED, comments.get(1).action);
		assertNull(comments.get(1).user);
		assertTrue(comments.get(1).isAnonymous());

		now = new Date().getTime();
		commentTime = comments.get(1).date.getTime();
		assertTrue(Math.abs(now - commentTime) < TEN_MINUTES);

		comments = privilegedDao.comment(myNote.id, TEXT + 2).comments;
		assertEquals(3, comments.size());
		assertEquals(TEXT + 2, comments.get(2).text);
		assertEquals(NoteComment.Action.COMMENTED, comments.get(2).action);
		assertNotNull(comments.get(2).user);
		assertFalse(comments.get(2).isAnonymous());

		now = new Date().getTime();
		commentTime = comments.get(2).date.getTime();
		assertTrue(Math.abs(now - commentTime) < TEN_MINUTES);

		privilegedDao.close(myNote.id);
	}

	public void testCloseAndReopenNote()
	{
		List<NoteComment> comments;

		Note myNote = anonymousDao.create(POINT5, TEXT);

		myNote = privilegedDao.close(myNote.id, TEXT + 1);

		assertNotNull(myNote.dateClosed);
		long now = new Date().getTime();
		long closedDate = myNote.dateClosed.getTime();
		assertTrue(Math.abs(now - closedDate) < TEN_MINUTES);

		comments = myNote.comments;
		assertEquals(2, comments.size());
		assertEquals(TEXT + 1, comments.get(1).text);
		assertEquals(NoteComment.Action.CLOSED, comments.get(1).action);
		assertNotNull(comments.get(1).user);
		assertFalse(comments.get(1).isAnonymous());

		myNote = privilegedDao.reopen(myNote.id, TEXT + 2);

		assertNull(myNote.dateClosed);

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
		try { privilegedDao.comment(0, TEXT); fail(); } catch(OsmNotFoundException e) {}
		try { privilegedDao.reopen(0); fail(); } catch(OsmNotFoundException e) {}
		try { privilegedDao.close(0); fail(); } catch(OsmNotFoundException e) {}
	}

	public void testConflict()
	{
		Note myNote = anonymousDao.create(POINT6, TEXT);

		try
		{
			privilegedDao.reopen(myNote.id);
			fail();
		}
		catch(OsmConflictException e) {}

		privilegedDao.close(myNote.id);

		try
		{
			privilegedDao.close(myNote.id);
			fail();
		}
		catch(OsmConflictException e) {}

		try
		{
			privilegedDao.comment(myNote.id, TEXT);
			fail();
		}
		catch(OsmConflictException e) {}
	}

	public void testGetNote()
	{
		Note note2 = unprivilegedDao.get(note.id);
		assertEquals(note.id, note2.id);
		assertEquals(note.status, note2.status);
		assertEquals(note.comments.size(), note2.comments.size());
		assertEquals(note.dateCreated, note2.dateCreated);
		assertEquals(note.position, note2.position);
	}

	public void testGetNoNote()
	{
		assertNull(unprivilegedDao.get(0));
	}

	public void testGetNoteAsAnonymousFails()
	{
		try
		{
			anonymousDao.get(note.id);
			fail();
		}
		catch (OsmAuthorizationException e) {}
	}

	public void testQueryTooBig()
	{
		try
		{
			// try to download the whole world...
			anonymousDao.getAll(WHOLE_WORLD, new FailIfCalled(), 10000, -1);
			fail();
		}
		catch (OsmQueryTooBigException e) {}
	}

	public void testWrongLimit()
	{
		try
		{
			anonymousDao.getAll(WHOLE_WORLD, new FailIfCalled(), 0, -1);
			fail();
		}
		catch (IllegalArgumentException e) {}

		try
		{
			anonymousDao.getAll(WHOLE_WORLD, new FailIfCalled(), 0, 10001);
			fail();
		}
		catch (IllegalArgumentException e) {}
	}

	public void testCrosses180thMeridian()
	{
		try
		{
			anonymousDao.getAll(CROSS_180TH_MERIDIAN, new FailIfCalled(), 10000, -1);
			fail();
		}
		catch (IllegalArgumentException e) {}
	}

	public void testGetNotes()
	{
		Counter counter = new Counter();
		unprivilegedDao.getAll(MY_AREA, counter, 100, -1);
		assertTrue(counter.count > 0);
	}

	public void testGetNotesAsAnonymousFails()
	{
		try
		{
			anonymousDao.getAll(MY_AREA, null, 100, -1);
			fail();
		}
		catch(OsmAuthorizationException e) {}
	}

	public void testSearchNotes()
	{
		Counter counter = new Counter();
		anonymousDao.getAll(MY_AREA, TEXT, counter, 100, -1);
		assertTrue(counter.count > 0);
	}

	private class Counter implements Handler<Note>
	{
		public int count;

		@Override
		public void handle(Note tea)
		{
			count++;
		}
	}

	private class FailIfCalled implements Handler<Note>
	{
		@Override
		public void handle(Note tea)
		{
			assertTrue(false);
		}
	};

}
