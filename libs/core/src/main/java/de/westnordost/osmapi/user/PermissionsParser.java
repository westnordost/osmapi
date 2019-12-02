package de.westnordost.osmapi.user;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.westnordost.osmapi.ApiResponseReader;
import de.westnordost.osmapi.common.XmlParser;

/** Parses the permissions this osm user has on this server (API 0.6). */
public class PermissionsParser extends XmlParser implements ApiResponseReader<List<String>>
{
	private static final String PERMISSIONS = "permissions";

	List<String> permissions;

	public List<String> parse(InputStream in) throws IOException
	{
		doParse(in);
		return permissions;
	}

	@Override
	protected void onStartElement()
	{
		if(PERMISSIONS.equals(getName()))
		{
			permissions = new ArrayList<>();
		}
		else if(PERMISSIONS.equals(getParentName()))
		{
			if("permission".equals(getName()))
			{
				permissions.add(getAttribute("name"));
			}
		}
	}

	@Override
	protected void onEndElement()
	{
		// nothing...
	}
}
