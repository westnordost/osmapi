package de.westnordost.osmapi.common;


/** Handler that expects just a single element. It can be queried via get().  */
public class SingleElementHandler<T> implements Handler<T>
{
	private T tea = null;

	@Override
	public synchronized void handle(T tea)
	{
		this.tea = tea;
	}

	/** @return the element */
	public synchronized T get()
	{
		return tea;
	}
}
