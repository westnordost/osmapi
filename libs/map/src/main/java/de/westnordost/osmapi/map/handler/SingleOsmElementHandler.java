package de.westnordost.osmapi.map.handler;

/** Handler that expects just one element of the given type */
public class SingleOsmElementHandler<T> extends OneElementTypeHandler<T>
{
	private T element = null;

	public SingleOsmElementHandler(Class<T> tClass)
	{
		super(tClass);
	}

	@Override
	protected void handleElement(T element)
	{
		this.element = element;
	}

	public T get()
	{
		return element;
	}
}