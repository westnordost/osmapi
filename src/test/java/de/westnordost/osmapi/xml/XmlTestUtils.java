package de.westnordost.osmapi.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class XmlTestUtils
{
	public static InputStream asInputStream(String xml)
	{
		try
		{
			return new ByteArrayInputStream(xml.getBytes("UTF-8"));
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
			return new String(out.toByteArray(), "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			// doesn't know UTF8? Why is this even a checked exception?
			assert false;
		}
		return null;
	}
}
