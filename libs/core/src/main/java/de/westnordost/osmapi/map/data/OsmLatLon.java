package de.westnordost.osmapi.map.data;

import java.io.Serializable;

/**
 * A geo position (without height).
 *
 * In prior versions, it was based on two integers, saving 8 byte from an implementation based on
 * doubles, but every access thus involved a division operation so that was bullocks for general
 * usage.
 */
public class OsmLatLon implements LatLon, Serializable
{
	private static final long serialVersionUID = 2L;

	private final double latitude;
	private final double longitude;

	/** @throws IllegalArgumentException if the given latitude and longitude do not make up a valid
	 *          position*/
	public OsmLatLon(double latitude, double longitude)
	{
		LatLons.checkValidity(latitude, longitude);
		
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public OsmLatLon(LatLon other)
	{
		this.latitude = other.getLatitude();
		this.longitude = other.getLongitude();
	}

	public static OsmLatLon parseLatLon(String lat, String lon)
	{
		return new OsmLatLon(Double.parseDouble(lat), Double.parseDouble(lon));
	}

	@Override
	public double getLatitude()
	{
		return latitude;
	}

	@Override
	public double getLongitude()
	{
		return longitude;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj == this) return true;
		if(!(obj instanceof LatLon)) return false;
		LatLon other = (LatLon) obj;
		return other.getLatitude() == getLatitude() && other.getLongitude() == getLongitude();
	}

	@Override public int hashCode()
	{
		return 31 * Double.hashCode(latitude) + Double.hashCode(longitude);
	}
}
