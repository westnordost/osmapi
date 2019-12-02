package de.westnordost.osmapi.notes;

import java.io.Serializable;
import java.util.Date;

import de.westnordost.osmapi.user.User;

/** A note comment from the osm notes api */
public class NoteComment implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public Date date;
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
