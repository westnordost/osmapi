package de.westnordost.osmapi.errors;

public class OsmBadUserInputException extends OsmApiException
{
	public OsmBadUserInputException(int responseCode, String responseBody)
	{
		super(responseCode, responseBody);
	}
}
