package de.westnordost.osmapi.map.changes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import de.westnordost.osmapi.ApiResponseReader;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.XmlParser;
import de.westnordost.osmapi.map.data.Element;

/** Parses a &lt;diffResult&gt; sent by the server when uploading a changeset */
public class MapDataDiffParser extends XmlParser implements ApiResponseReader<Void>
{
	private static final String NODE = "node",
	                            WAY = "way",
	                            RELATION = "relation";

	private final Handler<DiffElement> handler;

	public MapDataDiffParser(Handler<DiffElement> handler)
	{
		this.handler = handler;
	}

	@Override
	public Void parse(InputStream in) throws IOException
	{
		doParse(in);
		return null;
	}

	@Override
	protected void onStartElement()
	{
		String name = getName();

		if (name.equals(NODE) || name.equals(WAY) || name.equals(RELATION))
		{
			DiffElement e = new DiffElement();
			e.type = Element.Type.valueOf(name.toUpperCase(Locale.UK));
			e.clientId = getLongAttribute("old_id");
			e.serverId = getLongAttribute("new_id");
			e.serverVersion = getIntAttribute("new_version");
			handler.handle(e);
		}
	}

	@Override
	protected void onEndElement()
	{

	}
}
