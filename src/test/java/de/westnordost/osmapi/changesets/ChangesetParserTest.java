package de.westnordost.osmapi.changesets;

import junit.framework.TestCase;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import de.westnordost.osmapi.Handler;
import de.westnordost.osmapi.user.User;
import de.westnordost.osmapi.xml.XmlTestUtils;

public class ChangesetParserTest extends TestCase
{
	public void testBasicElements() throws UnsupportedEncodingException
	{
		String xml =
				"<changeset id=\"1654\" user=\"blub\" uid=\"123\" " +
						"created_at=\"2011-03-05T20:29:56Z\" open=\"true\" comments_count=\"0\" />";

		new ChangesetParser(new Handler<ChangesetInfo>()
		{
			@Override
			public void handle(ChangesetInfo changeset)
			{
				assertEquals(1654, changeset.getId());
				assertNotNull(changeset.getUser());
				assertEquals("blub", changeset.getUser().getDisplayName());
				assertEquals(123, changeset.getUser().getId());
				assertEquals(changeset.getDate(), changeset.getDateCreated());
				Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
				c.set(2011, Calendar.MARCH, 5, 20, 29, 56);
				assertEquals(c.getTimeInMillis() / 1000, changeset.getDate().getTime() / 1000);

				assertEquals(true, changeset.isOpen());
				assertEquals(0, changeset.getCommentsCount());

				assertNull(changeset.getBounds());
				assertNull(changeset.getChangesetComment());
				assertNull(changeset.getDateClosed());
				assertNull(changeset.getDiscussion());
				assertNull(changeset.getGenerator());
				assertNull(changeset.getTags());
				assertNull(changeset.getSources());
			}

		}).parse(XmlTestUtils.asInputStream(xml));
	}

	public void testOptionalElements() throws UnsupportedEncodingException
	{
		String xml =
				"<changeset id=\"1654\" user=\"blub\" uid=\"123\" " +
						"created_at=\"2011-03-05T20:29:56Z\" open=\"true\" comments_count=\"0\" "+
						"closed_at=\"2012-03-05T20:29:56Z\" open=\"false\" min_lat=\"42.1114934\" "+
						"min_lon=\"-4.845825\" max_lat=\"42.2143384\" max_lon=\"-4.6259554\">" +
						"<tag k=\"comment\" v=\"dongs\" />" +
						"<tag k=\"created_by\" v=\"dings\" />" +
						"<tag k=\"source\" v=\"dengs; bling ; box\" />" +
				"</changeset>";

		new ChangesetParser(new Handler<ChangesetInfo>()
		{
			@Override
			public void handle(ChangesetInfo changeset)
			{
				Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
				c.set(2012, Calendar.MARCH, 5, 20, 29, 56);
				assertNotNull(changeset.getDateClosed());
				assertEquals(c.getTimeInMillis() / 1000, changeset.getDateClosed().getTime() / 1000);

				assertEquals(false, changeset.isOpen());
				assertEquals(0, changeset.getCommentsCount());

				assertNotNull(changeset.getBounds());

				assertEquals(42.1114934, changeset.getBounds().getMinLatitude());
				assertEquals(-4.8458250, changeset.getBounds().getMinLongitude());
				assertEquals(42.2143384, changeset.getBounds().getMaxLatitude());
				assertEquals(-4.6259554, changeset.getBounds().getMaxLongitude());

				assertEquals("dongs", changeset.getChangesetComment());
				assertEquals("dings", changeset.getGenerator());
				String[] expected = new String[]{"dengs", "bling", "box"};
				assertEquals(expected[0], changeset.getSources()[0]);
				assertEquals(expected[1], changeset.getSources()[1]);
				assertEquals(expected[2], changeset.getSources()[2]);

				assertEquals(3, changeset.getTags().size());
				assertEquals(changeset.getChangesetComment(), changeset.getTags().get("comment"));
				assertEquals(changeset.getGenerator(), changeset.getTags().get("created_by"));
				assertEquals("dengs; bling ; box", changeset.getTags().get("source"));
			}

		}).parse(XmlTestUtils.asInputStream(xml));
	}


