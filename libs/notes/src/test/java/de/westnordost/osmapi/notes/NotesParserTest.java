package de.westnordost.osmapi.notes;

import org.junit.Test;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import de.westnordost.osmapi.TestUtils;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.ListHandler;
import de.westnordost.osmapi.common.SingleElementHandler;

import static org.junit.Assert.*;

public class NotesParserTest
{

	@Test public void parseNoteDate()
	{
		String xml =
				"<note lon=\"0\" lat=\"0\">" +
				"	<date_created>2015-12-20 22:52:30 UTC</date_created>" +
				"</note>";

		Note note = parseOne(xml);

		assertEquals(
				ZonedDateTime.of(2015, 12, 20, 22, 52, 30, 0, ZoneId.of("UTC")).toInstant(),
				note.createdAt
		);
	}

	@Test public void parseNoteStatus()
	{
		String xml =
				"<note lon=\"0\" lat=\"0\">" +
				"	<status>open</status>" +
				"</note>" +
				"<note lon=\"0\" lat=\"0\">" +
				"	<status>hidden</status>" +
				"</note>" +
				"<note lon=\"0\" lat=\"0\">" +
				"	<status>closed</status>" +
				"</note>";

		List<Note> notes = parseList(xml);
		
		assertEquals(Note.Status.OPEN, notes.get(0).status);
		assertEquals(Note.Status.HIDDEN, notes.get(1).status);
		assertEquals(Note.Status.CLOSED, notes.get(2).status);
	}

	@Test public void parseNoteOptionalDate()
	{
		String xml =
				"<note lon=\"0\" lat=\"0\">" +
				"	<date_closed>2015-12-20 22:52:30 UTC</date_closed>" +
				"</note>" +
				"<note lon=\"0\" lat=\"0\">" +
				"</note>";

		List<Note> notes = parseList(xml);
		
		assertNotNull(notes.get(0).closedAt);
		assertNull(notes.get(1).closedAt);
	}

	@Test public void parseBasicNoteFields()
	{
		String xml =
				"<note lon=\"-0.1904556\" lat=\"51.5464626\">" +
				"	<id>123456</id>" +
				"</note>";

		Note note = parseOne(xml);
		
		assertEquals(123456, note.id);
		assertEquals(-0.1904556, note.position.getLongitude(), 1e-7);
		assertEquals(51.5464626, note.position.getLatitude(), 1e-7);
	}

	@Test public void parseCommentDateField()
	{
		String xml =
				"<note lon=\"0\" lat=\"0\">" +
				"	<comments>" +
				"		<comment>" +
				"			<date>2015-12-22 22:52:40 UTC</date>" +
				"		</comment>" +
				"	</comments>" +
				"</note>";

		Note note = parseOne(xml);
		NoteComment comment = note.comments.get(0);

		assertEquals(
			ZonedDateTime.of(2015, 12, 22, 22, 52, 40, 0, ZoneId.of("UTC")).toInstant(),
			comment.date
		);
	}

	@Test public void parseCommentStatusField()
	{
		String xml =
				"<note lon=\"0\" lat=\"0\">" +
				"	<comments>" +
				"		<comment>" +
				"			<action>opened</action>" +
				"		</comment>" +
				"		<comment>" +
				"			<action>closed</action>" +
				"		</comment>" +
				"		<comment>" +
				"			<action>commented</action>" +
				"		</comment>" +
				"		<comment>" +
				"			<action>reopened</action>" +
				"		</comment>" +
				"		<comment>" +
				"			<action>hidden</action>" +
				"		</comment>" +
				"	</comments>" +
				"</note>";

		Note note = parseOne(xml);
		
		List<NoteComment> comments = note.comments;
		assertEquals(5, comments.size());
		assertEquals(NoteComment.Action.OPENED, comments.get(0).action);
		assertEquals(NoteComment.Action.CLOSED, comments.get(1).action);
		assertEquals(NoteComment.Action.COMMENTED, comments.get(2).action);
		assertEquals(NoteComment.Action.REOPENED, comments.get(3).action);
		assertEquals(NoteComment.Action.HIDDEN, comments.get(4).action);
	}

	@Test public void parseCommentOptionalFields()
	{
		String xml =
				"<note lon=\"0\" lat=\"0\">" +
				"	<comments>" +
				"		<comment>" +
				"		</comment>" +
				"	</comments>" +
				"</note>";

		Note note = parseOne(xml);

		NoteComment comment = note.comments.get(0);
		assertNull(comment.user);
	}

	@Test public void parseBasicCommentFields()
	{
		String xml =
				"<note lon=\"0\" lat=\"0\">" +
				"	<comments>" +
				"		<comment>" +
				"			<date>2015-12-20 22:52:30 UTC</date>" +
				"			<uid>123</uid>" +
				"			<user>mr_x</user>" +
				"			<user_url>http://www.openstreetmap.org/user/mr_x</user_url>" +
				"			<text>Last sighted here</text>" +
				"			<html>&lt;p&gt;Last sighted here&lt;/p&gt;</html>" +
				"		</comment>" +
				"	</comments>" +
				"</note>";

		Note note = parseOne(xml);

		NoteComment comment = note.comments.get(0);
		assertNotNull(comment.user);
		assertEquals(123, comment.user.id);
		assertEquals("mr_x", comment.user.displayName);
		assertEquals("Last sighted here", comment.text);
	}

	@Test public void reuseUserData()
	{
		String xml =
				"<note lon=\"0\" lat=\"0\">" +
				"	<comments>" +
				"		<comment>" +
				"			<date>2015-12-20 22:52:30 UTC</date>" +
				"			<uid>123</uid>" +
				"			<user>mr_x</user>" +
				"			<text>Last sighted here</text>" +
				"		</comment>" +
				"		<comment>" +
				"			<date>2015-12-20 22:54:30 UTC</date>" +
				"			<text>Didn't see you</text>" +
				"		</comment>" +
				"		<comment>" +
				"			<date>2015-12-20 22:56:30 UTC</date>" +
				"			<uid>123</uid>" +
				"			<user>mr_x</user>" +
				"			<text>Haha, I'm gone already!</text>" +
				"		</comment>" +
				"	</comments>" +
				"</note>";

		Note note = parseOne(xml);
		
		List<NoteComment> comments = note.comments;
		assertSame(comments.get(0).user, comments.get(2).user);
	}
	
	private List<Note> parseList(String xml)
	{
		ListHandler<Note> handler = new ListHandler<>();
		parse(xml, handler);
		return handler.get();
	}
	
	private Note parseOne(String xml)
	{
		SingleElementHandler<Note> handler = new SingleElementHandler<>();
		parse(xml, handler);
		return handler.get();
	}
	
	private void parse(String xml, Handler<Note> handler)
	{
		try
		{
			new NotesParser(handler).parse(TestUtils.asInputStream(xml));
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
