package de.westnordost.osmapi.map.data;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class BoundingBoxTest
{
	private static final double A = 34.1234, B = 12.1234, C = 37.1237, D = 15.1254;
	
	@Test public void validation4Doubles()
	{
		try
		{
			new BoundingBox(0, 0, -1, 0);
			fail();
		}
		catch (IllegalArgumentException ignore) {}
	}

	@Test public void validationLatLon()
	{
		try
		{
			new BoundingBox(new OsmLatLon(0, 0), new OsmLatLon(-1, 0));
			fail();
		}
		catch (IllegalArgumentException ignore) {}
	}

	
	@Test public void cross180thMeridian()
	{
		BoundingBox bounds = new BoundingBox(
				new OsmLatLon(0, 90),
				new OsmLatLon(1, -90));
		assertTrue(bounds.crosses180thMeridian());

		List<BoundingBox> boundses = bounds.splitAt180thMeridian();
		BoundingBox bounds1 = boundses.get(0);
		BoundingBox bounds2 = boundses.get(1);

		assertEquals(bounds.getMin(),bounds1.getMin());
		assertEquals(bounds.getMax().getLatitude(), bounds1.getMax().getLatitude(), 1e-7);
		assertEquals(LatLons.MAX_VALUE.getLongitude(), bounds1.getMax().getLongitude(), 1e-7);

		assertEquals(bounds.getMin().getLatitude(), bounds2.getMin().getLatitude(), 1e-7);
		assertEquals(LatLons.MIN_VALUE.getLongitude(), bounds2.getMin().getLongitude(), 1e-7);
		assertEquals(bounds.getMax(), bounds2.getMax());
	}

	@Test public void testEquals()
	{
		BoundingBox bounds1 = new BoundingBox(
				OsmLatLon.parseLatLon("51.7400243", "0.2400123"),
				OsmLatLon.parseLatLon("55.7410243", "0.2701123"));
		BoundingBox bounds2 = new BoundingBox(
				OsmLatLon.parseLatLon("51.7400243", "0.2400123"),
				OsmLatLon.parseLatLon("55.7410243", "0.2701123"));
		assertEquals(bounds1, bounds2);
	}
	
	@Test public void equalsWithDifferentConstructors()
	{
		BoundingBox bounds1 = new BoundingBox(A,B,C,D);
		BoundingBox bounds2 = new BoundingBox(new OsmLatLon(A,B), new OsmLatLon(C,D));
		
		assertEquals(bounds1, bounds2);
	}
	
	@Test public void equalsNull()
	{
		BoundingBox bounds1 = new BoundingBox(A,B,C,D);
		assertFalse(bounds1.equals(null));
	}
	
	@Test public void equalsOtherObject()
	{
		BoundingBox bounds1 = new BoundingBox(A,B,C,D);
		assertFalse(bounds1.equals(new Object()));
	}

	@Test public void doesNotUseScientificNotation()
	{
		BoundingBox bounds1 = new BoundingBox(-0.0000001,-0.0000001,0.0000001,0.0000001);
		assertEquals("-0.0000001,-0.0000001,0.0000001,0.0000001",bounds1.getAsLeftBottomRightTopString());
	}
		
	@Test public void testHashCode()
	{
		BoundingBox bounds1 = new BoundingBox(A,B,C,D);
		BoundingBox bounds2 = new BoundingBox(A,B,C,D);
		assertEquals(bounds1.hashCode(), bounds2.hashCode());
	}
	
	@Test public void hashCodeAlgoIsNotTooSimple()
	{
		BoundingBox bounds1 = new BoundingBox(A,B,C,D);
		BoundingBox bounds2 = new BoundingBox(B,A,D,C);

		assertFalse(bounds1.hashCode() == bounds2.hashCode());
	}
}
