package de.westnordost.osmapi.map;

import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.changesets.Changeset;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;
import de.westnordost.osmapi.map.data.Way;

public interface MapDataFactory
{	
	/** Create a node from the given data */
	Node createNode(long id, int version, double lat, double lon, Map<String,String> tags, 
			Changeset changeset);
	
	/** Create a way from the given data */
	Way createWay(long id, int version, List<Long> nodes, Map<String,String> tags, 
			Changeset changeset);
	
	/** Create a relation from the given data */
	Relation createRelation(long id, int version, List<RelationMember> members, 
			Map<String,String> tags, Changeset changeset);
	
	/** Create a relation member from the given data */
	RelationMember createRelationMember(long ref, String role, Element.Type type);
}
