package de.westnordost.osmapi.map.data;

import java.util.List;

import junit.framework.TestCase;

public class BoundingBoxTest extends TestCase
{
	public void testValidation4Doubles()
	{
		try
		{
			new BoundingBox(0, 0, -1, 0);
			fail();
		}
		catch (IllegalArgumentException e) {}
	}

	public void testValidationLatLon()
	{
		try
		{
			new BoundingBox(new OsmLatLon(0, 0), new OsmLatLon(-1, 0));
			fail();
		}
		catch (IllegalArgumentException e) {}
	}

	
	public void testCross180thMeridian()
	{
		BoundingBox bounds = new BoundingBox(
				new OsmLatLon(0, 90),
				new OsmLatLon(1, -90));
		assertTrue(bounds.crosses180thMeridian());

		List<BoundingBox> boundses = bounds.splitAt180thMeridian();
		BoundingBox bounds1 = boundses.get(0);
		BoundingBox bounds2 = boundses.get(1);

		assertEquals(bounds.getMin(),bounds1.getMin());
		assertEquals(bounds.getMax().getLatitude(), bounds1.getMax().getLatitude());
		assertEquals(LatLons.MAX_VALUE.getLongitude(), bounds1.getMax().getLongitude());

		assertEquals(bounds.getMin().getLatitude(), bounds2.getMin().getLatitude());
		assertEquals(LatLons.MIN_VALUE.getLongitude(), bounds2.getMin().getLongitude());
		assertEquals(bounds.getMax(),bounds2.getMax());
	}

	public void testEquals()
	{
		BoundingBox bounds1 = new BoundingBox(
				OsmLatLon.parseLatLon("51.7400243", "0.2400123"),
				OsmLatLon.parseLatLon("55.7410243", "0.2701123"));
		BoundingBox bounds2 = new BoundingBox(
				OsmLatLon.parseLatLon("51.7400243", "0.2400123"),
				OsmLatLon.parseLatLon("55.7410243", "0.2701123"));
		assertEquals(bounds1, bounds2);
	}
	
	public void testEqualsWithDifferentConstructors()
	{
		BoundingBox bounds1 = new BoundingBox(34.1234, 12.1234, 37.1237, 15.1254);
		BoundingBox bounds2 = new BoundingBox(new OsmLatLon(34.1234, 12.1234), new OsmLatLon(37.1237, 15.1254));
		
		assertEquals(bounds1, bounds2);
	}
	
	public void testEqualsNull()
	{
		BoundingBox bounds1 = new BoundingBox(34.1234, 12.1234, 37.1237, 15.1254);
		assertFalse(bounds1.equals(null));
	}
	
	public void testEqualsOtherObject()
	{
		BoundingBox bounds1 = new BoundingBox(34.1234, 12.1234, 37.1237, 15.1254);
		assertFalse(bounds1.equals(new Object()));
	}
}
