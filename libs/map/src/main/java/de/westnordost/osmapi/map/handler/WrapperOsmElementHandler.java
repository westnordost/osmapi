package de.westnordost.osmapi.map.handler;

import de.westnordost.osmapi.common.Handler;

/** Wraps a Handler in a MapDataHandler */
public class WrapperOsmElementHandler<T> extends OneElementTypeHandler<T>
{
	private Handler<T> handler;

	public WrapperOsmElementHandler(Class<T> tClass, Handler<T> handler)
	{
		super(tClass);
		this.handler = handler;
	}

	@Override
	protected void handleElement(T element)
	{
		handler.handle(element);
	}
}
