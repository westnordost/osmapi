package de.westnordost.osmapi.traces;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

import de.westnordost.osmapi.common.XmlWriter;

public class GpxTrackWriter extends XmlWriter
{
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT;

	private final String userAgent;
	private final Iterable<GpsTrackpoint> trackpoints;
	
	public GpxTrackWriter(String userAgent, Iterable<GpsTrackpoint> trackpoints)
	{
		this.userAgent = userAgent;
		this.trackpoints = trackpoints;
	}
	
	@Override
	protected void write() throws IOException
	{
		begin("gpx");
		attribute("version", 1.0);
		attribute("creator", userAgent);
		attribute("xmlns","http://www.topografix.com/GPX/1/0");
		attribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
		attribute("xsi:schemaLocation","http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd");
		
		writeTrack();
		
		end();
	}
	
	private void writeTrack() throws IOException
	{
		begin("trk");
		
		boolean isVeryFirst = true;
		int segmentCount = 0;
		for(GpsTrackpoint trackpoint : trackpoints)
		{
			if(trackpoint.isFirstPointInTrackSegment || isVeryFirst)
			{
				if(!isVeryFirst) 
				{
					end();
				}
				begin("trkseg");
				segmentCount++;
				isVeryFirst = false;
			}

			writeTrackpoint(trackpoint);
		}
		if(segmentCount > 0)
		{
			end();
		}
		
		end();
	}
	
	private void writeTrackpoint(GpsTrackpoint trackpoint) throws IOException
	{
		begin("trkpt");
		attribute("lat", trackpoint.position.getLatitude());
		attribute("lon", trackpoint.position.getLongitude());
		
		if(trackpoint.time != null)
		{
			begin("time");
			text(FORMATTER.format(trackpoint.time));
			end();
		}
		if(trackpoint.elevation != null)
		{
			begin("ele");
			float ele1Decimal = roundToOneDecimal(trackpoint.elevation);
			text(String.valueOf(ele1Decimal));
			end();
		}
		if(trackpoint.horizontalDilutionOfPrecision != null)
		{
			begin("hdop");
			float hdop1Decimal = roundToOneDecimal(trackpoint.horizontalDilutionOfPrecision);
			text(String.valueOf(hdop1Decimal));
			end();
		}
		end();
	}
	
	private static float roundToOneDecimal(float x)
	{
		return Math.round(10*x)/10f;
	}
}
