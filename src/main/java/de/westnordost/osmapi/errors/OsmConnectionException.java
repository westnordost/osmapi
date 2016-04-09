package de.westnordost.osmapi.errors;

/** (A proper) connection to the server cannot be established for some reason. This error usually
 * wraps an IOException when trying to reach the server but also includes any error that inhibits
 * the user to talk with the OSM Api for reasons that have no relation to the user's request (i.e.
 * whether it was valid or not). So in any case, nothing the user can do anything about.
 *
 * For example, if the Api correctly replies to the HTTP request but it replies that the service is
 * unavailable, it is also a OsmConnectionException (see OsmServiceUnavailableException).
 * Or, more generally, any HTTP error codes starting with 5xx will be OsmConnectionExceptions.
 *
 * See subclasses for further possibilities.
 *   */
public class OsmConnectionException extends RuntimeException
{
	private int responseCode;
	private String responseBody;

	public OsmConnectionException(int responseCode, String responseBody)
	{
		this.responseCode = responseCode;
		this.responseBody = responseBody;
	}

	public OsmConnectionException()
	{
		super();
	}

	public OsmConnectionException(Throwable cause)
	{
		super(cause);
	}

	@Override
	public String toString()
	{
		String name = getClass().getName();
		return name + ": Error " + responseCode + " - " + responseBody;
	}
}
