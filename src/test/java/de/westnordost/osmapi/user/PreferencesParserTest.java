package de.westnordost.osmapi.user;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.Map;

import de.westnordost.osmapi.TestUtils;

public class PreferencesParserTest extends TestCase
{
	public void testPreferencesParser() throws IOException
	{
		String xml =
				"<preferences>" +
				"	<preference k=\"gps.trace.visibility\" v=\"identifiable\"/>" +
				"	<preference k=\"something.else\" v=\"true\"/>" +
				"</preferences>";

		Map<String,String> preferences = new PreferencesParser().parse(TestUtils.asInputStream(xml));
		assertEquals("identifiable", preferences.get("gps.trace.visibility"));
		assertEquals("true",preferences.get("something.else"));
	}
}
