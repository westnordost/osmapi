package de.westnordost.osmapi.changesets;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.ApiResponseReader;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.OsmXmlDateFormat;
import de.westnordost.osmapi.common.XmlParser;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.user.User;

/**
 * Parses changeset infos. It parses the XML naively, i.e. it does not care
 * where in the XML the notes nodes are.
 */
public class ChangesetParser extends XmlParser implements ApiResponseReader<Void>
{
	private static final String TAG = "tag", CHANGESET = "changeset", COMMENT = "comment",
			TEXT = "text";

	private final OsmXmlDateFormat dateFormat = new OsmXmlDateFormat();
	
	private Map<Long, User> users;

	private Handler<ChangesetInfo> handler;

	private ChangesetInfo currentChangesetInfo;
	private ChangesetNote currentComment;
	private List<ChangesetNote> comments;
	private Map<String, String> tags;

	public ChangesetParser(Handler<ChangesetInfo> handler)
	{
		this.handler = handler;
	}

	@Override
	public Void parse(InputStream in) throws IOException
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

	private ChangesetInfo parseChangeset() throws ParseException
	{
		BoundingBox bounds = null;
		if(getAttribute("min_lat") != null)
		{
			bounds = new BoundingBox(OsmLatLon.parseLatLon(getAttribute("min_lat"),
					getAttribute("min_lon")), OsmLatLon.parseLatLon(getAttribute("max_lat"),
					getAttribute("max_lon")));
		}

		String closedAtStr = getAttribute("closed_at");
		Date closedAt = null;
		if(closedAtStr != null)
		{
			closedAt = dateFormat.parse(closedAtStr);
		}

		User user = parseUser();
		// user must be defined for a changeset
		if(user == null)
			throw new NullPointerException();

		ChangesetInfo result = new ChangesetInfo();
		result.id = getLongAttribute("id");
		result.date = result.dateCreated = dateFormat.parse(getAttribute("created_at"));
		result.dateClosed = closedAt;
		result.user = user;
		result.boundingBox = bounds;
		result.isOpen = getBooleanAttribute("open");
		result.notesCount = getIntAttribute("comments_count");
		result.changesCount = getIntAttribute("changes_count");
		return result;
	}

	private ChangesetNote parseChangesetComment() throws ParseException
	{
		ChangesetNote comment = new ChangesetNote();
		comment.user = parseUser();
		comment.date = dateFormat.parse(getAttribute("date"));
		return comment;
	}

	private User parseUser()
	{
		Long userId = getLongAttribute("uid");
		if(userId == null)
			return null;

		if(!users.containsKey(userId))
		{
			User user = new User(userId, getAttribute("user"));
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
			currentComment.text = getText();
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
			currentChangesetInfo.tags = tags;
			currentChangesetInfo.discussion = comments;

			handler.handle(currentChangesetInfo);
			currentChangesetInfo = null;

			tags = null;
			comments = null;
		}

	}
}
