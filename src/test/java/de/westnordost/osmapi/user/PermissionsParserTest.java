package de.westnordost.osmapi.user;

import junit.framework.TestCase;

import java.util.List;

import de.westnordost.osmapi.TestUtils;

public class PermissionsParserTest extends TestCase
{
	public void testPermissionsParser()
	{
		String xml =
				"<permissions>" +
				"	<permission name=\"allow_xyz\" />" +
				"	<permission name=\"allow_abc\" />" +
				"</permissions>";

		List<String> permissions = new PermissionsParser().parse(TestUtils.asInputStream(xml));
		assertTrue(permissions.contains("allow_xyz"));
		assertTrue(permissions.contains("allow_abc"));
		assertFalse(permissions.contains("allow_somethingElse"));
	}
}
