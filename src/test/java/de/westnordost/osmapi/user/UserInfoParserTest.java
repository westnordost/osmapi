package de.westnordost.osmapi.user;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import de.westnordost.osmapi.TestUtils;

public class UserInfoParserTest extends TestCase
{
	public void testNoInput() throws IOException
	{
		UserInfo user = new UserInfoParser().parse(TestUtils.asInputStream(""));
		assertNull(user);
	}

	public void testAttributes()
	{
		String xml =
				"<user id=\"123\" display_name=\"mr_x\" account_created=\"2013-01-20T17:16:23Z\">" +
				"</user>";

		UserInfo user = parse(xml);
		assertEquals(123, user.id);
		assertEquals("mr_x", user.displayName);

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
		c.set(2013, Calendar.JANUARY, 20, 17, 16, 23);
		assertEquals(c.getTimeInMillis() / 1000, user.createdDate.getTime() / 1000);
	}
	
	public void testOptionalElements()
	{
		String xml =
				"<user id=\"0\" display_name=\"\" account_created=\"2013-01-20T17:16:23Z\">" +
				"</user>";

		UserInfo user = parse(xml);
		assertNull(user.profileDescription);
		assertFalse(user.isAdministrator());
		assertFalse(user.isModerator());
		assertFalse(user.hasRole("stuntman"));
		assertEquals(0, user.changesetsCount);
		assertEquals(0, user.gpsTracesCount);
		assertFalse(user.isBlocked);
		assertFalse(user.hasAgreedToContributorTerms);
		assertNull(user.profileImageUrl);
	}

	public void testRoles()
	{
		String xml =
				"<user id=\"0\" display_name=\"\" account_created=\"2013-01-20T17:16:23Z\">" +
						"	<roles>" +
						"		<role>administrator</role>" +
						"		<role>moderator</role>" +
						"		<role>stuntman</role>" +
						"	</roles>" +
						"</user>";

		UserInfo user = parse(xml);

		assertTrue(user.isAdministrator());
		assertTrue(user.isModerator());
		assertTrue(user.hasRole("stuntman"));
		assertFalse(user.hasRole("Stuntman"));
	}

	public void testBlocked()
	{
		String xml =
				"<user id=\"0\" display_name=\"\" account_created=\"2013-01-20T17:16:23Z\">" +
						"	<blocks>" +
						"		<received count=\"12\" active=\"13\" />" +
						"	</blocks>" +
						"</user>";

		UserInfo user = parse(xml);
		assertTrue(user.isBlocked);
	}

	public void testNotBlocked()
	{
		String xml =
				"<user id=\"0\" display_name=\"\" account_created=\"2013-01-20T17:16:23Z\">" +
						"	<blocks>" +
						"		<received count=\"12\" active=\"0\" />" +
						"	</blocks>" +
						"</user>";

		UserInfo user = parse(xml);
		assertFalse(user.isBlocked);
	}

	public void testBasicElements()
	{
		String xml =
				"<user id=\"0\" display_name=\"\" account_created=\"2013-01-20T17:16:23Z\">" +
				"	<description>abc</description>" +
				"	<contributor-terms agreed=\"true\" pd=\"false\"/>" +
				"	<img href=\"http://someurl.com/img.png\"/>" +
				"	<changesets count=\"2129\"/>" +
				"	<home lat=\"16.8151\" lon=\"96.186\" zoom=\"3\"/>" +
				"	<traces count=\"80\"/>" +
				"</user>";

		UserInfo user = parse(xml);
		assertEquals("abc", user.profileDescription);
		assertTrue(user.hasAgreedToContributorTerms);
		assertEquals("http://someurl.com/img.png", user.profileImageUrl);
		assertEquals(2129, user.changesetsCount);
		assertEquals(80, user.gpsTracesCount);
	}
	
	private UserInfo parse(String xml)
	{
		try
		{
			return new UserInfoParser().parse(TestUtils.asInputStream(xml));
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
