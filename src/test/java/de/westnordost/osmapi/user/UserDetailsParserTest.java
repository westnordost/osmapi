package de.westnordost.osmapi.user;

import java.io.IOException;
import java.util.List;

import de.westnordost.osmapi.TestUtils;
import de.westnordost.osmapi.common.SingleElementHandler;

import junit.framework.TestCase;

public class UserDetailsParserTest extends TestCase
{
	public void testOptionalElements()
	{
		String xml =
				"<user id=\"0\" display_name=\"\" account_created=\"2013-01-20T17:16:23Z\">" +
				"</user>";

		UserDetails user = parseOneDetails(xml);
		assertFalse(user.considersHisContributionsAsPublicDomain);
		assertNull(user.homeLocation);
		assertNull(user.homeZoom);
		assertNull(user.preferredLanguages);
		assertEquals(0,user.inboxMessageCount);
		assertEquals(0,user.sentMessagesCount);
		assertEquals(0,user.unreadMessagesCount);
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

		UserDetails user = parseOneDetails(xml);
		assertFalse(user.considersHisContributionsAsPublicDomain);
		assertNotNull(user.homeLocation);
		assertEquals(16.8151000, user.homeLocation.getLatitude());
		assertEquals(96.1860000, user.homeLocation.getLongitude());
		assertNotNull(user.homeZoom);
		assertEquals(3, (byte) user.homeZoom);
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

		UserDetails user = parseOneDetails(xml);
		List<String> langs = user.preferredLanguages;
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

		UserDetails user = parseOneDetails(xml);
		assertEquals(24, user.inboxMessageCount);
		assertEquals(1, user.unreadMessagesCount);
		assertEquals(29, user.sentMessagesCount);
	}

	
	private UserDetails parseOneDetails(String xml)
	{
		try
		{
			SingleElementHandler<UserInfo> handler = new SingleElementHandler<>();
			new UserDetailsParser(handler).parse(TestUtils.asInputStream(xml));
			return (UserDetails) handler.get();
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
