package de.westnordost.osmapi.map.data;

import java.io.Serializable;

/**
 * A geo position (without height) implemented using two fixed 1E7 integers, meaning that the
 * maximum precision is limited to 7 decimal points. This is exactly the maximum precision the
 * positions are saved in the OSM database.
 *
 * This saves 8 byte per coordinate from an implementation based on doubles, but every access
 * involves a division operation. If the goal is to save disk space, rather just serialize a latlon
 * with two integers instead. This implementation should only be chosen if the goal is to save RAM
 * space but CPU is not the bottleneck.
 */
public class Fixed1E7LatLon implements LatLon, Serializable
{
	private static final long serialVersionUID = 1L;

	private final int latitude;
	private final int longitude;

	/** @throws IllegalArgumentException if the given latitude and longitude do not make up a valid
	 *          position*/
	public Fixed1E7LatLon(double latitude, double longitude)
	{
		LatLons.checkValidity(latitude, longitude);

		this.latitude = Fixed1E7.doubleToFixed(latitude);
		this.longitude = Fixed1E7.doubleToFixed(longitude);
	}

	@Override
	public double getLatitude()
	{
		return Fixed1E7.toDouble(latitude);
	}

	@Override
	public double getLongitude()
	{
		return Fixed1E7.toDouble(longitude);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj == this) return true;
		if(obj == null || !(obj instanceof LatLon)) return false;
		LatLon other = (LatLon) obj;
		return other.getLatitude() == getLatitude() && other.getLongitude() == getLongitude();
	}

	@Override
	public int hashCode()
	{
		return latitude * 31 + longitude;
	}
}
