package de.westnordost.osmapi.xml;


import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class XmlWriterTest extends TestCase
{
	private static final String xmlBlob = "<?xml version='1.0' encoding='UTF-8' ?>";

	public void testDidNotCloseTag() throws IOException
	{
		try
		{
			new TestXmlWriter()
			{
				@Override
				protected void write() throws IOException
				{
					begin("test");
				}
			}.test();
		}
		catch (IllegalStateException e)
		{
			assertTrue(true);
			return;
		}
		assertFalse("Should have thrown an exception", true);
	}

	public void testDidCloseOneTagTooMany() throws IOException
	{
		try
		{
			new TestXmlWriter()
			{
				@Override
				protected void write() throws IOException
				{
					begin("test");
					end();
					end();
				}
			}.test();
		}
		catch (IllegalStateException e)
		{
			assertTrue(true);
			return;
		}
		assertFalse("Should have thrown an exception", true);
	}

	public void testSimple() throws IOException
	{
		String result = new TestXmlWriter()
		{
			@Override
			protected void write() throws IOException
			{
				begin("test");
				end();
			}
		}.test();

		assertEquals(xmlBlob + "<test />", result);
	}

	public void testText() throws IOException
	{
		String result = new TestXmlWriter()
		{
			@Override
			protected void write() throws IOException
			{
				begin("test");
				text("jo <>");
				end();
			}
		}.test();

		assertEquals(xmlBlob + "<test>jo &lt;&gt;</test>", result);
	}


	public void testAttribute() throws IOException
	{
		String result = new TestXmlWriter()
		{
			@Override
			protected void write() throws IOException
			{
				begin("test");
				attribute("key", "value");
				end();
			}
		}.test();

		assertEquals(xmlBlob + "<test key='value' />", result);
	}

	public void testNested() throws IOException
	{
		String result = new TestXmlWriter()
		{
			@Override
			protected void write() throws IOException
			{
				begin("test");
				begin("a");
				end();
				end();
			}
		}.test();

		assertEquals(xmlBlob + "<test><a /></test>", result);
	}

	private abstract class TestXmlWriter extends XmlWriter
	{
		public String test() throws IOException
		{
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			write(os);
			String result = XmlTestUtils.asString(os);
			// not interested in indentation
			result = result.replaceAll("(?m)^[\\s]*", "");
			// not interested in tabs and newlines
			result = result.replaceAll("[\r\n\t]","");
			// " and ' does not make a difference
			result = result.replaceAll("\"","'");

			return result;
		}
	}
}
