# osmapi

osmapi is a complete Java implementation of the [OSM API 0.6](http://wiki.openstreetmap.org/wiki/API_v0.6) for clients.

It is well tested (test coverage over 90%) and being used by [StreetComplete](https://github.com/westnordost/StreetComplete), thus actively maintained.

## Copyright and License

© 2016-2018 Tobias Zwick. This library is released under the terms of the [GNU Lesser General Public License](http://www.gnu.org/licenses/lgpl-3.0.html) (LGPL).

## Installation

Add [`de.westnordost:osmapi:1.8`](https://maven-repository.com/artifact/de.westnordost/osmapi/1.8) as a Maven dependency or download the jar from there.
On Android, you need to exclude kxml2 from the dependencies since it is already built-in, like so:

```gradle
	compile ('de.westnordost:osmapi:1.8')
	{
		exclude group: 'net.sf.kxml', module: 'kxml2' // already included in Android
	}
```

## Basic Usage

Everything revolves around the OsmConnection, this is the class that talks to the Api. Specify where to reach the Api, how the client should identify itself towards the server etc.
If you plan to make calls that can only be made by a logged in user, such as uploading map data, an [OAuthConsumer](https://github.com/mttkay/signpost) (third parameter) needs to be specified.

```java
	OsmConnection osm = new OsmConnection(
	                          "https://api.openstreetmap.org/api/0.6/",
	                          "my user agent", null);
```

You can call osm.makeRequest(...) yourself to talk with the RESTful Api and write your own ApiRequestWriter and ApiResponseReader to write/read the request.
It is more convenient however to use the appropriate DAO to do that for you and return the data you are interested in. Currently there are the following DAOs:

| Class | Description
| ----- | -----------
| MapDataDao | download and upload map data, query single elements and their relations toward each other
| NotesDao | open, comment and close notes
| MapDataHistoryDao | query the history and specific versions of elements
| GpsTracesDao | query gps traces, upload and download traces and trackpoints
| ChangesetsDao | query changesets, take part in changeset discussions
| CapabilitiesDao | query the server capabilities
| UserDao | get user infos
| PermissionsDao | get user permissions
| UserPreferencesDao | query and edit user preferences

For example...

### Create a note

```java
	Note myNote = new NotesDao(osm).create(position, "My first note");
```

### Comment a changeset

```java
	ChangesetInfo changeset = new ChangesetsDao(osm).comment(id, "Cool changeset!");
```

### Get user info

```java
	UserInfo user = new UserDao(osm).get(id);
```

### Download map data

```java
	MapDataDao mapDao = new MapDataDao(osm);
	mapDao.getMap(boundingBox, myMapDataHandler);
```

myMapDataHandler implements MapDataHandler whose methods are called as the elements are parsed, think SAX parser. I.e. if you download 10MB of data, then the elements start arriving at the handler immediately so that you can process them on the fly.

```java
	/** This class is fed the map data. */
	public interface MapDataHandler
	{
		void handle(Bounds bounds);

		void handle(Node node);
		void handle(Way way);
		void handle(Relation relation);
	}
```

## Combine with data processing library
[Read this](https://github.com/westnordost/osmapi/wiki/Combine-With-Data-Processing-Libraries) if you want to use this library in conjunction with a data processing library like Osmosis, osm4j or have your own map data structures already.

## Troubleshooting

If you are getting the exception
```
sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target.
```
, try updating your Java SDK. Openstreetmap.org uses Let's Encrypt certificates which are not trusted in earlier versions of Java by default. [Read more here](https://stackoverflow.com/questions/34110426/does-java-support-lets-encrypt-certificates).
