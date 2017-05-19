package de.westnordost.osmapi.common;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import de.westnordost.osmapi.common.errors.XmlParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

/** A simple XML parser that can be used quite similarly to the SAX parser but is based on the pull
 *  parser. It has two convenience methods. You can get the parent element via getParentName and you
 *  can get the text in the current node (in onEndElement) via getText.
 *
 *  It is not very generic but generic enough for the purpose of this project */
public abstract class XmlParser
{
	private static final String CHARSET = "UTF-8";
	
	private Stack<String> parentElements = new Stack<>();
	private String text;
	private XmlPullParser xpp;

	protected final void doParse(InputStream in) throws XmlParserException, IOException
	{
		try
		{
			if(xpp == null)
			{
				xpp = XmlPullParserFactory.newInstance().newPullParser();
			}
			xpp.setInput(in, CHARSET);
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT)
			{
				switch (eventType)
				{
					case XmlPullParser.START_TAG:
						text = null;
						onStartElement();
						parentElements.push(xpp.getName());
						break;
					case XmlPullParser.TEXT:
						onTextNode(xpp.getText());
						break;
					case XmlPullParser.END_TAG:
						parentElements.pop();
						onEndElement();
						text = null;
						break;
				}
				eventType = xpp.next();
			}
		}
		catch(IOException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			if(xpp != null)
			{
				throw new XmlParserException(xpp.getPositionDescription(), e);
			}
			else
			{
				throw new XmlParserException(e);
			}
		}
	}

	/**
	 * @return the name of the current element
	 */
	protected String getName()
	{
		return xpp.getName();
	}

	/**
	 * @param name of the attribute
	 * @return the value of the attribute of the current element or null if it does not exist. Only non-null within
	 *         onStartElement
	 */
	protected String getAttribute(String name)
	{
		return xpp.getAttributeValue(null, name);
	}

	protected Float getFloatAttribute(String name)
	{
		String attr = getAttribute(name);
		return attr != null ? Float.parseFloat(attr) : null;
	}

	protected Double getDoubleAttribute(String name)
	{
		String attr = getAttribute(name);
		return attr != null ? Double.parseDouble(attr) : null;
	}

	protected Integer getIntAttribute(String name)
	{
		String attr = getAttribute(name);
		return attr != null ? Integer.parseInt(attr) : null;
	}

	protected Long getLongAttribute(String name)
	{
		String attr = getAttribute(name);
		return attr != null ? Long.parseLong(attr) : null;
	}

	protected Boolean getBooleanAttribute(String name)
	{
		String attr = getAttribute(name);
		return attr != null ? Boolean.parseBoolean(attr) : null;
	}

	protected Byte getByteAttribute(String name)
	{
		String attr = getAttribute(name);
		return attr != null ? Byte.parseByte(attr) : null;
	}

	/**
	 * @return the name of the element parent to the current one or null if there is none
	 */
	protected String getParentName()
	{
		if(parentElements.empty()) return null;
		return parentElements.peek();
	}

	/**
	 * @return the last text node that was encountered in the current element. Only returns anything
	 * else than null in onEndElement.
	 */
	protected String getText()
	{
		return text;
	}

	protected void onTextNode(String text)
	{
		this.text = text;
	}

	protected abstract void onStartElement() throws Exception;

	protected abstract void onEndElement() throws Exception;

}
