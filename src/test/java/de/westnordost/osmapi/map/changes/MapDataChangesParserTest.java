package de.westnordost.osmapi.map.changes;

import junit.framework.TestCase;

import de.westnordost.osmapi.xml.XmlTestUtils;

public class MapDataChangesParserTest extends TestCase
{
	public void testEmptyChange()
	{
		MapDataChanges changes = parse("<osmChange></osmChange>");

		assertTrue(changes.getAll().isEmpty());
	}

	public void testDeletions()
	{
		MapDataChanges changes =
				parse("<osmChange><delete><node id='1' version='1' lat='1' lon='1'/></delete></osmChange>");

		assertEquals(1, changes.getDeletions().size());
		assertEquals(0, changes.getCreations().size());
		assertEquals(0, changes.getModifications().size());
	}

	public void testCreations()
	{
		MapDataChanges changes =
				parse("<osmChange><create><node id='1' version='1' lat='1' lon='1'/></create></osmChange>");

		assertEquals(0, changes.getDeletions().size());
		assertEquals(1, changes.getCreations().size());
		assertEquals(0, changes.getModifications().size());
	}

	public void testModifications()
	{
		MapDataChanges changes =
				parse("<osmChange><modify><node id='1' version='1' lat='1' lon='1'/></modify></osmChange>");

		assertEquals(0, changes.getDeletions().size());
		assertEquals(0, changes.getCreations().size());
		assertEquals(1, changes.getModifications().size());
	}

	private MapDataChanges parse(String xml)
	{
		SimpleMapDataChangesHandler changeMapDataHandler = new SimpleMapDataChangesHandler();
		new MapDataChangesParser(changeMapDataHandler).parse(XmlTestUtils.asInputStream(xml));
		return changeMapDataHandler;
	}
}
