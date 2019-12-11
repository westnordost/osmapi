package de.westnordost.osmapi.user;

/** Simply some constants for known permissions. See
 *  https://github.com/openstreetmap/openstreetmap-website/blob/master/db/structure.sql#L1108 */
public final class Permission
{
	public static final String 
		READ_PREFERENCES_AND_USER_DETAILS = "allow_read_prefs",
		CHANGE_PREFERENCES = "allow_write_prefs",
		WRITE_DIARY = "allow_write_diary",
		MODIFY_MAP = "allow_write_api",
		READ_GPS_TRACES = "allow_read_gpx",
		WRITE_GPS_TRACES = "allow_write_gpx",
		WRITE_NOTES = "allow_write_notes";

	private Permission()
	{
		// not instantiable
	}
}
