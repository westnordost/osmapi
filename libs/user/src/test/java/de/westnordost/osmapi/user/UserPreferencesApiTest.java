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
		Class<OsmAuthorizationException> e = OsmAuthorizationException.class;
		// the unprivileged api may do nothing here, so lets just do it in one test case...
		assertThrows(e, () -> unprivilegedApi.delete("A"));
		assertThrows(e, () -> unprivilegedApi.get("A"));
		assertThrows(e, () -> unprivilegedApi.getAll());
		assertThrows(e, () -> unprivilegedApi.set("A","a"));
		assertThrows(e, () -> unprivilegedApi.setAll(new HashMap<>()));
		assertThrows(e, () -> unprivilegedApi.delete("A"));
		assertThrows(e, () -> unprivilegedApi.delete("A"));
		assertThrows(e, () -> unprivilegedApi.delete("A"));
		assertThrows(e, () -> unprivilegedApi.delete("A"));
		assertThrows(e, () -> unprivilegedApi.delete("A"));
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
		assertThrows(IllegalArgumentException.class, () -> privilegedApi.set(tooLong(), "jo"));
	}

	@Test public void valueTooLong()
	{
		assertThrows(IllegalArgumentException.class, () -> privilegedApi.set("jo", tooLong()));
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
