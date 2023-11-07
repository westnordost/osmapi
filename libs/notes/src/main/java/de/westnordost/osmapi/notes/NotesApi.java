package de.westnordost.osmapi.notes;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.Locale;

import de.westnordost.osmapi.ApiResponseReader;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.SingleElementHandler;
import de.westnordost.osmapi.common.errors.OsmAuthorizationException;
import de.westnordost.osmapi.common.errors.OsmBadUserInputException;
import de.westnordost.osmapi.common.errors.OsmConflictException;
import de.westnordost.osmapi.common.errors.OsmNotFoundException;
import de.westnordost.osmapi.common.errors.OsmQueryTooBigException;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;

/** Creates, comments, closes, reopens and search for notes
 *  All interactions with this class require an OsmConnection with a logged in user. */
public class NotesApi
{
	private static final String NOTES = "notes";

	private final OsmConnection osm;

	private final NumberFormat numberFormat;

	public NotesApi(OsmConnection osm)
	{
		this.osm = osm;
		numberFormat = NumberFormat.getNumberInstance(Locale.UK);
		numberFormat.setMaximumFractionDigits(340);
	}

	/**
	 * Create a new note at the given location
	 *
	 * @param pos position of the note. Must not be null.
	 * @param text text for the new note. Must not be empty nor null.
	 *
	 * @throws OsmAuthorizationException if this application is not authorized to write notes
	 *                                   (Permission.WRITE_NOTES)
	 *
	 * @return the new note
	 */
	public Note create(LatLon pos, String text)
	{
		if(text.isEmpty())
		{
			throw new IllegalArgumentException("Text may not be empty");
		}

		String data =
				"lat=" + numberFormat.format(pos.getLatitude()) +
				"&lon=" + numberFormat.format(pos.getLongitude()) +
				"&text=" + urlEncode(text);
		String call = NOTES + "?" + data;

		boolean authenticate = osm.getOAuth() != null;
		SingleElementHandler<Note> noteHandler = new SingleElementHandler<>();
		osm.makeRequest(call, "POST", authenticate, null, new NotesParser(noteHandler));
		return noteHandler.get();
	}

	/**
	 * @param id id of the note
	 * @param text comment to be added to the note. Must not be null or empty
	 *
	 * @throws OsmConflictException if the note has already been closed.
	 * @throws OsmAuthorizationException if this application is not authorized to write notes
	 *                                   (Permission.WRITE_NOTES)
	 * @throws OsmNotFoundException if the note with the given id does not exist (anymore)
	 *
	 * @return the updated commented note
	 */
	public Note comment(long id, String text)
	{
		if(text.isEmpty())
		{
			throw new IllegalArgumentException("comment must not be empty");
		}

		SingleElementHandler<Note> noteHandler = new SingleElementHandler<>();
		makeSingleNoteRequest(id, "comment", text, new NotesParser(noteHandler));
		return noteHandler.get();
	}

	/**
	 * Reopens the given note without providing a reason.
	 *
	 * @see #reopen(long, String)
	 */
	public Note reopen(long id)
	{
		return reopen(id, null);
	}

	/**
	 * Reopen the given note with the given reason. The reason is optional.
	 *
	 * @param id the id of the note to reopen
	 * @param reason the reason why it is reopened. May be null.
	 *
	 * @throws OsmConflictException if the note has already been reopened.
	 * @throws OsmAuthorizationException if this application is not authorized to write notes
	 *                                    (Permission.WRITE_NOTES)
	 * @throws OsmNotFoundException if the note with the given id does not exist (anymore)
	 *
	 * @return the updated reopened note
	 */
	public Note reopen(long id, String reason)
	{
		SingleElementHandler<Note> noteHandler = new SingleElementHandler<>();
		makeSingleNoteRequest(id, "reopen", reason, new NotesParser(noteHandler));
		return noteHandler.get();
	}

	/**
	 * Close aka resolve the note with the given id and reason.
	 *
	 * @param id id of the note
	 * @param reason comment to be added to the note as a reason for it being closed. Optional.
	 *
	 * @throws OsmConflictException if the note has already been closed.
	 * @throws OsmAuthorizationException if this application is not authorized to write notes
	 *                                   (Permission.WRITE_NOTES)
	 * @throws OsmNotFoundException if the note with the given id does not exist (anymore)
	 *
	 * @return the closed note
	 */
	public Note close(long id, String reason)
	{
		SingleElementHandler<Note> noteHandler = new SingleElementHandler<>();
		makeSingleNoteRequest(id, "close", reason, new NotesParser(noteHandler));
		return noteHandler.get();
	}

	/**
	 * Close aka resolve the note with the given id without providing a reason.
	 *
	 * @see #close(long, String)
	 */
	public Note close(long id)
	{
		return close(id, null);
	}

	/**
	 * @param id id of the note
	 *
	 * @return the note with the given id. null if the note with that id does not exist (anymore).
	 */
	public Note get(long id)
	{
		SingleElementHandler<Note> noteHandler = new SingleElementHandler<>();
		try
		{
			boolean authenticate = osm.getOAuth() != null;
			osm.makeRequest(NOTES + "/" + id, authenticate, new NotesParser(noteHandler));
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
	 * @see #getAll(BoundingBox, String, Handler, int, int)
	 */
	public void getAll(BoundingBox bounds, Handler<Note> handler, int limit, int hideClosedNoteAfter)
	{
		getAll(bounds, null, handler, limit, hideClosedNoteAfter);
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
	public void getAll(BoundingBox bounds, String search, Handler<Note> handler, int limit,
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

		final String call = NOTES + "?bbox=" + bounds.getAsLeftBottomRightTopString() +
				searchQuery + "&limit=" + limit + "&closed=" + hideClosedNoteAfter;

		try
		{
			boolean authenticate = osm.getOAuth() != null;
			osm.makeRequest(call, authenticate, new NotesParser(handler));
		}
		catch(OsmBadUserInputException e)
		{
			// we can be more specific here
			throw new OsmQueryTooBigException(e);
		}
	}

	/**
	 * Get a number of notes that match the given filters.
	 *
	 * @param handler The handler which is fed the incoming notes
	 * @param filters what to search for. E.g.
	 *                new QueryNotesFilters().byUser(123).limit(1000)
	 *
	 * @throws OsmQueryTooBigException if the bounds area is too large
	 */
	public void find(Handler<Note> handler, QueryNotesFilters filters)
	{
		String query = filters != null ? "?" + filters.toParamString() : "";
		boolean authenticate = osm.getOAuth() != null;
		try {
			osm.makeRequest(NOTES + "/search" + query, authenticate, new NotesParser(handler));
		}
		catch(OsmBadUserInputException e)
		{
			// we can be more specific here
			throw new OsmQueryTooBigException(e);
		}
	}

	private <T> T makeSingleNoteRequest(long id, String call, String text, ApiResponseReader<T> reader)
	{
		String data = "";
		if(text != null)
		{
			data = "?text=" + urlEncode(text);
		}
		String apiCall = NOTES + "/" + id + "/" + call + data;
		return osm.makeAuthenticatedRequest(apiCall, "POST", reader);
	}

	private String urlEncode(String text)
	{
		try
		{
			return URLEncoder.encode(text, OsmConnection.CHARSET);
		}
		catch (UnsupportedEncodingException e)
		{
			// should never happen since we use UTF-8
			throw new RuntimeException(e);
		}
	}
}
