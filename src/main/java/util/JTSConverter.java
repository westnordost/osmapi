package util;

import java.util.LinkedList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;
import de.westnordost.osmapi.map.data.Way;;

public class JTSConverter {
	
	private PrecisionModel precisionModel;
	private GeometryFactory geometryFactory;
	
	public static OsmConnection connection=new OsmConnection(
            "https://api.openstreetmap.org/api/0.6/",
            "my user agent", null);
	
	public JTSConverter() {
		geometryFactory = new GeometryFactory(precisionModel);
	}
	
	public Coordinate convertNodeToCoordinate(Node node) {
		return new Coordinate(node.getPosition().getLatitude(), node.getPosition().getLongitude());
	}

	public CoordinateSequence convertNodesToCoordinateSequence(List<Long> nodes) {
		MapDataDao mapdata = new MapDataDao(connection);
		List<Coordinate> coords = new LinkedList<Coordinate>();
		for (int i = 0; i < nodes.size(); i++) {
			try {
				coords.add(convertNodeToCoordinate(mapdata.getNode(nodes.get(i))));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new CoordinateArraySequence(coords.toArray(new Coordinate[coords.size()]));
	}
	
	public List<Coordinate> convertNodesToCoordinateList(List<Long> nodes) {
		MapDataDao mapdata = new MapDataDao(connection);
		List<Coordinate> coords = new LinkedList<Coordinate>();
		for (int i = 0; i < nodes.size(); i++) {
			try {
				coords.add(convertNodeToCoordinate(mapdata.getNode(nodes.get(i))));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return coords;
	}

	public Point convertNode(Node node) {
		Coordinate[] coords = { convertNodeToCoordinate(node) };
		return new Point(new CoordinateArraySequence(coords), geometryFactory);
	}

	public Geometry convertWay(Way way) {
		CoordinateSequence coordSeq = convertNodesToCoordinateSequence(way.getNodeIds());
		try {
			LineString wayy = new LineString(coordSeq, geometryFactory);
			if (wayy.isClosed()) {

				LinearRing ring = new LinearRing(coordSeq, geometryFactory);
				return new Polygon(ring, null, geometryFactory);

			} else {
				return new LineString(coordSeq, geometryFactory);
			}
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	public Geometry convertRelation(Relation relation) {
		MapDataDao mapdata = new MapDataDao(connection);
		List<Element> relpoints=new LinkedList<Element>();
		for(RelationMember member:relation.getMembers()) {
			switch(member.getType()) {
			case NODE: Node node=mapdata.getNode(member.getRef());
					relpoints.add(node);
					break;
			case WAY: Way way=mapdata.getWay(member.getRef());
					  relpoints.add(way);
					  break;
			default:
					
			}
		}
		List<Coordinate> coords = new LinkedList<Coordinate>();
		for(Element elem:relpoints) {
			if(elem instanceof Node) {
				coords.add(convertNodeToCoordinate((Node)elem));
			}else if(elem instanceof Way) {
				coords.addAll(convertNodesToCoordinateList(((Way)elem).getNodeIds()));
			}
		}
		CoordinateSequence coordSeq = new CoordinateArraySequence(coords.toArray(new Coordinate[coords.size()]));
		try {
			LineString wayy = new LineString(coordSeq, geometryFactory);
			if (wayy.isClosed()) {

				LinearRing ring = new LinearRing(coordSeq, geometryFactory);
				return new Polygon(ring, null, geometryFactory);

			} else {
				return new LineString(coordSeq, geometryFactory);
			}
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
}
