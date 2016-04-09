package de.westnordost.osmapi.map.data;

/**
 * A geo position (without height) implemented using two fixed 1E7 integers, meaning that the
 * maximum precision is limited to 7 decimal points. Coincidentally, this is exactly the maximum
 * precision the positions are saved in the OSM database, hence the name OsmLatLon
 *
 * So, this saves 8 byte per coordinate from an implementation based on doubles ;-)
 */
public class OsmLatLon implements LatLon
{
	private final int latitude;
	private final int longitude;

	private static final int LATITUDE_LIMIT = Fixed1E7.intToFixed(+90);
	private static final int LONGITUDE_LIMIT = Fixed1E7.intToFixed(+180);

	/** @throws IllegalArgumentException if the given latitude and longitude do not make up a valid
	 *          position*/
	public OsmLatLon(double latitude, double longitude)
	{
		this.latitude = Fixed1E7.doubleToFixed(latitude);
		this.longitude = Fixed1E7.doubleToFixed(longitude);

		if(!isValid())
		{
			throw new IllegalArgumentException("Latitude " + getLatitude() + ", Longitude " +
					getLongitude() + " is not a valid position.");
		}
	}

	public static OsmLatLon parseLatLon(String lat, String lon)
	{
		return new OsmLatLon(Double.parseDouble(lat), Double.parseDouble(lon));
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

	private boolean isLongitudeValid()
	{
		return longitude >= -LONGITUDE_LIMIT && longitude <= LONGITUDE_LIMIT;
	}

	private boolean isLatitudeValid()
	{
		return latitude >= -LATITUDE_LIMIT && latitude <= LATITUDE_LIMIT;
	}

	private boolean isValid()
	{
		return isLongitudeValid() && isLatitudeValid();
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj == null || !(obj instanceof LatLon)) return false;
		if(obj instanceof OsmLatLon)
		{
			OsmLatLon other = (OsmLatLon) obj;
			return other.latitude == latitude && other.longitude == longitude;
		}
		LatLon other = (LatLon) obj;
		return other.getLatitude() == getLatitude() && other.getLongitude() == getLongitude();
	}
}
