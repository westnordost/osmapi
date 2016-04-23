package de.westnordost.osmapi.map;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import junit.framework.TestCase;
import de.westnordost.osmapi.TestUtils;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;
import de.westnordost.osmapi.map.data.Way;
import de.westnordost.osmapi.map.handler.ListOsmElementHandler;
import de.westnordost.osmapi.map.handler.MapDataHandler;
import de.westnordost.osmapi.map.handler.SingleOsmElementHandler;

public class MapDataParserTest extends TestCase
{

	public void testBounds()
	{
		String xml =
				" <bounds minlat=\"51.7400000\" minlon=\"0.2400000\" maxlat=\"51.7500000\" maxlon=\"0.2500000\"/>";

		BoundingBox bounds = parseOne(xml, BoundingBox.class);
		
		assertEquals(51.7400000, bounds.getMinLatitude());
		assertEquals(0.2400000, bounds.getMinLongitude());
		assertEquals(51.7500000, bounds.getMaxLatitude());
		assertEquals(0.2500000, bounds.getMaxLongitude());
	}
	
	public void testNode()
	{
		String xml =
				" <node id=\"246773347\" visible=\"true\" version=\"1\" changeset=\"80692\" " +
						"timestamp=\"2008-02-09T10:59:23Z\" user=\"Yeah\" uid=\"12503\" " +
						"lat=\"51.7463194\" lon=\"0.2428181\"/>";

		Node node = parseOne(xml, Node.class);
		
		assertEquals(51.7463194, node.getPosition().getLatitude());
		assertEquals(0.2428181, node.getPosition().getLongitude());
		assertEquals(246773347, node.getId());
		assertEquals(1, node.getVersion());
		assertNotNull(node.getChangeset());
		assertEquals(80692, node.getChangeset().id);

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
		c.set(2008, Calendar.FEBRUARY, 9, 10, 59, 23);
		assertEquals(c.getTimeInMillis() / 1000, node.getChangeset().date.getTime() / 1000);

		assertNotNull(node.getChangeset().user);
		assertEquals("Yeah", node.getChangeset().user.displayName);
		assertEquals(12503, node.getChangeset().user.id);

		assertNull(node.getTags());
	}

	public void testWay()
	{
		String xml =
				"  <way id=\"22918072\" visible=\"true\" version=\"1\" changeset=\"80692\"" +
						" timestamp=\"2008-02-09T10:59:02Z\" user=\"Yeah\" uid=\"12503\">\n" +
						"  <nd ref=\"246773324\"/>\n" +
						"  <nd ref=\"246773326\"/>\n" +
						"  <nd ref=\"246773327\"/>\n" +
				" </way>";

		Way way = parseOne(xml, Way.class);
		
		assertEquals(22918072, way.getId());
		assertEquals(1, way.getVersion());
		assertNotNull(way.getChangeset());
		assertEquals(80692, way.getChangeset().id);

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
		c.set(2008, Calendar.FEBRUARY, 9, 10, 59, 2);
		assertEquals(c.getTimeInMillis() / 1000, way.getChangeset().date.getTime() / 1000);

		assertNotNull(way.getChangeset().user);
		assertEquals("Yeah", way.getChangeset().user.displayName);
		assertEquals(12503, way.getChangeset().user.id);

		assertEquals(3, way.getNodeIds().size());
		assertEquals(246773324L, (long) way.getNodeIds().get(0));
		assertEquals(246773326L, (long) way.getNodeIds().get(1));
		assertEquals(246773327L, (long) way.getNodeIds().get(2));
	}

	public void testRelation()
	{
		String xml =
				" <relation id=\"3190476\" visible=\"true\" version=\"1\" changeset=\"17738772\" " +
						"timestamp=\"2013-09-08T19:26:52Z\" user=\"Blub\" uid=\"30525\">\n" +
						"  <member type=\"way\" ref=\"236852867\" role=\"outer\"/>\n" +
						"  <member type=\"node\" ref=\"237143151\" role=\"inner\"/>\n" +
						"  <member type=\"relation\" ref=\"237143152\" role=\"\"/>\n" +
				" </relation>";

		Relation relation = parseOne(xml, Relation.class);
		
		assertEquals(3190476, relation.getId());
		assertEquals(1, relation.getVersion());
		assertNotNull(relation.getChangeset());
		assertEquals(17738772, relation.getChangeset().id);

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
		c.set(2013, Calendar.SEPTEMBER, 8, 19, 26, 52);
		assertEquals(c.getTimeInMillis() / 1000, relation.getChangeset().date.getTime() / 1000);

		assertNotNull(relation.getChangeset().user);
		assertEquals("Blub", relation.getChangeset().user.displayName);
		assertEquals(30525, relation.getChangeset().user.id);

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

	public void testOrder()
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
			public void handle(BoundingBox bounds)
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

		}).parse(TestUtils.asInputStream(xml));
	}

	public void testTags()
	{
		String xml =
				" <relation id=\"3190476\" visible=\"true\" version=\"1\" changeset=\"17738772\" " +
						"timestamp=\"2013-09-08T19:26:52Z\" user=\"Blub\" uid=\"30525\">\n" +
							"<tag k=\"operator\" v=\"Sustrans\"/>" +
							"<tag k=\"ref\" v=\"1\"/>" +
							"<tag k=\"route\" v=\"bicycle\"/>" +
							"<tag k=\"type\" v=\"route\"/>" +
						" </relation>";

		Relation relation = parseOne(xml, Relation.class);
		
		assertNotNull(relation.getTags());
		assertEquals(4, relation.getTags().size());

		assertTrue(relation.getTags().containsKey("operator"));
		assertTrue(relation.getTags().containsValue("Sustrans"));
		assertEquals("Sustrans", relation.getTags().get("operator"));

		assertEquals("1", relation.getTags().get("ref"));
		assertEquals("bicycle", relation.getTags().get("route"));
		assertEquals("route", relation.getTags().get("type"));
	}

	public void testReuseData()
	{
		String xml =
				" <node id=\"246773352\" visible=\"true\" version=\"1\" changeset=\"80692\" " +
						"timestamp=\"2008-02-09T10:59:23Z\" user=\"Paul Todd\" uid=\"12503\" " +
						"lat=\"51.7455489\" lon=\"0.2442600\"/>" +
				" <node id=\"246773353\" visible=\"true\" version=\"1\" changeset=\"80692\" " +
						"timestamp=\"2008-02-09T10:59:23Z\" user=\"Paul Todd\" uid=\"12503\" " +
						"lat=\"51.7454904\" lon=\"0.2441828\"/>";

		List<Element> elements = parseList(xml);
		
		assertEquals(2, elements.size());
		assertSame(elements.get(0).getChangeset(), elements.get(1).getChangeset());
		assertSame(elements.get(0).getChangeset().user, elements.get(1).getChangeset().user);
	}

	private List<Element> parseList(String xml)
	{
		ListOsmElementHandler<Element> handler = new ListOsmElementHandler<>(Element.class);
		new MapDataParser(handler).parse(TestUtils.asInputStream(xml));
		return handler.get();
	}
	
	private <T> T parseOne(String xml, Class<T> tClass)
	{
		SingleOsmElementHandler<T> handler = new SingleOsmElementHandler<>(tClass);
		new MapDataParser(handler).parse(TestUtils.asInputStream(xml));
		return handler.get();
	}
}
