package de.westnordost.osmapi.user;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.westnordost.osmapi.ApiResponseReader;
import de.westnordost.osmapi.OsmXmlDateFormat;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.xml.XmlParser;

/** Parses information for a user (API 0.6, since 2012).
 *
 *  See https://github.com/openstreetmap/openstreetmap-website/blob/master/app/views/user/api_read.builder
 *  for what the user actually sends. */
public class UserInfoParser extends XmlParser implements ApiResponseReader<UserDetails>
{
	private static final String USER = "user",
	                            LANGUAGES = "languages",
	                            ROLES = "roles",
	                            BLOCKS = "blocks";

	private static final SimpleDateFormat DATE_FORMAT = new OsmXmlDateFormat();

	private List<String> languages;
	private List<String> roles;
	private OsmUserDetails user;

	@Override
	public UserDetails parse(InputStream in)
	{
		doParse(in);
		return user;
	}

	@Override
	protected void onStartElement() throws ParseException
	{
		String name = getName();
		String parent = getParentName();

		if(USER.equals(name))
		{
			user = new OsmUserDetails(
					getLongAttribute("id"),
					getAttribute("display_name"),
					DATE_FORMAT.parse(getAttribute("account_created")));
		}
		else if(LANGUAGES.equals(name))
		{
			languages = new ArrayList<>();
		}

		else if(USER.equals(parent))
		{
			switch(name)
			{
				case "img":
					user.setProfileImageUrl(getAttribute("href"));
					break;
				case "changesets":
					user.setChangesetsCount(getIntAttribute("count"));
					break;
				case "traces":
					user.setGpsTracesCount(getIntAttribute("count"));
					break;
				case "contributor-terms":
					user.setContributorTermsAgreed(getBooleanAttribute("agreed"));
					// pd is optional
					Boolean publicDomain = getBooleanAttribute("pd");
					if(publicDomain != null)
						user.setConsidersHisContributionsAsPublicDomain(publicDomain);
					break;
				case "home":
					user.setHomeLocation(
							OsmLatLon.parseLatLon(getAttribute("lat"), getAttribute("lon")));
					user.setHomeZoom(getByteAttribute("zoom"));
					break;
			}
		}
		else if("messages".equals(parent))
		{
			switch(name)
			{
				case "received":
					user.setInboxMessageCount(getIntAttribute("count"));
					user.setUnreadMessagesCount(getIntAttribute("unread"));
					break;
				case "sent":
					user.setSentMessagesCount(getIntAttribute("count"));
					break;
			}
		}
		else if(BLOCKS.equals(parent))
		{
			if("received".equals(name))
			{
				user.setIsBlocked(getIntAttribute("active") != 0);
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

		if(LANGUAGES.equals(name))
		{
			user.setPreferredLanguages(languages);
			languages = null;
		}
		else if(ROLES.equals(name))
		{
			user.setRoles(roles);
			roles = null;
		}

		if(USER.equals(parent))
		{
			if("description".equals(name))
			{
				user.setProfileDescription(getText());
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
		else if(LANGUAGES.equals(parent))
		{
			if("lang".equals(name))
			{
				assert languages != null;
				languages.add(getText());
			}
		}
	}
}
