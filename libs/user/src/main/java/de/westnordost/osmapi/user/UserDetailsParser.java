package de.westnordost.osmapi.user;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.map.data.OsmLatLon;

/** Parses private information for a user  */
public class UserDetailsParser extends UserInfoParser
{
	private static final String USER = "user",
	                            LANGUAGES = "languages";
	
	private List<String> languages;
	
	private UserDetails userDetails;

	public UserDetailsParser(Handler<UserInfo> handler)
	{
		super(handler);
	}

	@Override
	protected void createUser(long id, String name)
	{
		user = userDetails = new UserDetails(id, name);
	}
	
	@Override
	protected void onStartElement() throws ParseException
	{
		super.onStartElement();
		
		String name = getName();
		String parent = getParentName();

		if(LANGUAGES.equals(name))
		{
			languages = new ArrayList<>();
		}
		else if(USER.equals(parent))
		{
			switch(name)
			{
				case "contributor-terms":
					// pd is optional
					Boolean publicDomain = getBooleanAttribute("pd");
					if(publicDomain != null)
						userDetails.considersHisContributionsAsPublicDomain = publicDomain;
					break;
				case "home":
					userDetails.homeLocation = OsmLatLon.parseLatLon(getAttribute("lat"), getAttribute("lon"));
					userDetails.homeZoom = getByteAttribute("zoom");
					break;
			}
		}
		else if("messages".equals(parent))
		{
			switch(name)
			{
				case "received":
					userDetails.inboxMessageCount = getIntAttribute("count");
					userDetails.unreadMessagesCount = getIntAttribute("unread");
					break;
				case "sent":
					userDetails.sentMessagesCount = getIntAttribute("count");
					break;
			}
		}
	}
	
	@Override
	protected void onEndElement()
	{
		String name = getName();
		String parent = getParentName();

		if(USER.equals(name))
		{
			handler.handle(userDetails);
			user = userDetails = null;
		}
		else if(LANGUAGES.equals(name))
		{
			userDetails.preferredLanguages = languages;
			languages = null;
		}
		if(LANGUAGES.equals(parent))
		{
			if("lang".equals(name))
			{
				assert languages != null;
				languages.add(getText());
			}
		}
	}
}
