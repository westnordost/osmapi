package de.westnordost.osmapi.traces;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;

import de.westnordost.osmapi.ApiResponseReader;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.OsmXmlDateFormat;
import de.westnordost.osmapi.common.XmlParser;
import de.westnordost.osmapi.map.data.OsmLatLon;

/** Parses the GPS traces response of the osm api (API 0.6). */
public class GpsTracesParser extends XmlParser implements ApiResponseReader<Void>
{

	private static final String
			GPX_FILE = "gpx_file",
			TAG = "tag",
			DESCRIPTION = "description";
	
	private final OsmXmlDateFormat dateFormat = new OsmXmlDateFormat();
	
	private Handler<GpsTraceDetails> handler;
	private GpsTraceDetails trace;

	public GpsTracesParser(Handler<GpsTraceDetails> handler)
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
	protected void onStartElement() throws ParseException
	{
		if(getName().equals(GPX_FILE))
		{
			trace = new GpsTraceDetails();
			trace.id = getLongAttribute("id");
			trace.visibility = GpsTraceDetails.Visibility.valueOf(getAttribute("visibility").toUpperCase(Locale.UK));

			trace.name = getAttribute("name");
			trace.userName = getAttribute("user");
			
			Boolean pendingAttribute = getBooleanAttribute("pending");
			trace.pending = pendingAttribute == null || pendingAttribute;
			
			String lat = getAttribute("lat");
			String lon = getAttribute("lon");
			if(lat != null && lon != null)
			{
				trace.position = OsmLatLon.parseLatLon(lat, lon);
			}
			
			String timestamp = getAttribute("timestamp");
			if(timestamp != null)
			trace.date = dateFormat.parse(timestamp);
		}
	}

	@Override
	protected void onEndElement()
	{
		String name = getName();

		switch (name) {
			case GPX_FILE:
				handler.handle(trace);
				trace = null;
				break;
			case DESCRIPTION:
				trace.description = getText();
				break;
			case TAG:
				if (trace.tags == null) trace.tags = new ArrayList<>();
				trace.tags.add(getText());
				break;
		}
	}
}
