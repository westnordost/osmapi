package de.westnordost.osmapi.changesets;


import java.util.Date;

import de.westnordost.osmapi.user.User;

public class ChangesetComment
{
	private Date date;
	private User user;
	private String text;

	public ChangesetComment(User user, Date date, String text)
	{
		this(user, date);
		this.text = text;
	}

	ChangesetComment(User user, Date date)
	{
		this.user = user;
		this.date = date;
	}

	void setText(String text)
	{
		this.text = text;
	}

	public Date getDate()
	{
		return date;
	}

	public User getUser()
	{
		return user;
	}

	public String getText()
	{
		return text;
	}
}
