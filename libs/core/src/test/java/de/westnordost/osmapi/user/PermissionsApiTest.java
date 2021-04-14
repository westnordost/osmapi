package de.westnordost.osmapi.user;

import org.junit.Test;

import java.util.List;

import de.westnordost.osmapi.ConnectionTestFactory;

import static org.junit.Assert.*;

public class PermissionsApiTest
{
	@Test public void getPermissions()
	{
		PermissionsApi privilegedDao = new PermissionsApi(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_EVERYTHING));
		PermissionsApi unprivilegedDao = new PermissionsApi(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_NOTHING));
		
		List<String> unprivilegedPermissions = unprivilegedDao.get();
		assertFalse(unprivilegedPermissions.contains(Permission.CHANGE_PREFERENCES));
		assertFalse(unprivilegedPermissions.contains(Permission.MODIFY_MAP));
		assertFalse(unprivilegedPermissions.contains(Permission.READ_PREFERENCES_AND_USER_DETAILS));
		assertFalse(unprivilegedPermissions.contains(Permission.READ_GPS_TRACES));
		assertFalse(unprivilegedPermissions.contains(Permission.WRITE_GPS_TRACES));
		// the unprivileged DAO has this one permission because an authorized App cannot have no
		// permissions at all (because then it is not authorized, see?)
		//assertFalse(unprivilegedPermissions.contains(Permission.WRITE_DIARY));
		assertFalse(unprivilegedPermissions.contains(Permission.WRITE_NOTES));

		List<String> privilegedPermissions = privilegedDao.get();
		assertTrue(privilegedPermissions.contains(Permission.CHANGE_PREFERENCES));
		assertTrue(privilegedPermissions.contains(Permission.MODIFY_MAP));
		assertTrue(privilegedPermissions.contains(Permission.READ_PREFERENCES_AND_USER_DETAILS));
		assertTrue(privilegedPermissions.contains(Permission.READ_GPS_TRACES));
		assertTrue(privilegedPermissions.contains(Permission.WRITE_GPS_TRACES));
		assertTrue(privilegedPermissions.contains(Permission.WRITE_DIARY));
		assertTrue(privilegedPermissions.contains(Permission.WRITE_NOTES));
	}
}
