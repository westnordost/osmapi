package de.westnordost.osmapi.capabilities;

import org.junit.Test;

import de.westnordost.osmapi.ConnectionTestFactory;
import de.westnordost.osmapi.OsmConnection;

public class CapabilitiesApiTest
{
	@Test public void capabilities()
	{
		OsmConnection c = ConnectionTestFactory.createConnection(null);
		new CapabilitiesApi(c).get();
		// as the response may vary, it should just not throw an exception
	}
}
