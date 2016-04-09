package de.westnordost.osmapi.user;

public class OsmUser implements User
{
	private final long id;
	private String displayName;

	public OsmUser(long id, String displayName)
	{
		this.id = id;
		this.displayName = displayName;
	}

	@Override
	public long getId()
	{
		return id;
	}

	@Override
	public String getDisplayName()
	{
		return displayName;
	}

	@Override
	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}
}
