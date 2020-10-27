package de.westnordost.osmapi.map;

import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.map.data.OsmNode;
import junit.framework.TestCase;

import java.util.Date;

public class ElementWithoutTagsTest extends TestCase
{
    public void testElementWithoutTags()
    {
        OsmNode node = new OsmNode(1, 1, new OsmLatLon(1, 1), null, null, new Date());
        assertEquals(node.getTags().size(), 0);
    }
}
