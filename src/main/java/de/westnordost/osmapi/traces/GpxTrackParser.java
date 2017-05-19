package de.westnordost.osmapi.traces;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import de.westnordost.osmapi.ApiResponseReader;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.XmlParser;
import de.westnordost.osmapi.map.data.OsmLatLon;

/** Parses the trackpoints from response of the osm api (API 0.6). the osm api response is in the 
 *  form of a GPX, but we do not parse the whole GPX here but only the information that is necessary
 *  for the trackpoints. Hence the name "GpxTrackParser" and not "GPXParser". */
public class GpxTrackParser extends XmlParser implements ApiResponseReader<Void>
{
	private static final String
	  TRACKPOINT = "trkpt",
	  TRACKSEGMENT = "trkseg";
	
	private final GpxDateFormat dateFormat = new GpxDateFormat();
	
	private Handler<GpsTrackpoint> handler;
	
	private boolean first;
	private GpsTrackpoint trackpoint;

	public GpxTrackParser(Handler<GpsTrackpoint> handler)
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
		String name = getName();
		
		if(name.equals(TRACKSEGMENT))
		{
			first = true;
		}
		else if(name.equals(TRACKPOINT))
		{
			trackpoint = new GpsTrackpoint(
					OsmLatLon.parseLatLon(getAttribute("lat"), getAttribute("lon")));
			if(first)
			{
				trackpoint.isFirstPointInTrackSegment = first;
				first = false;
			}
		}
	}

	@Override
	protected void onEndElement() throws ParseException
	{
		String name = getName();
		
		if(TRACKPOINT.equals(name))
		{
			handler.handle(trackpoint);
			trackpoint = null;
		}
		else if(TRACKPOINT.equals(getParentName()))
		{
			if(name.equals("time"))
				trackpoint.time = dateFormat.parse(getText());
			if(name.equals("ele"))
				trackpoint.elevation = Float.valueOf(getText());
			if(name.equals("hdop"))
				trackpoint.horizontalDilutionOfPrecision = Float.valueOf(getText());
		}
	}
}
