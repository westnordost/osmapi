package de.westnordost.osmapi.changesets;


import java.util.Date;

import de.westnordost.osmapi.user.User;

/** A post in the changeset discussion. Avoided the wording "changeset comment" here, because this
 *  is already what the "commit message" is called in editors */
public class ChangesetNote
{
	public Date date;
	public User user;
	public String text;
}
