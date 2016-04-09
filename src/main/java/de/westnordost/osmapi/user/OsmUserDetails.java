package de.westnordost.osmapi.user;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.westnordost.osmapi.map.data.LatLon;

public class OsmUserDetails extends OsmUser implements UserDetails
{
	private Date createdDate;

	private String profileImageUrl;
	private String profileDescription;

	private int changesetsCount;
	private int gpsTracesCount;

	private boolean contributorTermsAgreed;
	private boolean considersHisContributionsAsPublicDomain;

	private Byte homeZoom;
	private LatLon homeLocation;

	private List<String> preferredLanguages;

	private int inboxMessageCount;
	private int unreadMessagesCount;
	private int sentMessagesCount;

	private List<String> roles;

	private boolean isBlocked;

	public OsmUserDetails(long id, String displayName, Date dateCreated)
	{
		super(id, displayName);
		this.createdDate = dateCreated;
	}

	void setHomeLocation(LatLon homeLocation)
	{
		this.homeLocation = homeLocation;
	}

	void setHomeZoom(byte homeZoom)
	{
		this.homeZoom = homeZoom;
	}

	void setIsBlocked(boolean isBlocked)
	{
		this.isBlocked = isBlocked;
	}

	void setPreferredLanguages(List<String> preferredLanguages)
	{
		this.preferredLanguages = preferredLanguages;
	}

	void setProfileImageUrl(String profileImageUrl)
	{
		this.profileImageUrl = profileImageUrl;
	}

	void setRoles(List<String> roles)
	{
		this.roles = roles;
	}

	void setConsidersHisContributionsAsPublicDomain(boolean considersHisContributionsAsPublicDomain)
	{
		this.considersHisContributionsAsPublicDomain = considersHisContributionsAsPublicDomain;
	}

	void setContributorTermsAgreed(boolean contributorTermsAgreed)
	{
		this.contributorTermsAgreed = contributorTermsAgreed;
	}

	void setChangesetsCount(int changesetsCount)
	{
		this.changesetsCount = changesetsCount;
	}

	void setGpsTracesCount(int gpsTracesCount)
	{
		this.gpsTracesCount = gpsTracesCount;
	}

	void setProfileDescription(String profileDescription)
	{
		this.profileDescription = profileDescription;
	}

	void setInboxMessageCount(int inboxMessageCount)
	{
		this.inboxMessageCount = inboxMessageCount;
	}

	void setUnreadMessagesCount(int unreadMessagesCount)
	{
		this.unreadMessagesCount = unreadMessagesCount;
	}

	void setSentMessagesCount(int sentMessagesCount)
	{
		this.sentMessagesCount = sentMessagesCount;
	}

	public Date getCreatedDate()
	{
		return createdDate;
	}

	public int getChangesetsCount()
	{
		return changesetsCount;
	}

	public int getGpsTracesUploadedCount()
	{
		return gpsTracesCount;
	}

	public String getProfileImageUrl()
	{
		return profileImageUrl;
	}

	public String getProfileDescription()
	{
		return profileDescription;
	}

	public boolean hasAgreedToContributorTerms()
	{
		return contributorTermsAgreed;
	}

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

	public boolean isBlocked()
	{
		return isBlocked;
	}

	public int getInboxMessageCount()
	{
		return inboxMessageCount;
	}

	public int getSentMessagesCount()
	{
		return sentMessagesCount;
	}

	public int getUnreadMessagesCount()
	{
		return unreadMessagesCount;
	}

	public boolean getConsidersHisContributionsAsPublicDomain()
	{
		return considersHisContributionsAsPublicDomain;
	}

	public List<String> getPreferredLanguages()
	{
		if(preferredLanguages == null) return null;
		return Collections.unmodifiableList(preferredLanguages);
	}

	public LatLon getHomeLocation()
	{
		return homeLocation;
	}

	public Byte getHomeZoom()
	{
		return homeZoom;
	}
}
