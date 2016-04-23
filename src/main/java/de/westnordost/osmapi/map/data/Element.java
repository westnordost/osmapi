package de.westnordost.osmapi.map.data;

import java.util.Map;

import de.westnordost.osmapi.changesets.Changeset;

public interface Element
{
	enum Type
	{
		NODE, WAY, RELATION
	}

	boolean isNew();

	boolean isModified();

	boolean isDeleted();

	long getId();

	int getVersion();

	Changeset getChangeset();

	Map<String, String> getTags();

	Type getType();
}
