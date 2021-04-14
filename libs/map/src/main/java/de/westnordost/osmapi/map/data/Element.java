package de.westnordost.osmapi.map.data;

import java.time.Instant;
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
	
	Instant getEditedAt();

	Map<String, String> getTags();

	Type getType();
}
