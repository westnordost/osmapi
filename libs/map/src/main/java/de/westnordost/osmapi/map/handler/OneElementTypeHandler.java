package de.westnordost.osmapi.map.handler;

import de.westnordost.osmapi.map.data.BoundingBox;
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

	@SuppressWarnings("unchecked")
	@Override
	public void handle(BoundingBox bounds)
	{
		if(tClass.isAssignableFrom(bounds.getClass())) handleElement((T) bounds);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handle(Node node)
	{
		if(tClass.isAssignableFrom(node.getClass())) handleElement((T) node);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handle(Way way)
	{
		if(tClass.isAssignableFrom(way.getClass())) handleElement((T) way);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handle(Relation relation)
	{
		if(tClass.isAssignableFrom(relation.getClass())) handleElement((T) relation);
	}

	protected abstract void handleElement(T element);
}