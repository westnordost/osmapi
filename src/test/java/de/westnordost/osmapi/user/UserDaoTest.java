package de.westnordost.osmapi.user;

import junit.framework.TestCase;

import java.util.List;

import de.westnordost.osmapi.ConnectionTestFactory;
import de.westnordost.osmapi.errors.OsmAuthorizationException;

public class UserDaoTest extends TestCase
{
	private UserDao privilegedDao;
	private UserDao unprivilegedDao;

	@Override
	protected void setUp()
	{
		privilegedDao = new UserDao(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_EVERYTHING));
		unprivilegedDao = new UserDao(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_NOTHING));
	}

	public void testGetUserDetailsUnprivileged()
	{
		try
		{
			unprivilegedDao.getUserDetails();
			fail();
		}
		catch(OsmAuthorizationException e)	{ }
	}

	public void testGetUserDetailsPrivileged()
	{
		// should just not throw any exceptions. Since the user details may change, we do not
		// check the validity of the data here
		assertNotNull(privilegedDao.getUserDetails());
	}

	public void testGetUserInfo()
	{
		assertNull(unprivilegedDao.getUserInfo(0L));
		assertNotNull(unprivilegedDao.getUserInfo(1L));
	}

	public void testGetPermissions()
	{
		List<String> unprivilegedPermissions = unprivilegedDao.getUserPermissions();
		assertFalse(unprivilegedPermissions.contains(Permission.CHANGE_PREFERENCES));
		assertFalse(unprivilegedPermissions.contains(Permission.MODIFY_MAP));
		assertFalse(unprivilegedPermissions.contains(Permission.READ_PREFERENCES_AND_USER_DETAILS));
		assertFalse(unprivilegedPermissions.contains(Permission.READ_PRIVATE_GPS_TRACES));
		assertFalse(unprivilegedPermissions.contains(Permission.UPLOAD_GPS_TRACES));
		// the unprivileged DAO has this one permission because an authorized App cannot have no
		// permissions at all (because then it is not authorized, see?)
		//assertFalse(unprivilegedPermissions.contains(Permission.WRITE_DIARY));
		assertFalse(unprivilegedPermissions.contains(Permission.WRITE_NOTES));

		List<String> privilegedPermissions = privilegedDao.getUserPermissions();
		assertTrue(privilegedPermissions.contains(Permission.CHANGE_PREFERENCES));
		assertTrue(privilegedPermissions.contains(Permission.MODIFY_MAP));
		assertTrue(privilegedPermissions.contains(Permission.READ_PREFERENCES_AND_USER_DETAILS));
		assertTrue(privilegedPermissions.contains(Permission.READ_PRIVATE_GPS_TRACES));
		assertTrue(privilegedPermissions.contains(Permission.UPLOAD_GPS_TRACES));
		assertTrue(privilegedPermissions.contains(Permission.WRITE_DIARY));
		assertTrue(privilegedPermissions.contains(Permission.WRITE_NOTES));
	}
}
