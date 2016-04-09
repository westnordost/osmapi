package de.westnordost.osmapi.map;

import junit.framework.TestCase;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import de.westnordost.osmapi.map.data.Bounds;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;
import de.westnordost.osmapi.map.data.Way;
import de.westnordost.osmapi.map.handler.DefaultMapDataHandler;
import de.westnordost.osmapi.map.handler.MapDataHandler;
import de.westnordost.osmapi.changesets.Changeset;
import de.westnordost.osmapi.user.User;
import de.westnordost.osmapi.xml.XmlTestUtils;

public class MapDataParserTest extends TestCase
{

	public void testBounds() throws UnsupportedEncodingException
	{
		String xml =
				" <bounds minlat=\"51.7400000\" minlon=\"0.2400000\" maxlat=\"51.7500000\" maxlon=\"0.2500000\"/>";

		new MapDataParser(new DefaultMapDataHandler()
		{
			@Override
			public void handle(Bounds bounds)
			{
				assertEquals(51.7400000, bounds.getMinLatitude());
				assertEquals(0.2400000, bounds.getMinLongitude());
				assertEquals(51.7500000, bounds.getMaxLatitude());
				assertEquals(0.2500000, bounds.getMaxLongitude());
			}
		}).parse(XmlTestUtils.asInputStream(xml));
	}

	public void testNode() throws UnsupportedEncodingException
	{
		String xml =
				" <node id=\"246773347\" visible=\"true\" version=\"1\" changeset=\"80692\" " +
						"timestamp=\"2008-02-09T10:59:23Z\" user=\"Yeah\" uid=\"12503\" " +
						"lat=\"51.7463194\" lon=\"0.2428181\"/>";

		new MapDataParser(new DefaultMapDataHandler()
		{
			@Override
			public void handle(Node node)
			{
				assertEquals(51.7463194, node.getPosition().getLatitude());
				assertEquals(0.2428181, node.getPosition().getLongitude());
				assertEquals(246773347, node.getId());
				assertEquals(1, node.getVersion());
				assertNotNull(node.getChangeset());
				assertEquals(80692, node.getChangeset().getId());

				Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
				c.set(2008, Calendar.FEBRUARY, 9, 10, 59, 23);
				assertEquals(c.getTimeInMillis() / 1000, node.getChangeset().getDate().getTime() / 1000);

				assertNotNull(node.getChangeset().getUser());
				assertEquals("Yeah", node.getChangeset().getUser().getDisplayName());
				assertEquals(12503, node.getChangeset().getUser().getId());

				assertNull(node.getTags());
			}
		}).parse(XmlTestUtils.asInputStream(xml));
	}

	public void testWay() throws UnsupportedEncodingException
	{
		String xml =
				"  <way id=\"22918072\" visible=\"true\" version=\"1\" changeset=\"80692\"" +
						" timestamp=\"2008-02-09T10:59:02Z\" user=\"Yeah\" uid=\"12503\">\n" +
						"  <nd ref=\"246773324\"/>\n" +
						"  <nd ref=\"246773326\"/>\n" +
						"  <nd ref=\"246773327\"/>\n" +
				" </way>";

		new MapDataParser(new DefaultMapDataHandler()
		{
			@Override
			public void handle(Way way)
			{
				assertEquals(22918072, way.getId());
				assertEquals(1, way.getVersion());
				assertNotNull(way.getChangeset());
				assertEquals(80692, way.getChangeset().getId());

				Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
				c.set(2008, Calendar.FEBRUARY, 9, 10, 59, 2);
				assertEquals(c.getTimeInMillis() / 1000, way.getChangeset().getDate().getTime() / 1000);

				assertNotNull(way.getChangeset().getUser());
				assertEquals("Yeah", way.getChangeset().getUser().getDisplayName());
				assertEquals(12503, way.getChangeset().getUser().getId());

				assertEquals(3, way.getNodeIds().size());
				assertEquals(246773324L, (long) way.getNodeIds().get(0));
				assertEquals(246773326L, (long) way.getNodeIds().get(1));
				assertEquals(246773327L, (long) way.getNodeIds().get(2));

				assertNull(way.getTags());
			}
		}).parse(XmlTestUtils.asInputStream(xml));
	}

