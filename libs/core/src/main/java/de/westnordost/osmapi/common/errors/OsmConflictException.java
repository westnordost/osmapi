package de.westnordost.osmapi.common.errors;

/** Thrown when an action fails because the precondition changed on server side. I.e. trying to
 *  close a note that has already been closed, change an element whose version number already
 *  increased, change or close a changeset that has already been closed etc.
 */
public class OsmConflictException extends OsmApiException
{
	private static final long serialVersionUID = 1L;
	
	public OsmConflictException(int errorCode, String errorTitle, String description)
	{
		super(errorCode, errorTitle, description);
	}
}
