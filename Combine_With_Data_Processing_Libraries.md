# Combine With Data Processing Libraries

This library's core function is to facilitate communication with the Osm Api. For advanced data processing (i.e. converting between various data formats), there are some fully featured map data processing libraries like [Osmosis](https://github.com/openstreetmap/osmosis) or [osm4j](http://www.topobyte.de/projects/osm4j/) around. These libraries each use their own data structures, so how to best plug this library together with such a library?

There are several injection points where you can make the data your own:

## Option 1: Create own MapDataFactory
If you have control over your map data classes, you can make them implement `Node`, `Way` and `Relation` or (if not) write a decorator/wrapper that implements those interfaces. Then, you simply implement a custom [MapDataFactory](https://github.com/westnordost/osmapi/blob/master/libs/map/src/main/java/de/westnordost/osmapi/map/MapDataFactory.java) and pass it to the MapDataDao / MapDataHistoryDao.

	new MapDataApi(osm, new MyMapDataFactory());

## Option 2: Wrap the MapDataHandler
If you want to avoid adding wrappers for the data classes, you can instead copy the osmapi data in a wrapper around [MapDataHandler](https://github.com/westnordost/osmapi/blob/master/libs/map/src/main/java/de/westnordost/osmapi/map/handler/MapDataHandler.java) into your data. If you intend to upload changes using osmapi functionality, you need to convert the data back on upload of course. I.e.

	// MyMapDataHandlerWrapper implements MapDataHandler and creates my data from osmapi data 
	new MapDataApi(osm).getMap(bounds, new MyMapDataHandlerWrapper(sink));

With this option, osmapi data structures only serve as simple data transfer objects that are created and discarded during parsing and writing.

## Option 3: Write own Dao using OsmConnection
Osmosis can parse map data and also write map data _changes_ (few libraries can do that) itself, so in the case of Osmosis it can make sense to simply pass the InputStream/OutputStream to the library and leave the xml parsing and writing to itself. osmapi can still facilitate this a bit by letting the [OsmConnection](https://github.com/westnordost/osmapi/blob/master/libs/core/src/main/java/de/westnordost/osmapi/OsmConnection.java) manage the connection to the Osm Api

	// in OsmosisMapDataDao.java
	public void getMap(BoundingBox bounds, Sink sink)
	{
		osm.makeRequest("map?bbox=" + bounds.getAsLeftBottomRightTopString(), 
			new ApiResponseReader<Void>()
			{
				Void parse(InputStream in)
				{
					SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
					parser.parse(in, new OsmHandler(sink, true));
					return null;
				}
			}
	}

...and something similar for the upload. Note that Osmosis does not read the diff response on uploading map data changes to the server.

## Option 4: Don't use this library for map data

Now, OsmConnection alone does not do _that_ much, mostly streamlining error handling and managing the URL connection.

So in case your data processing library offers everything that osmapi offers in that aspect, it is your choice whether to leave everything regarding map data to that library and only use the osmapi for other Api Calls.

Osmosis has the [XmlDownloader](https://github.com/openstreetmap/osmosis/blob/master/osmosis-xml/src/main/java/org/openstreetmap/osmosis/xml/v0_6/XmlDownloader.java) and [XmlChangeUploader](https://github.com/openstreetmap/osmosis/blob/master/osmosis-xml/src/main/java/org/openstreetmap/osmosis/xml/v0_6/XmlChangeUploader.java). Again, it is to note that XmlChangeUploader does not read the diff response from the server, i.e. does not enable you to update your data model.
