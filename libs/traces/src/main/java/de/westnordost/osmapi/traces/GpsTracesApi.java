package de.westnordost.osmapi.traces;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import de.westnordost.osmapi.ApiRequestWriter;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.common.FormDataWriter;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.IdResponseReader;
import de.westnordost.osmapi.common.SingleElementHandler;
import de.westnordost.osmapi.common.errors.OsmAuthorizationException;
import de.westnordost.osmapi.common.errors.OsmBadUserInputException;
import de.westnordost.osmapi.common.errors.OsmNotFoundException;
import de.westnordost.osmapi.common.errors.OsmQueryTooBigException;
import de.westnordost.osmapi.map.data.BoundingBox;

/** Create, get, edit and delete gpx traces */
public class GpsTracesApi
{
	private static final String GPX = "gpx";
	
	private final OsmConnection osm;
	
	public GpsTracesApi(OsmConnection osm)
	{
		this.osm = osm;
	}

	/**
	 * Upload a new trace of trackpoints.
	 * 
	 * @param name this is usually the "file name" of the GPX trace (when files are involved)
	 * @param visibility the visibility the trace should have
	 * @param description short description of the trace. May not be null or empty.
	 * @param tags keywords with which this trace can be found. May be null.
	 * @param trackpoints The GPX track to upload
	 * @return trace id
	 * 
	 * @throws IllegalArgumentException if either name, description or any single tag is longer than
	 *                                  255 characters
	 * @throws OsmBadUserInputException if the trace is invalid
	 * @throws OsmAuthorizationException if this application is not authorized to write traces
	 *                                   (Permission.WRITE_GPS_TRACES)
	 */
	public long create(
			final String name, final GpsTraceDetails.Visibility visibility,
			final String description, final List<String> tags,
			final Iterable<GpsTrackpoint> trackpoints)
	{
		return create(name, visibility, description, tags, new GpxTrackWriter(osm.getUserAgent(), trackpoints));
	}

	/**
	 * Upload a new trace from GPX input stream.
	 *
	 * @param name this is usually the "file name" of the GPX trace
	 * @param visibility the visibility the trace should have
	 * @param description short description of the trace. May not be null or empty.
	 * @param tags keywords with which this trace can be found. May be null.
	 * @param gpx The GPX to upload. It is responsibility of the caller to close the stream.
	 * @return trace id
	 *
	 * @throws IllegalArgumentException if either name, description or any single tag is longer than
	 *                                  255 characters
	 * @throws OsmBadUserInputException if the trace is invalid
	 * @throws OsmAuthorizationException if this application is not authorized to write traces
	 *                                   (Permission.WRITE_GPS_TRACES)
	 */
	public long create(
			final String name, final GpsTraceDetails.Visibility visibility,
			final String description, final List<String> tags,
			final InputStream gpx)
	{
		return create(name, visibility, description, tags, new GpxInputStreamWriter(gpx));
	}
	
	/** Upload a new trace with no tags
	 * 
	 *  @see #create(String, GpsTraceDetails.Visibility, String, List, Iterable) */
	public long create(String name, GpsTraceDetails.Visibility visibility, String description,
			final Iterable<GpsTrackpoint> trackpoints)
	{
		return create(name, visibility, description, null, trackpoints);
	}

	private long create(
			final String name, final GpsTraceDetails.Visibility visibility,
			final String description, final List<String> tags,
			final ApiRequestWriter writer)
	{
		checkFieldLength("Name", name);
		checkFieldLength("Description", description);
		checkTagsLength(tags);

		/*
		 * uploading a new GPX trace works with a multipart/form-data HTML form, we need to cobble
		 * together a valid request ourselves here which is why this is a little bit more complex
		 * than specifying the parameters simply as URL parameters. But it is not so much more
		 * complex, see FormDataWriter class and
		 * http://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.2
		 */

		FormDataWriter formDataWriter = new FormDataWriter()
		{
			@Override
			protected void write() throws IOException
			{
				addFileField("file", name, writer);

				if(tags != null && !tags.isEmpty())
					addField("tags", toCommaList(tags));

				addField("description", description);
				addField("visibility", visibility.toString().toLowerCase(Locale.UK));
			}
		};

		return osm.makeAuthenticatedRequest(GPX + "/create", "POST", formDataWriter, new IdResponseReader());
	}
	
	private static String toCommaList(List<String> vals)
	{
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for(String val : vals)
		{
			if(first) first = false;
			else      result.append(",");
			result.append(val);
		}
		return result.toString();
	}
	
	private void checkTagsLength(List<String> tags)
	{
		if(tags == null) return;
		for(String tag : tags)
		{
			checkFieldLength("Tag \""+tag+"\"", tag);
		}
	}
	
	private void checkFieldLength(String name, String value)
	{
		if(value.length() >= 256)
		{
			throw new IllegalArgumentException(name+" must have less than 256 characters.");
		}
	}
	
