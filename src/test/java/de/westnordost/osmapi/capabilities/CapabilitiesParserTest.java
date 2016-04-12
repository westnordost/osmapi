package de.westnordost.osmapi.capabilities;

import junit.framework.TestCase;

import de.westnordost.osmapi.xml.XmlTestUtils;

public class CapabilitiesParserTest extends TestCase
{
	public void testBasicFields()
	{
		String xml =
				"<api>" +
				"	<version minimum=\"0.6\" maximum=\"0.6\"/>" +
				"	<area maximum=\"0.25\"/>" +
				"	<tracepoints per_page=\"5000\"/>" +
				"	<waynodes maximum=\"2000\"/>" +
				"	<changesets maximum_elements=\"50000\"/>" +
				"	<timeout seconds=\"300\"/>" +
				"	<status database=\"online\" api=\"online\" gpx=\"online\"/>" +
				"</api>";

		Capabilities capabilities = new CapabilitiesParser().parse(XmlTestUtils.asInputStream(xml));
		assertEquals(0.6f,capabilities.getMinSupportedApiVersion());
		assertEquals(0.6f, capabilities.getMaxSupportedApiVersion());
		assertEquals(0.25f, capabilities.getMaxMapQueryAreaInSquareDegrees());
		assertEquals(5000, capabilities.getMaxPointsInGpsTracePerPage());
		assertEquals(2000, capabilities.getMaxNodesInWay());
		assertEquals(50000, capabilities.getMaxElementsPerChangeset());
		assertEquals(300, capabilities.getTimeoutInSeconds());
	}

	public void testApiStatus()
	{
		String xml =
				"<api>" +
				"	<status database=\"online\" api=\"offline\" gpx=\"readonly\"/>" +
				"</api>";

		Capabilities capabilities = new CapabilitiesParser().parse(XmlTestUtils.asInputStream(xml));
		assertTrue(capabilities.isDatabaseReadable());
		assertTrue(capabilities.isDatabaseWritable());
		assertFalse(capabilities.isMapDataModifiable());
		assertFalse(capabilities.isMapDataReadable());
		assertTrue(capabilities.isGpsTracesReadable());
		assertFalse(capabilities.isGpsTracesUploadable());
	}

	public void testPolicy()
	{
		String xml =
				"<policy>" +
				"	<imagery>" +
				"		<blacklist regex=\".*\\.googleapis\\.com/.*\"/>" +
				"		<blacklist regex=\".*\\.google\\.com/.*\"/>" +
				"		<blacklist regex=\".*\\.google\\.ru/.*\"/>" +
				"	</imagery>" +
				"</policy>";

		Capabilities capabilities = new CapabilitiesParser().parse(XmlTestUtils.asInputStream(xml));
		assertEquals(".*\\.googleapis\\.com/.*", capabilities.getImageryBlacklistRegExes().get(0));
		assertEquals(".*\\.google\\.com/.*", capabilities.getImageryBlacklistRegExes().get(1));
		assertEquals(".*\\.google\\.ru/.*", capabilities.getImageryBlacklistRegExes().get(2));
	}

}
