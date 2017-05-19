package de.westnordost.osmapi.notes;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.westnordost.osmapi.TestUtils;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.ListHandler;
import de.westnordost.osmapi.common.SingleElementHandler;

public class NotesParserTest extends TestCase
{

	public void testParseNoteDate()
	{
		String xml =
				"<note lon=\"0\" lat=\"0\">" +
				"	<date_created>2015-12-20 22:52:30 UTC</date_created>" +
				"</note>";

		Note note = parseOne(xml);

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
		c.set(2015, Calendar.DECEMBER, 20, 22, 52, 30);
		assertEquals(c.getTimeInMillis() / 1000, note.dateCreated.getTime() / 1000);
	}

	public void testParseNoteStatus()
	{
		String xml =
				"<note lon=\"0\" lat=\"0\">" +
				"	<status>open</status>" +
				"</note>" +
				"<note lon=\"0\" lat=\"0\">" +
				"	<status>closed</status>" +
				"</note>";

		List<Note> notes = parseList(xml);
		
		assertEquals(Note.Status.OPEN, notes.get(0).status);
		assertEquals(Note.Status.CLOSED, notes.get(1).status);
	}

	public void testParseNoteOptionalDate()
	{
		String xml =
				"<note lon=\"0\" lat=\"0\">" +
				"	<date_closed>2015-12-20 22:52:30 UTC</date_closed>" +
				"</note>" +
				"<note lon=\"0\" lat=\"0\">" +
				"</note>";

		List<Note> notes = parseList(xml);
		
		assertNotNull(notes.get(0).dateClosed);
		assertNull(notes.get(1).dateClosed);
	}

	public void testParseBasicNoteFields()
	{
		String xml =
				"<note lon=\"-0.1904556\" lat=\"51.5464626\">" +
				"	<id>123456</id>" +
				"</note>";

		Note note = parseOne(xml);
		
		assertEquals(123456, note.id);
		assertEquals(-0.1904556, note.position.getLongitude());
		assertEquals(51.5464626, note.position.getLatitude());
	}

	public void testParseCommentDateField()
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
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
		c.set(2015, Calendar.DECEMBER, 22, 22, 52, 40);
		assertEquals(c.getTimeInMillis() / 1000, comment.date.getTime() / 1000);
	}

	public void testParseCommentStatusField()
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
				"	</comments>" +
				"</note>";

		Note note = parseOne(xml);
		
		List<NoteComment> comments = note.comments;
		assertEquals(4, comments.size());
		assertEquals(NoteComment.Action.OPENED, comments.get(0).action);
		assertEquals(NoteComment.Action.CLOSED, comments.get(1).action);
		assertEquals(NoteComment.Action.COMMENTED, comments.get(2).action);
		assertEquals(NoteComment.Action.REOPENED, comments.get(3).action);
	}

	public void testParseCommentOptionalFields()
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

	public void testParseBasicCommentFields()
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
		assertEquals(123, (long) comment.user.id);
		assertEquals("mr_x", comment.user.displayName);
		assertEquals("Last sighted here", comment.text);
	}

	public void testReuseUserData()
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
