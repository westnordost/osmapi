package de.westnordost.osmapi.map;

import de.westnordost.osmapi.changesets.Changeset;
import de.westnordost.osmapi.map.data.Element.Type;
import de.westnordost.osmapi.map.data.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class OsmMapDataFactory implements MapDataFactory
{
	@Override
	public Node createNode(long id, int version, Double lat, Double lon, Map<String, String> tags,
			Changeset changeset, Instant editedAt)
	{
		return new OsmNode(id, version, lat, lon, tags, changeset, editedAt);
	}

	@Override
	public Way createWay(long id, int version, List<Long> nodes, Map<String, String> tags,
			Changeset changeset, Instant editedAt)
	{
		return new OsmWay(id, version, nodes, tags, changeset, editedAt);
	}

	@Override
	public Relation createRelation(long id, int version, List<RelationMember> members,
			Map<String, String> tags, Changeset changeset, Instant editedAt)
	{
		return new OsmRelation(id, version, members, tags, changeset, editedAt);
	}

	@Override
	public RelationMember createRelationMember(long ref, String role, Type type)
	{
		return new OsmRelationMember( ref, role, type );
	}
}
