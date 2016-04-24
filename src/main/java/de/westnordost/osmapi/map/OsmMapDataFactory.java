package de.westnordost.osmapi.map;

import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.changesets.Changeset;
import de.westnordost.osmapi.map.data.Element.Type;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.OsmNode;
import de.westnordost.osmapi.map.data.OsmRelation;
import de.westnordost.osmapi.map.data.OsmRelationMember;
import de.westnordost.osmapi.map.data.OsmWay;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;
import de.westnordost.osmapi.map.data.Way;

public class OsmMapDataFactory implements MapDataFactory
{
	@Override
	public Node createNode(long id, int version, double lat, double lon, Map<String, String> tags,
			Changeset changeset)
	{
		return new OsmNode(id, version, lat, lon, tags, changeset);
	}

	@Override
	public Way createWay(long id, int version, List<Long> nodes, Map<String, String> tags,
			Changeset changeset)
	{
		return new OsmWay(id, version, nodes, tags, changeset);
	}

	@Override
	public Relation createRelation(long id, int version, List<RelationMember> members,
			Map<String, String> tags, Changeset changeset)
	{
		return new OsmRelation(id, version, members, tags, changeset);
	}

	@Override
	public RelationMember createRelationMember(long ref, String role, Type type)
	{
		return new OsmRelationMember( ref, role, type );
	}




}
