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

	void setDeleted(boolean deleted);

	long getId();

	int getVersion();

	Changeset getChangeset();

	Map<String, String> getTags();

	void setTags(Map<String, String> tags);

	Type getType();
}
