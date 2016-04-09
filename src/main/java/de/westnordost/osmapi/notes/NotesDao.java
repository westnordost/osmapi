package de.westnordost.osmapi.notes;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import de.westnordost.osmapi.ApiResponseReader;
import de.westnordost.osmapi.Handler;
import de.westnordost.osmapi.errors.OsmAuthenticationException;
import de.westnordost.osmapi.errors.OsmBadUserInputException;
import de.westnordost.osmapi.errors.OsmConflictException;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.errors.OsmNotFoundException;
import de.westnordost.osmapi.errors.OsmQueryTooBigException;
import de.westnordost.osmapi.SingleElementHandler;
import de.westnordost.osmapi.map.data.Bounds;
import de.westnordost.osmapi.map.data.LatLon;

/** Creates, comments, closes, reopens and search for notes */
public class NotesDao
{
	private static final String NOTES = "notes";

	private final OsmConnection osm;

	public NotesDao(OsmConnection osm)
	{
		this.osm = osm;
	}

	/**
	 * Create a new note at the given location as the current user or anonymous if not logged in.
	 *
	 * @param pos position of the note. Must not be null.
	 * @param text text for the new note. Must not be empty nor null.
	 * @throws OsmAuthenticationException if this application is not authenticated to write notes
	 *                                    (Permission.WRITE_NOTES)
	 * @return the new note
	 */
	public Note createNote(LatLon pos, String text)
	{
		boolean asAnonymous = osm.getOAuth() == null;
		return createNote(pos, text, asAnonymous);
	}

	/**
	 * Create a new note at the given location
	 *
	 * @param pos position of the note. Must not be null.
	 * @param text text for the new note. Must not be empty nor null.
	 * @param asAnonymous whether to post this note as anonymous
	 *
	 * @throws OsmAuthenticationException if this application is not authenticated to write notes
	 *                                    (Permission.WRITE_NOTES) - and posting as non anonymous
	 *
	 * @return the new note
	 */
	public Note createNote(LatLon pos, String text, boolean asAnonymous)
	{
		if(text.isEmpty())
		{
			throw new IllegalArgumentException("Text may not be empty");
		}

		String data = "lat=" + pos.getLatitude() + "&lon=" + pos.getLongitude()
				+ "&text=" + urlEncode(text);
		String call = NOTES + "?" + data;

		SingleElementHandler<Note> noteHandler = new SingleElementHandler<>();
		osm.makeRequest(call, "POST", !asAnonymous, null, new NotesParser(noteHandler));
		return noteHandler.get();
	}

	/**
	 * Comment the note with the given id as the current user or anonymous if not logged in.
	 *
	 * @see #commentNote(long, String, boolean)
	 */
	public Note commentNote(long id, String text)
	{
		boolean asAnonymous = osm.getOAuth() == null;
		return commentNote(id, text, asAnonymous);
	}

	/**
	 * @param id id of the note
	 * @param text comment to be added to the note. Must not be null or empty
	 * @param asAnonymous whether to comment on this note as anonymous
	 *
	 * @throws OsmConflictException if the note has already been closed.
	 * @throws OsmAuthenticationException if this application is not authenticated to write notes
	 *                                    (Permission.WRITE_NOTES) - and posting as non anonymous
	 * @throws OsmNotFoundException if the note with the given id does not exist (anymore)
	 *
	 * @return the updated commented note
	 */
	public Note commentNote(long id, String text, boolean asAnonymous)
	{
		if(text.isEmpty())
		{
			throw new IllegalArgumentException("comment must not be empty");
		}

		SingleElementHandler<Note> noteHandler = new SingleElementHandler<>();
		makeSingleNoteRequest(id, "comment", !asAnonymous, text, new NotesParser(noteHandler));
		return noteHandler.get();
	}

	/**
	 * Reopens the given note without providing a reason.
	 *
	 * @see #reopenNote(long, String)
	 */
	public Note reopenNote(long id)
	{
		return reopenNote(id, null);
	}

	/**
	 * Reopen the given note with the given reason. The reason is optional.
	 *
	 * @throws OsmConflictException if the note has already been reopened.
	 * @throws OsmAuthenticationException if this application is not authenticated to write notes
	 *                                    (Permission.WRITE_NOTES)
	 * @throws OsmNotFoundException if the note with the given id does not exist (anymore)
	 *
	 * @return the updated reopened note
	 */
	public Note reopenNote(long id, String reason)
	{
		SingleElementHandler<Note> noteHandler = new SingleElementHandler<>();
		makeSingleNoteRequest(id, "reopen", true, reason, new NotesParser(noteHandler));
		return noteHandler.get();
	}

