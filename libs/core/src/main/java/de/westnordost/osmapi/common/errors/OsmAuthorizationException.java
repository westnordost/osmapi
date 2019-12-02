package de.westnordost.osmapi.common.errors;

/** Thrown when the user is blocked, did not agree to the terms, his OAuth token does not have this
 *  capability, he has not the necessary rights (i.e. a moderator action) or is not authenticated 
 *  at all */
public class OsmAuthorizationException extends OsmApiException
{
	private static final long serialVersionUID = 1L;
	
	public OsmAuthorizationException(Throwable cause)
	{
		super(cause);
	}

	public OsmAuthorizationException(int errorCode, String errorTitle, String description)
	{
		super(errorCode, errorTitle, description);
	}
}
