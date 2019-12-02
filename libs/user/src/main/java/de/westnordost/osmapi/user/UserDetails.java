package de.westnordost.osmapi.user;

import java.io.Serializable;
import java.util.List;

import de.westnordost.osmapi.map.data.LatLon;

/** OSM user details. The OSM api does not reveal personal information of other users, so this
 *  information is only available to the current user. */
public class UserDetails extends UserInfo implements Serializable
{
	private static final long serialVersionUID = 1L;

	public UserDetails(long id, String displayName)
	{
		super(id, displayName);
	}
	
	public boolean considersHisContributionsAsPublicDomain;

	/** user's self-chosen home zoom level is something between 0-19. Null if not set. */
	public Byte homeZoom;
	/** user's self-chosen home location. Null if not set. */
	public LatLon homeLocation;

	/** the language and country codes of the user's preferred languages, sorted by
	 *  preferedness. The format is i.e. "en-US" or "en" (according to ISO 639-1 and ISO 3166) */
	public List<String> preferredLanguages;

	public int inboxMessageCount;
	public int unreadMessagesCount;
	public int sentMessagesCount;
}
