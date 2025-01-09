package de.westnordost.osmapi.user;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import de.westnordost.osmapi.ConnectionTestFactory;
import de.westnordost.osmapi.common.errors.OsmAuthorizationException;

import static org.junit.Assert.*;

public class UserApiTest
{
	private UserApi privilegedApi;
	private UserApi anonymousApi;
	private UserApi unprivilegedApi;

	@Before public void setUp()
	{
		anonymousApi = new UserApi(ConnectionTestFactory.createConnection(null));
		privilegedApi = new UserApi(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_EVERYTHING));
		unprivilegedApi = new UserApi(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_NOTHING));
	}

	@Test public void getUserDetailsUnprivilegedFails()
	{
		assertThrows(OsmAuthorizationException.class, () -> unprivilegedApi.getMine());
	}

	@Test public void getUserDetailsPrivilegedWorks()
	{
		// should just not throw any exceptions. Since the user details may change, we do not
		// check the validity of the data here
		assertNotNull(privilegedApi.getMine());
	}

	@Test public void getUserInfo()
	{
		assertNull(unprivilegedApi.get(0L));
		assertNotNull(unprivilegedApi.get(1L));
		assertNotNull(anonymousApi.get(1L));
	}

	@Test public void getUserInfos()
	{
		assertEquals(2,unprivilegedApi.getAll(Arrays.asList(1L, 2L)).size());
		assertEquals(2,anonymousApi.getAll(Arrays.asList(1L, 2L)).size());
	}
}
