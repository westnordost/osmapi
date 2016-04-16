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
	
	public static void checkValidity(LatLon x)
	{
		if(!isValid(x))
		{
			throw new IllegalArgumentException("Latitude " + x.getLatitude() + ", Longitude " +
					x.getLongitude() + " is not a valid position.");
		}
	}
	
	public static boolean isValid(LatLon x)
	{
		return x.getLongitude() >= MIN_VALUE.getLongitude() && x.getLongitude() <= MAX_VALUE.getLongitude()
		    && x.getLatitude()  >= MIN_VALUE.getLatitude()  && x.getLatitude()  <= MAX_VALUE.getLatitude();
	}
	
}
