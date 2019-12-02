package de.westnordost.osmapi.map.changes;

import de.westnordost.osmapi.map.handler.MapDataHandler;

public interface MapDataChangesHandler extends MapDataHandler
{
	void onStartCreations();
	void onStartModifications();
	void onStartDeletions();
}
