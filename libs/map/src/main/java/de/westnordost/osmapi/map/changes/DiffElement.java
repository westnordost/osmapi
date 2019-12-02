package de.westnordost.osmapi.map.changes;

import java.io.Serializable;

import de.westnordost.osmapi.map.data.Element;

public class DiffElement implements Serializable
{
	private static final long serialVersionUID = 1L;

	public Element.Type type;
	/** aka old_id: the (placeholder) id the element had when the client sent it. */
	public long clientId;
	/** aka new_id: the id the element now has on the server. Can be null if it i.e. a deleted element*/
	public Long serverId;
	/** aka new_version: the new version element now has on the server. Can be null if it i.e. a deleted element */
	public Integer serverVersion;
}
