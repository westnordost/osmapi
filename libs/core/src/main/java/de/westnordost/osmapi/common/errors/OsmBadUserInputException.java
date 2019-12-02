package de.westnordost.osmapi.common.errors;

public class OsmBadUserInputException extends OsmApiException
{
	private static final long serialVersionUID = 1L;
	
	public OsmBadUserInputException(int errorCode, String errorTitle, String description)
	{
		super(errorCode, errorTitle, description);
	}
}
