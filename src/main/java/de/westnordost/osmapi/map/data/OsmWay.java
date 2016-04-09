package de.westnordost.osmapi.map.data;

import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.changesets.Changeset;

public class OsmWay extends OsmElement implements Way
{
	private ModificationAwareList<Long> nodes;

	public OsmWay(long id, int version, List<Long> nodes,
				  Map<String, String> tags, Changeset changeset)
	{
		super(id, version, tags, changeset);
		this.nodes = new ModificationAwareList<>(nodes);
	}

	@Override
	public boolean isModified()
	{
		return nodes.isModified() || super.isModified();
	}

	@Override
	public boolean isClosed()
	{
		return nodes.size() >= 3 && nodes.get(0).equals(nodes.get(nodes.size() - 1));
	}

	@Override
	public List<Long> getNodeIds()
	{
		return nodes;
	}

	@Override
	public Type getType()
	{
		return Type.WAY;
	}

}
