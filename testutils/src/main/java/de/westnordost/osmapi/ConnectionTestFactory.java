package de.westnordost.osmapi;

public class ConnectionTestFactory
{
	// to request new token:
	// 1. open in browser https://master.apis.dev.openstreetmap.org/oauth2/authorize?response_type=code&client_id=_RN0elf1uUxGvpdRQram0s_hfPpOkimHVXhlHN5Cx5I&redirect_uri=https://127.0.0.1/oauth&scope=read_prefs%20write_prefs%20write_diary%20write_api%20read_gpx%20write_gpx%20write_notes%20write_redactions%20consume_messages%20send_messages%20openid
	// 2. get the CODE from the url
	// 3. POST 'https://master.apis.dev.openstreetmap.org/oauth2/token?grant_type=authorization_code&code=<CODE>&client_id=_RN0elf1uUxGvpdRQram0s_hfPpOkimHVXhlHN5Cx5I&redirect_uri=https://127.0.0.1/oauth'
	private static final String ALLOW_EVERYTHING_TOKEN = "toivxF3JtN5d9CmOW9-r6JbaLwtfDFeJOjaJ4kf5tP4";

	// to request new token:
	// 1. open in browser https://master.apis.dev.openstreetmap.org/oauth2/authorize?response_type=code&client_id=_RN0elf1uUxGvpdRQram0s_hfPpOkimHVXhlHN5Cx5I&redirect_uri=https://127.0.0.1/oauth&scope=openid
	// 2. get the CODE from the url
	// 3. POST 'https://master.apis.dev.openstreetmap.org/oauth2/token?grant_type=authorization_code&code=<CODE>&client_id=_RN0elf1uUxGvpdRQram0s_hfPpOkimHVXhlHN5Cx5I&redirect_uri=https://127.0.0.1/oauth'
	private static final String ALLOW_NOTHING_TOKEN = "Nyj_dTRKE9o5Oyj5nzPuC9EbB-B9q_MlVM73QYeWP0o";

	private static final String UNKNOWN_TOKEN = "unknown";

	private static final String TEST_API_URL = "https://master.apis.dev.openstreetmap.org/api/0.6/";

	private static final String LIVE_API_URL = "https://api.openstreetmap.org/api/0.6/";

	public static final String USER_AGENT = "osmapi unit test";

	public enum User
	{
		ALLOW_EVERYTHING, ALLOW_NOTHING, UNKNOWN
	}

	public static OsmConnection createLiveConnection()
	{
		return new OsmConnection(LIVE_API_URL, USER_AGENT, null);
	}

	public static OsmConnection createConnection(User user)
	{
		String accessToken = "";
		if(user == User.ALLOW_EVERYTHING)
			accessToken = ALLOW_EVERYTHING_TOKEN;
		else if(user == User.ALLOW_NOTHING)
			accessToken = ALLOW_NOTHING_TOKEN;
		else if(user == User.UNKNOWN)
			accessToken = UNKNOWN_TOKEN;

		return new OsmConnection(TEST_API_URL, USER_AGENT, accessToken);
	}
}
