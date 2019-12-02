package de.westnordost.osmapi.map;

/** A static collection of elements that should exist in the OSM database. If not, this needs to be
 *  updated to be something that exists for the tests to work.
 *
 *  The tests simply need something where it is known that the API will return <b>something</b> */
public class ElementShouldExist
{
	public static final long NODE = 26576175L, // Yangon (place node)
	                         WAY = 23564402L, // some harbour area in Hamburg
	                         RELATION = 180627L; // a quarter in Hamburg
}
