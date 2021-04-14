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
	private UserPreferencesApi privilegedApi;
	private UserPreferencesApi unprivilegedApi;

	@Before	public void setUp()
	{
		privilegedApi = new UserPreferencesApi(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_EVERYTHING));
		unprivilegedApi = new UserPreferencesApi(ConnectionTestFactory.createConnection(
				ConnectionTestFactory.User.ALLOW_NOTHING));
	}

	@Test public void unprivileged()
	{
		// the unprivileged api may do nothing here, so lets just do it in one test case...

		try {
			unprivilegedApi.delete("A");
			fail();
		}
		catch (OsmAuthorizationException ignore) { }

		try {
			unprivilegedApi.get("A");
			fail();
		}
		catch (OsmAuthorizationException ignore) { }

		try {
			unprivilegedApi.getAll();
			fail();
		}
		catch (OsmAuthorizationException ignore) { }

		try {
			unprivilegedApi.set("A","a");
			fail();
		}
		catch (OsmAuthorizationException ignore) { }

		try {
			unprivilegedApi.setAll(new HashMap<String, String>());
			fail();
		}
		catch (OsmAuthorizationException ignore) { }
	}

	@Test public void setGetAndDeleteUserPreference()
	{
		privilegedApi.set("A","a");
		assertEquals("a",privilegedApi.get("A"));
		privilegedApi.delete("A");
		assertNull(privilegedApi.get("A"));
	}
	
	@Test public void keyTooLong()
	{
		try
		{
			privilegedApi.set(tooLong(), "jo");
			fail();
		}
		catch(IllegalArgumentException ignore) { }
	}

	@Test public void valueTooLong()
	{
		try
		{
			privilegedApi.set("jo", tooLong());
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

		privilegedApi.setAll(preferences);
		Map<String,String> updatedPreferences = privilegedApi.getAll();
		assertEquals(preferences, updatedPreferences);

		// deleting a previously set user preferences by omitting it
		preferences.remove("D");

		privilegedApi.setAll(preferences);
		updatedPreferences = privilegedApi.getAll();
		assertEquals(preferences, updatedPreferences);
	}
}
