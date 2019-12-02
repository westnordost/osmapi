package de.westnordost.osmapi.map.handler;

import java.util.ArrayList;
import java.util.List;

/** Handler that expects a number of elements of the given type */
public class ListOsmElementHandler<T> extends OneElementTypeHandler<T>
{
	private List<T> result = new ArrayList<>();

	public ListOsmElementHandler(Class<T> tClass)
	{
		super(tClass);
	}

	@Override
	protected void handleElement(T element)
	{
		result.add(element);
	}

	public List<T> get()
	{
		return result;
	}
}