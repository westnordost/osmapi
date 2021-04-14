package de.westnordost.osmapi.map;

import java.time.Instant;
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
	/** Create a node from the given data.
	 *  @param id id of the node
	 *  @param version version of the node
	 *  @param lat latitude position of the node
	 *  @param lon longitude position of the node
	 *  @param tags tags of the node. May be null.
	 *  @param changeset changeset in which the node was last updated. May be null.
	 *  @param editedAt time at which the node was last updated. May be null.
	 *  @return the node */
	Node createNode(long id, int version, Double lat, Double lon, Map<String,String> tags,
			Changeset changeset, Instant editedAt);

	/** Create a way from the given data
	 * @param id id of the wy
	 * @param version version of the way
	 * @param nodes list of nodes this way consists of
	 * @param tags tags of the way. May be null.
	 * @param changeset changeset in which the way was last updated. May be null.
	 * @param editedAt time at which the way was last updated. May be null.
	 * @return the way
	 */
	Way createWay(long id, int version, List<Long> nodes, Map<String,String> tags, 
			Changeset changeset, Instant editedAt);
	
	/** Create a relation from the given data
	 * @param id id of the relation
	 * @param version version of the relation
	 * @param members list of members this relation consists of
	 * @param tags tags of the relation. May be null.
	 * @param changeset changeset in which the relation was last updated. May be null.
	 * @param editedAt time at which the relation was last updated. May be null.
	 * @return the relation */
	Relation createRelation(long id, int version, List<RelationMember> members, 
			Map<String,String> tags, Changeset changeset, Instant editedAt);
	
	/** Create a relation member from the given data
	 * @param ref id of the member node, way or relation
	 * @param role role of the member
	 * @param type element type of the member, either node, way or relation
	 * @return the relation member */
	RelationMember createRelationMember(long ref, String role, Element.Type type);
}
