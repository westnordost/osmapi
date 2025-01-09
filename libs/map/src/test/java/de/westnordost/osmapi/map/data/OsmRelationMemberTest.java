package de.westnordost.osmapi.map.data;

import org.junit.Test;

import de.westnordost.osmapi.map.data.Element.Type;

import static org.junit.Assert.*;

public class OsmRelationMemberTest
{
	private static final String TOO_LONG = 
			"Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy "
			+ "eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptu"
			+ "a. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gube"
			+ "rgren, no sea takimata ";
	
	@Test public void setTooLongRoleFails()
	{
		assertThrows(
				IllegalArgumentException.class,
				() -> new OsmRelationMember(1,"jo", Type.NODE).setRole(TOO_LONG)
		);
	}
	
	@Test public void initWithTooLongRoleFails()
	{
		assertThrows(
				IllegalArgumentException.class,
				() -> new OsmRelationMember(1,TOO_LONG, Type.NODE)
		);
	}
}
