package de.westnordost.osmapi.map;

import org.junit.Test;

import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.map.data.OsmNode;

import static org.junit.Assert.assertEquals;

public class ElementWithoutTagsTest
{
    @Test public void elementWithoutTags()
    {
        OsmNode node = new OsmNode(1, 1, new OsmLatLon(1, 1), null, null, null);
        assertEquals(node.getTags().size(), 0);
    }
}
