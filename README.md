# osmapi

osmapi is a complete Java implementation of the [OSM API 0.6](http://wiki.openstreetmap.org/wiki/API_v0.6) for clients.

It is well tested (test coverage over 90%) and being used by [StreetComplete](https://github.com/westnordost/StreetComplete), thus actively maintained.

## Copyright and License

© 2016-2019 Tobias Zwick. This library is released under the terms of the [GNU Lesser General Public License](http://www.gnu.org/licenses/lgpl-3.0.html) (LGPL).

## Installation

Depending on which part of the API you use, you can only include what you need:

| Class | Dependency | Description
| ----- | ---------- | -----------
| CapabilitiesDao | `de.westnordost:osmapi-core:1.0` | Getting server capabilities
| PermissionsDao | `de.westnordost:osmapi-core:1.0` | Getting user permissions
| MapDataDao | `de.westnordost:osmapi-map:1.0` | Getting map data, querying single elements and their relations toward each other and uploading changes in changesets
| MapDataHistoryDao | `de.westnordost:osmapi-map:1.0` | Getting the history and specific versions of elements
| NotesDao | `de.westnordost:osmapi-notes:1.0` | Getting finding, creating, commenting on and solving notes
| GpsTracesDao | `de.westnordost:osmapi-traces:1.0` | Getting, uploading, updating and deleting GPS traces and trackpoints
| ChangesetsDao | `de.westnordost:osmapi-changesets:1.0` | Finding changesets, changeset discussion, subscription and data
| UserDao | `de.westnordost:osmapi-user:1.0` | Getting user information
| UserPreferencesDao | `de.westnordost:osmapi-user:1.0` | Managing user preferences

To include everything, add [`de.westnordost:osmapi:3.5`](https://maven-repository.com/artifact/de.westnordost/osmapi/3.5) as a Maven dependency or download the jar from there.
On Android, you need to exclude kxml2 from the dependencies since it is already built-in, like so:

```gradle
	compile ('de.westnordost:osmapi:3.5')
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
It is more convenient however to use the appropriate DAO to do that for you and return the data you are interested in. See the table above for which DAOs are available.

For example...

### Create a note

```java
	Note myNote = new NotesDao(osm).create(position, "My first note");
```

### Comment a changeset

```java
	ChangesetInfo changeset = new ChangesetsDao(osm).comment(id, "Nice work!");
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

myMapDataHandler implements MapDataHandler whose methods are called as the elements are parsed, think SAX parser. I.e. if you download 10MB of data, then the elements start arriving at the handler as the data comes in so that you can process them on the fly.

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
