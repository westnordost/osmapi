package de.westnordost.osmapi.errors;

/** Thrown when the user is blocked, did not agree to the terms, his OAuth token does not have this
 *  capability or he has not the necessary rights (i.e. a moderator action) */
public class OsmAuthenticationException extends OsmApiException
{
	public OsmAuthenticationException(Throwable cause)
	{
		super(cause);
	}

	public OsmAuthenticationException(int errorCode, String response)
	{
		super(errorCode, response);
	}
}
