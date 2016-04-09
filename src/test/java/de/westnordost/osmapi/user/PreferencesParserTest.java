package de.westnordost.osmapi.user;

import junit.framework.TestCase;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import de.westnordost.osmapi.xml.XmlTestUtils;

public class PreferencesParserTest extends TestCase
{
	public void testPreferencesParser() throws UnsupportedEncodingException
	{
		String xml =
				"<preferences>" +
				"	<preference k=\"gps.trace.visibility\" v=\"identifiable\"/>" +
				"	<preference k=\"something.else\" v=\"true\"/>" +
				"</preferences>";

		Map<String,String> preferences = new PreferencesParser().parse(XmlTestUtils.asInputStream(xml));
		assertEquals("identifiable", preferences.get("gps.trace.visibility"));
		assertEquals("true",preferences.get("something.else"));
	}
}
