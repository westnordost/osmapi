package de.westnordost.osmapi.capabilities;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

/** (Current) capabilities of the osm server. */
public class Capabilities implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public float minSupportedApiVersion;
	public float maxSupportedApiVersion;
	
	public float maxMapQueryAreaInSquareDegrees;
	// the API does not give out this information yet
	public float maxNotesQueryAreaInSquareDegrees = 25;
	
	public int maxNodesInWay;
	/** See http://wiki.openstreetmap.org/wiki/API_v0.6#Retrieving_GPS_points */
	public int maxPointsInGpsTracePerPage;
	public int maxElementsPerChangeset;
	
	public int timeoutInSeconds;

	public ApiStatus databaseStatus;
	public ApiStatus mapDataStatus;
	public ApiStatus gpsTracesStatus;

	/** list of regular expressions to match URLs from which the use of imagery is prohibited 
	 *  explicitly by the API. Naturally, this is not an exhaustive list, it is only to
	 *  save users from attempting to use imagery from sources obviously not allowed for OSM.
	 *  I.e. anything google.
	 *  Note that if the server did not send this information, this method returns null. */
	public List<String> imageryBlacklistRegExes;

	public enum ApiStatus
	{
		ONLINE,
		OFFLINE,
		READONLY
	}

	static ApiStatus parseApiStatus(String status)
	{
		return ApiStatus.valueOf(status.toUpperCase(Locale.UK));
	}

	/* Convenience getters */
	
	/** @return whether the data in the database can be modified. Implies that it can also be read. */
	public boolean isDatabaseWritable()
	{
		return databaseStatus == ApiStatus.ONLINE;
	}

	public boolean isDatabaseReadable()
	{
		return databaseStatus != ApiStatus.OFFLINE;
	}

	public boolean isMapDataModifiable()
	{
		return mapDataStatus == ApiStatus.ONLINE;
	}

	public boolean isMapDataReadable()
	{
		return mapDataStatus != ApiStatus.OFFLINE;
	}

	public boolean isGpsTracesUploadable()
	{
		return gpsTracesStatus == ApiStatus.ONLINE;
	}

	public boolean isGpsTracesReadable()
	{
		return gpsTracesStatus != ApiStatus.OFFLINE;
	}
}
