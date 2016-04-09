package de.westnordost.osmapi.changesets;


import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.ApiResponseReader;
import de.westnordost.osmapi.Handler;
import de.westnordost.osmapi.OsmXmlDateFormat;
import de.westnordost.osmapi.map.data.Bounds;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.user.OsmUser;
import de.westnordost.osmapi.user.User;
import de.westnordost.osmapi.xml.XmlParser;

/** Parses changeset infos. It parses the XML naively, i.e. it does not care where in the XML the
 *  notes nodes are. */
public class ChangesetParser extends XmlParser implements ApiResponseReader<Void>
{
	private static final SimpleDateFormat DATE_FORMAT = new OsmXmlDateFormat();

	private static final String
			TAG = "tag",
			CHANGESET = "changeset",
			COMMENT = "comment",
			TEXT = "text";

	private Map<Long, User> users;

	private Handler<ChangesetInfo> handler;

	private OsmChangesetInfo currentChangesetInfo;
	private ChangesetComment currentComment;
	private List<ChangesetComment> comments;
	private Map<String, String> tags;

	public ChangesetParser(Handler<ChangesetInfo> handler)
	{
		this.handler = handler;
	}

	@Override
	public Void parse(InputStream in)
	{
		users = new HashMap<>();
		doParse(in);
		users = null;
		return null;
	}

	@Override
	protected void onStartElement() throws ParseException
	{
		String name = getName();

		if(CHANGESET.equals(name))
		{
			currentChangesetInfo = parseChangeset();
		}
		else if(TAG.equals(name))
		{
			if(tags == null)
			{
				tags = new HashMap<>();
			}
			tags.put(getAttribute("k"), getAttribute("v"));
		}
		else if(COMMENT.equals(name))
		{
			currentComment = parseChangesetComment();
		}
	}

	private OsmChangesetInfo parseChangeset() throws ParseException
	{
		Bounds bounds = null;
		if(getAttribute("min_lat") != null)
		{
			bounds = new Bounds(
					OsmLatLon.parseLatLon(getAttribute("min_lat"), getAttribute("min_lon")),
					OsmLatLon.parseLatLon(getAttribute("max_lat"), getAttribute("max_lon")));
		}

		String closedAtStr = getAttribute("closed_at");
		Date closedAt = null;
		if(closedAtStr != null)
		{
			closedAt = DATE_FORMAT.parse(closedAtStr);
		}

		User user = parseUser();
		// user must be defined for a changeset
		if(user == null) throw new NullPointerException();

		return new OsmChangesetInfo(
				getLongAttribute("id"),
				DATE_FORMAT.parse(getAttribute("created_at")),
				closedAt,
				user,
				bounds,
				getBooleanAttribute("open"),
				getIntAttribute("comments_count")
		);
	}

	private ChangesetComment parseChangesetComment() throws ParseException
	{
		return new ChangesetComment(
				parseUser(),
				DATE_FORMAT.parse(getAttribute("date"))
		);
	}

	private User parseUser()
	{
		Long userId = getLongAttribute("uid");
		if(userId == null) return null;

		if(!users.containsKey(userId))
		{
			User user = new OsmUser(userId, getAttribute("user"));
			users.put(userId, user);
			return user;
		}
		return users.get(userId);
	}

	@Override
	protected void onEndElement()
	{
		String name = getName();

		if(TEXT.equals(name))
		{
			currentComment.setText(getText());
		}
		if(COMMENT.equals(name))
		{
			if(comments == null)
			{
				comments = new ArrayList<>();
			}
			comments.add(currentComment);
			currentComment = null;
		}
		else if(CHANGESET.equals(name))
		{
			currentChangesetInfo.setTags(tags);
			currentChangesetInfo.setComments(comments);

			handler.handle(currentChangesetInfo);
			currentChangesetInfo = null;

			tags = null;
			comments = null;
		}

	}
}
