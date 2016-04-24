package de.westnordost.osmapi.user;

import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import de.westnordost.osmapi.ApiResponseReader;
import de.westnordost.osmapi.common.OsmXmlDateFormat;
import de.westnordost.osmapi.common.XmlParser;

/** Parses information for a user (API 0.6, since 2012).
 *
 *  See https://github.com/openstreetmap/openstreetmap-website/blob/master/app/views/user/api_read.builder
 *  for what the user actually sends. */
public class UserInfoParser extends XmlParser implements ApiResponseReader<UserInfo>
{
	private static final String USER = "user",
	                            ROLES = "roles",
	                            BLOCKS = "blocks";

	private final OsmXmlDateFormat dateFormat = new OsmXmlDateFormat();
	
	private List<String> roles;
	
	protected UserInfo user;

	@Override
	public UserInfo parse(InputStream in)
	{
		doParse(in);
		return user;
	}

	protected void createUser(long id, String name)
	{
		user = new UserInfo(id, name);
	}
	
	@Override
	protected void onStartElement() throws ParseException
	{
		String name = getName();
		String parent = getParentName();

		if(USER.equals(name))
		{
			createUser(getLongAttribute("id"),getAttribute("display_name"));
			user.createdDate = dateFormat.parse(getAttribute("account_created"));
		}
		
		if(USER.equals(parent))
		{
			switch(name)
			{
				case "img":
					user.profileImageUrl = getAttribute("href");
					break;
				case "changesets":
					user.changesetsCount = getIntAttribute("count");
					break;
				case "traces":
					user.gpsTracesCount = getIntAttribute("count");
					break;
				case "contributor-terms":
					user.hasAgreedToContributorTerms = getBooleanAttribute("agreed");
					break;
			}
		}
		else if(BLOCKS.equals(parent))
		{
			if("received".equals(name))
			{
				user.isBlocked = getIntAttribute("active") != 0;
				/* There is more information that could be parsed here.
				   But I really do not see any sense for the user of the API to know whether a user
				   was once blocked but not anymore or the number of blocks that are active. Tell me
				   if you think otherwise. */
			}
		}
	}

	@Override
	protected void onEndElement()
	{
		String name = getName();
		String parent = getParentName();

		if(ROLES.equals(name))
		{
			user.roles = roles;
			roles = null;
		}

		if(USER.equals(parent))
		{
			if("description".equals(name))
			{
				user.profileDescription = getText();
			}
		}
		else if(ROLES.equals(parent))
		{
			if("role".equals(name))
			{
				// the vast majority of users has no roles (but still there is an empty <roles>
				// element in the xml), so we create the list lazily
				if(roles == null)
				{
					roles = new ArrayList<>(1);
				}
				roles.add(getText());
			}
		}
	}
}
