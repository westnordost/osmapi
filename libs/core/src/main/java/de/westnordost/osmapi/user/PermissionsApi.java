package de.westnordost.osmapi.user;

import java.util.List;

import de.westnordost.osmapi.OsmConnection;

/** Get the user permissions */
public class PermissionsApi
{
	private final OsmConnection osm;

	public PermissionsApi(OsmConnection osm)
	{
		this.osm = osm;
	}
	
	/** @return a list of permissions the user has on this server (=are granted though OAuth). Use
	 *          the constants defined in the Permission, i.e Permission.CHANGE_PREFERENCES */
	public List<String> get()
	{
		return osm.makeAuthenticatedRequest("permissions", "GET", new PermissionsParser());
	}
}
