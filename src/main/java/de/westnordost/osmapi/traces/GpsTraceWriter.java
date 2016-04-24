package de.westnordost.osmapi.traces;

import java.io.IOException;
import java.util.List;

import de.westnordost.osmapi.common.XmlWriter;

/** Writes only those GPS trace properties that can be changed in an update call to the API.
 *  It does not write everything, like i.e. the creation date. */
public class GpsTraceWriter extends XmlWriter
{
	private long id;
	private GpsTraceDetails.Visibility visibility;
	private String description;
	private List<String> tags;
	
	public GpsTraceWriter(long id, GpsTraceDetails.Visibility visibility, String description, List<String> tags)
	{
		this.id = id;
		this.visibility = visibility;
		this.description = description;
		this.tags = tags;
	}
	
	@Override
	protected void write() throws IOException
	{
		begin("osm");
		begin("gpx_file");
		writeTrace();
		end();
		end();
	}
	
	private void writeTrace() throws IOException
	{
		attribute("id", id);
		attribute("visibility", visibility.toString().toLowerCase());
		
		begin("description");
		if(description != null) text(description);
		end();
		
		if(tags != null)
		{
			for(String tag : tags)
			{
				begin("tag");
				text(tag);
				end();
			}
		}
	}
}
