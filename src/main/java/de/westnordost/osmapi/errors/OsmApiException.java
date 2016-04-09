package de.westnordost.osmapi.errors;

/**
 * Thrown when the OSM Api returns a HTTP error. As opposed to the OsmConnectionException, the type
 * of the thrown error is in direct relation to the user's request, i.e. the request itself was
 * invalid, the user was not authenticated or the user was working with old/invalid data.
 *
 * Any HTTP error codes starting with 4xx will be OsmApiExceptions.
 *
 * See subclasses.
 */
public class OsmApiException extends RuntimeException
{
	private int responseCode;
	private String responseBody;

	public OsmApiException(Throwable cause)
	{
		super(cause);
	}

	public OsmApiException(int responseCode, String responseBody)
	{
		this.responseCode = responseCode;
		this.responseBody = responseBody;
	}

	public int getResponseCode()
	{
		return responseCode;
	}

	public String getResponseBody()
	{
		return responseBody;
	}

	@Override
	public String toString() {
		if( getCause() != null ) return super.toString();

		String name = getClass().getName();
		return name + ": Error " + responseCode + " - " + responseBody;
	}
}
