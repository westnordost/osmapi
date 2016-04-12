package de.westnordost.osmapi.user;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import de.westnordost.osmapi.ApiRequestWriter;
import de.westnordost.osmapi.ApiResponseReader;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.errors.OsmAuthorizationException;
import de.westnordost.osmapi.errors.OsmNotFoundException;
import de.westnordost.osmapi.xml.XmlWriter;

/** Get and set the user's custom preferences */
public class UserPreferencesDao
{
	private static final String USERPREFS = "user/preferences/";

	/** buffer size when reading single preferences */
	private static final int BUFFER_SIZE_PREFS = 256;

	private final OsmConnection osm;

	public UserPreferencesDao(OsmConnection osm)
	{
		this.osm = osm;
	}

	/**
	 * @return	a key-value map of the user preferences
	 * @throws OsmAuthorizationException if the application is not authenticated to read the
	 *                                     user's preferences. (Permission.READ_PREFERENCES_AND_USER_DETAILS) */
	public Map<String,String> getUserPreferences()
	{
		return osm.makeAuthenticatedRequest(USERPREFS, "GET", new PreferencesParser());
	}

	/**
	 * @param key the preference to query
	 * @return	the value of the given preference or null if the preference does not exist
	 *
	 * @throws	OsmAuthorizationException if the application is not authenticated to read the
	 *                                     user's preferences. (Permission.READ_PREFERENCES_AND_USER_DETAILS) */
	public String getUserPreference(String key)
	{
		String urlKey = urlEncode(key);
		ApiResponseReader<String> reader = new ApiResponseReader<String>()
		{
			public String parse(InputStream in) throws Exception
			{
				InputStreamReader isr = new InputStreamReader(in, osm.getCharset());
				BufferedReader reader = new BufferedReader(isr, BUFFER_SIZE_PREFS);
				return reader.readLine();
			}
		};
		try
		{
			return osm.makeAuthenticatedRequest(USERPREFS + urlKey, "GET", reader);
		}
		catch(OsmNotFoundException e)
		{
			return null;
		}
	}

	/**
	 * Sets all the given preference keys to the given preference values.
	 *
	 * @param preferences preferences to set. Each key and each value must be less than 256
	 *                    characters.
	 *
	 * @throws OsmAuthorizationException if this application is not authorized to change the
	 *                                    user's preferences. (Permission.CHANGE_PREFERENCES)
	 */
	public void setUserPreferences(Map<String, String> preferences)
	{
		// check it before sending it to the server in order to be able to raise a precise exception
		for(Map.Entry<String,String> preference : preferences.entrySet())
		{
			checkPreferenceKeyLength(preference.getKey());
			checkPreferenceValueLength(preference.getValue());
		}

		final Map<String, String> prefs = preferences;
		osm.makeAuthenticatedRequest(
				USERPREFS, "PUT", new XmlWriter()
				{
					@Override
					protected void write() throws IOException
					{
						begin("osm");
						begin("preferences");

						for (Map.Entry<String, String> preference : prefs.entrySet())
						{
							begin("preference");
							attribute("k", preference.getKey());
							attribute("v", preference.getValue());
							end();
						}

						end();
						end();
					}
				});
	}

	/**
	 * Sets the given preference key to the given preference value.
	 *
	 * @param key preference to set. Must be less than 256 characters.
	 * @param value preference to set. Must be less than 256 characters.
	 *
	 * @throws OsmAuthorizationException if this application is not authorized to change the
	 *                                    user's preferences. (Permission.CHANGE_PREFERENCES)
	 */
	public void setUserPreference(String key, String value)
	{
		String urlKey = urlEncode(key);
		checkPreferenceKeyLength(urlKey);
		checkPreferenceValueLength(value);

		osm.makeAuthenticatedRequest(USERPREFS + urlKey, "PUT", new StringWriter(value));
	}

	/**
	 * Deletes the given preference key from the user preferences
	 *
	 * @param key preference to delete. Must be less than 256 characters.
	 * @throws OsmAuthorizationException if this application is not authorized to change the
	 *                                    user's preferences. (Permission.CHANGE_PREFERENCES)
	 */
	public void deleteUserPreference(String key)
	{
		String urlKey = urlEncode(key);
		checkPreferenceKeyLength(urlKey);

		osm.makeAuthenticatedRequest(USERPREFS + urlKey, "DELETE");
	}

	private void checkPreferenceKeyLength(String key)
	{
		if(key.length() >= 256)
		{
			throw new IllegalArgumentException("Key \""+key+"\" must be less than 256 characters.");
		}
	}

	private void checkPreferenceValueLength(String value)
	{
		if(value.length() >= 256)
		{
			throw new IllegalArgumentException("Value \""+value+"\" must be less than 256 characters.");
		}
	}

	private class StringWriter implements ApiRequestWriter
	{
		private String data;

		public StringWriter(String data)
		{
			this.data = data;
		}

		@Override
		public String getContentType()
		{
			return "text/plain";
		}

		public void write(OutputStream out) throws IOException
		{
			out.write( data.getBytes(osm.getCharset()) );
		}
	}

	private String urlEncode(String text)
	{
		try
		{
			return URLEncoder.encode(text, osm.getCharset());
		}
		catch (UnsupportedEncodingException e)
		{
			// should never happen since we use UTF-8
			throw new RuntimeException(e);
		}
	}
}
