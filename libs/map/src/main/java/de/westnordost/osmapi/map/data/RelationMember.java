package de.westnordost.osmapi.map.data;

public interface RelationMember
{
	String getRole();

	/** @return id of the element this object refers to */
	long getRef();

	Element.Type getType();

	boolean isModified();
}
