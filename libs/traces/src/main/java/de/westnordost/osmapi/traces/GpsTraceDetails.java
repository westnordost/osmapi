package de.westnordost.osmapi.traces;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import de.westnordost.osmapi.map.data.LatLon;

/** Details aka meta informations for a GPS, so not the actual trace */
public class GpsTraceDetails implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	/** See http://wiki.openstreetmap.org/wiki/Visibility_of_GPS_traces for a more detailed description */
	public enum Visibility
	{
		PRIVATE,
		TRACKABLE,
		PUBLIC,
		IDENTIFIABLE
	}
	
	public long id;
	public String name;
	/** null until the whole trace has been imported completely by the server */
	public LatLon position;
	/** (current) user name of the uploader */
	public String userName;
	public Visibility visibility;
	/** whether the server did not complete the import of the trace yet */
	public boolean pending;
	public Date date;
	/** may be empty/null */
	public String description;
	/** may be empty/null */
	public List<String> tags;
}
