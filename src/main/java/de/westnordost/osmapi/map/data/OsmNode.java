package de.westnordost.osmapi.map.data;

import java.util.Map;

import de.westnordost.osmapi.changesets.Changeset;

public class OsmNode extends OsmElement implements Node
{
	private boolean modified;
	private LatLon pos;

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
