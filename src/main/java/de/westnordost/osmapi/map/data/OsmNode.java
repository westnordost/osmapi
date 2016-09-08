package de.westnordost.osmapi.map.data;

import de.westnordost.osmapi.changesets.Changeset;

import java.io.Serializable;
import java.util.Map;

public class OsmNode extends OsmElement implements Node, Serializable
{
	private static final long serialVersionUID = 1L;

	private boolean modified;
	private LatLon pos;

	public OsmNode(long id, int version, Double lat, Double lon,
			   Map<String, String> tags, Changeset changeset)
{
	super(id, version, tags, changeset);
	if (lat != null && lon != null) {
		this.pos = new OsmLatLon(lat, lon);
	}
}
	
	public OsmNode(long id, int version, LatLon pos,
				   Map<String, String> tags, Changeset changeset)
	{
		super(id, version, tags, changeset);
		this.pos = pos;
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