	public void testMultipleElements() throws UnsupportedEncodingException
	{
		String xml =
				"<changeset id=\"1654\" user=\"blub\" uid=\"123\" " +
						"created_at=\"2011-03-05T20:29:56Z\" open=\"true\" comments_count=\"0\" />"+
				"<changeset id=\"1655\" user=\"blub\" uid=\"123\" " +
						"created_at=\"2011-03-05T20:29:56Z\" open=\"true\" comments_count=\"0\" />";

		new ChangesetParser(new Handler<ChangesetInfo>()
		{
			private int count = 0;

			@Override
			public void handle(ChangesetInfo changeset)
			{
				assertEquals(1654 + count, changeset.getId());
				count++;
			}
		}).parse(XmlTestUtils.asInputStream(xml));
	}

	public void testComments() throws UnsupportedEncodingException
	{
		String xml =
				"<changeset id=\"1654\" user=\"blub\" uid=\"123\" " +
						"created_at=\"2011-03-05T20:29:56Z\" open=\"true\" comments_count=\"2\" >" +
						"<discussion>" +
						"     <comment date=\"2015-01-01T18:56:48Z\">" +
						"       <text>Did you verify those street names?</text>" +
						"     </comment>" +
						"     <comment date=\"2015-01-01T18:58:03Z\" uid=\"234\" user=\"fred\">" +
						"       <text>sure!</text>" +
						"     </comment>" +
						"</discussion>" +
				"</changeset>";

		new ChangesetParser(new Handler<ChangesetInfo>()
		{
			@Override
			public void handle(ChangesetInfo changeset)
			{
				assertEquals(2, changeset.getCommentsCount());
				assertNotNull(changeset.getDiscussion());
				assertEquals(2, changeset.getDiscussion().size());

				ChangesetComment a = changeset.getDiscussion().get(0);
				assertNull(a.getUser());

				Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
				c.set(2015, Calendar.JANUARY, 1, 18, 56, 48);
				assertEquals(c.getTimeInMillis() / 1000, a.getDate().getTime() / 1000);

				ChangesetComment b = changeset.getDiscussion().get(1);

				assertNotNull(b.getUser());
				assertEquals(234, b.getUser().getId());
				assertEquals("fred", b.getUser().getDisplayName());
				c.set(2015, Calendar.JANUARY, 1, 18, 58, 3);
				assertEquals(c.getTimeInMillis() / 1000, b.getDate().getTime() / 1000);
			}

		}).parse(XmlTestUtils.asInputStream(xml));
	}

	public void testReuseUser() throws UnsupportedEncodingException
	{
		String xml =
				"<changeset id=\"1654\" user=\"blub\" uid=\"123\" " +
						"created_at=\"2011-03-05T20:29:56Z\" open=\"true\" comments_count=\"0\" />"+
						" <changeset id=\"1655\" user=\"bleb\" uid=\"123\" " +
						"created_at=\"2011-03-05T20:29:56Z\" open=\"true\" comments_count=\"1\" >"+
						"<discussion>" +
						"     <comment date=\"2015-01-01T18:56:48Z\" uid=\"123\" user=\"blub\">" +
						"       <text>Yo</text>" +
						"     </comment>" +
						"</discussion>" +
				"</changeset>";

		new ChangesetParser(new Handler<ChangesetInfo>()
		{
			private User mrX;

			@Override
			public void handle(ChangesetInfo changeset)
			{
				if(mrX == null)
				{
					mrX = changeset.getUser();
				}
				assertEquals(mrX, changeset.getUser());
				if(changeset.getDiscussion() != null)
				{
					assertEquals(mrX, changeset.getDiscussion().get(0).getUser());
				}
			}

		}).parse(XmlTestUtils.asInputStream(xml));
	}
}
