package de.westnordost.osmapi.map.data;

import de.westnordost.osmapi.map.data.Element.Type;
import junit.framework.TestCase;

public class OsmRelationMemberTest extends TestCase
{
	private static final String TOO_LONG = 
			"Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy "
			+ "eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptu"
			+ "a. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gube"
			+ "rgren, no sea takimata ";
	
	public void testSetTooLongRoleFails()
	{
		try
		{
			new OsmRelationMember(1,"jo", Type.NODE).setRole(TOO_LONG);
			fail();
		}
		catch(IllegalArgumentException e) {}
	}
	
	public void testInitWithTooLongRoleFails()
	{
		try
		{
			new OsmRelationMember(1,TOO_LONG, Type.NODE);
			fail();
		}
		catch(IllegalArgumentException e) {}
	}
}
