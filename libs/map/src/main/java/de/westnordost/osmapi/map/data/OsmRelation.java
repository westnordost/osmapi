package de.westnordost.osmapi.map.data;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.changesets.Changeset;

public class OsmRelation extends OsmElement implements Relation, Serializable
{
	private static final long serialVersionUID = 2L;

	private final ModificationAwareList<RelationMember> members;

	public OsmRelation(long id, int version, List<RelationMember> members,
					   Map<String, String> tags, Changeset changeset, Instant editedAt)
	{
		super(id, version, tags, changeset, editedAt);
		this.members = new ModificationAwareList<>(members);
	}

	public OsmRelation(long id, int version, List<RelationMember> members, Map<String, String> tags)
	{
		this(id, version, members, tags, null, null);
	}
	
	@Override
	public List<RelationMember> getMembers()
	{
		return members;
	}

	@Override
	public Type getType()
	{
		return Type.RELATION;
	}

	@Override
	public boolean isModified()
	{
		if(members.isModified()) return true;

		for(RelationMember member : members)
		{
			if(member.isModified())
			{
				return true;
			}
		}
		return super.isModified();
	}
}
