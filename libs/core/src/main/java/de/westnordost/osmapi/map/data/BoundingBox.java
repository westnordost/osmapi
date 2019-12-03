package de.westnordost.osmapi.map.data;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** A rectangle in latitude longitude coordinates. Bounds are immutable. */
public class BoundingBox implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private OsmLatLon min;
	private OsmLatLon max;

	public BoundingBox(final double latMin, final double lonMin, final double latMax, final double lonMax)
	{
		this(new OsmLatLon(latMin, lonMin), new OsmLatLon(latMax, lonMax));
	}
	
	public BoundingBox(LatLon min, LatLon max)
	{
		this.min = new OsmLatLon(min);
		this.max = new OsmLatLon(max);

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
		NumberFormat df = NumberFormat.getNumberInstance(Locale.UK);
		df.setMaximumFractionDigits(340);

		return df.format(getMinLongitude()) + "," + df.format(getMinLatitude()) + "," +
				df.format(getMaxLongitude()) + "," + df.format(getMaxLatitude());
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
					new BoundingBox( min, new OsmLatLon(max.getLatitude(), LatLons.MAX_VALUE.getLongitude()) ),
					new BoundingBox( new OsmLatLon(min.getLatitude(), LatLons.MIN_VALUE.getLongitude()), max )
			);
		}

		return Arrays.asList(this);
	}

	@Override
	public boolean equals(Object other)
	{
		if(other == this) return true;
		if(other == null || !(other instanceof BoundingBox)) return false;

		// we do not rely on that every implementation of LatLon implements equals() properly
		BoundingBox otherBounds = (BoundingBox) other;
		return otherBounds.getMinLatitude() == getMinLatitude()
			&& otherBounds.getMaxLatitude() == getMaxLatitude()
			&& otherBounds.getMinLongitude() == getMinLongitude()
			&& otherBounds.getMaxLongitude() == getMaxLongitude();
	}
	
	@Override
	public int hashCode()
	{
		double[] allThemDoubles = new double[]
		{
				getMinLatitude(), getMaxLatitude(), getMaxLatitude(), getMaxLongitude()
		};
		
		return Arrays.hashCode(allThemDoubles);
	}
	
}