	/**
	 * Close aka resolve the note with the given id and reason.
	 *
	 * @param id id of the note
	 * @param reason comment to be added to the note as a reason for it being closed. Optional.
	 *
	 * @throws OsmConflictException if the note has already been closed.
	 * @throws OsmAuthenticationException if this application is not authenticated to write notes
	 *                                    (Permission.WRITE_NOTES)
	 * @throws OsmNotFoundException if the note with the given id does not exist (anymore)
	 *
	 * @return the closed note
	 */
	public Note closeNote(long id, String reason)
	{
		SingleElementHandler<Note> noteHandler = new SingleElementHandler<>();
		makeSingleNoteRequest(id, "close", true, reason, new NotesParser(noteHandler));
		return noteHandler.get();
	}

	/**
	 * Close aka resolve the note with the given id without providing a reason.
	 *
	 * @see #closeNote(long, String)
	 */
	public Note closeNote(long id)
	{
		return closeNote(id, null);
	}

	/**
	 * @param id id of the note
	 * @return the note with the given id. null if the note with that id does not exist (anymore).
	 */
	public Note getNote(long id)
	{
		SingleElementHandler<Note> noteHandler = new SingleElementHandler<>();
		try
		{
			osm.makeRequest(NOTES + "/" + id, new NotesParser(noteHandler));
		}
		catch (OsmNotFoundException e)
		{
			return null;
		}
		return noteHandler.get();
	}

	/**
	 * Retrieve all notes in the given area and feed them to the given handler.
	 *
	 * @see #getNotes(Handler, Bounds, String, int, int)
	 */
	public void getNotes(Handler<Note> handler, Bounds bounds, int limit, int hideClosedNoteAfter)
	{
		getNotes(handler, bounds, null, limit, hideClosedNoteAfter);
	}

	/**
	 * Retrieve those notes in the given area that match the given search string
	 *
	 * @param handler The handler which is fed the incoming notes
	 * @param bounds the area within the notes should be queried. This is usually limited at 25
	 *               square degrees. Check the server capabilities.
	 * @param search what to search for. Null to return everything.
	 * @param limit number of entries returned at maximum. Any value between 1 and 10000
	 * @param hideClosedNoteAfter number of days until a closed note should not be shown anymore.
	 *                            -1 means that all notes should be returned, 0 that only open notes
	 *                            are returned.
	 *
	 * @throws OsmQueryTooBigException if the bounds area is too large
	 * @throws IllegalArgumentException if the bounds cross the 180th meridian
	 */
	public void getNotes(Handler<Note> handler, Bounds bounds, String search, int limit,
						 int hideClosedNoteAfter)
	{
		if(limit <= 0 || limit > 10000)
		{
			throw new IllegalArgumentException("limit must be within 1 and 10000");
		}
		if(bounds.crosses180thMeridian())
		{
			throw new IllegalArgumentException("bounds may not cross the 180th meridian");
		}

		String searchQuery = "";
		if(search != null && !search.isEmpty())
		{
			searchQuery = "&q=" + urlEncode(search);
		}

		String call = NOTES + "?bbox=" + bounds.getAsLeftBottomRightTopString() +
				searchQuery + "&limit=" + limit + "&closed=" + hideClosedNoteAfter;

		try
		{
			osm.makeRequest(call, new NotesParser(handler));
		}
		catch(OsmBadUserInputException e)
		{
			// we can be more specific here
			throw new OsmQueryTooBigException(e.getResponseCode(), e.getResponseBody());
		}
	}

	private <T> T makeSingleNoteRequest(long id, String call, boolean authenticate,	String text,
										ApiResponseReader<T> reader)
	{
		String data = "";
		if(text != null)
		{
			data = "?text=" + urlEncode(text);
		}
		String apiCall = NOTES + "/" + id + "/" + call + data;
		return osm.makeRequest(apiCall, "POST", authenticate, null, reader);
	}

	private String urlEncode(String text)
	{
		try
		{
			return URLEncoder.encode(text, osm.getCharset());
		}
		catch (UnsupportedEncodingException e)
		{
			// should never happen since we use UTF-8
			throw new RuntimeException(e);
		}
	}
}
