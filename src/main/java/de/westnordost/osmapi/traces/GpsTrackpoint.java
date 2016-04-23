package de.westnordost.osmapi.traces;

import java.util.Date;

import de.westnordost.osmapi.map.data.LatLon;

public class GpsTrackpoint
{
	public GpsTrackpoint(LatLon position)
	{
		this.position = position;
	}
	
	/** whether this trackpoint is the first point in a new segment/track (no differentiation made) */
	public boolean isFirstPointInTrackSegment;
	
	public LatLon position;
	
	/** null if unknown. The time is only specified in tracks uploaded with the visibility
	 *  identifiable or trackable (see GpsTraceDetails) */
	public Date time;
	
	public Float horizontalDilutionOfPrecision;
	public Float elevation;
}
