package de.westnordost.osmapi.map.data;

import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.changesets.Changeset;

public class OsmRelation extends OsmElement implements Relation
{
	private ModificationAwareList<RelationMember> members;

	public OsmRelation(long id, int version, List<RelationMember> members,
					   Map<String, String> tags, Changeset changeset)
	{
		super(id, version, tags, changeset);
		this.members = new ModificationAwareList<>(members);
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
