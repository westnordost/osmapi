package de.westnordost.osmapi.notes;

import java.io.InputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.westnordost.osmapi.ApiResponseReader;
import de.westnordost.osmapi.Handler;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.user.User;
import de.westnordost.osmapi.xml.XmlParser;

/** Parses a list of openstreetmap notes from OSM Notes API 0.6. It parses the XML naively, i.e. it
 *  does not care where in the XML the notes nodes are. */
public class NotesParser extends XmlParser implements ApiResponseReader<Void>
{
	private static final String
			NOTE = "note",
			COMMENT = "comment";

	private NotesDateFormat dateFormat = new NotesDateFormat();
	
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
			currentNote = new Note();
			currentNote.position = OsmLatLon.parseLatLon(getAttribute("lat"), getAttribute("lon"));
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
					users.put(userId, new User(userId, userName));
				}

				currentComment.user = users.get(userId);

				userId = -1;
				userName = null;
			}
			currentNote.comments.add(currentComment);
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
				currentNote.id = Long.parseLong(txt);
				break;
			case "status":
				currentNote.status = Note.Status.valueOf(txt.toUpperCase(Locale.UK));
				break;
			case "date_created":
				currentNote.dateCreated = dateFormat.parse(txt);
				break;
			case "date_closed":
				currentNote.dateClosed = dateFormat.parse(txt);
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
				currentComment.date = dateFormat.parse(txt);
				break;
			case "user":
				userName = txt;
				break;
			case "uid":
				userId = Long.parseLong(txt);
				break;
			case "text":
				currentComment.text = txt;
				break;
			case "action":
				currentComment.action = NoteComment.Action.valueOf(txt.toUpperCase(Locale.UK));
				break;
		}
	}
}
