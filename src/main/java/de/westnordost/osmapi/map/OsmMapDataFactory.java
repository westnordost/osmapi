package de.westnordost.osmapi.map;

import de.westnordost.osmapi.changesets.Changeset;
import de.westnordost.osmapi.map.data.Element.Type;
import de.westnordost.osmapi.map.data.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class OsmMapDataFactory implements MapDataFactory
{
	@Override
	public Node createNode(long id, int version, Double lat, Double lon, Map<String, String> tags,
			Changeset changeset, Date dateEdited)
	{
		return new OsmNode(id, version, lat, lon, tags, changeset, dateEdited);
	}

	@Override
	public Way createWay(long id, int version, List<Long> nodes, Map<String, String> tags,
			Changeset changeset, Date dateEdited)
	{
		return new OsmWay(id, version, nodes, tags, changeset, dateEdited);
	}

	@Override
	public Relation createRelation(long id, int version, List<RelationMember> members,
			Map<String, String> tags, Changeset changeset, Date dateEdited)
	{
		return new OsmRelation(id, version, members, tags, changeset, dateEdited);
	}

	@Override
	public RelationMember createRelationMember(long ref, String role, Type type)
	{
		return new OsmRelationMember( ref, role, type );
	}




}
