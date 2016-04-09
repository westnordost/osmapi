package de.westnordost.osmapi.map.handler;

import de.westnordost.osmapi.map.data.Bounds;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.Way;

/**
 * Empty implementation of the map data handler
 */
public class DefaultMapDataHandler implements MapDataHandler
{
	@Override
	public void handle(Bounds bounds)	{}

	@Override
	public void handle(Node node) {}

	@Override
	public void handle(Way way) {}

	@Override
	public void handle(Relation relation) {}
}
