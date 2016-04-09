package de.westnordost.osmapi.map.data;

import java.util.Arrays;
import java.util.List;

/** A rectangle in latitude longitude coordinates. Bounds are immutable. */
public class Bounds
{
	private LatLon min;
	private LatLon max;

	public Bounds(double latMin, double lonMin, double latMax, double lonMax)
	{
		this(new OsmLatLon(latMin, lonMin), new OsmLatLon(latMax, lonMax));
	}
	
	public Bounds(LatLon min, LatLon max)
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
	public List<Bounds> splitAt180thMeridian()
	{
		if(crosses180thMeridian())
		{
			return Arrays.asList(
					new Bounds( min, new OsmLatLon(max.getLatitude(), LatLon.MAX_VALUE.getLongitude()) ),
					new Bounds( new OsmLatLon(min.getLatitude(), LatLon.MIN_VALUE.getLongitude()), max )
			);
		}

		return Arrays.asList(this);
	}

	@Override
	public boolean equals(Object other)
	{
		if(other == null || !(other instanceof Bounds)) return false;

		Bounds otherBounds = (Bounds) other;
		return otherBounds.getMin().equals(getMin()) && otherBounds.getMax().equals(getMax());
	}
}
