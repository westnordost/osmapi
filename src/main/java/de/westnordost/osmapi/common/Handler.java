package de.westnordost.osmapi.common;

public interface Handler<T>
{
	/** Called when a new object is created from the input stream */
	void handle(T tea);
}
