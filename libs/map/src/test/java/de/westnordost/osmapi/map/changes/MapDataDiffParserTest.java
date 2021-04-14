package de.westnordost.osmapi.map.changes;

import org.junit.Test;

import java.io.IOException;

import de.westnordost.osmapi.TestUtils;
import de.westnordost.osmapi.common.SingleElementHandler;
import de.westnordost.osmapi.map.data.Element;

import static org.junit.Assert.*;

public class MapDataDiffParserTest
{
	@Test public void empty()
	{
		DiffElement e = parseOne("<diffResult></diffResult>");

		assertNull(e);
	}

	@Test public void fields()
	{
		DiffElement e = parseOne("<diffResult><node old_id='1' new_id='2' new_version='3'/></diffResult>");

		assertEquals(1, e.clientId);
		assertEquals(2, (long) e.serverId);
		assertEquals(3, (int) e.serverVersion);
	}

	@Test public void nullableFields()
	{
		DiffElement e = parseOne("<diffResult><node old_id='1'/></diffResult>");

		assertEquals(1, e.clientId);
		assertNull(e.serverId);
		assertNull(e.serverVersion);
	}

	@Test public void node()
	{
		DiffElement e = parseOne("<diffResult><node old_id='1'/></diffResult>");
		assertEquals(Element.Type.NODE, e.type);
	}

	@Test public void way()
	{
		DiffElement e = parseOne("<diffResult><way old_id='1'/></diffResult>");
		assertEquals(Element.Type.WAY, e.type);
	}

	@Test public void relation()
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