	/**
	 * Change the visibility, description and tags of a GPS trace. description and tags may be null 
	 * if there should be none.
	 *
	 * @param id id of the trace to update
	 * @param visibility desired visibility of the trace
	 * @param description desired description of the trace. May be null.
	 * @param tags desired tags of the trace. May be null.
	 *
	 * @throws OsmNotFoundException if the trace with the given id does not exist
	 * @throws OsmAuthorizationException if this application is not authorized to write traces
	 *                                   (Permission.WRITE_GPS_TRACES)
	 *                                   OR if the trace in question is not the user's own trace
	 * @throws IllegalArgumentException if the length of the description or any one single tag
	 *                                  is more than 256 characters
	 */
	public void update(
			long id, GpsTraceDetails.Visibility visibility, String description, List<String> tags)
	{
		checkFieldLength("Description", description);
		checkTagsLength(tags);
		
		GpsTraceWriter writer = new GpsTraceWriter(id, visibility, description, tags);
		
		osm.makeAuthenticatedRequest(GPX + "/" + id, "PUT", writer);
	}
	
	/**
	 * Delete one of the user's traces
	 *
	 * @param id id of the trace to delete
	 * 
	 * @throws OsmAuthorizationException if this application is not authorized to write traces
	 *                                   (Permission.WRITE_GPS_TRACES)
	 *                                   OR if the trace in question is not the user's own trace
	 */
	public void delete(long id)
	{
		osm.makeAuthenticatedRequest(GPX + "/" + id, "DELETE");
	}
	
	/**
	 * Get information about a given GPS trace or null if it does not exist.
	 *
	 * @param id id of the trace to get
	 * @return The information for the trace with the given id.
	 * 
	 * @throws OsmAuthorizationException if this application is not authorized to read the user's
	 *                                   traces (Permission.READ_GPS_TRACES)
	 *                                   OR if the trace in question is not the user's own trace 
	 *                                   and at the same time it is not public
	 *                                   OR if not logged in at all
	 */
	public GpsTraceDetails get(long id)
	{
		SingleElementHandler<GpsTraceDetails> handler = new SingleElementHandler<>();
		try
		{
			osm.makeAuthenticatedRequest(GPX + "/" + id, "GET", new GpsTracesParser(handler) );
		}
		catch(OsmNotFoundException e)
		{
			return null;
		}
		return handler.get();
	}
	
	/**
	 * Get all trackpoints contained in the given trace id. Note that the trace is a GPX file, so 
	 * there is potentially much more information in there than simply the trackpoints. However, 
	 * this method only parses the trackpoints and ignores everything else.
	 *
	 * @param id id of the trace to get the data for
	 * @param handler Handler to which the trackpoints are fed to
	 *
	 * @throws OsmNotFoundException if the trace with the given id does not exist
	 * @throws OsmAuthorizationException if this application is not authorized to read the user's 
	 *                                   traces (Permission.READ_GPS_TRACES)
	 *                                   OR if the trace in question is not the user's own trace and
	 *                                   at the same time it is not public
	 *                                   OR if not logged in at all
	 */
	public void getData(long id, Handler<GpsTrackpoint> handler)
	{
		osm.makeAuthenticatedRequest(GPX + "/" + id + "/data", "GET", new GpxTrackParser(handler));
	}
	
	/** Pass all gps traces the current user uploaded to the given handler 
	 * 	
	 * @throws OsmAuthorizationException if this application is not authorized to read the user's
	 *                                   traces (Permission.READ_GPS_TRACES)
	 */
	public void getMine(Handler<GpsTraceDetails> handler)
	{
		osm.makeAuthenticatedRequest("user/gpx_files", "GET", new GpsTracesParser(handler));
	}
	
	/** Get all the trackpoints in the given bounding box. As of April 2016, trackpoints returned
	 *  by the method will only have a position and (sometimes) a time set.
	 *
	 * @param bounds the bounding box in which to get the trackpoints
	 * @param handler Handler to which the trackpoints are fed to
	 * @param page since there are loads and loads of traces, the method will only return a certain
	 *             number of trackpoints per page (see Capabilities.maxPointsInGpsTracePerPage).
	 *             You can repeat calling this method, incrementing the page number until you get
	 *             an empty response
	 *
	 * @throws OsmQueryTooBigException if the bounds area is too large
	 * @throws IllegalArgumentException if the bounds cross the 180th meridian or page is &lt; 0
	 */
	public void getAll(BoundingBox bounds, Handler<GpsTrackpoint> handler, int page)
	{
		if(page < 0)
			throw new IllegalArgumentException("Page number must be greater than or equal to 0");
		
		String query = "trackpoints?bbox="+bounds.getAsLeftBottomRightTopString() + "&page=" + page;
		try
		{
			osm.makeRequest(query, new GpxTrackParser(handler));
		}
		catch(OsmBadUserInputException e)
		{
			/* we can be more specific here because we checked the validity of all the other
			   parameters already */
			throw new OsmQueryTooBigException(e);
		}
	}
}
