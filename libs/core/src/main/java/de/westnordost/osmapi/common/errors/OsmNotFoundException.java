package de.westnordost.osmapi.common.errors;

/** Thrown when an action fails because the element does not exist */
public class OsmNotFoundException extends OsmApiException
{
	private static final long serialVersionUID = 1L;
	
	public OsmNotFoundException(int errorCode, String errorTitle, String description)
	{
		super(errorCode, errorTitle, description);
	}
}
