package de.westnordost.osmapi.changesets;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.map.data.Bounds;
import de.westnordost.osmapi.user.User;

public class OsmChangesetInfo extends OsmChangeset implements ChangesetInfo
{
	private Map<String, String> tags;
	private List<ChangesetComment> comments;
	private int commentsCount;
	private Bounds bounds;
	private boolean open;
	private Date closedDate;

	public OsmChangesetInfo(
			long id, Date creationDate, Date closedDate, User user, Bounds bounds, boolean open, int commentsCount)
	{
		super(id, creationDate, user);
		this.closedDate = closedDate;
		this.open = open;
		this.bounds = bounds;
		this.commentsCount = commentsCount;
	}

	void setComments(List<ChangesetComment> comments)
	{
		this.comments = comments;
	}

	void setTags(Map<String,String> tags)
	{
		this.tags = tags;
	}

	@Override
	public boolean isOpen()
	{
		return open;
	}

	@Override
	public Bounds getBounds()
	{
		return bounds;
	}

	@Override
	public Map<String, String> getTags()
	{
		return tags;
	}

	@Override
	public String getChangesetComment()
	{
		return tags != null ? tags.get("comment") : null;
	}

	@Override
	public String[] getSources()
	{
		String source = tags != null ? tags.get("source") : null;
		return source != null ? source.split("(\\s)?;(\\s)?") : null;
	}

	@Override
	public String getGenerator()
	{
		return tags != null ? tags.get("created_by") : null;
	}

	@Override
	public List<ChangesetComment> getDiscussion()
	{
		return comments != null ? Collections.unmodifiableList(comments) : null;
	}

	public int getCommentsCount()
	{
		return commentsCount;
	}

	@Override
	public Date getDateClosed()
	{
		return closedDate;
	}

	@Override
	public Date getDateCreated()
	{
		return getDate();
	}
}
