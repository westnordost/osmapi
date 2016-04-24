package de.westnordost.osmapi.common.errors;

/** Wraps any exception that occurs during parsing with the XmlParser */
public class XmlParserException extends RuntimeException
{

	public XmlParserException(String positionDescription, Throwable cause)
	{
		super("Error parsing XML at " + positionDescription, cause);
	}

	public XmlParserException(Throwable cause)
	{
		super(cause);
	}

	public XmlParserException(String reason)
	{
		super(reason);
	}
}
