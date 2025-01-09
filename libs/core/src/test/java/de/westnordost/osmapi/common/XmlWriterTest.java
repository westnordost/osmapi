package de.westnordost.osmapi.common;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.westnordost.osmapi.TestUtils;

import static org.junit.Assert.*;

public class XmlWriterTest
{
	private static final String xmlBlob = "<?xml version='1.0' encoding='UTF-8' ?>";

	@Test public void didNotCloseTag()
	{
		assertThrows(
				IllegalStateException.class,
				() -> new TestXmlWriter()
				{
					@Override
					protected void write() throws IOException
					{
						begin("test");
					}
				}.test()
		);
	}

	@Test public void didCloseOneTagTooMany()
	{
		assertThrows(
				IllegalStateException.class,
				() -> new TestXmlWriter() {
                    @Override protected void write() throws IOException
                    {
                        begin("test");
                        end();
                        end();
                    }
                }.test()
		);
	}

	@Test public void simple() throws IOException
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

	@Test public void text() throws IOException
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


	@Test public void attribute() throws IOException
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

	@Test public void doubleAttributeIsNotInScientificNotation() throws IOException
	{
		String result = new TestXmlWriter()
		{
			@Override
			protected void write() throws IOException
			{
				begin("test");
				attribute("key", 0.0000001);
				end();
			}
		}.test();

		assertEquals(xmlBlob + "<test key='0.0000001' />", result);
	}

	@Test public void nested() throws IOException
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

	private static abstract class TestXmlWriter extends XmlWriter
	{
		public String test() throws IOException
		{
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			write(os);
			String result = TestUtils.asString(os);
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
