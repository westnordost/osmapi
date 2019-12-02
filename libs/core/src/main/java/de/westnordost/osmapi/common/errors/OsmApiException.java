package de.westnordost.osmapi.common.errors;

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
	private static final long serialVersionUID = 1L;
	
	private int errorCode;
	private String errorTitle;
	private String description;

	public OsmApiException(Throwable cause)
	{
		super(cause);
	}

	public OsmApiException(int errorCode, String errorTitle, String description)
	{
		this.errorCode = errorCode;
		this.errorTitle = errorTitle;
		this.description = description;
	}

	public int getErrorCode()
	{
		return errorCode;
	}

	public String getErrorTitle()
	{
		return errorTitle;
	}

	public String getDescription()
	{
		return description;
	}
	
	@Override
	public String toString() {
		if( getCause() != null ) return super.toString();

		String name = getClass().getName();
		return name + ": " + errorTitle + " ("+errorCode+") - " + description;
	}
}
