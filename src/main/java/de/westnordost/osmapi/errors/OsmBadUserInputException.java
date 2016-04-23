package de.westnordost.osmapi.errors;

public class OsmBadUserInputException extends OsmApiException
{
	public OsmBadUserInputException(int errorCode, String errorTitle, String description)
	{
		super(errorCode, errorTitle, description);
	}
}
