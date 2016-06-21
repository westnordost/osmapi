package de.westnordost.osmapi.map;

import de.westnordost.osmapi.changesets.Changeset;
import de.westnordost.osmapi.map.data.*;

import java.util.List;
import java.util.Map;

public interface MapDataFactory
{	
	/** Create a node from the given data */
	Node createNode(long id, int version, Double lat, Double lon, Map<String,String> tags,
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
