package de.westnordost.osmapi.user;

import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import de.westnordost.osmapi.TestUtils;

import static org.junit.Assert.assertEquals;

public class PreferencesParserTest
{
	@Test public void preferencesParser() throws IOException
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
