package de.westnordost.osmapi.errors;

/** Thrown when trying to request an area that is either too big or contains too much data.  */
public class OsmQueryTooBigException extends OsmBadUserInputException
{
	public OsmQueryTooBigException(OsmApiException other)
	{
		super(other.getErrorCode(), other.getErrorTitle(), other.getDescription());
	}
	
	public OsmQueryTooBigException(int responseCode, String responseBody, String description)
	{
		super(responseCode, responseBody, description);
	}
}
