package de.westnordost.osmapi.map.data;

import junit.framework.TestCase;

public class OsmLatLonTest extends TestCase
{
	public void testFixedE7()
	{
		OsmLatLon pos = new OsmLatLon(51.7400243, 0.2400123);
		assertEquals(51.7400243, pos.getLatitude());
		assertEquals(0.2400123, pos.getLongitude());
	}

	public void testNegativeFixedE7()
	{
		OsmLatLon pos = new OsmLatLon(-51.7400243, -0.2400123);
		assertEquals(-51.7400243, pos.getLatitude());
		assertEquals(-0.2400123, pos.getLongitude());
	}

	public void testParse()
	{
		OsmLatLon pos = OsmLatLon.parseLatLon("51.7400243", "0.2400123");
		assertEquals(51.7400243, pos.getLatitude());
		assertEquals(0.2400123, pos.getLongitude());
	}

	public void testZeroSomething()
	{
		OsmLatLon pos = new OsmLatLon(0.0000005,0.0000003);
		assertEquals(0.0000005, pos.getLatitude());
		assertEquals(0.0000003, pos.getLongitude());
	}

	public void testEquals()
	{
		OsmLatLon pos1 = OsmLatLon.parseLatLon("51.7400243", "0.2400123");
		OsmLatLon pos2 = OsmLatLon.parseLatLon("51.7400243", "0.2400123");
		assertEquals(pos1, pos2);
	}

	public void testInvalidPositiveLatitude()
	{
		try
		{
			new OsmLatLon(90.0000001, 0);
			fail();
		}
		catch(IllegalArgumentException e) {}
	}

	public void testInvalidNegativeLatitude()
	{
		try
		{
			new OsmLatLon(-90.0000001, 0);
			fail();
		}
		catch(IllegalArgumentException e) {}
	}

	public void testInvalidPositiveLongitude()
	{
		try
		{
			new OsmLatLon(0, 180.0000001);
			fail();
		}
		catch(IllegalArgumentException e) {}
	}

	public void testInvalidNegativeLongitude()
	{
		try
		{
			new OsmLatLon(0, -180.0000001);
			fail();
		}
		catch(IllegalArgumentException e) {}
	}
}
