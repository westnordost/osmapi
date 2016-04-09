package de.westnordost.osmapi.errors;

/** Thrown when an action fails because the element does not exist */
public class OsmNotFoundException extends OsmApiException
{
	public OsmNotFoundException(int errorCode, String response)
	{
		super(errorCode, response);
	}
}
