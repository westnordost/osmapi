package de.westnordost.osmapi.user;

import java.io.Serializable;

/** Short info for a user */
public class User implements Serializable
{
	private static final long serialVersionUID = 1L;

	public long id;
	public String displayName;
	
	public User(long id, String displayName)
	{
		this.id = id;
		this.displayName = displayName;
	}
}
