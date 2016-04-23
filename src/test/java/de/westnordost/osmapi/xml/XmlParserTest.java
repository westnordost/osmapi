package de.westnordost.osmapi.xml;

import de.westnordost.osmapi.TestUtils;
import junit.framework.TestCase;

public class XmlParserTest extends TestCase
{
	public void testGetParentName()
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

	public void testGetText()
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
				assertEquals(null, getText());
			}

			@Override
			protected void onEndElement()
			{
				assertEquals(texts[textCounter++], getText());
			}
		};
		parser.test(xml);
	}

	public void testGetName()
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

	public void testGetAttribute()
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
					assertEquals(null, getAttribute("z"));
				}
				// but not anymore for element b
				else if(getName().equals("b"))
				{
					assertEquals(null, getAttribute("x"));
					assertEquals(null, getAttribute("y"));
					assertEquals(null, getAttribute("z"));
				}
			}
		};
		parser.test(xml);
	}

	public void testConvenienceAttributeGetters()
	{
		String xml = "<a a_float='123.456' a_int='122' a_bool='true' />";
		
		TestXmlParser parser = new TestXmlParser()
		{
			@Override
			protected void onStartElement()
			{
				assertEquals(123.456f, getFloatAttribute("a_float"));
				assertEquals(123.456d, getDoubleAttribute("a_float"));
				assertEquals(122L, (long) getLongAttribute("a_int"));
				assertEquals(122, (int) getIntAttribute("a_int"));
				assertEquals(122, (byte) getByteAttribute("a_int"));
				assertEquals(true, (boolean) getBooleanAttribute("a_bool"));
				
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
	
	public void testException()
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

		try
		{
			parser.test(xml);
			fail();
		}
		catch(XmlParserException e1)
		{
			// test passed
		}
	}

	private class TestXmlParser extends XmlParser
	{
		public void test(String xml)
		{
			doParse(TestUtils.asInputStream(xml));
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
