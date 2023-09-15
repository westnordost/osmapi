package de.westnordost.osmapi.map.data;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class OsmTagsTest
{
	private static final String TOO_LONG = 
			"Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy "
			+ "eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptu"
			+ "a. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gube"
			+ "rgren, no sea takimata ";

	@Test public void testStringIsExactly256CharactersLong()
	{
		assertEquals(256, TOO_LONG.length());
	}

	@Test public void initWithKeyOrValueBelow256CharactersIsFine()
	{
		StringBuilder cools = new StringBuilder();
		while (cools.codePoints().count() < 255)
		{
			cools.append("\uD83D\uDE0E"); // 😎

			Map<String, String> tags = new HashMap<>();
			tags.put(cools.toString(), cools.toString());
			new OsmTags(tags);
		}
	}

	@Test public void initWithTooLongKeyFails()
	{
		Map<String,String> tooLong = new HashMap<>();
		tooLong.put(TOO_LONG, "jo");
		
		try
		{
			new OsmTags(tooLong);
			fail();
		}
		catch(IllegalArgumentException ignore) { }
	}
	
	@Test public void initWithTooLongValueFails()
	{
		Map<String,String> tooLong = new HashMap<>();
		tooLong.put("jo", TOO_LONG);
		
		try
		{
			new OsmTags(tooLong);
			fail();
		}
		catch(IllegalArgumentException ignore) { }
	}
	
	@Test public void putWithTooLongKeyFails()
	{
		try
		{
			new OsmTags(new HashMap<String,String>()).put(TOO_LONG, "jo");
			fail();
		}
		catch(IllegalArgumentException ignore) { }
	}
	
	@Test public void putWithTooLongValueFails()
	{
		try
		{
			new OsmTags(new HashMap<String,String>()).put("jo", TOO_LONG);
			fail();
		}
		catch(IllegalArgumentException ignore) { }
	}
	
	@Test public void putAllWithTooLongKeyFails()
	{
		Map<String,String> tooLong = new HashMap<>();
		tooLong.put(TOO_LONG, "jo");
		
		try
		{
			new OsmTags(new HashMap<String,String>()).putAll(tooLong);
			fail();
		}
		catch(IllegalArgumentException ignore) { }
	}
	
	@Test public void putAllWithTooLongValueFails()
	{
		Map<String,String> tooLong= new HashMap<>();
		tooLong.put("jo", TOO_LONG);
		
		try
		{
			new OsmTags(new HashMap<String,String>()).putAll(tooLong);
			fail();
		}
		catch(IllegalArgumentException ignore) { }
	}
}
