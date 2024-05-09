package de.westnordost.osmapi.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import de.westnordost.osmapi.ApiRequestWriter;

public class GpxInputStreamWriter implements ApiRequestWriter
{
	private static final String CHARSET = "UTF-8";

	private InputStream gpx;

	public GpxInputStreamWriter(InputStream in)
	{
		this.gpx = in;
	}

	@Override
	public String getContentType()
	{
		return "text/plain";
	}

	public void write(OutputStream out) throws IOException
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(gpx));
		String line;
		while ((line = br.readLine()) != null)
		{
			out.write((line + System.lineSeparator()).getBytes(CHARSET));
		}
	}
}
