package de.westnordost.osmapi.user;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.common.ListHandler;
import de.westnordost.osmapi.common.SingleElementHandler;
import de.westnordost.osmapi.common.errors.OsmNotFoundException;
import de.westnordost.osmapi.common.errors.OsmAuthorizationException;

/** Get user infos and details.
 *  All interactions with this class require an OsmConnection with a logged in user. */
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
		SingleElementHandler<UserInfo> handler = new SingleElementHandler<>();
		osm.makeAuthenticatedRequest("user/details", null, new UserDetailsParser(handler));
		return (UserDetails) handler.get();
	}

	/**
	 * @param userId id of the user to get the user info for
	 * @throws OsmAuthorizationException if not logged in
	 * @return the user info of the given user. Null if the user does not exist.
	 *  */
	public UserInfo get(long userId)
	{
		try
		{
			SingleElementHandler<UserInfo> handler = new SingleElementHandler<>();
			osm.makeAuthenticatedRequest("user/" + userId, null, new UserInfoParser(handler));
			return handler.get();
		}
		catch(OsmNotFoundException e)
		{
			return null;
		}
	}

	public List<UserInfo> getAll(Collection<Long> userIds)
	{
		if(userIds.isEmpty()) return Collections.emptyList();
		ListHandler<UserInfo> handler = new ListHandler<>();
		osm.makeAuthenticatedRequest("users?users=" + toCommaList(userIds), null, new UserInfoParser(handler));
		return handler.get();
	}

	private static String toCommaList(Iterable<Long> vals)
	{
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for(Long id : vals)
		{
			if(id == null) continue;

			if(first) first = false;
			else      result.append(",");
			result.append(id);
		}
		return result.toString();
	}
}
