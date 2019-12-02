package de.westnordost.osmapi.map.data;

import java.util.List;

public interface Way extends Element
{
	List<Long> getNodeIds();
}
