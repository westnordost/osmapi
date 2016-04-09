package de.westnordost.osmapi.errors;

/** Thrown when trying to request an area that is either too big or contains too much data.  */
public class OsmQueryTooBigException extends OsmBadUserInputException
{
	public OsmQueryTooBigException(int responseCode, String responseBody)
	{
		super(responseCode, responseBody);
	}
}
