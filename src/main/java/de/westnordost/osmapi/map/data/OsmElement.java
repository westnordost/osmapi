package de.westnordost.osmapi.map.data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.wololo.geojson.GeoJSON;
import org.wololo.jts2geojson.GeoJSONWriter;

import de.westnordost.osmapi.changesets.Changeset;
import util.JTSConverter;

/**
 * Base class for the osm primitives nodes, ways and relations
 */
public abstract class OsmElement implements Element, Serializable
{
	private static final long serialVersionUID = 1L;
	
	private long id;
	private int version;
	private Changeset changeset;
	private Date dateEdited;
	private OsmTags tags;
	private boolean deleted;
	private boolean modified;
	protected JTSConverter converter=new JTSConverter();

	public OsmElement(long id, int version, Map<String,String> tags)
	{
		this(id,version,tags,null,null);
	}
	
	public OsmElement(long id, int version, Map<String,String> tags, Changeset changeset, Date dateEdited)
	{
		this.id = id;
		this.version = version;
		this.changeset = changeset;
		this.tags = tags != null ? new OsmTags(tags) : null;
		this.dateEdited = dateEdited;
	}

	@Override
	public long getId()
	{
		return id;
	}

	@Override
	public Changeset getChangeset()
	{
		return changeset;
	}

	@Override
	public int getVersion()
	{
		return version;
	}
	
	@Override
	public Map<String, String> getTags()
	{
		return tags;
	}

	public void setTags(Map<String, String> tags)
	{
		modified = true;
		this.tags = tags != null ? new OsmTags(tags) : null;
	}

	@Override
	public boolean isNew()
	{
		return id < 0;
	}

	@Override
	public boolean isModified()
	{
		return modified || tags != null && tags.isModified();
	}

	@Override
	public boolean isDeleted()
	{
		return deleted;
	}

	public void setDeleted(boolean deleted)
	{
		this.deleted = deleted;
	}

	@Override
	public abstract Type getType();

	public Date getDateEdited()
	{
		return dateEdited;
	}
	
	public JSONObject toJSON() {
		Geometry geom=this.toJTSGeometry();
		JSONObject objj=new JSONObject();
		JSONArray features=new JSONArray();
		objj.put("type", "FeatureCollection");
		objj.put("features", features);
		JSONObject objfeat=new JSONObject();
		features.put(objfeat);
		JSONObject obj=new JSONObject();
		objfeat.put("type", "Feature");
		obj.put("elementId", this.getId());
		obj.put("version", this.getVersion());
		obj.put("entityType", "Way");
		obj.put("tmstmp", this.getChangeset().date.getTime());
		obj.put("changesetId", this.getChangeset().id);
		JSONArray tags=new JSONArray();
		obj.put("tags", tags);
		if(this.getTags()!=null) {
		for(String tag:this.getTags().keySet()) {
			JSONObject keyval=new JSONObject();
			keyval.put("key", tag);
			keyval.put("value", this.getTags().get(tag));
			tags.put(keyval);
		}
		}
		GeoJSONWriter writer = new GeoJSONWriter();
		  GeoJSON json = writer.write(geom);
		  String jsonstring = json.toString();
		  JSONObject geomobj=new JSONObject(jsonstring);
			objfeat.put("geometry", geomobj);
			objfeat.put("properties", obj);
		System.out.println(objj.toString());
		return objj;
	}
	
	public abstract String toWKT();
		
	public abstract Geometry toJTSGeometry();

	@Override
	public String toString() {
		return "OsmElement [id=" + id + ", version=" + version + ", changeset=" + changeset + ", dateEdited="
				+ dateEdited + ", tags=" + tags + ", deleted=" + deleted + ", modified=" + modified + ", converter="
				+ converter + "]";
	}
	

	
	
}
