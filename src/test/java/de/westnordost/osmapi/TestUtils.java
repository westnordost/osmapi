package de.westnordost.osmapi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class TestUtils
{
	private static final String CHARSET = "UTF-8";
	
	public static InputStream asInputStream(String str)
	{
		try
		{
			return new ByteArrayInputStream(str.getBytes(CHARSET));
		}
		catch (UnsupportedEncodingException e)
		{
			// doesn't know UTF8? Why is this even a checked exception?
			assert false;
		}
		return null;
	}
	
	public static String asString(ByteArrayOutputStream out)
	{
		try
		{
			return new String(out.toByteArray(), CHARSET);
		}
		catch (UnsupportedEncodingException e)
		{
			// doesn't know UTF8? Why is this even a checked exception?
			assert false;
		}
		return null;
	}
}
