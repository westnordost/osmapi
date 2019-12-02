package de.westnordost.osmapi.user;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

import de.westnordost.osmapi.ConnectionTestFactory;
import de.westnordost.osmapi.common.errors.OsmAuthorizationException;

public class UserPreferencesDaoTest extends TestCase
{
	private UserPreferencesDao privilegedDao;
	private UserPreferencesDao unprivilegedDao;

	@Override
	protected void setUp()
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
			unprivilegedDao.delete("A");
			fail();
		}
		catch (OsmAuthorizationException ignore) { }

		try {
			unprivilegedDao.get("A");
			fail();
		}
		catch (OsmAuthorizationException ignore) { }

		try {
			unprivilegedDao.getAll();
			fail();
		}
		catch (OsmAuthorizationException ignore) { }

		try {
			unprivilegedDao.set("A","a");
			fail();
		}
		catch (OsmAuthorizationException ignore) { }

		try {
			unprivilegedDao.setAll(new HashMap<String, String>());
			fail();
		}
		catch (OsmAuthorizationException ignore) { }
	}

	public void testSetGetAndDeleteUserPreference()
	{
		privilegedDao.set("A","a");
		assertEquals("a",privilegedDao.get("A"));
		privilegedDao.delete("A");
		assertNull(privilegedDao.get("A"));
	}
	
	public void testKeyTooLong()
	{
		try
		{
			privilegedDao.set(tooLong(), "jo");
			fail();
		}
		catch(IllegalArgumentException ignore) { }
	}

	public void testValueTooLong()
	{
		try
		{
			privilegedDao.set("jo", tooLong());
			fail();
		}
		catch(IllegalArgumentException ignore) { }
	}
	
	private static String tooLong()
	{
		String result = "";
		for(int i=0; i<=256; ++i) result += "x";
		return result;
	}
	
	public void testSetAndGetUserPreferences()
	{
		Map<String,String> preferences = new HashMap<>();
		preferences.put("D", "d");
		preferences.put("E", "e");

		privilegedDao.setAll(preferences);
		Map<String,String> updatedPreferences = privilegedDao.getAll();
		assertEquals(preferences, updatedPreferences);

		// deleting a previously set user preferences by omitting it
		preferences.remove("D");

		privilegedDao.setAll(preferences);
		updatedPreferences = privilegedDao.getAll();
		assertEquals(preferences, updatedPreferences);
	}
}
