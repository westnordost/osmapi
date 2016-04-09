package de.westnordost.osmapi.notes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.westnordost.osmapi.map.data.LatLon;

/** A note from the osm notes API. A Note is immutable (from outside the package) */
public class Note
{
	private LatLon pos;

	private long id;
	private Date dateCreated;
	private Date dateClosed;
	private Status status;
	private List<NoteComment> comments;

	Note(LatLon pos)
	{
		this.pos = pos;
		comments = new ArrayList<>();
	}

	public Note(LatLon pos, long id, Status status, Date dateCreated,
				Date dateClosed, List<NoteComment> comments )
	{
		this.pos = pos;
		this.id = id;
		this.dateCreated = dateCreated;
		this.status = status;
		this.comments = comments;
		this.dateClosed = dateClosed;
	}

	public LatLon getPosition()
	{
		return pos;
	}

	public long getId()
	{
		return id;
	}

	public Date getDateCreated()
	{
		return dateCreated;
	}

	/** @return the date the note was closed. May be null if the note is not closed. */
	public Date getDateClosed()
	{
		return dateClosed;
	}

	public Status getStatus()
	{
		return status;
	}

	public boolean isOpen()
	{
		return status == Status.OPEN;
	}

	public List<NoteComment> getComments()
	{
		return Collections.unmodifiableList(comments);
	}

	void addComment(NoteComment comment)
	{
		comments.add(comment);
	}

	void setStatus(Status status)
	{
		this.status = status;
	}

	void setDateClosed(Date dateClosed)
	{
		this.dateClosed = dateClosed;
	}

	void setId(long id)
	{
		this.id = id;
	}

	void setDateCreated(Date dateCreated)
	{
		this.dateCreated = dateCreated;
	}

	public enum Status {
		OPEN,
		CLOSED
	}
}
