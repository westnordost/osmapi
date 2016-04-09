package de.westnordost.osmapi.changesets;

import java.util.Date;

import de.westnordost.osmapi.user.User;

public class OsmChangeset implements Changeset
{
	private long id;
	private Date date;
	private User user;

	public OsmChangeset(long id, Date date, User user)
	{
		this.id = id;
		this.date = date;
		this.user = user;
	}

	@Override
	public long getId()
	{
		return id;
	}

	@Override
	public Date getDate()
	{
		return date;
	}

	@Override
	public User getUser()
	{
		return user;
	}
}
