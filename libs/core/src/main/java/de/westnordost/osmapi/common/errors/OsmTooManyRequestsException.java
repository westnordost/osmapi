package de.westnordost.osmapi.common.errors;

/** When the request has been blocked due to rate limiting */
public class OsmTooManyRequestsException extends OsmApiException {
    private static final long serialVersionUID = 1L;

    public OsmTooManyRequestsException(int errorCode, String errorTitle, String description)
    {
        super(errorCode, errorTitle, description);
    }
}
