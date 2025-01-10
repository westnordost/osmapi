package de.westnordost.osmapi.map.data;

import org.junit.Test;

import static org.junit.Assert.*;

public class OsmLatLonTest
{
	private static final double VALID_LAT = 51.7400243;
	private static final double VALID_LON = 0.2400123;
	
	@Test public void fixedE7()
	{
		OsmLatLon pos = new OsmLatLon(VALID_LAT, VALID_LON);
		assertEquals(VALID_LAT, pos.getLatitude(), 1e-7);
		assertEquals(VALID_LON, pos.getLongitude(), 1e-7);
	}

	@Test public void negativeFixedE7()
	{
		OsmLatLon pos = new OsmLatLon(-VALID_LAT, -VALID_LON);
		assertEquals(-VALID_LAT, pos.getLatitude(), 1e-7);
		assertEquals(-VALID_LON, pos.getLongitude(), 1e-7);
	}

	@Test public void parse()
	{
		OsmLatLon pos = OsmLatLon.parseLatLon(String.valueOf(VALID_LAT), String.valueOf(VALID_LON));
		assertEquals(VALID_LAT, pos.getLatitude(), 1e-7);
		assertEquals(VALID_LON, pos.getLongitude(), 1e-7);
	}

	@Test public void zeroSomething()
	{
		OsmLatLon pos = new OsmLatLon(0.0000005,0.0000003);
		assertEquals(0.0000005, pos.getLatitude(), 1e-7);
		assertEquals(0.0000003, pos.getLongitude(), 1e-7);
	}

	@Test public void testEquals()
	{
		OsmLatLon pos1 = OsmLatLon.parseLatLon(String.valueOf(VALID_LAT), String.valueOf(VALID_LON));
		OsmLatLon pos2 = new OsmLatLon(VALID_LAT, VALID_LON);
		assertEquals(pos1, pos2);
	}
	
	@Test public void equalsWithNonOsmLatLon()
	{
		LatLon pos1 = new OsmLatLon(VALID_LAT, VALID_LON);
		LatLon pos2 = new LatLon()
		{
			public double getLongitude() { return VALID_LON; }
			public double getLatitude() { return VALID_LAT; }
		};
		assertEquals(pos1, pos2);
	}
	
	@Test public void equalsNull()
	{
		LatLon pos1 = new OsmLatLon(VALID_LAT, VALID_LON);
        assertNotEquals(null, pos1);
	}
	
	@Test public void equalsOtherObject()
	{
		LatLon pos1 = new OsmLatLon(VALID_LAT, VALID_LON);
        assertNotEquals(pos1, new Object());
	}
	
	@Test public void testHashCode()
	{
		OsmLatLon pos1 = new OsmLatLon(VALID_LAT, VALID_LON);
		OsmLatLon pos2 = new OsmLatLon(VALID_LAT, VALID_LON);
		assertEquals(pos1.hashCode(), pos2.hashCode());
	}
	
	@Test public void hashCodeAlgoIsNotTooSimple()
	{
		OsmLatLon pos1 = new OsmLatLon(VALID_LAT, VALID_LON);
		OsmLatLon pos2 = new OsmLatLon(VALID_LON, VALID_LAT);
        assertNotEquals(pos1.hashCode(), pos2.hashCode());
	}
	
	@Test public void invalidPositiveLatitude()
	{
		assertThrows(IllegalArgumentException.class, () -> new OsmLatLon(90.0000001, 0));
	}

	@Test public void invalidNegativeLatitude()
	{
		assertThrows(IllegalArgumentException.class, () -> new OsmLatLon(-90.0000001, 0));
	}

	@Test public void invalidPositiveLongitude()
	{
		assertThrows(IllegalArgumentException.class, () -> new OsmLatLon(0, 180.0000001));
	}

	@Test public void invalidNegativeLongitude()
	{
		assertThrows(IllegalArgumentException.class, () -> new OsmLatLon(0, -180.0000001));
	}
	
	@Test public void invalidNegative360Longitude()
	{
		assertThrows(IllegalArgumentException.class, () -> new OsmLatLon(0, -350.0));
	}
}
