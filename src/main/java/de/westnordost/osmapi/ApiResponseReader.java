package de.westnordost.osmapi;

import java.io.InputStream;

/**
 * Handles the response from the API: Parses whatever comes through the input stream and returns
 * a result. Assumed to use the charset UTF-8
 *
 * @param <T> Parsing result type
 */
public interface ApiResponseReader<T>
{
	/** Called when the input stream is available.
	 *
	 * @param in the input stream from the server response
	 * @return the result of the parsing process */
	T parse(InputStream in) throws Exception;
}
