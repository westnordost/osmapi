package de.westnordost.osmapi.capabilities;

import java.io.IOException;

import junit.framework.TestCase;
import de.westnordost.osmapi.TestUtils;

public class CapabilitiesParserTest extends TestCase
{
	public void testBasicFields() throws IOException
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

		Capabilities capabilities = new CapabilitiesParser().parse(TestUtils.asInputStream(xml));
		assertEquals(0.6f,capabilities.minSupportedApiVersion);
		assertEquals(0.6f, capabilities.maxSupportedApiVersion);
		assertEquals(0.25f, capabilities.maxMapQueryAreaInSquareDegrees);
		assertEquals(5000, capabilities.maxPointsInGpsTracePerPage);
		assertEquals(2000, capabilities.maxNodesInWay);
		assertEquals(50000, capabilities.maxElementsPerChangeset);
		assertEquals(300, capabilities.timeoutInSeconds);
	}

	public void testApiStatus() throws IOException
	{
		String xml =
				"<api>" +
				"	<status database=\"online\" api=\"offline\" gpx=\"readonly\"/>" +
				"</api>";

		Capabilities capabilities = new CapabilitiesParser().parse(TestUtils.asInputStream(xml));
		assertTrue(capabilities.isDatabaseReadable());
		assertTrue(capabilities.isDatabaseWritable());
		assertFalse(capabilities.isMapDataModifiable());
		assertFalse(capabilities.isMapDataReadable());
		assertTrue(capabilities.isGpsTracesReadable());
		assertFalse(capabilities.isGpsTracesUploadable());
	}

	public void testPolicy() throws IOException
	{
		String xml =
				"<policy>" +
				"	<imagery>" +
				"		<blacklist regex=\".*\\.googleapis\\.com/.*\"/>" +
				"		<blacklist regex=\".*\\.google\\.com/.*\"/>" +
				"		<blacklist regex=\".*\\.google\\.ru/.*\"/>" +
				"	</imagery>" +
				"</policy>";

		Capabilities capabilities = new CapabilitiesParser().parse(TestUtils.asInputStream(xml));
		assertEquals(".*\\.googleapis\\.com/.*", capabilities.imageryBlacklistRegExes.get(0));
		assertEquals(".*\\.google\\.com/.*", capabilities.imageryBlacklistRegExes.get(1));
		assertEquals(".*\\.google\\.ru/.*", capabilities.imageryBlacklistRegExes.get(2));
	}

}
