package de.westnordost.osmapi.user;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/** Non-private info of a user queried through the user-API */
public class UserInfo extends User implements Serializable
{
	private static final long serialVersionUID = 1L;

	public UserInfo(long id, String displayName)
	{
		super(id, displayName);
	}
	
	public Date createdDate;
	
	/** aka the number of edits */
	public int changesetsCount;
	public int gpsTracesCount;
	
	/** URL to the user's profile picture. May be null if no profile picture has been chosen. */
	public String profileImageUrl;
	/** The profile description is formatted with markdown. May be null if no description was 
	 * provided. */
	public String profileDescription;
	
	public boolean hasAgreedToContributorTerms;

	/** may be null if the user has no roles */
	public List<String> roles;
	
	/** whether the user is currently blocked (=cannot make any modifications on the map). */
	public boolean isBlocked;
	
	/** whether this user holds the given role. See UserRole for constants for known roles
	 *  and the methods isModerator and isAdministrator */
	public boolean hasRole(String roleName)
	{
		return roles != null && roles.contains(roleName);
	}

	public boolean isModerator()
	{
		return hasRole(UserRole.MODERATOR);
	}

	public boolean isAdministrator()
	{
		return hasRole(UserRole.ADMINISTRATOR);
	}
	
}
