package de.westnordost.osmapi.map.changes;

import java.util.ArrayList;
import java.util.List;

import de.westnordost.osmapi.map.data.Bounds;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.Way;

public class SimpleMapDataChangesHandler implements MapDataChangesHandler, MapDataChanges
{
	private List<Element> creations;
	private List<Element> modifications;
	private List<Element> deletions;
	private Mode mode;
	private enum Mode
	{
		CREATIONS, MODIFICATIONS, DELETIONS
	}

	public SimpleMapDataChangesHandler()
	{
		creations = new ArrayList<>();
		modifications = new ArrayList<>();
		deletions = new ArrayList<>();
	}

	@Override
	public void onStartCreations()
	{
		mode = Mode.CREATIONS;
	}

	@Override
	public void onStartModifications()
	{
		mode = Mode.MODIFICATIONS;
	}

	@Override
	public void onStartDeletions()
	{
		mode = Mode.DELETIONS;
	}

	@Override
	public void handle(Bounds bounds)
	{
		// ignore, not interested in that...
	}

	@Override
	public void handle(Node node)
	{
		handleElement(node);
	}

	@Override
	public void handle(Way way)
	{
		handleElement(way);
	}

	@Override
	public void handle(Relation relation)
	{
		handleElement(relation);
	}

	private void handleElement(Element element)
	{
		switch(mode)
		{
			case CREATIONS:
				creations.add(element);
				break;
			case MODIFICATIONS:
				modifications.add(element);
				break;
			case DELETIONS:
				deletions.add(element);
				break;
		}
	}

	public List<Element> getDeletions()
	{
		return deletions;
	}

	public List<Element> getModifications()
	{
		return modifications;
	}

	public List<Element> getCreations()
	{
		return creations;
	}

	public List<Element> getAll()
	{
		List<Element> result = new ArrayList<>();
		result.addAll(creations);
		result.addAll(modifications);
		result.addAll(deletions);
		return result;
	}
}
