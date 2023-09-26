package de.westnordost.osmapi.map.data;

import java.io.Serializable;
import java.util.Map;

/** Represent the tags for any osm element. It is a Map of Strings which registers whether it has been modified  */
public class OsmTags extends ModificationAwareMap<String, String> implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public OsmTags(Map<String, String> map)
	{	
		super(map);
	}
}
