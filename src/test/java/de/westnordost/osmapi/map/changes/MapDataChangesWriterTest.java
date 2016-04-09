package de.westnordost.osmapi.map.changes;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.map.MapDataParser;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.map.data.OsmNode;
import de.westnordost.osmapi.map.data.OsmRelation;
import de.westnordost.osmapi.map.data.OsmRelationMember;
import de.westnordost.osmapi.map.data.OsmWay;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;
import de.westnordost.osmapi.map.data.Way;

public class MapDataChangesWriterTest extends TestCase
{
	public void testWriteElement() throws IOException
	{
		Element originalElement = createNode(-1);
		originalElement.setTags(tags);

		String xml = writeXml(123, originalElement);
		List<Element> xmlElements = parseXml(xml).getAll();

		assertEquals(1, xmlElements.size());

		Element node = xmlElements.get(0);

		assertEquals(originalElement.getId(), node.getId());
		assertEquals(123, node.getChangeset().getId());
		assertEquals(originalElement.getVersion(), node.getVersion());
		assertEquals(originalElement.getTags(), node.getTags());
	}

	public void testWriteNode() throws IOException
	{
		Node originalNode = createNode(-1);

		String xml = writeXml(0, originalNode);
		List<Element> xmlElements = parseXml(xml).getAll();

		Node node = (Node) xmlElements.get(0);
		assertEquals(originalNode.getPosition(), node.getPosition());
		assertEquals(originalNode.getTags(), node.getTags());
	}

	public void testWriteWay() throws IOException
	{
		Way originalWay = createWay(-1);

		String xml = writeXml(0, originalWay);
		List<Element> xmlElements = parseXml(xml).getAll();

		Way way = (Way) xmlElements.get(0);
		assertEquals(originalWay.getNodeIds(), way.getNodeIds());
	}

	public void testWriteRelation() throws IOException
	{
		Relation originalRelation = createRelation(-1);

		String xml = writeXml(0, originalRelation);
		List<Element> xmlElements = parseXml(xml).getAll();

		Relation relation = (Relation) xmlElements.get(0);
		assertEquals(originalRelation.getMembers(), relation.getMembers());
	}

	public void testCreate() throws IOException
	{
		Element originalElement = createNode(-1);
		String xml = writeXml(0, originalElement);

		assertEquals(1,parseXml(xml).getCreations().size());
	}

	public void testModified() throws IOException
	{
		Element originalElement = createNode(1);
		originalElement.setTags(tags);
		String xml = writeXml(0, originalElement);

		assertEquals(1, parseXml(xml).getModifications().size());
	}

	public void testDelete() throws IOException
	{
		Element originalElement = createNode(1);
		originalElement.setDeleted(true);
		String xml = writeXml(0, originalElement);

		assertEquals(1, parseXml(xml).getDeletions().size());
	}

	public void testOrder() throws IOException
	{
		List<Element> correctOrder = createListOfChangesInCorrectOrder();
		List<Element> someOrder = createListOfChangesInCorrectOrder();
		Collections.shuffle(someOrder);

		String xml = writeXml(1, someOrder);
		List<Element> xmlOrder = parseXml(xml).getAll();

		assertEquals(correctOrder.size(), xmlOrder.size());
		for(int i = 0; i<correctOrder.size(); ++i)
		{
			assertEquals(correctOrder.get(i).getId(), xmlOrder.get(i).getId());
		}
	}

	public void testWriteUnchanged() throws IOException
	{
		List<Element> elements = new ArrayList<>();
		elements.add(createNode(1));
		elements.add(createWay(1));
		elements.add(createRelation(1));

		MapDataChangesWriter writer = new MapDataChangesWriter(1, elements);
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		assertFalse(writer.hasChanges());

		writer.write(out);
		String xml = new String( out.toByteArray(), "UTF-8" );

		assertTrue(parseXml(xml).getAll().isEmpty());
	}

	public void testIgnoreDeletedCreation()
	{
		Element element = createNode(-1);
		element.setDeleted(true);

		MapDataChangesWriter writer = new MapDataChangesWriter(1, Arrays.asList(element));
		assertFalse(writer.hasChanges());
	}

	private static String writeXml(long changesetId, Element singleElement) throws IOException
	{
		List<Element> elements = new ArrayList<>(1);
		elements.add(singleElement);
		return writeXml(changesetId, elements);
	}

	private static String writeXml(long changesetId, List<Element> elements) throws IOException
	{
		MapDataChangesWriter writer = new MapDataChangesWriter(changesetId, elements);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writer.write(out);
		return new String( out.toByteArray(), "UTF-8" );
	}

	private static MapDataChanges parseXml(String xml) throws UnsupportedEncodingException
	{
		SimpleMapDataChangesHandler handler = new SimpleMapDataChangesHandler();

		MapDataParser parser = new MapDataChangesParser(handler);
		ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
		parser.parse(stream);

		return handler;
	}

	private static LatLon pos = new OsmLatLon(5,6);
	private static Map<String,String> tags = createTags();
	private static List<Long> nodes = createNodes();
	private static List<RelationMember> relationMembers = createMembers();

	private static Map<String,String> createTags()
	{
		Map<String,String> tags = new HashMap<>();
		tags.put("test", "blub");
		return tags;
	}

	private static List<Long> createNodes()
	{
		List<Long> nodes = new ArrayList<>();
		nodes.add(555L);
		return nodes;
	}

	private static List<RelationMember> createMembers()
	{
		List<RelationMember> members = new ArrayList<>();
		members.add(new OsmRelationMember(5,"outer",Element.Type.WAY));
		return members;
	}

	private static ArrayList<Element> createListOfChangesInCorrectOrder()
	{
		ArrayList<Element> elements = new ArrayList<>();

		Node newNode = createNode(-3);
		Node modifiedNode = createNode(6);
		modifiedNode.setTags(tags);
		Node deleteNode = createNode(9);
		deleteNode.setDeleted(true);

		Way newWay = createWay(-2);
		Way modifiedWay = createWay(5);
		modifiedWay.setTags(tags);
		Way deleteWay = createWay(8);
		deleteWay.setDeleted(true);

		Relation newRelation = createRelation(-1);
		Relation modifiedRelation = createRelation(4);
		modifiedRelation.setTags(tags);
		Relation deleteRelation = createRelation(7);
		deleteRelation.setDeleted(true);

		elements.add(newNode);
		elements.add(newWay);
		elements.add(newRelation);
		elements.add(modifiedRelation);
		elements.add(modifiedWay);
		elements.add(modifiedNode);
		elements.add(deleteRelation);
		elements.add(deleteWay);
		elements.add(deleteNode);

		return elements;
	}

	private static Node createNode(long id)
	{
		return new OsmNode(id, 1, pos, null, null);
	}

	private static Way createWay(long id)
	{
		return new OsmWay(id, 1, nodes, null, null);
	}

	private static Relation createRelation(long id)
	{
		return new OsmRelation(id, 1, relationMembers, null, null);
	}
}
