package de.westnordost.osmapi.map.handler;

import de.westnordost.osmapi.map.data.Bounds;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.Way;

/** Handles one OsmElement type (Node, Way, Relation, Bounds) and ignores everything else */
public abstract class OneElementTypeHandler<T> implements MapDataHandler
{
	private Class<T> tClass;

	public OneElementTypeHandler(Class<T> tClass)
	{
		this.tClass = tClass;
	}

	@Override
	public void handle(Bounds bounds)
	{
		if(tClass.isAssignableFrom(bounds.getClass())) handleElement((T) bounds);
	}

	@Override
	public void handle(Node node)
	{
		if(tClass.isAssignableFrom(node.getClass())) handleElement((T) node);
	}

	@Override
	public void handle(Way way)
	{
		if(tClass.isAssignableFrom(way.getClass())) handleElement((T) way);
	}

	@Override
	public void handle(Relation relation)
	{
		if(tClass.isAssignableFrom(relation.getClass())) handleElement((T) relation);
	}

	protected abstract void handleElement(T element);
}