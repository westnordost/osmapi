package de.westnordost.osmapi.errors;

/** Thrown when the user is blocked, did not agree to the terms, his OAuth token does not have this
 *  capability or he has not the necessary rights (i.e. a moderator action) */
public class OsmAuthorizationException extends OsmApiException
{
	public OsmAuthorizationException(Throwable cause)
	{
		super(cause);
	}

	public OsmAuthorizationException(int errorCode, String response)
	{
		super(errorCode, response);
	}
}
