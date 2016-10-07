package de.westnordost.osmapi.common.errors;

/** Thrown when an API request failed because the server responded that the service is not
 *  available (i.e. database offline)*/
public class OsmServiceUnavailableException extends OsmConnectionException
{
	private static final long serialVersionUID = 1L;
	
	public OsmServiceUnavailableException(int errorCode, String errorTitle, String description)
	{
		super(errorCode, errorTitle, description);
	}
}
