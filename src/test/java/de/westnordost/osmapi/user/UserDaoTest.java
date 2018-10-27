package de.westnordost.osmapi.user;

import junit.framework.TestCase;

import java.util.Arrays;

import de.westnordost.osmapi.ConnectionTestFactory;
import de.westnordost.osmapi.common.errors.OsmAuthorizationException;
import de.westnordost.osmapi.notes.NotesDao;

public class UserDaoTest extends TestCase
{
	private UserDao privilegedDao;
	private UserDao anonymousDao;
	private UserDao unprivilegedDao;

	@Override
	protected void setUp()
	{
		anonymousDao = new UserDao(ConnectionTestFactory.createConnection(null));
		privilegedDao = new UserDao(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_EVERYTHING));
		unprivilegedDao = new UserDao(ConnectionTestFactory.createConnection(
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
	}

	public void testGetUserInfoAsAnonymousFails()
	{
		try
		{
			anonymousDao.get(0L);
			fail();
		}
		catch (OsmAuthorizationException ignore) {}
	}

	public void testGetUserInfos()
	{
		assertEquals(2,unprivilegedDao.getAll(Arrays.asList(1L, 2L)).size());
	}

	public void testGetUserInfosAsAnonymousFails()
	{
		try
		{
			anonymousDao.getAll(Arrays.asList(1L, 2L));
			fail();
		}
		catch (OsmAuthorizationException ignore) {}
	}
}
