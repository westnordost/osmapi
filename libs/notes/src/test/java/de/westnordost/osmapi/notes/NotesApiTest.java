package de.westnordost.osmapi.notes;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.List;

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
		assertThrows(OsmAuthorizationException.class, () -> unprivilegedApi.create(POINT, TEXT));
	}

	@Test public void createNoteAsAnonymousAllowed()
	{
		Note note = anonymousApi.create(POINT, TEXT);

		assertEquals(POINT, note.position);
		assertEquals(TEXT, note.comments.get(0).text);
	}

	@Test public void commentNoteInsufficientPrivileges()
	{
		assertThrows(OsmAuthorizationException.class, () -> unprivilegedApi.comment(note.id, TEXT));
	}

	@Test public void commentNoteAsAnonymousFails()
	{
		assertThrows(OsmAuthorizationException.class, () -> anonymousApi.comment(note.id, TEXT));
	}

	@Test public void reopenNoteInsufficientPrivileges()
	{
		assertThrows(OsmAuthorizationException.class, () -> unprivilegedApi.reopen(note.id, TEXT));
	}

	@Test public void closeNoteInsufficientPrivileges()
	{
		assertThrows(OsmAuthorizationException.class, () -> unprivilegedApi.close(note.id, TEXT));
	}

	@Test public void reopenNoteAsAnonymousFails()
	{
		assertThrows(OsmAuthorizationException.class, () -> anonymousApi.reopen(note.id, TEXT));
	}

	@Test public void closeNoteAsAnonymousFails()
	{
		assertThrows(OsmAuthorizationException.class, () -> anonymousApi.close(note.id, TEXT));
	}

	@Test public void createNoteWithoutTextFails()
	{
		assertThrows(IllegalArgumentException.class, () -> privilegedApi.create(POINT, ""));
	}

	@Test public void commentNoteWithoutTextFails()
	{
		assertThrows(IllegalArgumentException.class, () -> privilegedApi.comment(note.id, ""));
	}

	@Test public void commentNoteWithNullTextFails()
	{
		assertThrows(NullPointerException.class, () -> privilegedApi.comment(note.id, null));
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
		assertThrows(OsmNotFoundException.class, () -> privilegedApi.comment(0, TEXT));
		assertThrows(OsmNotFoundException.class, () -> privilegedApi.reopen(0));
		assertThrows(OsmNotFoundException.class, () -> privilegedApi.close(0));
	}

	@Test public void conflict()
	{
		Note myNote = privilegedApi.create(POINT6, TEXT);

		assertThrows(OsmConflictException.class, () -> privilegedApi.reopen(myNote.id));

		privilegedApi.close(myNote.id);

		assertThrows(OsmConflictException.class, () -> privilegedApi.close(myNote.id));

		assertThrows(OsmConflictException.class, () -> privilegedApi.comment(myNote.id, TEXT));
	}

	@Test public void subscribeAsAnonymousFails() {
		Note note = privilegedApi.create(POINT, "subscribe as anonymous");
		try {
			anonymousApi.subscribe(note.id);
			fail();
		} catch (OsmAuthorizationException ignored) {
		} finally {
			privilegedApi.close(note.id);
		}
	}

	@Test public void subscribeTwiceFails() {
		Note note = privilegedApi.create(POINT, "subscribe twice");
		try {
			// user is already subscribed automatically by creating the note
			privilegedApi.subscribe(note.id);
			fail();
		} catch (OsmConflictException ignored) {
		} finally {
			privilegedApi.close(note.id);
		}
	}

	@Test public void subscribeNonExistingNoteFails() {
		assertThrows(OsmNotFoundException.class, () -> privilegedApi.subscribe(0));
	}

	@Test public void unsubscribeNonExistingNoteFails() {
		assertThrows(OsmNotFoundException.class, () -> privilegedApi.unsubscribe(0));
	}

	@Test public void unsubscribeNotSubscribedNoteFails() {
		Note note = privilegedApi.create(POINT, "unsubscribe twice");
		// user is already subscribed by creating the note
		privilegedApi.unsubscribe(note.id);
		try {
			privilegedApi.unsubscribe(note.id);
			fail();
		} catch (OsmNotFoundException ignored) {
		} finally {
			privilegedApi.close(note.id);
		}
	}

	@Test public void unsubscribeAsAnonymousFails() {
		assertThrows(OsmAuthorizationException.class, () -> anonymousApi.unsubscribe(0));
	}

	@Test public void subscribeAndUnsubscribe() {
		Note note = privilegedApi.create(POINT, "subscribe and unsubscribe");
		privilegedApi.unsubscribe(note.id);
		privilegedApi.subscribe(note.id);
		privilegedApi.unsubscribe(note.id);
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
		// try to download the whole world...
		assertThrows(
				OsmQueryTooBigException.class,
				() -> unprivilegedApi.getAll(WHOLE_WORLD, new FailIfCalled(), 10000, -1)
		);
	}

	@Test public void wrongLimit()
	{
		assertThrows(
				IllegalArgumentException.class,
				() -> unprivilegedApi.getAll(WHOLE_WORLD, new FailIfCalled(), 0, -1)
		);

		assertThrows(
				IllegalArgumentException.class,
				() -> unprivilegedApi.getAll(WHOLE_WORLD, new FailIfCalled(), 0, 10001)
		);
	}

	@Test public void crosses180thMeridian()
	{
		assertThrows(
				IllegalArgumentException.class,
				() -> unprivilegedApi.getAll(CROSS_180TH_MERIDIAN, new FailIfCalled(), 10000, -1)
		);
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
