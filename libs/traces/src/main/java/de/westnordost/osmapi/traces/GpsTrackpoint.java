package de.westnordost.osmapi.traces;

import java.io.Serializable;
import java.time.Instant;

import de.westnordost.osmapi.map.data.LatLon;

public class GpsTrackpoint implements Serializable
{
	private static final long serialVersionUID = 2L;

	public GpsTrackpoint(LatLon position, Instant time)
	{
		this(position, time, false, null, null);
	}

	public GpsTrackpoint(
			LatLon position, Instant time, boolean isFirstPointInTrackSegment,
			Float horizontalDilutionOfPrecision, Float elevation
	)
	{
		this.position = position;
		this.time = time;
		this.isFirstPointInTrackSegment = isFirstPointInTrackSegment;
		this.horizontalDilutionOfPrecision = horizontalDilutionOfPrecision;
		this.elevation = elevation;
	}
	
	/** whether this trackpoint is the first point in a new segment/track (no differentiation made) */
	public final boolean isFirstPointInTrackSegment;
	
	public final LatLon position;
	public final Instant time;
	
	public final Float horizontalDilutionOfPrecision;
	public final Float elevation;
}
