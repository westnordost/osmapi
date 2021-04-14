package de.westnordost.osmapi.changesets;

import java.io.Serializable;

import de.westnordost.osmapi.user.User;

/**
 * Short info for a changeset.
 */
public class Changeset implements Serializable
{
	private static final long serialVersionUID = 2L;
	
	public long id;
	public User user;
}
