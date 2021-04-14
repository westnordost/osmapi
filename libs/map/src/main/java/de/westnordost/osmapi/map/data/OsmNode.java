package de.westnordost.osmapi.map.data;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

import de.westnordost.osmapi.changesets.Changeset;

public class OsmNode extends OsmElement implements Node, Serializable
{
	private static final long serialVersionUID = 2L;

	private boolean modified;
	private LatLon pos;

	public OsmNode(long id, int version, Double lat, Double lon,
			   Map<String, String> tags, Changeset changeset, Instant editedAt)
	{
		super(id, version, tags, changeset, editedAt);
		if (lat != null && lon != null) {
			this.pos = new OsmLatLon(lat, lon);
		}
	}
	
	public OsmNode(long id, int version, LatLon pos,
				   Map<String, String> tags, Changeset changeset, Instant editedAt)
	{
		super(id, version, tags, changeset, editedAt);
		this.pos = pos;
	}

	// convenience constructors
	
	public OsmNode(long id, int version, Double lat, Double lon, Map<String, String> tags)
	{
		this(id, version, lat, lon, tags, null, null);
	}
	
	public OsmNode(long id, int version, LatLon pos, Map<String, String> tags)
	{
		this(id, version, pos, tags, null, null);
	}
	
	@Override
	public LatLon getPosition()
	{
		return pos;
	}

	public void setPosition(LatLon pos)
	{
		this.pos = pos;
		modified = true;
	}

	@Override
	public boolean isModified()
	{
		return modified || super.isModified();
	}

	@Override
	public Type getType()
	{
		return Type.NODE;
	}
}
