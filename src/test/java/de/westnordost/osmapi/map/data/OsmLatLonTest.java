package de.westnordost.osmapi.map.data;

import junit.framework.TestCase;

public class OsmLatLonTest extends TestCase
{
	private static final double VALID_LAT = 51.7400243;
	private static final double VALID_LON = 0.2400123;
	
	public void testFixedE7()
	{
		OsmLatLon pos = new OsmLatLon(VALID_LAT, VALID_LON);
		assertEquals(VALID_LAT, pos.getLatitude());
		assertEquals(VALID_LON, pos.getLongitude());
	}

	public void testNegativeFixedE7()
	{
		OsmLatLon pos = new OsmLatLon(-VALID_LAT, -VALID_LON);
		assertEquals(-VALID_LAT, pos.getLatitude());
		assertEquals(-VALID_LON, pos.getLongitude());
	}

	public void testParse()
	{
		OsmLatLon pos = OsmLatLon.parseLatLon(String.valueOf(VALID_LAT), String.valueOf(VALID_LON));
		assertEquals(VALID_LAT, pos.getLatitude());
		assertEquals(VALID_LON, pos.getLongitude());
	}

	public void testZeroSomething()
	{
		OsmLatLon pos = new OsmLatLon(0.0000005,0.0000003);
		assertEquals(0.0000005, pos.getLatitude());
		assertEquals(0.0000003, pos.getLongitude());
	}

	public void testEquals()
	{
		OsmLatLon pos1 = OsmLatLon.parseLatLon(String.valueOf(VALID_LAT), String.valueOf(VALID_LON));
		OsmLatLon pos2 = new OsmLatLon(VALID_LAT, VALID_LON);
		assertEquals(pos1, pos2);
	}
	
	public void testEqualsWithNonOsmLatLon()
	{
		LatLon pos1 = new OsmLatLon(VALID_LAT, VALID_LON);
		LatLon pos2 = new LatLon()
		{
			public double getLongitude() { return VALID_LON; }
			public double getLatitude() { return VALID_LAT; }
		};
		assertEquals(pos1, pos2);
	}
	
	public void testEqualsNull()
	{
		LatLon pos1 = new OsmLatLon(VALID_LAT, VALID_LON);
		assertFalse(pos1.equals(null));
	}
	
	public void testEqualsOtherObject()
	{
		LatLon pos1 = new OsmLatLon(VALID_LAT, VALID_LON);
		assertFalse(pos1.equals(new Object()));
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
