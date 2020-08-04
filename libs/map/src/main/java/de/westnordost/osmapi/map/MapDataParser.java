package de.westnordost.osmapi.map;

import de.westnordost.osmapi.ApiResponseReader;
import de.westnordost.osmapi.changesets.Changeset;
import de.westnordost.osmapi.common.OsmXmlDateFormat;
import de.westnordost.osmapi.common.XmlParser;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.RelationMember;
import de.westnordost.osmapi.map.handler.MapDataHandler;
import de.westnordost.osmapi.user.User;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;

/** Parses the map data. It parses the XML naively, i.e. it does not care where in the XML the map
 *  data is. */
public class MapDataParser extends XmlParser implements ApiResponseReader<Void>
{
	private static final String NODE = "node",
	                            WAY = "way",
	                            RELATION = "relation";

	private final OsmXmlDateFormat dateFormat = new OsmXmlDateFormat();
	
	private MapDataHandler handler;
	private MapDataFactory factory;

	/* temporary maps so we do not parse and hold many times the same user and changeset */
	private Map<Long, User> users;
	private Map<Long, Changeset> changesets;

	private long id = -1;
	private int version = 0;
	private Long changesetId;
	private Date timestamp;

	private Double lat;
	private Double lon;
	private Map<String, String> tags;
	private List<RelationMember> members = new ArrayList<>();
	private List<Long> nodes = new LinkedList<>();

	public MapDataParser( MapDataHandler handler, MapDataFactory factory )
	{
		this.handler = handler;
		this.factory = factory;
	}
	
	@Override
	public Void parse(InputStream in) throws IOException
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

        switch (name) {
            case "tag":
                if (tags == null) {
                    tags = new HashMap<>();
                }
                tags.put(getAttribute("k"), getAttribute("v"));
                break;
            case "nd":
                nodes.add(getLongAttribute("ref"));
                break;
            case "member":
                members.add(factory.createRelationMember(
                        getLongAttribute("ref"),
                        getAttribute("role"),
                        Element.Type.valueOf(getAttribute("type").toUpperCase(Locale.UK))
                ));
                break;
            case "bounds":
                BoundingBox bounds = new BoundingBox(
                        getDoubleAttribute("minlat"), getDoubleAttribute("minlon"),
                        getDoubleAttribute("maxlat"), getDoubleAttribute("maxlon"));
                handler.handle(bounds);
                break;
            case NODE:
            case WAY:
            case RELATION:
                timestamp = parseDate();

                changesetId = getLongAttribute("changeset");
                if (changesetId != null && !changesets.containsKey(changesetId)) {
                    Changeset changeset = new Changeset();
                    changeset.id = changesetId;
                    changeset.date = timestamp;
                    changeset.user = parseUser();

                    changesets.put(changesetId, changeset);
                }

                id = getLongAttribute("id");
                Integer v = getIntAttribute("version");
                version = v != null ? v : -1;

                if (name.equals(NODE)) {
                    lat = getDoubleAttribute("lat");
                    lon = getDoubleAttribute("lon");
                }
                break;
        }
	}

	private Date parseDate() throws ParseException
	{
		String timestamp = getAttribute("timestamp");
		if(timestamp == null) return null;

		return dateFormat.parse(timestamp);
	}

	private User parseUser()
	{
		Long userId = getLongAttribute("uid");
		if(userId == null) return null;

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

        switch (name) {
            case NODE:
                handler.handle(
                        factory.createNode(id, version, lat, lon, tags, changesets.get(changesetId), timestamp));
                break;
            case WAY:
                handler.handle(
                        factory.createWay(id, version, nodes, tags, changesets.get(changesetId), timestamp));

                nodes = new LinkedList<>();
                break;
            case RELATION:
                handler.handle(
                        factory.createRelation(id, version, members, tags, changesets.get(changesetId), timestamp));

                members = new ArrayList<>();
                break;
        }

		if (name.equals(NODE) || name.equals(WAY) || name.equals(RELATION))
		{
			tags = null;
		}
	}
}
