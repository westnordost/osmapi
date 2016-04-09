package de.westnordost.osmapi.xml;

import junit.framework.TestCase;

import java.io.UnsupportedEncodingException;

public class XmlParserTest extends TestCase
{
	public void testGetParentName() throws UnsupportedEncodingException
	{
		String xml = "<a><b><c></c></b><d></d></a>";
		final String[] parents = {null, "a", "b", "b", "a", "a", "a", null };

		TestXmlParser parser = new TestXmlParser()
		{
			private int counter = 0;

			@Override
			protected void onStartElement() throws Exception
			{
				assertEquals(parents[counter++],getParentName());
			}

			@Override
			protected void onEndElement() throws Exception
			{
				assertEquals(parents[counter++],getParentName());
			}
		};
		parser.test(xml);
	}

	public void testGetText() throws UnsupportedEncodingException
	{
		String xml = "<a>hi</a><b>ha<c>ho</c><d/>he</b>";
		// "ha" is ignored
		final String[] texts = {"hi", "ho", null, "he"};

		TestXmlParser parser = new TestXmlParser()
		{
			private int textCounter = 0;

			@Override
			protected void onStartElement() throws Exception
			{
				assertEquals(null, getText());
			}

			@Override
			protected void onEndElement() throws Exception
			{
				assertEquals(texts[textCounter++], getText());
			}
		};
		parser.test(xml);
	}

	public void testGetName() throws UnsupportedEncodingException
	{
		String xml = "<a><b><c></c></b><d></d></a>";
		final String[] names = {"a","b","c","c","b","d","d","a"};

		TestXmlParser parser = new TestXmlParser()
		{
			private int counter = 0;

			@Override
			protected void onStartElement() throws Exception
			{
				assertEquals(names[counter++],getName());
			}

			@Override
			protected void onEndElement() throws Exception
			{
				assertEquals(names[counter++],getName());
			}
		};
		parser.test(xml);
	}

	public void testGetAttribute() throws UnsupportedEncodingException
	{
		String xml = "<a x='hi' y='ho' /><b/>";

		TestXmlParser parser = new TestXmlParser()
		{
			@Override
			protected void onStartElement() throws Exception
			{
				checkDemAll();
			}

			@Override
			protected void onEndElement() throws Exception
			{

			}

			private void checkDemAll()
			{
				// exist anywhere for element a
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

	public void testException() throws UnsupportedEncodingException
	{
		String xml = "<a x='hi'/>";

		TestXmlParser parser = new TestXmlParser()
		{
			@Override
			protected void onStartElement() throws Exception
			{
				// should fail with an exception because x is a string, not a long
				long x = Long.parseLong(getAttribute("x"));
			}

			@Override
			protected void onEndElement() throws Exception
			{

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

	private abstract class TestXmlParser extends XmlParser
	{
		public void test(String xml) throws UnsupportedEncodingException
		{
			doParse(XmlTestUtils.asInputStream(xml));
		}
	}
}
