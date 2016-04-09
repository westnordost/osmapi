package de.westnordost.osmapi;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;

public class ConnectionTestFactory
{
	private static final String CONSUMER_KEY = "CuPCn3sRc8FDiepAoSkH4a9n7w2QuqVCykStfVPG";
	private static final String CONSUMER_SECRET = "D1nX6BF1NMAZtIq8ouGJJ7zGtSaTRDTz8QfZl5mo";

	private static final String TEST_API_URL = "http://api06.dev.openstreetmap.org/api/0.6/";

	private static final String LIVE_API_URL = "http://api.openstreetmap.org/api/0.6/";

	public static final String USER_AGENT = "osmapi unit test";

	public enum User
	{
		ALLOW_EVERYTHING, ALLOW_NOTHING
	}

	public static OsmConnection createLiveConnection()
	{
		return new OsmConnection(LIVE_API_URL, USER_AGENT, null);
	}

	public static OsmConnection createConnection(User user)
	{
		OAuthConsumer consumer = null;

		if(user == User.ALLOW_EVERYTHING)
			consumer = createConsumerThatAllowsEverything();
		else if(user == User.ALLOW_NOTHING)
			consumer = createConsumerThatProhibitsEverything();

		return new OsmConnection(TEST_API_URL, USER_AGENT, consumer);
	}


	private static OAuthConsumer createConsumerThatProhibitsEverything()
	{
		OAuthConsumer result = new DefaultOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
		result.setTokenWithSecret(
				"RCamNf4TT7uNeFjmigvOUWhajp5ERFZmcN1qvi7a",
				"72dzmAvuNBEOVKkif3JSYdzMlAq2dw5OnIG75dtX");
		return result;
	}

	private static OAuthConsumer createConsumerThatAllowsEverything()
	{
		OAuthConsumer result = new DefaultOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
		result.setTokenWithSecret(
				"2C4LiOQBOn96kXHyal7uzMJiqpCsiyDBvb8pomyX",
				"1bFMIQpgmu5yjywt3kknopQpcRmwJ6snDDGF7kdr");
		return result;
	}
}
