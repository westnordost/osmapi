package de.westnordost.osmapi.changesets;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import junit.framework.TestCase;
import de.westnordost.osmapi.TestUtils;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.ListHandler;
import de.westnordost.osmapi.common.SingleElementHandler;

public class ChangesetParserTest extends TestCase
{
	public void testBasicElements()
	{
		String xml =
				"<changeset id=\"1654\" user=\"blub\" uid=\"123\" " +
					"created_at=\"2011-03-05T20:29:56Z\" open=\"true\" comments_count=\"0\" " +
					"changes_count=\"123\" />";

		ChangesetInfo changeset = parseOne(xml);

		assertEquals(1654, changeset.id);
		assertNotNull(changeset.user);
		assertEquals("blub", changeset.user.displayName);
		assertEquals(123, changeset.user.id);
		assertEquals(Instant.parse("2011-03-05T20:29:56Z"), changeset.createdAt);
		assertEquals(true, changeset.isOpen);
		assertEquals(0, changeset.notesCount);
		assertEquals(123, changeset.changesCount);

		assertNull(changeset.boundingBox);
		assertNull(changeset.getChangesetComment());
		assertNull(changeset.closedAt);
		assertNull(changeset.discussion);
		assertNull(changeset.getGenerator());
		assertNull(changeset.tags);
		assertNull(changeset.getSources());
	}

	public void testOptionalElements()
	{
		String xml =
				"<changeset id=\"1654\" user=\"blub\" uid=\"123\" " +
						"created_at=\"2011-03-05T20:29:56Z\" changes_count=\"0\" comments_count=\"0\" "+
						"closed_at=\"2012-03-05T20:29:56Z\" open=\"false\" min_lat=\"42.1114934\" "+
						"min_lon=\"-4.845825\" max_lat=\"42.2143384\" max_lon=\"-4.6259554\">" +
						"<tag k=\"comment\" v=\"dongs\" />" +
						"<tag k=\"created_by\" v=\"dings\" />" +
						"<tag k=\"source\" v=\"dengs; bling ; box\" />" +
				"</changeset>";
		
		ChangesetInfo changeset = parseOne(xml);

		assertNotNull(changeset.closedAt);
		assertEquals(Instant.parse("2012-03-05T20:29:56Z"), changeset.closedAt);

		assertEquals(false, changeset.isOpen);
		assertEquals(0, changeset.notesCount);
		assertEquals(0, changeset.changesCount);

		assertNotNull(changeset.boundingBox);

		assertEquals(42.1114934, changeset.boundingBox.getMinLatitude());
		assertEquals(-4.8458250, changeset.boundingBox.getMinLongitude());
		assertEquals(42.2143384, changeset.boundingBox.getMaxLatitude());
		assertEquals(-4.6259554, changeset.boundingBox.getMaxLongitude());

		assertEquals("dongs", changeset.getChangesetComment());
		assertEquals("dings", changeset.getGenerator());
		String[] expected = new String[]{"dengs", "bling", "box"};
		assertEquals(expected[0], changeset.getSources()[0]);
		assertEquals(expected[1], changeset.getSources()[1]);
		assertEquals(expected[2], changeset.getSources()[2]);

		assertEquals(3, changeset.tags.size());
		assertEquals(changeset.getChangesetComment(), changeset.tags.get("comment"));
		assertEquals(changeset.getGenerator(), changeset.tags.get("created_by"));
		assertEquals("dengs; bling ; box", changeset.tags.get("source"));
	}


	public void testMultipleElements()
	{
		String xml =
				"<changeset id=\"1654\" user=\"blub\" uid=\"123\" changes_count=\"0\" " +
						"created_at=\"2011-03-05T20:29:56Z\" open=\"true\" comments_count=\"0\" />"+
				"<changeset id=\"1655\" user=\"blub\" uid=\"123\" changes_count=\"0\" " +
						"created_at=\"2011-03-05T20:29:56Z\" open=\"true\" comments_count=\"0\" />";

		List<ChangesetInfo> changesets = parseList(xml);
		
		assertEquals(2,changesets.size());
		assertEquals(1654,changesets.get(0).id);
		assertEquals(1655,changesets.get(1).id);
	}

	public void testComments()
	{
		String xml =
				"<changeset id=\"1654\" user=\"blub\" uid=\"123\" changes_count=\"0\" " +
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

		ChangesetInfo changeset = parseOne(xml);
		
		assertEquals(2, changeset.notesCount);
		assertEquals(0, changeset.changesCount);
		assertNotNull(changeset.discussion);
		assertEquals(2, changeset.discussion.size());

		ChangesetNote a = changeset.discussion.get(0);
		assertNull(a.user);
		assertEquals(Instant.parse("2015-01-01T18:56:48Z"), a.createdAt);

		ChangesetNote b = changeset.discussion.get(1);

		assertNotNull(b.user);
		assertEquals(234, b.user.id);
		assertEquals("fred", b.user.displayName);
		assertEquals(Instant.parse("2015-01-01T18:58:03Z"), b.createdAt);
	}

	public void testReuseUser()
	{
		String xml =
				"<changeset id=\"1654\" user=\"blub\" uid=\"123\" changes_count=\"0\"" +
						"created_at=\"2011-03-05T20:29:56Z\" open=\"true\" comments_count=\"0\" />"+
				"<changeset id=\"1655\" user=\"bleb\" uid=\"123\" changes_count=\"0\"" +
						"created_at=\"2011-03-05T20:29:56Z\" open=\"true\" comments_count=\"1\" >"+
						"<discussion>" +
						"     <comment date=\"2015-01-01T18:56:48Z\" uid=\"123\" user=\"blub\">" +
						"       <text>Yo</text>" +
						"     </comment>" +
						"</discussion>" +
				"</changeset>" + 
				"<changeset id=\"1656\" user=\"blub\" uid=\"123\" changes_count=\"0\"" +
						"created_at=\"2011-03-05T20:29:56Z\" open=\"true\" comments_count=\"0\" />";

		List<ChangesetInfo> changesets = parseList(xml);
		
		assertSame(changesets.get(0).user, changesets.get(1).discussion.get(0).user);
		assertSame(changesets.get(0).user, changesets.get(2).user);
	}
	
	private List<ChangesetInfo> parseList(String xml)
	{
		ListHandler<ChangesetInfo> handler = new ListHandler<>();
		parse(xml, handler);
		return handler.get();
	}
	
	private ChangesetInfo parseOne(String xml)
	{
		SingleElementHandler<ChangesetInfo> handler = new SingleElementHandler<>();
		parse(xml, handler);
		return handler.get();
	}
	
	private void parse(String xml, Handler<ChangesetInfo> handler)
	{
		try
		{
			new ChangesetParser(handler).parse(TestUtils.asInputStream(xml));
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
