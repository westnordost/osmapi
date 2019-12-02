package de.westnordost.osmapi.map.data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.changesets.Changeset;

public class OsmWay extends OsmElement implements Way, Serializable
{
	private static final long serialVersionUID = 1L;

	private ModificationAwareList<Long> nodes;

	public OsmWay(long id, int version, List<Long> nodes,
				  Map<String, String> tags, Changeset changeset, Date dateEdited)
	{
		super(id, version, tags, changeset, dateEdited);
		this.nodes = new ModificationAwareList<>(nodes);
	}

	public OsmWay(long id, int version, List<Long> nodes, Map<String, String> tags)
	{
		this(id, version, nodes, tags, null, null);
	}
	
	@Override
	public boolean isModified()
	{
		return nodes.isModified() || super.isModified();
	}

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
