package de.westnordost.osmapi.notes;

import junit.framework.TestCase;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.westnordost.osmapi.Handler;
import de.westnordost.osmapi.xml.XmlTestUtils;

public class NotesParserTest extends TestCase
{

	public void testParseNoteDate() throws UnsupportedEncodingException
	{
		String xml =
				"<note lon=\"0\" lat=\"0\">" +
				"	<date_created>2015-12-20 22:52:30 UTC</date_created>" +
				"</note>";

		new NotesParser( new TestNoteHandler()
		{
			@Override
			protected void onFirstNote(Note note)
			{
				Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
				c.set(2015, Calendar.DECEMBER, 20, 22, 52, 30);
				assertEquals(c.getTimeInMillis() / 1000, note.getDateCreated().getTime() / 1000);
			}
		}).parse(XmlTestUtils.asInputStream(xml));
	}

	public void testParseNoteStatus() throws UnsupportedEncodingException
	{
		String xml =
				"<note lon=\"0\" lat=\"0\">" +
				"	<status>open</status>" +
				"</note>" +
				"<note lon=\"0\" lat=\"0\">" +
				"	<status>closed</status>" +
				"</note>";

		new NotesParser( new TestNoteHandler()
		{
			@Override
			protected void onFirstNote(Note note)
			{
				assertEquals(Note.Status.OPEN, note.getStatus());
			}

			@Override
			protected void onSecondNote(Note note)
			{
				assertEquals(Note.Status.CLOSED, note.getStatus());
			}
		}).parse(XmlTestUtils.asInputStream(xml));

	}

	public void testParseNoteOptionalDate() throws UnsupportedEncodingException
	{
		String xml =
				"<note lon=\"0\" lat=\"0\">" +
				"	<date_closed>2015-12-20 22:52:30 UTC</date_closed>" +
				"</note>" +
				"<note lon=\"0\" lat=\"0\">" +
				"</note>";

		new NotesParser(new TestNoteHandler()
		{
			@Override
			protected void onFirstNote(Note note)
			{
				assertNotNull(note.getDateClosed());
			}

			@Override
			protected void onSecondNote(Note note)
			{
				assertNull(note.getDateClosed());
			}
		}).parse(XmlTestUtils.asInputStream(xml));
	}

	public void testParseBasicNoteFields() throws UnsupportedEncodingException
	{
		String xml =
				"<note lon=\"-0.1904556\" lat=\"51.5464626\">" +
				"	<id>123456</id>" +
				"</note>";

		new NotesParser(new TestNoteHandler()
		{
			@Override
			protected void onFirstNote(Note note)
			{
				assertEquals(123456, note.getId());
				assertEquals(-0.1904556, note.getPosition().getLongitude());
				assertEquals(51.5464626, note.getPosition().getLatitude());
			}
		}).parse(XmlTestUtils.asInputStream(xml));
	}

	public void testParseCommentDateField() throws UnsupportedEncodingException
	{
		String xml =
				"<note lon=\"0\" lat=\"0\">" +
				"	<comments>" +
				"		<comment>" +
				"			<date>2015-12-22 22:52:40 UTC</date>" +
				"		</comment>" +
				"	</comments>" +
				"</note>";

		new NotesParser(new TestNoteHandler()
		{
			@Override
			protected void onFirstNote(Note note)
			{
				NoteComment comment = note.getComments().get(0);
				Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
				c.set(2015, Calendar.DECEMBER, 22, 22, 52, 40);
				assertEquals(c.getTimeInMillis() / 1000, comment.getDate().getTime() / 1000);
			}
		}).parse(XmlTestUtils.asInputStream(xml));
	}

	public void testParseCommentStatusField() throws UnsupportedEncodingException
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

		new NotesParser(new TestNoteHandler()
		{
			@Override
			protected void onFirstNote(Note note)
			{
				List<NoteComment> comments = note.getComments();
				assertEquals(4, comments.size());
				assertEquals(NoteComment.Action.OPENED, comments.get(0).getAction());
				assertEquals(NoteComment.Action.CLOSED, comments.get(1).getAction());
				assertEquals(NoteComment.Action.COMMENTED, comments.get(2).getAction());
				assertEquals(NoteComment.Action.REOPENED, comments.get(3).getAction());
			}
		}).parse(XmlTestUtils.asInputStream(xml));
	}

	public void testParseCommentOptionalFields() throws UnsupportedEncodingException
	{
		String xml =
				"<note lon=\"0\" lat=\"0\">" +
				"	<comments>" +
				"		<comment>" +
				"		</comment>" +
				"	</comments>" +
				"</note>";

		new NotesParser(new TestNoteHandler()
		{
			@Override
			protected void onFirstNote(Note note)
			{
				NoteComment comment = note.getComments().get(0);
				assertNull(comment.getUser());
			}
		}).parse(XmlTestUtils.asInputStream(xml));
	}

	public void testParseBasicCommentFields() throws UnsupportedEncodingException
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

		new NotesParser(new TestNoteHandler()
		{
			@Override
			protected void onFirstNote(Note note)
			{
				NoteComment comment = note.getComments().get(0);
				assertNotNull(comment.getUser());
				assertEquals(123, (long) comment.getUser().getId());
				assertEquals("mr_x", comment.getUser().getDisplayName());
				assertEquals("Last sighted here", comment.getText());
			}
		}).parse(XmlTestUtils.asInputStream(xml));
	}

	public void testReuseUserData() throws UnsupportedEncodingException
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

		new NotesParser(new TestNoteHandler()
		{
			@Override
			protected void onFirstNote(Note note)
			{
				List<NoteComment> comments = note.getComments();
				assertSame(comments.get(0).getUser(), comments.get(2).getUser());
			}
		}).parse(XmlTestUtils.asInputStream(xml));
	}

	private class TestNoteHandler implements Handler<Note>
	{

		private int counter = 0;

		@Override
		public void handle(Note note)
		{
			if(counter == 0) onFirstNote(note);
			if(counter == 1) onSecondNote(note);
			counter++;
		}

		protected void onFirstNote(Note note) {}
		protected void onSecondNote(Note note) {}
	}
}
