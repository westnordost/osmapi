package de.westnordost.osmapi.notes;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import oauth.signpost.exception.OAuthExpectationFailedException;
import de.westnordost.osmapi.ConnectionTestFactory;
import de.westnordost.osmapi.Handler;
import de.westnordost.osmapi.errors.OsmAuthorizationException;
import de.westnordost.osmapi.errors.OsmConflictException;
import de.westnordost.osmapi.errors.OsmNotFoundException;
import de.westnordost.osmapi.errors.OsmQueryTooBigException;
import de.westnordost.osmapi.map.data.Bounds;
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

	private static final Bounds WHOLE_WORLD = new Bounds(LatLons.MIN_VALUE, LatLons.MAX_VALUE);

	// the area in which this test spawns all those notes
	private static final Bounds MY_AREA = new Bounds(
			OsmLatLon.parseLatLon("-21.00002", "94"),
			OsmLatLon.parseLatLon("-21", "94.00002"));

	private static final Bounds CROSS_180TH_MERIDIAN = new Bounds(
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
		note = privilegedDao.createNote(POINT, TEXT);
	}

	@Override
	protected void tearDown() throws Exception
	{
		privilegedDao.closeNote(note.getId());
	}

	public void testCreateNote()
	{
		// was already created it in setUp
		assertTrue(note.isOpen());
		assertEquals(POINT, note.getPosition());
		assertEquals(Note.Status.OPEN, note.getStatus());
		assertEquals(1, note.getComments().size());

		NoteComment firstComment = note.getComments().get(0);
		assertEquals(TEXT, firstComment.getText());
		assertEquals(NoteComment.Action.OPENED, firstComment.getAction());
		assertFalse(firstComment.isAnonymous());

		long now = new Date().getTime();
		long creationTime = note.getDateCreated().getTime();
		assertTrue(Math.abs(now - creationTime) < TEN_MINUTES);
	}

	public void testCreateNoteInsufficientPrivileges()
	{
		try
		{
			unprivilegedDao.createNote(POINT, TEXT, false);
			fail();
		}
		catch(OsmAuthorizationException e) {}

		try
		{
			unprivilegedDao.createNote(POINT, TEXT);
			fail();
		}
		catch(OsmAuthorizationException e) {}

	}

	public void testCommentNoteInsufficientPrivileges()
	{
		try
		{
			unprivilegedDao.commentNote(note.getId(), TEXT, false);
			fail();
		}
		catch(OsmAuthorizationException e) {}

		try
		{
			unprivilegedDao.commentNote(note.getId(), TEXT);
			fail();
		}
		catch(OsmAuthorizationException e) {}
	}

	public void testReopenNoteInsufficientPrivileges()
	{
		try
		{
			unprivilegedDao.reopenNote(note.getId(), TEXT);
			fail();
		}
		catch(OsmAuthorizationException e) {}
	}

	public void testCloseNoteInsufficientPrivileges()
	{
		try
		{
			unprivilegedDao.closeNote(note.getId(), TEXT);
			fail();
		}
		catch(OsmAuthorizationException e) {}
	}

	public void testReopenNoteAsAnonymousFails()
	{
		try
		{
			anonymousDao.reopenNote(note.getId(), TEXT);
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
			anonymousDao.closeNote(note.getId(), TEXT);
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
			privilegedDao.createNote(POINT, "");
			fail();
		}
		catch(IllegalArgumentException e) {}

		try
		{
			privilegedDao.createNote(POINT, "", false);
			fail();
		}
		catch(IllegalArgumentException e) {}
	}

	public void testCommentNoteWithoutTextFails()
	{
		try
		{
			privilegedDao.commentNote(note.getId(), "");
			fail();
		}
		catch(IllegalArgumentException e) {}

		try
		{
			privilegedDao.commentNote(note.getId(), "", false);
			fail();
		}
		catch(IllegalArgumentException e) {}
	}

	public void testCommentNoteWithNullTextFails()
	{
		try
		{
			privilegedDao.commentNote(note.getId(), null);
			fail();
		}
		catch(NullPointerException e) {}

		try
		{
			privilegedDao.commentNote(note.getId(), null, false);
			fail();
		}
		catch(NullPointerException e) {}
	}

	public void testCloseAndReopenNoteWithoutTextDoesNotFail()
	{
		Note myNote = privilegedDao.createNote(POINT3, TEXT);

		myNote = privilegedDao.closeNote(myNote.getId(), "");
		assertEquals(2, myNote.getComments().size());
		assertEquals(null, myNote.getComments().get(1).getText());

		myNote = privilegedDao.reopenNote(myNote.getId(), "");
		assertEquals(3, myNote.getComments().size());
		assertEquals(null, myNote.getComments().get(2).getText());

		myNote = privilegedDao.closeNote(myNote.getId());
		assertEquals(4, myNote.getComments().size());
		assertEquals(null, myNote.getComments().get(3).getText());

		myNote = privilegedDao.reopenNote(myNote.getId());
		assertEquals(5, myNote.getComments().size());
		assertEquals(null, myNote.getComments().get(4).getText());

		privilegedDao.closeNote(myNote.getId());
	}

	public void testCreateNoteAsAnonymousWorks()
	{
		Note myNote = anonymousDao.createNote(POINT2, TEXT);
		assertTrue(myNote.isOpen());
		assertEquals(POINT2, myNote.getPosition());
		assertEquals(Note.Status.OPEN, myNote.getStatus());
		assertEquals(1, myNote.getComments().size());

		Note closedNote = privilegedDao.closeNote(myNote.getId());
		assertFalse(closedNote.isOpen());
		assertEquals(POINT2, closedNote.getPosition());
		assertEquals(Note.Status.CLOSED, closedNote.getStatus());
	}

	public void testCommentNote()
	{
		List<NoteComment> comments;
		long now, commentTime;

		Note myNote = anonymousDao.createNote(POINT4, TEXT);

		comments = anonymousDao.commentNote(myNote.getId(), TEXT + 1).getComments();
		assertEquals(2, comments.size());
		assertEquals(TEXT + 1, comments.get(1).getText());
		assertEquals(NoteComment.Action.COMMENTED, comments.get(1).getAction());
		assertNull(comments.get(1).getUser());
		assertTrue(comments.get(1).isAnonymous());

		now = new Date().getTime();
		commentTime = comments.get(1).getDate().getTime();
		assertTrue(Math.abs(now - commentTime) < TEN_MINUTES);

		comments = privilegedDao.commentNote(myNote.getId(), TEXT + 2).getComments();
		assertEquals(3, comments.size());
		assertEquals(TEXT + 2, comments.get(2).getText());
		assertEquals(NoteComment.Action.COMMENTED, comments.get(2).getAction());
		assertNotNull(comments.get(2).getUser());
		assertFalse(comments.get(2).isAnonymous());

		now = new Date().getTime();
		commentTime = comments.get(2).getDate().getTime();
		assertTrue(Math.abs(now - commentTime) < TEN_MINUTES);

		privilegedDao.closeNote(myNote.getId());
	}

	public void testCloseAndReopenNote()
	{
		List<NoteComment> comments;

		Note myNote = anonymousDao.createNote(POINT5, TEXT);

		myNote = privilegedDao.closeNote(myNote.getId(), TEXT + 1);

		assertNotNull(myNote.getDateClosed());
		long now = new Date().getTime();
		long closedDate = myNote.getDateClosed().getTime();
		assertTrue(Math.abs(now - closedDate) < TEN_MINUTES);

		comments = myNote.getComments();
		assertEquals(2, comments.size());
		assertEquals(TEXT + 1, comments.get(1).getText());
		assertEquals(NoteComment.Action.CLOSED, comments.get(1).getAction());
		assertNotNull(comments.get(1).getUser());
		assertFalse(comments.get(1).isAnonymous());

		myNote = privilegedDao.reopenNote(myNote.getId(), TEXT + 2);

		assertNull(myNote.getDateClosed());

		comments = myNote.getComments();
		assertEquals(3, comments.size());
		assertEquals(TEXT + 2, comments.get(2).getText());
		assertEquals(NoteComment.Action.REOPENED, comments.get(2).getAction());
		assertNotNull(comments.get(2).getUser());
		assertFalse(comments.get(2).isAnonymous());

		privilegedDao.closeNote(myNote.getId());
	}

	public void testNoteNotFound()
	{
		try { privilegedDao.commentNote(0, TEXT); fail(); } catch(OsmNotFoundException e) {}
		try { privilegedDao.reopenNote(0); fail(); } catch(OsmNotFoundException e) {}
		try { privilegedDao.closeNote(0); fail(); } catch(OsmNotFoundException e) {}
	}

	public void testConflict()
	{
		Note myNote = anonymousDao.createNote(POINT6, TEXT);

		try
		{
			privilegedDao.reopenNote(myNote.getId());
			fail();
		}
		catch(OsmConflictException e) {}

		privilegedDao.closeNote(myNote.getId());

		try
		{
			privilegedDao.closeNote(myNote.getId());
			fail();
		}
		catch(OsmConflictException e) {}

		try
		{
			privilegedDao.commentNote(myNote.getId(), TEXT);
			fail();
		}
		catch(OsmConflictException e) {}
	}

	public void testGetNote()
	{
		Note note2 = anonymousDao.getNote(note.getId());
		assertEquals(note.getId(), note2.getId());
		assertEquals(note.getStatus(), note2.getStatus());
		assertEquals(note.getComments().size(), note2.getComments().size());
		assertEquals(note.getDateCreated(), note2.getDateCreated());
		assertEquals(note.getPosition(), note2.getPosition());
	}

	public void testGetNoNote()
	{
		assertNull(anonymousDao.getNote(0));
	}

	public void testQueryTooBig()
	{
		try
		{
			// try to download the whole world...
			anonymousDao.getNotes(new FailIfCalled(), WHOLE_WORLD, 10000, -1);
			fail();
		}
		catch (OsmQueryTooBigException e) {}
	}

	public void testWrongLimit()
	{
		try
		{
			anonymousDao.getNotes(new FailIfCalled(), WHOLE_WORLD, 0, -1);
			fail();
		}
		catch (IllegalArgumentException e) {}

		try
		{
			anonymousDao.getNotes(new FailIfCalled(), WHOLE_WORLD, 0, 10001);
			fail();
		}
		catch (IllegalArgumentException e) {}
	}

	public void testCrosses180thMeridian()
	{
		try
		{
			anonymousDao.getNotes(new FailIfCalled(), CROSS_180TH_MERIDIAN, 10000, -1);
			fail();
		}
		catch (IllegalArgumentException e) {}
	}

	public void testGetNotes()
	{
		Counter counter = new Counter();
		anonymousDao.getNotes(counter, MY_AREA, 100, -1);
		assertTrue(counter.count > 0);
	}

	public void testSearchNotes()
	{
		Counter counter = new Counter();
		anonymousDao.getNotes(counter, MY_AREA, TEXT, 100, -1);
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
