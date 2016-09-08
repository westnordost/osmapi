package de.westnordost.osmapi.map.data;

import java.io.Serializable;

public class OsmRelationMember implements RelationMember, Serializable
{
	private static final long serialVersionUID = 1L;

	private boolean modified;

	private final long ref;
	private Element.Type type;

	private String role;

	public OsmRelationMember(long ref, String role, Element.Type type)
	{
		this.ref = ref;
		this.role = role;
		this.type = type;
	}

	public long getRef()
	{
		return ref;
	}

	public String getRole()
	{
		return role;
	}

	public Element.Type getType()
	{
		return type;
	}

	public void setRole(String newRole)
	{
		if(newRole.length() >= 256)
		{
			throw new IllegalArgumentException("Role \"" + newRole + "\": Role length is limited" +
					"to less than 256 characters.");
		}

		if(!role.equals(newRole))
		{
			modified = true;
			role = newRole;
		}
	}

	@Override
	public boolean isModified()
	{
		return modified;
	}

	@Override
	public boolean equals(Object other)
	{
		if(other == null) return false;
		if(!(other instanceof RelationMember)) return false;

		RelationMember otherMember = (RelationMember) other;
		return
				getRole().equals(otherMember.getRole())	&&
				getRef() == otherMember.getRef() &&
				getType() == otherMember.getType();
	}
}
