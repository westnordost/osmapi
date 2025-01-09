package de.westnordost.osmapi;

import org.junit.Test;

import de.westnordost.osmapi.ConnectionTestFactory.User;
import de.westnordost.osmapi.common.errors.OsmApiReadResponseException;
import de.westnordost.osmapi.common.errors.OsmAuthorizationException;
import de.westnordost.osmapi.common.errors.OsmConnectionException;

import static org.junit.Assert.*;

public class OsmConnectionTest
{
	@Test public void authorizationException()
	{
		assertThrows(
				OsmAuthorizationException.class,
				() -> {
					OsmConnection osm = ConnectionTestFactory.createConnection(null);
					osm.makeAuthenticatedRequest("changeset/create", "PUT", null, null);
				}
		);
	}
	
	@Test public void authorizationException2()
	{
		assertThrows(
				OsmAuthorizationException.class,
				() -> {
					OsmConnection osm = ConnectionTestFactory.createConnection(User.UNKNOWN);
					osm.makeAuthenticatedRequest("changeset/create", "PUT", null, null);
				}
		);
	}
	
	@Test public void connectionException()
	{
		assertThrows(
				OsmConnectionException.class,
				() -> {
					OsmConnection osm = new OsmConnection("http://cant.connect.to.this.server.hm", "blub", null);
					osm.makeRequest("doesntMatter", null);
				}
		);
	}
	
	@Test public void errorParsingApiResponse()
	{
		assertThrows(
				OsmApiReadResponseException.class,
				() -> {
					OsmConnection osm = ConnectionTestFactory.createConnection(null);
					osm.makeRequest("capabilities", (ApiResponseReader<Void>) in -> {
                        throw new Exception();
                    });
				}
		);
	}
}
