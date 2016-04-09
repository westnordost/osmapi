package de.westnordost.osmapi;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Handles writing data to an API request. Assumed to use the charset UTF-8
 */
public interface ApiRequestWriter
{
	String getContentType();

	void write(OutputStream out) throws IOException;
}
