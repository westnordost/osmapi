package de.westnordost.osmapi.map.changes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;
import de.westnordost.osmapi.map.data.Way;
import de.westnordost.osmapi.xml.XmlWriter;

public class MapDataChangesWriter extends XmlWriter
{
	private long changesetId;
	private List<Element> creations;
	private List<Element> modifications;
	private List<Element> deletions;

	public MapDataChangesWriter(long changesetId, Iterable<Element> elements)
	{
		this.changesetId = changesetId;

		creations = new ArrayList<>();
		modifications = new ArrayList<>();
		deletions = new ArrayList<>();
		for(Element element : elements)
		{
			// new deleted elements are ignored
			if(element.isNew() && element.isDeleted())
				continue;
			else if(element.isNew())
				creations.add(element);
			else if(element.isDeleted())
				deletions.add(element);
			else if(element.isModified())
				modifications.add(element);
		}
		/* Order changes in such a way that they can be applied to a data store while maintaining
		   data integrity (ie. a database). For example, the ordering prevents a way being added
		   before the underlying nodes are created.
		   Idea taken from Osmosis.*/
		Collections.sort(creations, new OrderByNodeWayRelation());
		Collections.sort(modifications, new OrderByRelationWayNode());
		Collections.sort(deletions, new OrderByRelationWayNode());
	}

	public boolean hasChanges()
	{
		return !creations.isEmpty() || !modifications.isEmpty() || !deletions.isEmpty();
	}

	private class OrderByRelationWayNode implements Comparator<Element>
	{
		public int compare(Element lhs, Element rhs)
		{
			return getTypeOrder(rhs.getType()) - getTypeOrder(lhs.getType());
		}
	}

	private class OrderByNodeWayRelation implements Comparator<Element>
	{
		public int compare(Element lhs, Element rhs)
		{
			return getTypeOrder(lhs.getType()) - getTypeOrder(rhs.getType());
		}
	}

	private static int getTypeOrder(Element.Type type)
	{
		switch(type)
		{
			case NODE:		return 1;
			case WAY:		return 2;
			case RELATION:	return 3;
		}
		return 0;
	}

	@Override
	protected void write() throws IOException
	{
		begin("osmChange");

		if(!creations.isEmpty())
		{
			begin("create");
			for (Element element : creations) writeElement(element);
			end();
		}

		if(!modifications.isEmpty())
		{
			begin("modify");
			for (Element element : modifications) writeElement(element);
			end();
		}

		if(!deletions.isEmpty())
		{
			begin("delete");
			for (Element element : deletions) writeElement(element);
			end();
		}

		end();
	}

	private void writeElement(Element element) throws IOException
	{
		begin(toXmlName(element.getType()));
		writeElementAttributes(element);

		if(element instanceof Node)
		{
			writeNodeContents((Node) element);
		}
		else if(element instanceof Way)
		{
			writeWayContents((Way) element);
		}
		else if(element instanceof Relation)
		{
			writeRelationContents((Relation) element);
		}

		writeTags(element.getTags());
		end();
	}

	private static String toXmlName(Element.Type type)
	{
		return type.toString().toLowerCase();
	}

	private void writeElementAttributes(Element element) throws IOException
	{
		attribute("id", element.getId());
		attribute("version", element.getVersion());
		attribute("changeset", changesetId);
	}

	private void writeNodeContents(Node node) throws IOException
	{
		LatLon position = node.getPosition();
		attribute("lat", position.getLatitude());
		attribute("lon", position.getLongitude());
	}

	private void writeWayContents(Way way) throws IOException
	{
		for(Long node : way.getNodeIds())
		{
			begin("nd");
			attribute("ref", node);
			end();
		}
	}

	private void writeRelationContents(Relation relation) throws IOException
	{
		for(RelationMember member : relation.getMembers())
		{
			begin("member");
			attribute("ref", member.getRef());
			attribute("type", toXmlName(member.getType()));
			attribute("role", member.getRole());
			end();
		}
	}

	private void writeTags(Map<String, String> tags) throws IOException
	{
		if(tags != null)
		{
			for (Map.Entry<String, String> tag : tags.entrySet())
			{
				begin("tag");
				attribute("k", tag.getKey());
				attribute("v", tag.getValue());
				end();
			}
		}
	}
}
