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
		checkRoleLength(role);
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
		checkRoleLength(newRole);

		if(!role.equals(newRole))
		{
			modified = true;
			role = newRole;
		}
	}

	private void checkRoleLength(String r)
	{
		if(r.length() >= 256)
		{
			throw new IllegalArgumentException("Role \"" + r + "\": Role length is limited" +
					"to less than 256 characters.");
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
		if(other == this) return true;
		if(other == null || !(other instanceof RelationMember)) return false;

		RelationMember otherMember = (RelationMember) other;
		return
				getRole().equals(otherMember.getRole())	&&
				getRef() == otherMember.getRef() &&
				getType() == otherMember.getType();
	}
	
	@Override
	public int hashCode()
	{
		int result = 11;
		result = 31 * result + role.hashCode();
		result = 31 * result + type.ordinal();
		result = 31 * result + Long.hashCode(ref);
		return result;
	}
}
