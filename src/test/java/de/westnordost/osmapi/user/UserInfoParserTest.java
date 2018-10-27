package de.westnordost.osmapi.user;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import de.westnordost.osmapi.TestUtils;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.ListHandler;
import de.westnordost.osmapi.common.SingleElementHandler;
import de.westnordost.osmapi.map.handler.ListOsmElementHandler;

public class UserInfoParserTest extends TestCase
{
	public void testNoInput() throws IOException
	{
		new UserInfoParser(new FailIfCalled()).parse(TestUtils.asInputStream(""));
	}

	public void testAttributes()
	{
		String xml =
				"<user id=\"123\" display_name=\"mr_x\" account_created=\"2013-01-20T17:16:23Z\">" +
				"</user>";

		UserInfo user = parseOne(xml);
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

		UserInfo user = parseOne(xml);
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

		UserInfo user = parseOne(xml);

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

		UserInfo user = parseOne(xml);
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

		UserInfo user = parseOne(xml);
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

		UserInfo user = parseOne(xml);
		assertEquals("abc", user.profileDescription);
		assertTrue(user.hasAgreedToContributorTerms);
		assertEquals("http://someurl.com/img.png", user.profileImageUrl);
		assertEquals(2129, user.changesetsCount);
		assertEquals(80, user.gpsTracesCount);
	}

	public void testParseMultiple() throws IOException
	{
		String xml =
				"<user id=\"0\" display_name=\"\" account_created=\"2013-01-20T17:16:23Z\"/>" +
				"<user id=\"1\" display_name=\"\" account_created=\"2015-01-20T17:16:23Z\"/>";

		ListHandler<UserInfo> handler = new ListHandler<>();
		new UserInfoParser(handler).parse(TestUtils.asInputStream(xml));
		assertEquals(2, handler.get().size());
	}

	private UserInfo parseOne(String xml)
	{
		try
		{
			SingleElementHandler<UserInfo> handler = new SingleElementHandler<>();
			new UserInfoParser(handler).parse(TestUtils.asInputStream(xml));
			return handler.get();
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static class FailIfCalled implements Handler<UserInfo>
	{
		@Override
		public void handle(UserInfo tea)
		{
			fail();
		}
	}
}
