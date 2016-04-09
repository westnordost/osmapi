package de.westnordost.osmapi.map.changes;

import java.util.List;

import de.westnordost.osmapi.map.data.Element;

public interface MapDataChanges
{
	List<Element> getDeletions();
	List<Element> getModifications();
	List<Element> getCreations();
	List<Element> getAll();
}
