package de.westnordost.osmapi.notes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.westnordost.osmapi.map.data.LatLon;

/** A note from the osm notes API */
public class Note implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public LatLon position;

	public long id;
	public Date dateCreated;
	/** the date the note was closed. May be null if the note is not closed. */
	public Date dateClosed;
	public Status status;
	public List<NoteComment> comments = new ArrayList<>();

	public boolean isOpen()
	{
		return status == Status.OPEN;
	}
	
	public boolean isClosed()
	{
		return status == Status.CLOSED;
	}

	public boolean isHidden()
	{
		return status == Status.HIDDEN;
	}
	
	public enum Status
	{
		OPEN,
		CLOSED,
		HIDDEN
	}
}
