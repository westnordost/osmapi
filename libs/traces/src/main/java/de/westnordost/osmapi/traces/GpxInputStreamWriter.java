package de.westnordost.osmapi.traces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.westnordost.osmapi.ApiRequestWriter;

public class GpxInputStreamWriter implements ApiRequestWriter
{
	private InputStream gpx;

	public GpxInputStreamWriter(InputStream in)
	{
		this.gpx = in;
	}

	@Override
	public String getContentType()
	{
		return "application/gpx+xml";
	}

	public void write(OutputStream out) throws IOException
	{
		byte[] buffer = new byte[8192];
		int length;
		while ((length = gpx.read(buffer)) != -1) out.write(buffer, 0, length);
	}
}
