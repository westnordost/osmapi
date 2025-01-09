package de.westnordost.osmapi.common;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import de.westnordost.osmapi.TestUtils;
import de.westnordost.osmapi.common.errors.XmlParserException;

import static org.junit.Assert.*;

public class XmlParserTest
{
	@Test public void getParentName()
	{
		String xml = "<a><b><c></c></b><d></d></a>";
		final String[] parents = {null, "a", "b", "b", "a", "a", "a", null };

		TestXmlParser parser = new TestXmlParser()
		{
			private int counter = 0;

			@Override
			protected void onStartElement()
			{
				assertEquals(parents[counter++],getParentName());
			}

			@Override
			protected void onEndElement()
			{
				assertEquals(parents[counter++],getParentName());
			}
		};
		parser.test(xml);
	}

	@Test public void getText()
	{
		String xml = "<a>hi</a><b>ha<c>ho</c><d/>he</b>";
		// "ha" is ignored
		final String[] texts = {"hi", "ho", null, "he"};

		TestXmlParser parser = new TestXmlParser()
		{
			private int textCounter = 0;

			@Override
			protected void onStartElement()
			{
                assertNull(getText());
			}

			@Override
			protected void onEndElement()
			{
				assertEquals(texts[textCounter++], getText());
			}
		};
		parser.test(xml);
	}

	@Test public void getTextWithEntityRefs()
	{
		new TestXmlParser()
		{
			@Override
			protected void onEndElement()
			{
				assertEquals("hello & <goodbye>", getText());
			}
		}.test("<a>hello &amp; &lt;goodbye&gt;</a>");
	}

	@Test public void getTextWithCdata()
	{
		new TestXmlParser()
		{
			@Override
			protected void onEndElement()
			{
				assertEquals("hello & <good> bye", getText());
			}
		}.test("<a>hello <![CDATA[& <good>]]> bye</a>");
	}

	@Test public void getName()
	{
		String xml = "<a><b><c></c></b><d></d></a>";
		final String[] names = {"a","b","c","c","b","d","d","a"};

		TestXmlParser parser = new TestXmlParser()
		{
			private int counter = 0;

			@Override
			protected void onStartElement()
			{
				assertEquals(names[counter++],getName());
			}

			@Override
			protected void onEndElement()
			{
				assertEquals(names[counter++],getName());
			}
		};
		parser.test(xml);
	}

	@Test public void getAttribute()
	{
		String xml = "<a x='hi' y='ho' /><b/>";

		TestXmlParser parser = new TestXmlParser()
		{
			@Override
			protected void onStartElement()
			{
				checkDemAll();
			}
			
			private void checkDemAll()
			{
				// exist for element a
				if(getName().equals("a"))
				{
					assertEquals("hi", getAttribute("x"));
					assertEquals("ho", getAttribute("y"));
                    assertNull(getAttribute("z"));
				}
				// but not anymore for element b
				else if(getName().equals("b"))
				{
                    assertNull(getAttribute("x"));
                    assertNull(getAttribute("y"));
                    assertNull(getAttribute("z"));
				}
			}
		};
		parser.test(xml);
	}

	@Test public void convenienceAttributeGetters()
	{
		String xml = "<a a_float='123.456' a_int='122' a_bool='true' />";
		
		TestXmlParser parser = new TestXmlParser()
		{
			@Override
			protected void onStartElement()
			{
				assertEquals(123.456f, getFloatAttribute("a_float"), 0.0f);
				assertEquals(123.456d, getDoubleAttribute("a_float"), 0.0f);
				assertEquals(122L, (long) getLongAttribute("a_int"));
				assertEquals(122, (int) getIntAttribute("a_int"));
				assertEquals(122, (byte) getByteAttribute("a_int"));
				assertEquals(true, getBooleanAttribute("a_bool"));
				
				assertNull(getFloatAttribute("does_not_exist"));
				assertNull(getDoubleAttribute("does_not_exist"));
				assertNull(getLongAttribute("does_not_exist"));
				assertNull(getIntAttribute("does_not_exist"));
				assertNull(getByteAttribute("does_not_exist"));
				assertNull(getBooleanAttribute("does_not_exist"));
			}
		};
		parser.test(xml);
	}
	
	@Test public void exception()
	{
		String xml = "<a x='hi'/>";

		TestXmlParser parser = new TestXmlParser()
		{
			@Override
			protected void onStartElement()
			{
				// should fail with an exception because x is a string, not a long
				Long.parseLong(getAttribute("x"));
			}
		};

		assertThrows(XmlParserException.class, () -> parser.test(xml));
	}
	
	@Test public void ioExceptionIsNotWrappedIntoXmlParserException()
	{
		InputStream exceptionStream = new InputStream() {
			@Override public int read() throws IOException
			{
				throw new IOException();
			}
		};

		assertThrows(IOException.class, () -> new TestXmlParser().testStream(exceptionStream));
	}

	private static class TestXmlParser extends XmlParser
	{
		public void test(String xml)
		{
			try
			{
				doParse(TestUtils.asInputStream(xml));
			}
			catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		public void testStream(InputStream is) throws IOException
		{
			doParse(is);
		}
		
		@Override
		protected void onStartElement()
		{
			// empty default implementation
		}

		@Override
		protected void onEndElement()
		{
			// empty default implementation
		}
	}
}
