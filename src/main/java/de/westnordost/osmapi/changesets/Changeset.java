package de.westnordost.osmapi.changesets;

import java.util.Date;

import de.westnordost.osmapi.user.User;

/**
 * Short info for a changeset.
 */
public class Changeset
{
	public long id;
	/**
	 * the date of the changeset. (It is deliberately unspecified whether this is the creation or 
	 * closed date, see note below)
	 *
	 * <p>Note that the OSM API does actually not give out this information per changeset but for each
	 * element last edited as committing a changeset in OSM is not an atomic operation. In theory,
	 * a changeset can be open for up to 24 hours (after which it is automatically closed).
	 * In practice, changesets created by popular editors are open a few seconds to minutes.</p>
	 */
	public Date date;
	public User user;
}
