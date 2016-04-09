package de.westnordost.osmapi.user;

import java.util.List;

import de.westnordost.osmapi.map.data.LatLon;

/** OSM user details. The OSM api does not reveal personal information of other users, so this
 *  information is only available to the current user. */
public interface UserDetails extends UserInfo
{
	int getInboxMessageCount();

	int getSentMessagesCount();

	int getUnreadMessagesCount();

	boolean getConsidersHisContributionsAsPublicDomain();

	/** @return the language and country codes of the user's preferred languages, sorted by
	 *  preferedness. The format is i.e. "en-US" or "en" (according to ISO 639-1 and ISO 3166) */
	List<String> getPreferredLanguages();

	/** @return the user's home location. Null if not set. */
	LatLon getHomeLocation();

	/** @return the user's home zoom. Null if not set. */
	Byte getHomeZoom();
}
