package de.westnordost.osmapi.xml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class XmlTestUtils
{
	public static InputStream asInputStream(String xml) throws UnsupportedEncodingException
	{
		return new ByteArrayInputStream(xml.getBytes("UTF-8"));
	}
}
