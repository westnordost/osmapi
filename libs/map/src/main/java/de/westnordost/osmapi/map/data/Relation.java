package de.westnordost.osmapi.map.data;

import java.util.List;

public interface Relation extends Element
{
	List<RelationMember> getMembers();
}
