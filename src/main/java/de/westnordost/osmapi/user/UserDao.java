package de.westnordost.osmapi.user;

import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.common.errors.OsmNotFoundException;
import de.westnordost.osmapi.common.errors.OsmAuthorizationException;

/** Get user infos and details */
public class UserDao
{
	private final OsmConnection osm;

	public UserDao(OsmConnection osm)
	{
		this.osm = osm;
	}

	/** @return the user info of the current user
	 *  @throws OsmAuthorizationException if the user does not have the permission
	 *                                     Permission.READ_PREFERENCES_AND_USER_DETAILS*/
	public UserDetails getMine()
	{
		return (UserDetails) osm.makeAuthenticatedRequest("user/details", null, new UserDetailsParser());
	}

	/**
	 * @throws OsmAuthorizationException if not logged in
	 * @return the user info of the given user. Null if the user does not exist.
	 *  */
	public UserInfo get(long userId)
	{
		try
		{
			return osm.makeAuthenticatedRequest("user/" + userId, null, new UserInfoParser());
		}
		catch(OsmNotFoundException e)
		{
			return null;
		}
	}
}
