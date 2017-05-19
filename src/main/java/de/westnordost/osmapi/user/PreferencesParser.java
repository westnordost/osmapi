package de.westnordost.osmapi.user;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.westnordost.osmapi.ApiResponseReader;
import de.westnordost.osmapi.common.XmlParser;

/** Parses the preferences of this osm user on this server (API 0.6). */
public class PreferencesParser extends XmlParser implements ApiResponseReader<Map<String, String>>
{
	private static final String PREFERENCES = "preferences";

	Map<String,String> preferences;

	@Override
	public Map<String,String> parse(InputStream in) throws IOException
	{
		doParse(in);
		return preferences;
	}

	@Override
	protected void onStartElement()
	{
		if(PREFERENCES.equals(getName()))
		{
			preferences = new HashMap<>();
		}
		else if(PREFERENCES.equals(getParentName()))
		{
			if("preference".equals(getName()))
			{
				preferences.put(getAttribute("k"), getAttribute("v"));
			}
		}
	}

	@Override
	protected void onEndElement()
	{
		// nothing...
	}
}
