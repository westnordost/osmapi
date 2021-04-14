package de.westnordost.osmapi.notes;

import java.io.Serializable;
import java.time.Instant;

import de.westnordost.osmapi.user.User;

/** A note comment from the osm notes api */
public class NoteComment implements Serializable
{
	private static final long serialVersionUID = 2L;
	
	public Instant date;
	public Action action;
	public String text;
	/** the user who wrote the comment. May be null if the user is anonymous */
	public User user;

	public boolean isAnonymous()
	{
		return user == null;
	}

	public enum Action
	{
		OPENED,
		COMMENTED,
		CLOSED,
		REOPENED,
		HIDDEN
	}
}
