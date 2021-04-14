package de.westnordost.osmapi.user;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import de.westnordost.osmapi.ConnectionTestFactory;
import de.westnordost.osmapi.common.errors.OsmAuthorizationException;

import static org.junit.Assert.*;

public class UserPreferencesApiTest
{
	private UserPreferencesApi privilegedDao;
	private UserPreferencesApi unprivilegedDao;

	@Before	public void setUp()
	{
		privilegedDao = new UserPreferencesApi(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_EVERYTHING));
		unprivilegedDao = new UserPreferencesApi(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_NOTHING));
	}

	@Test public void unprivileged()
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

	@Test public void setGetAndDeleteUserPreference()
	{
		privilegedDao.set("A","a");
		assertEquals("a",privilegedDao.get("A"));
		privilegedDao.delete("A");
		assertNull(privilegedDao.get("A"));
	}
	
	@Test public void keyTooLong()
	{
		try
		{
			privilegedDao.set(tooLong(), "jo");
			fail();
		}
		catch(IllegalArgumentException ignore) { }
	}

	@Test public void valueTooLong()
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
	
	@Test public void setAndGetUserPreferences()
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
