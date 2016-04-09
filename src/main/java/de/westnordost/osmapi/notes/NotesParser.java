package de.westnordost.osmapi.notes;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.westnordost.osmapi.ApiResponseReader;
import de.westnordost.osmapi.Handler;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.user.OsmUser;
import de.westnordost.osmapi.user.User;
import de.westnordost.osmapi.xml.XmlParser;

/** Parses a list of openstreetmap notes from OSM Notes API 0.6. It parses the XML naively, i.e. it
 *  does not care where in the XML the notes nodes are. */
public class NotesParser extends XmlParser implements ApiResponseReader<Void>
{
	private static final SimpleDateFormat DATE_FORMAT =
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.UK);

	private static final String
			NOTE = "note",
			COMMENT = "comment";

	/* temporary map so we do not parse and hold many times the same user */
	private Map<Long, User> users;

	private Handler<Note> handler;
	private Note currentNote;
	private NoteComment currentComment;

	private long userId = -1;
	private String userName;

	public NotesParser(Handler<Note> handler)
	{
		this.handler = handler;
	}

	@Override
	public Void parse(InputStream in)
	{
		users = new HashMap<>();
		doParse(in);
		users = null;
		return null;
	}

	@Override
	protected void onStartElement()
	{
		String name = getName();

		if (name.equals(NOTE))
		{
			currentNote = new Note(	OsmLatLon.parseLatLon(getAttribute("lat"), getAttribute("lon")) );
		}
		else if(name.equals(COMMENT))
		{
			currentComment = new NoteComment();
		}
	}

	@Override
	protected void onEndElement() throws ParseException
	{
		String name = getName();
		String parentName = getParentName();
		String txt = getText();

		if(NOTE.equals(name))
		{
			handler.handle( currentNote );
			currentNote = null;
		}
		else if(COMMENT.equals(name))
		{
			if(userId != -1 || userName != null)
			{
				if(!users.containsKey(userId))
				{
					users.put(userId, new OsmUser(userId, userName));
				}

				currentComment.setUser(users.get(userId));

				userId = -1;
				userName = null;
			}
			currentNote.addComment(currentComment);
			currentComment = null;
		}
		else if(NOTE.equals(parentName))
		{
			parseNoteTextNode(name,txt);
		}
		else if(COMMENT.equals(parentName))
		{
			parseCommentTextNode(name,txt);
		}
	}

	private void parseNoteTextNode(String name, String txt) throws ParseException
	{
		assert(txt != null);
		assert(currentNote != null);
		switch (name)
		{
			case "id":
				currentNote.setId(Long.parseLong(txt));
				break;
			case "status":
				currentNote.setStatus(Note.Status.valueOf(txt.toUpperCase(Locale.UK)));
				break;
			case "date_created":
				currentNote.setDateCreated(DATE_FORMAT.parse(txt));
				break;
			case "date_closed":
				currentNote.setDateClosed(DATE_FORMAT.parse(txt));
				break;
		}
	}

	private void parseCommentTextNode(String name, String txt) throws ParseException
	{
		assert(txt != null);
		assert(currentComment != null);
		switch (name)
		{
			case "date":
				currentComment.setDate(DATE_FORMAT.parse(txt));
				break;
			case "user":
				userName = txt;
				break;
			case "uid":
				userId = Long.parseLong(txt);
				break;
			case "text":
				currentComment.setText(txt);
				break;
			case "action":
				currentComment.setAction(NoteComment.Action.valueOf(txt.toUpperCase(Locale.UK)));
				break;
		}
	}
}
