# osmapi

osmapi is a client for the [OSM API 0.6](http://wiki.openstreetmap.org/wiki/API_v0.6).

It is well tested (test coverage over 90%) and being used by [StreetComplete](https://github.com/westnordost/StreetComplete), thus actively maintained.

Note, the OSM API, particularly the part to download the map data, is intended only for editing the map. It's not made for pulling larger amounts of data or data analysis of certain map features. If this is what you intend to do, the [Overpass API](https://wiki.openstreetmap.org/wiki/Overpass_API) is what you will want to use. I created a basic Java client for the Overpass API here, it builts upon this library: [osmapi-overpass](https://github.com/westnordost/osmapi-overpass).

## Copyright and License

© 2016-2021 Tobias Zwick. This library is released under the terms of the [GNU Lesser General Public License](http://www.gnu.org/licenses/lgpl-3.0.html) (LGPL).

## Installation

Depending on which part of the API you use, you can only include what you need:

<table>
<tr><th>Class</th><th>Dependency</th><th>Description</th></tr>
<tr><td>CapabilitiesDao</td><td><pre>de.westnordost:osmapi-core:2.0</pre></td><td>Getting server capabilities</td></tr>
<tr><td>PermissionsDao</td><td><pre>de.westnordost:osmapi-core:2.0</pre></td><td>Getting user permissions</td></tr>
<tr><td>MapDataDao</td><td><pre>de.westnordost:osmapi-map:2.0</pre></td><td>Getting map data, querying single elements and their relations toward each other and uploading changes in changesets</td></tr>
<tr><td>MapDataHistoryDao</td><td><pre>de.westnordost:osmapi-map:2.0</pre></td><td>Getting the history and specific versions of elements</td></tr>
<tr><td>NotesDao</td><td><pre>de.westnordost:osmapi-notes:2.0</pre></td><td>Getting finding, creating, commenting on and solving notes</td></tr>
<tr><td>GpsTracesDao</td><td><pre>de.westnordost:osmapi-traces:2.0</pre></td><td>Getting, uploading, updating and deleting GPS traces and trackpoints</td></tr>
<tr><td>ChangesetsDao</td><td><pre>de.westnordost:osmapi-changesets:2.0</pre></td><td>Finding changesets, changeset discussion, subscription and data</td></tr>
<tr><td>UserDao</td><td><pre>de.westnordost:osmapi-user:2.0</pre></td><td>Getting user information</td></tr>
<tr><td>UserPreferencesDao</td><td><pre>de.westnordost:osmapi-user:2.0</pre></td><td>Managing user preferences</td></tr>
</table>

To include everything, add [`de.westnordost:osmapi:4.0`](https://mvnrepository.com/artifact/de.westnordost/osmapi/4.0) as a Maven dependency or download the jar from there.

On Android, you need to exclude kxml2 from the dependencies since it is already built-in, like so:

```gradle
dependencies {
    implementation 'de.westnordost:osmapi:4.0'
}

configurations {
    // already included in Android
    all*.exclude group: 'net.sf.kxml', module: 'kxml2'
    
    // @NonNull etc annotations are also already included in Android
    cleanedAnnotations
    compile.exclude group: 'org.jetbrains', module:'annotations'
    compile.exclude group: 'com.intellij', module:'annotations'
    compile.exclude group: 'org.intellij', module:'annotations'
}
```

## Basic Usage

Everything revolves around the OsmConnection, this is the class that talks to the Api. Specify where to reach the Api, how the client should identify itself towards the server etc.
If you plan to make calls that can only be made by a logged in user, such as uploading map data, an [OAuthConsumer](https://github.com/mttkay/signpost) (third parameter) needs to be specified.

```java
	OsmConnection osm = new OsmConnection(
        "https://api.openstreetmap.org/api/0.6/",
        "my user agent", null
    );
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
