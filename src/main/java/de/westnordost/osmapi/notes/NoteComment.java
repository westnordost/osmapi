package de.westnordost.osmapi.notes;

import java.util.Date;

import de.westnordost.osmapi.user.User;

/** A note comment from the osm notes api */
public class NoteComment
{
	private Date date;
	private Action action;
	private String text;
	private User user;

	public NoteComment(Date date, Action action, String text, User user)
	{
		this.date = date;
		this.action = action;
		this.text = text;
		this.user = user;
	}

	NoteComment()
	{

	}

	/** @return the user who wrote the comment. May be null if the user is anonymous */
	public User getUser()
	{
		return user;
	}

	void setUser(User user)
	{
		this.user = user;
	}

	public boolean isAnonymous()
	{
		return user == null;
	}

	public Date getDate()
	{
		return date;
	}

	public Action getAction()
	{
		return action;
	}

	public String getText()
	{
		return text;
	}

	void setText(String text)
	{
		this.text = text;
	}

	void setAction(Action action)
	{
		this.action = action;
	}

	void setDate(Date date)
	{
		this.date = date;
	}

	public enum Action
	{
		OPENED,
		COMMENTED,
		CLOSED,
		REOPENED
	}


}
