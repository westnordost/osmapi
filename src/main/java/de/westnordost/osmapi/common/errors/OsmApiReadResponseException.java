package de.westnordost.osmapi.common.errors;

/** An error occured while parsing the response of an Osm Api call */
public class OsmApiReadResponseException extends RuntimeException
{
	public OsmApiReadResponseException(Exception e)
	{
		super(e);
	}
}