	public void testRelation() throws UnsupportedEncodingException
	{
		String xml =
				" <relation id=\"3190476\" visible=\"true\" version=\"1\" changeset=\"17738772\" " +
						"timestamp=\"2013-09-08T19:26:52Z\" user=\"Blub\" uid=\"30525\">\n" +
						"  <member type=\"way\" ref=\"236852867\" role=\"outer\"/>\n" +
						"  <member type=\"node\" ref=\"237143151\" role=\"inner\"/>\n" +
						"  <member type=\"relation\" ref=\"237143152\" role=\"\"/>\n" +
				" </relation>";

		new MapDataParser( new DefaultMapDataHandler()
		{
			@Override
			public void handle(Relation relation)
			{
				assertEquals(3190476, relation.getId());
				assertEquals(1, relation.getVersion());
				assertNotNull(relation.getChangeset());
				assertEquals(17738772, relation.getChangeset().getId());

				Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
				c.set(2013, Calendar.SEPTEMBER, 8, 19, 26, 52);
				assertEquals(c.getTimeInMillis() / 1000, relation.getChangeset().getDate().getTime() / 1000);

				assertNotNull(relation.getChangeset().getUser());
				assertEquals("Blub", relation.getChangeset().getUser().getDisplayName());
				assertEquals(30525, relation.getChangeset().getUser().getId());

				assertEquals(3, relation.getMembers().size());
				RelationMember one = relation.getMembers().get(0);
				RelationMember two = relation.getMembers().get(1);
				RelationMember three = relation.getMembers().get(2);

				assertEquals(236852867, one.getRef());
				assertEquals(Element.Type.WAY, one.getType());
				assertEquals("outer", one.getRole());

				assertEquals(237143151, two.getRef());
				assertEquals(Element.Type.NODE, two.getType());
				assertEquals("inner", two.getRole());

				assertEquals(237143152, three.getRef());
				assertEquals(Element.Type.RELATION, three.getType());
				assertEquals("", three.getRole());

				assertNull(relation.getTags());
			}
		}).parse(XmlTestUtils.asInputStream(xml));
	}

	public void testOrder() throws UnsupportedEncodingException
	{
		String xml =
				"<bounds minlat=\"51.7400000\" minlon=\"0.2400000\" maxlat=\"51.7500000\" maxlon=\"0.2500000\"/> " +
				"<node id=\"246773347\" visible=\"true\" version=\"1\" changeset=\"80692\" " +
						"timestamp=\"2008-02-09T10:59:23Z\" user=\"Yeah\" uid=\"12503\" " +
						"lat=\"51.7463194\" lon=\"0.2428181\"/>" +
				"<way id=\"22918072\" visible=\"true\" version=\"1\" changeset=\"80692\"" +
						" timestamp=\"2008-02-09T10:59:02Z\" user=\"Yeah\" uid=\"12503\" />" +
				" <relation id=\"3190476\" visible=\"true\" version=\"1\" changeset=\"17738772\" " +
						"timestamp=\"2013-09-08T19:26:52Z\" user=\"Blub\" uid=\"30525\">" +
				" </relation>";

		new MapDataParser( new MapDataHandler()
		{
			private int step = 0;

			@Override
			public void handle(Bounds bounds)
			{
				assertEquals(0, step++);
			}

			@Override
			public void handle(Node node)
			{
				assertEquals(1, step++);
			}

			@Override
			public void handle(Way way)
			{
				assertEquals(2, step++);
			}

			@Override
			public void handle(Relation relation)
			{
				assertEquals(3, step++);
			}

		}).parse(XmlTestUtils.asInputStream(xml));
	}

	public void testTags() throws UnsupportedEncodingException
	{
		String xml =
				" <relation id=\"3190476\" visible=\"true\" version=\"1\" changeset=\"17738772\" " +
						"timestamp=\"2013-09-08T19:26:52Z\" user=\"Blub\" uid=\"30525\">\n" +
							"<tag k=\"operator\" v=\"Sustrans\"/>" +
							"<tag k=\"ref\" v=\"1\"/>" +
							"<tag k=\"route\" v=\"bicycle\"/>" +
							"<tag k=\"type\" v=\"route\"/>" +
						" </relation>";

		new MapDataParser( new DefaultMapDataHandler()
		{
			@Override
			public void handle(Relation relation)
			{
				assertNotNull(relation.getTags());
				assertEquals(4, relation.getTags().size());

				assertTrue(relation.getTags().containsKey("operator"));
				assertTrue(relation.getTags().containsValue("Sustrans"));
				assertEquals("Sustrans", relation.getTags().get("operator"));

				assertEquals("1", relation.getTags().get("ref"));
				assertEquals("bicycle", relation.getTags().get("route"));
				assertEquals("route", relation.getTags().get("type"));
			}
		}).parse(XmlTestUtils.asInputStream(xml));
	}

	public void testReuseData() throws UnsupportedEncodingException
	{
		String xml =
				" <node id=\"246773352\" visible=\"true\" version=\"1\" changeset=\"80692\" " +
						"timestamp=\"2008-02-09T10:59:23Z\" user=\"Paul Todd\" uid=\"12503\" " +
						"lat=\"51.7455489\" lon=\"0.2442600\"/>" +
				" <node id=\"246773353\" visible=\"true\" version=\"1\" changeset=\"80692\" " +
						"timestamp=\"2008-02-09T10:59:23Z\" user=\"Paul Todd\" uid=\"12503\" " +
						"lat=\"51.7454904\" lon=\"0.2441828\"/>";

		new MapDataParser( new DefaultMapDataHandler()
		{
			private Changeset otherChangeset;
			private User otherUser;

			@Override
			public void handle(Node node)
			{
				if(otherChangeset == null)
				{
					otherChangeset = node.getChangeset();
					otherUser = node.getChangeset().getUser();
				}
				else
				{
					assertSame(otherChangeset, node.getChangeset());
					assertSame(otherUser, node.getChangeset().getUser());
				}
			}
		}).parse(XmlTestUtils.asInputStream(xml));
	}

}
