package de.westnordost.osmapi.map.data;

/**
 * A geo position (without height)
 */
public interface LatLon
{
	double getLatitude();

	double getLongitude();

	LatLon MIN_VALUE = new LatLon()
	{
		public double getLatitude()	{ return -90; }
		public double getLongitude() { return -180; }
	};

	LatLon MAX_VALUE = new LatLon()
	{
		public double getLatitude()	{ return +90; }
		public double getLongitude() { return +180; }
	};
}
