package de.westnordost.osmapi.common;

import java.io.IOException;
import java.io.OutputStream;

import de.westnordost.osmapi.ApiRequestWriter;

public class PlainTextWriter implements ApiRequestWriter
{
	private static final String CHARSET = "UTF-8";
	
	private String data;

	public PlainTextWriter(String data)
	{
		this.data = data;
	}

	@Override
	public String getContentType()
	{
		return "text/plain";
	}

	public void write(OutputStream out) throws IOException
	{
		out.write( data.getBytes(CHARSET) );
	}
}
