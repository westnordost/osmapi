package de.westnordost.osmapi.user;

/** Short info for a user */
public class User
{
	public long id;
	public String displayName;
	
	public User(long id, String displayName)
	{
		this.id = id;
		this.displayName = displayName;
	}
}
