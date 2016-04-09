package de.westnordost.osmapi.user;

import java.util.List;

import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.errors.OsmNotFoundException;

/** Get user infos and details */
public class UserDao
{
	private final OsmConnection osm;

	public UserDao(OsmConnection osm)
	{
		this.osm = osm;
	}

	/** @return the user info of the current user
	 *  @throws OsmAuthenticationException if the user does not have the permission
	 *                                     Permission.READ_PREFERENCES_AND_USER_DETAILS*/
	public UserDetails getUserDetails()
	{
		return osm.makeAuthenticatedRequest("user/details", "GET", new UserInfoParser());
	}

	/** @return the user info of the given user. Null if the user does not exist. */
	public UserInfo getUserInfo(long userId)
	{
		try
		{
			return osm.makeRequest("user/" + userId, new UserInfoParser());
		}
		catch(OsmNotFoundException e)
		{
			return null;
		}
	}

	/** @return a list of permissions the user has on this server (=are granted though OAuth). Use
	 *          the constants defined in the Permission, i.e Permission.CHANGE_PREFERENCES */
	public List<String> getUserPermissions()
	{
		return osm.makeAuthenticatedRequest("permissions", "GET", new PermissionsParser());
	}
}
