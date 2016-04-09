package de.westnordost.osmapi.errors;

/** Thrown when an API request failed because the server responded that the service is not
 *  available (i.e. database offline)*/
public class OsmServiceUnavailableException extends OsmConnectionException
{
	public OsmServiceUnavailableException(int responseCode, String responseBody)
	{
		super(responseCode, responseBody);
	}
}
