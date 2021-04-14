package de.westnordost.osmapi.user;

import junit.framework.TestCase;

import java.util.Arrays;

import de.westnordost.osmapi.ConnectionTestFactory;
import de.westnordost.osmapi.common.errors.OsmAuthorizationException;

public class UserApiTest extends TestCase
{
	private UserApi privilegedDao;
	private UserApi anonymousDao;
	private UserApi unprivilegedDao;

	@Override
	protected void setUp()
	{
		anonymousDao = new UserApi(ConnectionTestFactory.createConnection(null));
		privilegedDao = new UserApi(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_EVERYTHING));
		unprivilegedDao = new UserApi(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_NOTHING));
	}

	public void testGetUserDetailsUnprivilegedFails()
	{
		try
		{
			unprivilegedDao.getMine();
			fail();
		}
		catch(OsmAuthorizationException ignore) { }
	}

	public void testGetUserDetailsPrivilegedWorks()
	{
		// should just not throw any exceptions. Since the user details may change, we do not
		// check the validity of the data here
		assertNotNull(privilegedDao.getMine());
	}

	public void testGetUserInfo()
	{
		assertNull(unprivilegedDao.get(0L));
		assertNotNull(unprivilegedDao.get(1L));
		assertNotNull(anonymousDao.get(1L));
	}

	public void testGetUserInfos()
	{
		assertEquals(2,unprivilegedDao.getAll(Arrays.asList(1L, 2L)).size());
		assertEquals(2,anonymousDao.getAll(Arrays.asList(1L, 2L)).size());
	}
}
