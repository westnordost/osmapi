package de.westnordost.osmapi;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/** Parses an id response sent via plain text by the server */
public class IdResponseReader implements ApiResponseReader<Long>
{
	/** size of a stream buffer to accommodate any long value send as text/plain */
	private static final int BUFFER_SIZE = String.valueOf(Long.MAX_VALUE).length();
	
	public Long parse(InputStream in) throws Exception
	{
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(in, "UTF-8"), BUFFER_SIZE
		);
		return Long.parseLong(reader.readLine());
	}
}
