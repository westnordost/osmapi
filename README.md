#osmapi#

osmapi is a Java implementation of the [OSM API 0.6](http://wiki.openstreetmap.org/wiki/API_v0.6) for clients

## License

This library is released under the terms of the [GNU Lesser General Public License](http://www.gnu.org/licenses/lgpl-3.0.html). If this does not satisfy your needs, talk to me. I am inclined to change it if an ISC/BSD based open source project wants to use this library.

## Installation

Add `de.westnordost:osmapi:1.0` as a Maven dependency. On Android, exclude kxml2 from the dependencies since it is already built-in.

## Basic Usage

Everything revolves around the OsmConnection, this is the class that talks to the Api. Specify where to reach the Api, how the client should identify itself towards the server etc.
If you plan to make calls that can only be made by a logged in user, such as uploading map data, an [OAuthConsumer](https://github.com/mttkay/signpost) (third parameter) needs to be specified.

		OsmConnection osm = new OsmConnection(
		                          "https://api.openstreetmap.org/api/0.6/",
		                          "my user agent");

You can call osm.makeRequest(...) yourself to talk with the RESTful Api and write your own ApiRequestWriter and ApiResponseReader to write/read the request.
It is more convenient however to use the appropriate DAO to do that for you and return the data you are interested in. Currently there are the following DAOs:

* MapDataDao - download and upload map data, query single elements and their relations toward each other
* NotesDao - open, comment and close notes
* MapDataHistoryDao - query the history and specific versions of elements
* GpsTracesDao - query gps traces, upload and download traces and trackpoints
* ChangesetsDao - query changesets, take part in changeset discussions
* CapabilitiesDao - query the server capabilities
* UserDao - get user infos
* PermissionsDao - get user permissions
* UserPreferencesDao - query and edit user preferences

For example...

### Create a note

		Note myNote = new NotesDao(osm).create(position, "My first note");

### Comment a changeset

		ChangesetInfo changeset = new ChangesetsDao(osm).comment(id, "Cool changeset!");

### Get user info

		UserInfo user = new UserDao(osm).get(id);
		
### Download map data

		MapDataDao mapDao = new MapDataDao(osm);
		mapDao.getMap(boundingBox, myMapDataHandler);

myMapDataHandler implements MapDataHandler whose methods are called as the elements are parsed, think SAX parser. I.e. if you download 10MB of data, then the elements start arriving at the handler immediately so that you can process them on the fly.

		/** This class is fed the map data. */
		public interface MapDataHandler
		{
			void handle(Bounds bounds);

			void handle(Node node);
			void handle(Way way);
			void handle(Relation relation);
		}

## Combine with data processing library
[Read this](https://github.com/westnordost/osmapi/wiki/Combine-With-Data-Processing-Libraries) if you want to use this library in conjunction with a data processing library like Osmosis, osm4j or have your own map data structures already.