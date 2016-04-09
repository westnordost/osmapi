package de.westnordost.osmapi.map;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.westnordost.osmapi.ApiResponseReader;
import de.westnordost.osmapi.map.data.Bounds;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.map.data.OsmNode;
import de.westnordost.osmapi.map.data.OsmRelation;
import de.westnordost.osmapi.map.data.OsmRelationMember;
import de.westnordost.osmapi.map.data.OsmWay;
import de.westnordost.osmapi.map.data.RelationMember;
import de.westnordost.osmapi.map.handler.MapDataHandler;
import de.westnordost.osmapi.OsmXmlDateFormat;
import de.westnordost.osmapi.changesets.Changeset;
import de.westnordost.osmapi.changesets.OsmChangeset;
import de.westnordost.osmapi.user.OsmUser;
import de.westnordost.osmapi.user.User;
import de.westnordost.osmapi.xml.XmlParser;

/** Parses the map data. It parses the XML naively, i.e. it does not care where in the XML the map
 *  data is. */
public class MapDataParser extends XmlParser implements ApiResponseReader<Void>
{
	private static final SimpleDateFormat DATE_FORMAT = new OsmXmlDateFormat();

	private static final String NODE = "node",
	                            WAY = "way",
	                            RELATION = "relation";

	private MapDataHandler handler;

	/* temporary maps so we do not parse and hold many times the same user and changeset */
	private Map<Long, User> users;
	private Map<Long, Changeset> changesets;

	private long id = -1;
	private int version = 0;
	Long changesetId;

	private LatLon pos;
	private Map<String, String> tags;
	private List<RelationMember> members;
	private List<Long> nodes;

	public MapDataParser( MapDataHandler handler )
	{
		this.handler = handler;
	}

	@Override
	public Void parse(InputStream in)
	{
		users = new HashMap<>();
		changesets = new HashMap<>();

		doParse(in);
		users = null;
		changesets = null;

		return null;
	}

	@Override
	protected void onStartElement() throws ParseException
	{
		String name = getName();

		if(name.equals("tag"))
		{
			if(tags == null)
			{
				tags = new HashMap<>();
			}
			tags.put(getAttribute("k"), getAttribute("v"));
		}
		else if(name.equals("nd"))
		{
			if(nodes == null)
			{
				nodes = new LinkedList<>();
			}
			nodes.add( getLongAttribute("ref") );
		}
		else if(name.equals("member"))
		{
			if(members == null)
			{
				members = new ArrayList<>();
			}
			members.add( new OsmRelationMember(
					getLongAttribute("ref"),
					getAttribute("role"),
					Element.Type.valueOf(getAttribute("type").toUpperCase(Locale.UK))
			));
		}
		else if (name.equals("bounds"))
		{
			Bounds bounds = new Bounds(
					OsmLatLon.parseLatLon(getAttribute("minlat"), getAttribute("minlon")),
					OsmLatLon.parseLatLon(getAttribute("maxlat"), getAttribute("maxlon")));
			handler.handle(bounds);
		}
		else if (name.equals(NODE) || name.equals(WAY) || name.equals(RELATION))
		{
			changesetId = getLongAttribute("changeset");
			if(changesetId != null && !changesets.containsKey(changesetId))
			{
				changesets.put( changesetId, new OsmChangeset(
						changesetId,
						parseDate(),
						parseUser()));
			}

			id = getLongAttribute("id");
			version = getIntAttribute("version");

			if(name.equals(NODE))
			{
				pos = OsmLatLon.parseLatLon(getAttribute("lat"), getAttribute("lon"));
			}
		}
	}

	private Date parseDate() throws ParseException
	{
		String timestamp = getAttribute("timestamp");
		if(timestamp == null) return null;

		return DATE_FORMAT.parse(timestamp);
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

		if(name.equals(NODE))
		{
			handler.handle(
					new OsmNode(id, version, pos, tags, changesets.get(changesetId))
			);

			pos = null;
		}
		else if(name.equals(WAY))
		{
			handler.handle(
					new OsmWay(id, version, nodes, tags, changesets.get(changesetId))
			);
			nodes = null;
		}
		else if(name.equals(RELATION))
		{
			handler.handle(
					new OsmRelation(id, version, members, tags, changesets.get(changesetId))
			);
			members = null;
		}

		if (name.equals(NODE) || name.equals(WAY) || name.equals(RELATION))
		{
			tags = null;
		}
	}
}
