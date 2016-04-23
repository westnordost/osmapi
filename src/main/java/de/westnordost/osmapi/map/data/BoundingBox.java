package de.westnordost.osmapi.map.data;

import java.util.Arrays;
import java.util.List;

/** A rectangle in latitude longitude coordinates. Bounds are immutable. */
public class BoundingBox
{
	private LatLon min;
	private LatLon max;

	public BoundingBox(final double latMin, final double lonMin, final double latMax, final double lonMax)
	{
		this(createLatLon(latMin, lonMin), createLatLon(latMax, lonMax));
	}
	
	private static LatLon createLatLon(final double lat, final double lon)
	{
		// bake the parameters into anonymous LatLons so there is no dependency on any particular LatLon implementation
		LatLon result = new LatLon()
		{
			public double getLatitude() { return lat; }
			public double getLongitude() { return lon; }
		};
		LatLons.checkValidity(result);
		return result;
	}
	
	public BoundingBox(LatLon min, LatLon max)
	{
		this.min = min;
		this.max = max;

		if(!isValid())
		{
			throw new IllegalArgumentException("Min latitude " + min.getLatitude() +
					" is greater than max latitude " + max.getLatitude());
		}
	}

	public LatLon getMin()
	{
		return min;
	}

	public LatLon getMax()
	{
		return max;
	}

	public double getMinLatitude()
	{
		return min.getLatitude();
	}

	public double getMaxLatitude()
	{
		return max.getLatitude();
	}

	public double getMinLongitude()
	{
		return min.getLongitude();
	}

	public double getMaxLongitude()
	{
		return max.getLongitude();
	}

	public String getAsLeftBottomRightTopString()
	{
		return getMinLongitude() + "," + getMinLatitude() + "," +
				getMaxLongitude() + "," + getMaxLatitude();
	}

	public boolean crosses180thMeridian()
	{
		return min.getLongitude() > max.getLongitude();
	}

	private boolean isValid()
	{
		return min.getLatitude() <= max.getLatitude();
	}

	/** @return two new bounds split alongside the 180th meridian or, if these bounds do not cross
	 *          the 180th meridian, just this object in a list */
	public List<BoundingBox> splitAt180thMeridian()
	{
		if(crosses180thMeridian())
		{
			return Arrays.asList(
					new BoundingBox( min, createLatLon(max.getLatitude(), LatLons.MAX_VALUE.getLongitude()) ),
					new BoundingBox( createLatLon(min.getLatitude(), LatLons.MIN_VALUE.getLongitude()), max )
			);
		}

		return Arrays.asList(this);
	}

	@Override
	public boolean equals(Object other)
	{
		if(other == null || !(other instanceof BoundingBox)) return false;

		// we do not rely on that every implementation of LatLon implements equals() properly
		BoundingBox otherBounds = (BoundingBox) other;
		return otherBounds.getMinLatitude() == getMinLatitude()
			&& otherBounds.getMaxLatitude() == getMaxLatitude()
			&& otherBounds.getMinLongitude() == getMinLongitude()
			&& otherBounds.getMaxLongitude() == getMaxLongitude();
	}
}
