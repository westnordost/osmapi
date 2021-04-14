package de.westnordost.osmapi.user;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import de.westnordost.osmapi.ConnectionTestFactory;
import de.westnordost.osmapi.common.errors.OsmAuthorizationException;

import static org.junit.Assert.*;

public class UserApiTest
{
	private UserApi privilegedDao;
	private UserApi anonymousDao;
	private UserApi unprivilegedDao;

	@Before public void setUp()
	{
		anonymousDao = new UserApi(ConnectionTestFactory.createConnection(null));
		privilegedDao = new UserApi(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_EVERYTHING));
		unprivilegedDao = new UserApi(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_NOTHING));
	}

	@Test public void getUserDetailsUnprivilegedFails()
	{
		try
		{
			unprivilegedDao.getMine();
			fail();
		}
		catch(OsmAuthorizationException ignore) { }
	}

	@Test public void getUserDetailsPrivilegedWorks()
	{
		// should just not throw any exceptions. Since the user details may change, we do not
		// check the validity of the data here
		assertNotNull(privilegedDao.getMine());
	}

	@Test public void getUserInfo()
	{
		assertNull(unprivilegedDao.get(0L));
		assertNotNull(unprivilegedDao.get(1L));
		assertNotNull(anonymousDao.get(1L));
	}

	@Test public void getUserInfos()
	{
		assertEquals(2,unprivilegedDao.getAll(Arrays.asList(1L, 2L)).size());
		assertEquals(2,anonymousDao.getAll(Arrays.asList(1L, 2L)).size());
	}
}
