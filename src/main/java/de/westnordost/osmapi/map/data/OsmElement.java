package de.westnordost.osmapi.map.data;

import java.io.Serializable;
import java.util.Map;

import de.westnordost.osmapi.changesets.Changeset;

/**
 * Base class for the osm primitives nodes, ways and relations
 */
public abstract class OsmElement implements Element, Serializable
{
	private static final long serialVersionUID = 1L;
	
	private long id;
	private int version;
	private Changeset changeset;
	private OsmTags tags;
	private boolean deleted;
	private boolean modified;

	public OsmElement(long id, int version, Map<String,String> tags, Changeset changeset)
	{
		this.id = id;
		this.version = version;
		this.changeset = changeset;
		this.tags = tags != null ? new OsmTags(tags) : null;
	}

	@Override
	public long getId()
	{
		return id;
	}

	@Override
	public Changeset getChangeset()
	{
		return changeset;
	}

	@Override
	public int getVersion()
	{
		return version;
	}
	
	@Override
	public Map<String, String> getTags()
	{
		return tags;
	}

	public void setTags(Map<String, String> tags)
	{
		modified = true;
		this.tags = tags != null ? new OsmTags(tags) : null;
	}

	@Override
	public boolean isNew()
	{
		return id < 0;
	}

	@Override
	public boolean isModified()
	{
		return modified || tags != null && tags.isModified();
	}

	@Override
	public boolean isDeleted()
	{
		return deleted;
	}

	public void setDeleted(boolean deleted)
	{
		this.deleted = deleted;
	}

	@Override
	public abstract Type getType();
}
