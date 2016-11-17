package de.westnordost.osmapi.map.data;

public class LatLons
{
	public static LatLon MIN_VALUE = new LatLon()
	{
		public double getLatitude()	{ return -90; }
		public double getLongitude() { return -180; }
	};
	
	public static LatLon MAX_VALUE = new LatLon()
	{
		public double getLatitude()	{ return +90; }
		public double getLongitude() { return +180; }
	};
	
	public static void checkValidity(double lat, double lon)
	{
		if(!isValid(lat, lon))
		{
			throw new IllegalArgumentException("Latitude " + lat + ", Longitude " + lon + 
					" is not a valid position.");
		}
	}
	
	public static void checkValidity(LatLon x)
	{
		checkValidity(x.getLatitude(), x.getLongitude());
	}
	
	public static boolean isValid(LatLon x)
	{
		return isValid(x.getLatitude(), x.getLongitude());
	}
	
	public static boolean isValid(double lat, double lon)
	{
		return lon >= MIN_VALUE.getLongitude() && lon <= MAX_VALUE.getLongitude()
		    && lat >= MIN_VALUE.getLatitude()  && lat <= MAX_VALUE.getLatitude();
	}
	
}
