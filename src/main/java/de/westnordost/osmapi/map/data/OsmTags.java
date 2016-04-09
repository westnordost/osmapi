package de.westnordost.osmapi.map.data;

import java.util.Map;

/** Represent the tags for any osm element. It is a Map of Strings which registers whether or not it
 *  has been modified and whose keys and values are both limited to less than 256 characters */
public class OsmTags extends ModificationAwareMap<String, String>
{
	public OsmTags(Map<String, String> map)
	{
		super(map);
	}

	@Override
	public String put(String key, String value)
	{
		checkKeyValueLength(key, value);
		return super.put(key, value);
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> map)
	{
		for(Entry<? extends String, ? extends String> entry : map.entrySet())
		{
			checkKeyValueLength(entry.getKey(), entry.getValue());
		}
		super.putAll(map);
	}

	private static void checkKeyValueLength(String key, String value)
	{
		if(key.length() >= 256)
		{
			throw new IllegalArgumentException("For key \"" + key + "\": Key length is limited" +
					"to less than 256 characters.");
		}
		if(value.length() >= 256)
		{
			throw new IllegalArgumentException("For value \"" + value + "\": Value length is " +
					"limited to less than 256 characters.");
		}
	}
}
