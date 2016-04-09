package de.westnordost.osmapi.user;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

import de.westnordost.osmapi.ConnectionTestFactory;
import de.westnordost.osmapi.errors.OsmAuthenticationException;

public class UserPreferencesDaoTest extends TestCase
{
	private UserPreferencesDao privilegedDao;
	private UserPreferencesDao unprivilegedDao;

	@Override
	protected void setUp() throws Exception
	{
		privilegedDao = new UserPreferencesDao(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_EVERYTHING));
		unprivilegedDao = new UserPreferencesDao(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_NOTHING));
	}

	public void testUnprivileged()
	{
		// the unprivileged DAO may do nothing here, so lets just do it in one test case...

		try {
			unprivilegedDao.deleteUserPreference("A");
			fail();
		}
		catch (OsmAuthenticationException e) { }

		try {
			unprivilegedDao.getUserPreference("A");
			fail();
		}
		catch (OsmAuthenticationException e) { }

		try {
			unprivilegedDao.getUserPreferences();
			fail();
		}
		catch (OsmAuthenticationException e) { }

		try {
			unprivilegedDao.setUserPreference("A","a");
			fail();
		}
		catch (OsmAuthenticationException e) { }

		try {
			unprivilegedDao.setUserPreferences(new HashMap<String, String>());
			fail();
		}
		catch (OsmAuthenticationException e) { }
	}

	public void testSetGetAndDeleteUserPreference()
	{
		privilegedDao.setUserPreference("A","a");
		assertEquals("a",privilegedDao.getUserPreference("A"));
		privilegedDao.deleteUserPreference("A");
		assertNull(privilegedDao.getUserPreference("A"));
	}

	public void testSetAndGetUserPreferences()
	{
		Map<String,String> preferences = new HashMap<>();
		preferences.put("D", "d");
		preferences.put("E", "e");

		privilegedDao.setUserPreferences(preferences);
		Map<String,String> updatedPreferences = privilegedDao.getUserPreferences();
		assertEquals(preferences, updatedPreferences);

		// deleting a previously set user preferences by omitting it
		preferences.remove("D");

		privilegedDao.setUserPreferences(preferences);
		updatedPreferences = privilegedDao.getUserPreferences();
		assertEquals(preferences, updatedPreferences);
	}
}
