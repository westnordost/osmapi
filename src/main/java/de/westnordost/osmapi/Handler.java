package de.westnordost.osmapi;

public interface Handler<T>
{
	/** Called when a new object is created from the input stream */
	void handle(T tea);
}
