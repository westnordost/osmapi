package de.westnordost.osmapi.errors;

/** Thrown when an action fails because the precondition changed on server side. I.e. trying to
 *  close a note that has already been closed, change an element whose version number already
 *  increased, change or close a changeset that has already been closed etc.
 */
public class OsmConflictException extends OsmApiException
{
	public OsmConflictException(int errorCode, String response)
	{
		super(errorCode, response);
	}
}
