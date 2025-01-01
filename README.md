# osmapi

osmapi is a client for the [OSM API 0.6](http://wiki.openstreetmap.org/wiki/API_v0.6).

It is well tested (test coverage over 90%) and being used by [StreetComplete](https://github.com/westnordost/StreetComplete), thus actively maintained. It does not have any dependencies.

Note, the OSM API, particularly the part to download the map data, is intended only for editing the map. It's not made for pulling larger amounts of data or data analysis of certain map features. If this is what you intend to do, the [Overpass API](https://wiki.openstreetmap.org/wiki/Overpass_API) is what you will want to use. I created a basic Java client for the Overpass API here, it builts upon this library: [osmapi-overpass](https://github.com/westnordost/osmapi-overpass).

## Copyright and License

© 2016-2023 Tobias Zwick. This library is released under the terms of the [GNU Lesser General Public License](http://www.gnu.org/licenses/lgpl-3.0.html) (LGPL).

## Installation

Depending on which part of the API you use, you can only include what you need:

<table>
<tr><th>Class</th><th>Dependency</th><th>Description</th></tr>
<tr><td>CapabilitiesApi</td><td><pre>de.westnordost:osmapi-core:3.1</pre></td><td>Getting server capabilities</td></tr>
<tr><td>PermissionsApi</td><td><pre>de.westnordost:osmapi-core:3.1</pre></td><td>Getting user permissions</td></tr>
<tr><td>MapDataApi</td><td><pre>de.westnordost:osmapi-map:3.1</pre></td><td>Getting map data, querying single elements and their relations toward each other and uploading changes in changesets</td></tr>
<tr><td>MapDataHistoryApi</td><td><pre>de.westnordost:osmapi-map:3.1</pre></td><td>Getting the history and specific versions of elements</td></tr>
<tr><td>NotesApi</td><td><pre>de.westnordost:osmapi-notes:3.1</pre></td><td>Getting finding, creating, commenting on and solving notes</td></tr>
<tr><td>GpsTracesApi</td><td><pre>de.westnordost:osmapi-traces:3.2</pre></td><td>Getting, uploading, updating and deleting GPS traces and trackpoints</td></tr>
<tr><td>ChangesetsApi</td><td><pre>de.westnordost:osmapi-changesets:3.1</pre></td><td>Finding changesets, changeset discussion, subscription and data</td></tr>
<tr><td>UserApi</td><td><pre>de.westnordost:osmapi-user:3.1</pre></td><td>Getting user information</td></tr>
<tr><td>UserPreferencesApi</td><td><pre>de.westnordost:osmapi-user:3.1</pre></td><td>Managing user preferences</td></tr>
<tr><td>MessagesApi</td><td><pre>de.westnordost:osmapi-messages:1.0</pre></td><td>Send and receive messages</td></tr>
</table>

To include everything, add [`de.westnordost:osmapi:5.2`](https://mvnrepository.com/artifact/de.westnordost/osmapi/5.1) as a Maven dependency or download the jar from there.

### Android

On Android, you need to exclude kxml2 from the dependencies in your `gradle.kts` since it is already built-in, like so:

```kotlin
configurations {
    all {
        // it's already included in Android
        exclude(group = "net.sf.kxml", module = "kxml2")
        exclude(group = "xmlpull", module = "xmlpull")
    }
}
```

This library uses classes from the Java 8 time API, like [`Instant`](https://developer.android.com/reference/java/time/Instant) etc., so if your app supports Android API levels below 26, you need to enable [Java 8+ API desugaring support](https://developer.android.com/studio/write/java8-support#library-desugaring).

## Basic Usage

Everything revolves around the OsmConnection, this is the class that talks to the Api. Specify where to reach the Api, how the client should identify itself towards the server etc.
If you plan to make calls that can only be made by a logged in user, such as uploading map data, the OAuth 2.0 access token (third parameter) needs to be specified.

```java
    OsmConnection osm = new OsmConnection(
        "https://api.openstreetmap.org/api/0.6/",
        "my user agent", null
    );
```

You can call `osm.makeRequest(...)` yourself to talk with the RESTful Api and write your own ApiRequestWriter and ApiResponseReader to write/read the request.
It is more convenient however to use the appropriate class to do that for you and return the data you are interested in. See the table above for which classes are available.

For example...

### Create a note

```java
    Note myNote = new NotesApi(osm).create(position, "My first note");
```

### Comment a changeset

```java
    ChangesetInfo changeset = new ChangesetsApi(osm).comment(id, "Nice work!");
```

### Get user info

```java
    UserInfo user = new UserApi(osm).get(id);
```

### Download map data

```java
    MapDataApi mapApi = new MapDataApi(osm);
    mapApi.getMap(boundingBox, myMapDataHandler);
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
[Read this](Combine_With_Data_Processing_Libraries.md) if you want to use this library in conjunction with a data processing library like Osmosis, osm4j or have your own map data structures already.

## Troubleshooting

If you are getting the exception
```
sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target.
```
, try updating your Java SDK. Openstreetmap.org uses Let's Encrypt certificates which are not trusted in earlier versions of Java by default. [Read more here](https://stackoverflow.com/questions/34110426/does-java-support-lets-encrypt-certificates).
