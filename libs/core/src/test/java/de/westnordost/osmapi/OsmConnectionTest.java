package de.westnordost.osmapi;

import org.junit.Test;

import java.io.InputStream;

import de.westnordost.osmapi.ConnectionTestFactory.User;
import de.westnordost.osmapi.common.errors.OsmApiReadResponseException;
import de.westnordost.osmapi.common.errors.OsmAuthorizationException;
import de.westnordost.osmapi.common.errors.OsmConnectionException;

import static org.junit.Assert.*;

public class OsmConnectionTest
{
	@Test public void authorizationException()
	{
		try
		{
			OsmConnection osm = ConnectionTestFactory.createConnection(null);
			osm.makeAuthenticatedRequest("doesntMatter", "GET");
			fail();
		}
		catch(OsmAuthorizationException ignore) {}
	}
	
	@Test public void authorizationException2()
	{
		try
		{
			OsmConnection osm = ConnectionTestFactory.createConnection(User.UNKNOWN);
			osm.makeAuthenticatedRequest("changeset/create", "PUT", null, null);
			fail();
		}
		catch(OsmAuthorizationException ignore) {}
	}
	
	@Test public void connectionException()
	{
		try
		{
			OsmConnection osm = new OsmConnection("http://cant.connect.to.this.server.hm", "blub", null);
			osm.makeRequest("doesntMatter", null);
			fail();
		}
		catch(OsmConnectionException ignore) {}
	}
	
	@Test public void errorParsingApiResponse()
	{
		try
		{
			OsmConnection osm = ConnectionTestFactory.createConnection(null);
			osm.makeRequest("capabilities", new ApiResponseReader<Void>()
			{
				@Override
				public Void parse(InputStream in) throws Exception
				{
					throw new Exception();
				}
			});
			fail();
		}
		catch(OsmApiReadResponseException ignore) {}
	}

}
