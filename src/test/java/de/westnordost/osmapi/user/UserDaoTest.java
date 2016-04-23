package de.westnordost.osmapi.user;

import junit.framework.TestCase;

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
			unprivilegedDao.getMine();
			fail();
		}
		catch(OsmAuthorizationException e)	{ }
	}

	public void testGetUserDetailsPrivileged()
	{
		// should just not throw any exceptions. Since the user details may change, we do not
		// check the validity of the data here
		assertNotNull(privilegedDao.getMine());
	}

	public void testGetUserInfo()
	{
		assertNull(unprivilegedDao.get(0L));
		assertNotNull(unprivilegedDao.get(1L));
	}
}
