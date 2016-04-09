package de.westnordost.osmapi.user;

import java.util.Date;

/** Info of a user queried through the user-API */
public interface UserInfo extends User
{
	Date getCreatedDate();

	/** aka the number of edits */
	int getChangesetsCount();

	int getGpsTracesUploadedCount();

	/** @return the URL to the image chosen as the profile picture. May be null if no profile
	 *          picture has been chosen. */
	String getProfileImageUrl();

	/** @return the profile description. It is formatted with markdown. May be null if no
	 *          description was provided. */
	String getProfileDescription();

	boolean hasAgreedToContributorTerms();

	/** @return whether this user holds the given role. See UserRole for constants for known roles
	 *  and the methods isModerator and isAdministrator */
	boolean hasRole(String roleName);

	boolean isModerator();

	boolean isAdministrator();

	/** @return whether the user is currently blocked (=cannot make any modifications on the map). */
	boolean isBlocked();
}
