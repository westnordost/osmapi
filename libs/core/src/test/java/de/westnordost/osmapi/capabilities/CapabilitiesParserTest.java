package de.westnordost.osmapi.capabilities;

import org.junit.Test;

import java.io.IOException;

import de.westnordost.osmapi.TestUtils;

import static org.junit.Assert.*;

public class CapabilitiesParserTest
{
	@Test public void basicFields() throws IOException
	{
		String xml =
				"<api>" +
				"	<version minimum=\"0.6\" maximum=\"0.6\"/>" +
				"	<area maximum=\"0.25\"/>" +
				"	<note_area maximum=\"25\"/>" +
				"	<tracepoints per_page=\"5000\"/>" +
				"	<waynodes maximum=\"2000\"/>" +
				"	<relationmembers maximum=\"32000\"/>" +
				"	<changesets maximum_elements=\"50000\" default_query_limit=\"100\" maximum_query_limit=\"200\"/>" +
				"   <notes default_query_limit=\"100\" maximum_query_limit=\"10000\"/>" +
				"	<timeout seconds=\"300\"/>" +
				"	<status database=\"online\" api=\"online\" gpx=\"online\"/>" +
				"</api>";

		Capabilities capabilities = new CapabilitiesParser().parse(TestUtils.asInputStream(xml));
		assertEquals(0.6f,capabilities.minSupportedApiVersion, 0.0);
		assertEquals(0.6f, capabilities.maxSupportedApiVersion, 0.0);
		assertEquals(0.25f, capabilities.maxMapQueryAreaInSquareDegrees, 0.0);
		assertEquals(25f, capabilities.maxNotesQueryAreaInSquareDegrees, 0.0);
		assertEquals(5000, capabilities.maxPointsInGpsTracePerPage);
		assertEquals(2000, capabilities.maxNodesInWay);
		assertEquals(32000, capabilities.maxMembersInRelation);
		assertEquals(50000, capabilities.maxElementsPerChangeset);
		assertEquals(300, capabilities.timeoutInSeconds);
		assertEquals(100, capabilities.defaultNotesQueryLimit);
		assertEquals(10000, capabilities.maximumNotesQueryLimit);
		assertEquals(100, capabilities.defaultChangesetsQueryLimit);
		assertEquals(200, capabilities.maximumChangesetsQueryLimit);
	}

	@Test public void apiStatus() throws IOException
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

	@Test public void policy() throws IOException
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
