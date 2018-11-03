package de.westnordost.osmapi.common;


import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import de.westnordost.osmapi.ApiRequestWriter;

/**
 * A simple XML writer / serializer with convenience method and less generic
 */
public abstract class XmlWriter implements ApiRequestWriter
{
	private static final String CHARSET = "UTF-8";
	
	private XmlSerializer xml;

	private final NumberFormat numberFormat;

	public XmlWriter()
	{
		numberFormat = NumberFormat.getNumberInstance(Locale.UK);
		numberFormat.setMaximumFractionDigits(340);
	}

	@Override
	public final String getContentType()
	{
		return "text/xml";
	}

	@Override
	public final void write(OutputStream out) throws IOException
	{
		try
		{
			xml = XmlPullParserFactory.newInstance().newSerializer();
		}
		catch(XmlPullParserException e)
		{
			throw new RuntimeException("Cannot initialize serializer", e);
		}
		xml.setOutput(out, CHARSET);
		xml.startDocument(CHARSET, null);

		write();

		if(xml.getName() != null)
		{
			throw new IllegalStateException("Forgot to close a tag");
		}

		xml.endDocument();
		xml.flush();
	}

	protected final void begin(String name) throws IOException
	{
		xml.startTag(null, name);
	}

	protected final void end() throws IOException
	{
		if(xml.getName() == null)
		{
			throw new IllegalStateException("Closed one tag to many");
		}
		xml.endTag(null, xml.getName());
	}

	protected final void attribute(String key, String value) throws IOException
	{
		xml.attribute(null, key, value);
	}

	protected final void attribute(String key, float value) throws IOException
	{
		xml.attribute(null, key, numberFormat.format(value));
	}
	
	protected final void attribute(String key, double value) throws IOException
	{
		xml.attribute(null, key, numberFormat.format(value));
	}
	
	protected final void attribute(String key, int value) throws IOException
	{
		xml.attribute(null, key, String.valueOf(value));
	}
	
	protected final void attribute(String key, long value) throws IOException
	{
		xml.attribute(null, key, String.valueOf(value));
	}
	
	protected final void attribute(String key, byte value) throws IOException
	{
		xml.attribute(null, key, String.valueOf(value));
	}
	
	protected final void attribute(String key, boolean value) throws IOException
	{
		xml.attribute(null, key, String.valueOf(value));
	}
	
	protected final void text(String text) throws IOException
	{
		xml.text(text);
	}

	protected abstract void write() throws IOException;
}
