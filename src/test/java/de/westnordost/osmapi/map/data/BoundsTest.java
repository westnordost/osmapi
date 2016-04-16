package de.westnordost.osmapi.map.data;

import java.util.List;

import junit.framework.TestCase;

public class BoundsTest extends TestCase
{
	public void testValidation4Doubles()
	{
		try
		{
			new Bounds(0, 0, -1, 0);
			fail();
		}
		catch (IllegalArgumentException e) {}
	}

	public void testValidationLatLon()
	{
		try
		{
			new Bounds(new OsmLatLon(0, 0), new OsmLatLon(-1, 0));
			fail();
		}
		catch (IllegalArgumentException e) {}
	}

	
	public void testCross180thMeridian()
	{
		Bounds bounds = new Bounds(
				new OsmLatLon(0, 90),
				new OsmLatLon(1, -90));
		assertTrue(bounds.crosses180thMeridian());

		List<Bounds> boundses = bounds.splitAt180thMeridian();
		Bounds bounds1 = boundses.get(0);
		Bounds bounds2 = boundses.get(1);

		assertEquals(bounds.getMin(),bounds1.getMin());
		assertEquals(bounds.getMax().getLatitude(), bounds1.getMax().getLatitude());
		assertEquals(LatLons.MAX_VALUE.getLongitude(), bounds1.getMax().getLongitude());

		assertEquals(bounds.getMin().getLatitude(), bounds2.getMin().getLatitude());
		assertEquals(LatLons.MIN_VALUE.getLongitude(), bounds2.getMin().getLongitude());
		assertEquals(bounds.getMax(),bounds2.getMax());
	}

	public void testEquals()
	{
		Bounds bounds1 = new Bounds(
				OsmLatLon.parseLatLon("51.7400243", "0.2400123"),
				OsmLatLon.parseLatLon("55.7410243", "0.2701123"));
		Bounds bounds2 = new Bounds(
				OsmLatLon.parseLatLon("51.7400243", "0.2400123"),
				OsmLatLon.parseLatLon("55.7410243", "0.2701123"));
		assertEquals(bounds1, bounds2);
	}
	
	public void testEqualsWithDifferentConstructors()
	{
		Bounds bounds1 = new Bounds(34.1234, 12.1234, 37.1237, 15.1254);
		Bounds bounds2 = new Bounds(new OsmLatLon(34.1234, 12.1234), new OsmLatLon(37.1237, 15.1254));
		
		assertEquals(bounds1, bounds2);
	}
	
	public void testEqualsNull()
	{
		Bounds bounds1 = new Bounds(34.1234, 12.1234, 37.1237, 15.1254);
		assertFalse(bounds1.equals(null));
	}
	
	public void testEqualsOtherObject()
	{
		Bounds bounds1 = new Bounds(34.1234, 12.1234, 37.1237, 15.1254);
		assertFalse(bounds1.equals(new Object()));
	}
}
