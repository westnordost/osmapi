package de.westnordost.osmapi.map.changes;

import java.io.IOException;

import junit.framework.TestCase;
import de.westnordost.osmapi.TestUtils;
import de.westnordost.osmapi.common.SingleElementHandler;
import de.westnordost.osmapi.map.data.Element;

public class MapDataDiffParserTest extends TestCase
{
	public void testEmpty()
	{
		DiffElement e = parseOne("<diffResult></diffResult>");

		assertNull(e);
	}

	public void testFields()
	{
		DiffElement e = parseOne("<diffResult><node old_id='1' new_id='2' new_version='3'/></diffResult>");

		assertEquals(1, e.clientId);
		assertEquals(2, (long) e.serverId);
		assertEquals(3, (int) e.serverVersion);
	}

	public void testNullableFields()
	{
		DiffElement e = parseOne("<diffResult><node old_id='1'/></diffResult>");

		assertEquals(1, e.clientId);
		assertNull(e.serverId);
		assertNull(e.serverVersion);
	}

	public void testNode()
	{
		DiffElement e = parseOne("<diffResult><node old_id='1'/></diffResult>");
		assertEquals(Element.Type.NODE, e.type);
	}

	public void testWay()
	{
		DiffElement e = parseOne("<diffResult><way old_id='1'/></diffResult>");
		assertEquals(Element.Type.WAY, e.type);
	}

	public void testRelation()
	{
		DiffElement e = parseOne("<diffResult><relation old_id='1'/></diffResult>");
		assertEquals(Element.Type.RELATION, e.type);
	}

	private DiffElement parseOne(String xml)
	{
		try
		{
			SingleElementHandler<DiffElement> handler = new SingleElementHandler<>();
			new MapDataDiffParser(handler).parse(TestUtils.asInputStream(xml));
			return handler.get();
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
