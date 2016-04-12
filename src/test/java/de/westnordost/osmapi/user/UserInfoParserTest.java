package de.westnordost.osmapi.user;

import junit.framework.TestCase;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.westnordost.osmapi.xml.XmlTestUtils;

public class UserInfoParserTest extends TestCase
{
	public void testNoInput()
	{
		UserDetails user = new UserInfoParser().parse(XmlTestUtils.asInputStream(""));
		assertNull(user);
	}

	public void testAttributes()
	{
		String xml =
				"<user id=\"123\" display_name=\"mr_x\" account_created=\"2013-01-20T17:16:23Z\">" +
				"</user>";

		UserDetails user = new UserInfoParser().parse(XmlTestUtils.asInputStream(xml));
		assertEquals(123, user.getId());
		assertEquals("mr_x", user.getDisplayName());

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
		c.set(2013, Calendar.JANUARY, 20, 17, 16, 23);
		assertEquals(c.getTimeInMillis() / 1000, user.getCreatedDate().getTime() / 1000);
	}

	public void testOptionalElements()
	{
		String xml =
				"<user id=\"0\" display_name=\"\" account_created=\"2013-01-20T17:16:23Z\">" +
				"</user>";

		UserDetails user = new UserInfoParser().parse(XmlTestUtils.asInputStream(xml));
		assertNull(user.getProfileDescription());
		assertFalse(user.getConsidersHisContributionsAsPublicDomain());
		assertFalse(user.isAdministrator());
		assertFalse(user.isModerator());
		assertFalse(user.hasRole("stuntman"));
		assertNull(user.getHomeLocation());
		assertNull(user.getHomeZoom());
		assertNull(user.getPreferredLanguages());
		assertEquals(0,user.getInboxMessageCount());
		assertEquals(0,user.getSentMessagesCount());
		assertEquals(0,user.getUnreadMessagesCount());
		assertEquals(0, user.getChangesetsCount());
		assertEquals(0, user.getGpsTracesUploadedCount());
		assertFalse(user.isBlocked());
		assertFalse(user.hasAgreedToContributorTerms());
		assertNull(user.getProfileImageUrl());
	}

	public void testPreferredLanguages()
	{
		String xml =
				"<user id=\"0\" display_name=\"\" account_created=\"2013-01-20T17:16:23Z\">" +
						"	<languages>" +
						"		<lang>de</lang>" +
						"		<lang>en-US</lang>" +
						"		<lang>en</lang>" +
						"	</languages>" +
						"</user>";

		UserDetails user = new UserInfoParser().parse(XmlTestUtils.asInputStream(xml));
		List<String> langs = user.getPreferredLanguages();
		assertNotNull(langs);
		assertEquals(3, langs.size());
		assertEquals("de", langs.get(0));
		assertEquals("en-US", langs.get(1));
		assertEquals("en", langs.get(2));
	}

	public void testMessages()
	{
		String xml =
				"<user id=\"0\" display_name=\"\" account_created=\"2013-01-20T17:16:23Z\">" +
						"	<messages>" +
						"		<received count=\"24\" unread=\"1\"/>" +
						"		<sent count=\"29\"/>" +
						"	</messages>" +
						"</user>";

		UserDetails user = new UserInfoParser().parse(XmlTestUtils.asInputStream(xml));
		assertNotNull(user.getInboxMessageCount());
		assertEquals(24, (int) user.getInboxMessageCount());
		assertNotNull(user.getUnreadMessagesCount());
		assertEquals(1, (int) user.getUnreadMessagesCount());
		assertNotNull(user.getSentMessagesCount());
		assertEquals(29, (int) user.getSentMessagesCount());
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

		UserDetails user = new UserInfoParser().parse(XmlTestUtils.asInputStream(xml));

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

		UserDetails user = new UserInfoParser().parse(XmlTestUtils.asInputStream(xml));
		assertTrue(user.isBlocked());
	}

	public void testNotBlocked()
	{
		String xml =
				"<user id=\"0\" display_name=\"\" account_created=\"2013-01-20T17:16:23Z\">" +
						"	<blocks>" +
						"		<received count=\"12\" active=\"0\" />" +
						"	</blocks>" +
						"</user>";

		UserDetails user = new UserInfoParser().parse(XmlTestUtils.asInputStream(xml));
		assertFalse(user.isBlocked());
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

		UserDetails user = new UserInfoParser().parse(XmlTestUtils.asInputStream(xml));
		assertEquals("abc", user.getProfileDescription());
		assertTrue(user.hasAgreedToContributorTerms());
		assertNotNull(user.getConsidersHisContributionsAsPublicDomain());
		assertFalse(user.getConsidersHisContributionsAsPublicDomain());
		assertEquals("http://someurl.com/img.png", user.getProfileImageUrl());
		assertEquals(2129, user.getChangesetsCount());
		assertEquals(80, user.getGpsTracesUploadedCount());
		assertNotNull(user.getHomeLocation());
		assertEquals(16.8151000, user.getHomeLocation().getLatitude());
		assertEquals(96.1860000, user.getHomeLocation().getLongitude());
		assertNotNull(user.getHomeZoom());
		assertEquals(3, (byte) user.getHomeZoom());
	}
}
