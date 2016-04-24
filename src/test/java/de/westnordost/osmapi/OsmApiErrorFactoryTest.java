package de.westnordost.osmapi;

import java.net.HttpURLConnection;

import junit.framework.TestCase;
import de.westnordost.osmapi.common.errors.OsmApiException;
import de.westnordost.osmapi.common.errors.OsmAuthorizationException;
import de.westnordost.osmapi.common.errors.OsmBadUserInputException;
import de.westnordost.osmapi.common.errors.OsmConflictException;
import de.westnordost.osmapi.common.errors.OsmConnectionException;
import de.westnordost.osmapi.common.errors.OsmNotFoundException;
import de.westnordost.osmapi.common.errors.OsmServiceUnavailableException;

public class OsmApiErrorFactoryTest extends TestCase
{

	public void testError()
	{
		for(int i = 0; i < 600; ++i)
		{
			RuntimeException e = OsmApiErrorFactory.createError(i, "test", "description");
			
			if(i >= 400 && i < 500) assertTrue(e instanceof OsmApiException);
			else assertTrue(e instanceof OsmConnectionException);
			
			switch(i)
			{
				case HttpURLConnection.HTTP_UNAVAILABLE:
					assertTrue(e instanceof OsmServiceUnavailableException);
					break;
				case HttpURLConnection.HTTP_NOT_FOUND:
				case HttpURLConnection.HTTP_GONE:
					assertTrue(e instanceof OsmNotFoundException);
					break;
				case HttpURLConnection.HTTP_FORBIDDEN:
					assertTrue(e instanceof OsmAuthorizationException);
					break;
				case HttpURLConnection.HTTP_CONFLICT:
					assertTrue(e instanceof OsmConflictException);
					break;
				case HttpURLConnection.HTTP_BAD_REQUEST:
					assertTrue(e instanceof OsmBadUserInputException);
					break;
			}
		}
	}
}
