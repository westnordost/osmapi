package de.westnordost.osmapi.capabilities;

import junit.framework.TestCase;

import de.westnordost.osmapi.ConnectionTestFactory;
import de.westnordost.osmapi.OsmConnection;

public class CapabilitiesDaoTest extends TestCase
{
	public void testCapabilities()
	{
		OsmConnection c = ConnectionTestFactory.createConnection(null);
		new CapabilitiesDao(c).get();
		// as the response may vary, it should just not throw an exception
	}
}
