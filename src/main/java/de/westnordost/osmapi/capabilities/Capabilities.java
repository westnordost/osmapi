package de.westnordost.osmapi.capabilities;

import java.util.Collections;
import java.util.List;

/** (Current) capabilities of the osm server. */
public class Capabilities
{
	private float minSupportedApiVersion;
	private float maxSupportedApiVersion;
	private float maxMapQueryArea;
	private int maxNodesInWay;
	private int maxPointsInGpsTracePerPage;
	private int maxElementsPerChangeset;
	private int timeoutInSeconds;
	// the API does not give out this information yet
	private static final float maxNotesQueryArea = 25;

	private ApiStatus databaseStatus;
	private ApiStatus mapDataStatus;
	private ApiStatus gpsTracesStatus;

	private List<String> imageryBlacklistRegExes;

	/* I think it's better to not make this public, as the user is not primarily interested in
	   the "status", but whether or not he can expect read/write requests to work. There are boolean
	   public methods for that.
	 */
	enum ApiStatus
	{
		ONLINE,
		OFFLINE,
		READONLY
	}

	void setTimeoutInSeconds(int timeoutInSeconds)
	{
		this.timeoutInSeconds = timeoutInSeconds;
	}

	void setImageryBlacklistRegExes(List<String> imageryBlacklistRegExes)
	{
		this.imageryBlacklistRegExes = imageryBlacklistRegExes;
	}

	static ApiStatus parseApiStatus(String status)
	{
		return ApiStatus.valueOf(status.toUpperCase());
	}

	void setDatabaseStatus(ApiStatus databaseStatus)
	{
		this.databaseStatus = databaseStatus;
	}

	void setMapDataStatus(ApiStatus mapDataStatus)
	{
		this.mapDataStatus = mapDataStatus;
	}

	void setGpsTracesStatus(ApiStatus gpsTracesStatus)
	{
		this.gpsTracesStatus = gpsTracesStatus;
	}

	void setMaxElementsPerChangeset(int maxElementsPerChangeset)
	{
		this.maxElementsPerChangeset = maxElementsPerChangeset;
	}

	void setMaxPointsInGpsTracePerPage(int maxPointsInGpsTracePerPage)
	{
		this.maxPointsInGpsTracePerPage = maxPointsInGpsTracePerPage;
	}

	void setMinSupportedApiVersion(float minSupportedApiVersion)
	{
		this.minSupportedApiVersion = minSupportedApiVersion;
	}

	void setMaxSupportedApiVersion(float maxSupportedApiVersion)
	{
		this.maxSupportedApiVersion = maxSupportedApiVersion;
	}

	void setMaxMapQueryArea(float maxMapQueryArea)
	{
		this.maxMapQueryArea = maxMapQueryArea;
	}

	void setMaxNodesInWay(int maxNodesInWay)
	{
		this.maxNodesInWay = maxNodesInWay;
	}

	public int getTimeoutInSeconds()
	{
		return timeoutInSeconds;
	}

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

	public int getMaxElementsPerChangeset()
	{
		return maxElementsPerChangeset;
	}

	/** @return See http://wiki.openstreetmap.org/wiki/API_v0.6#Retrieving_GPS_points */
	public int getMaxPointsInGpsTracePerPage()
	{
		return maxPointsInGpsTracePerPage;
	}

	public int getMaxNodesInWay()
	{
		return maxNodesInWay;
	}

	public float getMinSupportedApiVersion()
	{
		return minSupportedApiVersion;
	}

	public float getMaxSupportedApiVersion()
	{
		return maxSupportedApiVersion;
	}

	public float getMaxMapQueryAreaInSquareDegrees()
	{
		return maxMapQueryArea;
	}

	public float getMaxNotesQueryAreaInSquareDegrees()
	{
		return maxNotesQueryArea;
	}

	/** Returns a list of regular expressions to match URLs from which the use of imagery is
	 *  prohibited explicitly by the API. Naturally, this is not an exhaustive list, it is only to
	 *  save users from attempting to use imagery from sources obviously not allowed for OSM.
	 *  I.e. anything google.
	 *  Note that if the server did not send this information, this method returns null. */
	public List<String> getImageryBlacklistRegExes()
	{
		return Collections.unmodifiableList(imageryBlacklistRegExes);
	}
}
