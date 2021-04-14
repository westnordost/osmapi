package de.westnordost.osmapi.map.changes;

import java.text.ParseException;

import de.westnordost.osmapi.map.MapDataFactory;
import de.westnordost.osmapi.map.MapDataParser;

/** Parses a xml in &lt;osmChange&gt; format */
public class MapDataChangesParser extends MapDataParser
{
	private final MapDataChangesHandler handler;

	public MapDataChangesParser(MapDataChangesHandler handler, MapDataFactory factory)
	{
		super(handler, factory);
		this.handler = handler;
	}

	@Override
	protected void onStartElement() throws ParseException
	{
		super.onStartElement();

		String name = getName();

		if(name.equals("create"))
		{
			handler.onStartCreations();
		}
		else if(name.equals("modify"))
		{
			handler.onStartModifications();
		}
		else if(name.equals("delete"))
		{
			handler.onStartDeletions();
		}
	}
}
