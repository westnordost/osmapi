package de.westnordost.osmapi.changesets;

import java.io.Serializable;
import java.util.Date;

import de.westnordost.osmapi.user.User;

/**
 * Short info for a changeset.
 */
public class Changeset implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public long id;
	/**
	 * the date of the changeset. (It is deliberately unspecified whether this is the creation or 
	 * closed date, see note below)
	 *
	 * <p>Note that the OSM API does actually not give out this information per changeset but for each
	 * element last edited as committing a changeset in OSM is not an atomic operation. For 
	 * convenience, an edited date of an element included in the changeset is put here additionally.
	 * (In an element, this is the dateEdited property.)</p>
	 */
	public Date date;
	public User user;
}
