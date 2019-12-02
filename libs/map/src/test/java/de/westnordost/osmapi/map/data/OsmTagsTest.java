package de.westnordost.osmapi.map.data;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class OsmTagsTest extends TestCase
{
	private static final String TOO_LONG = 
			"Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy "
			+ "eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptu"
			+ "a. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gube"
			+ "rgren, no sea takimata ";
	
	public void testInitWithTooLongKeyFails()
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
	
	public void testInitWithTooLongValueFails()
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
	
	public void testPutWithTooLongKeyFails()
	{
		try
		{
			new OsmTags(new HashMap<String,String>()).put(TOO_LONG, "jo");
			fail();
		}
		catch(IllegalArgumentException ignore) { }
	}
	
	public void testPutWithTooLongValueFails()
	{
		try
		{
			new OsmTags(new HashMap<String,String>()).put("jo", TOO_LONG);
			fail();
		}
		catch(IllegalArgumentException ignore) { }
	}
	
	public void testPutAllWithTooLongKeyFails()
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
	
	public void testPutAllWithTooLongValueFails()
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
