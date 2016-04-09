package de.westnordost.osmapi.user;

/** Short info for a user */
public interface User
{
	long getId();

	/** @return name of the user. Note that the user can change this any time. */
	String getDisplayName();

	void setDisplayName(String displayName);
}
