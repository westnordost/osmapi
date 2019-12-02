package de.westnordost.osmapi.map.handler;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.Way;

/** This class is fed the map data. */
public interface MapDataHandler
{
	void handle(BoundingBox bounds);

	void handle(Node node);
	void handle(Way way);
	void handle(Relation relation);
}
