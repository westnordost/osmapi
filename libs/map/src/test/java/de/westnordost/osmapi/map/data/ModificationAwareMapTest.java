package de.westnordost.osmapi.map.data;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.TestCase;

public class ModificationAwareMapTest extends TestCase
{
	private ModificationAwareMap<String, String> map;
	
	public void setUp()
	{
		map = makeMap();
	}
	
	public void testInitiallyNoModification()
	{
		assertFalse(map.isModified());
	}
	
	public void testModificationViaClear()
	{
		map.clear();
		assertTrue(map.isModified());
	}
	
	public void testModificationViaPut()
	{
		map.put("key3", "value3");
		assertTrue(map.isModified());
	}
	
	public void testModificationViaPutExistingValue()
	{
		map.put("key1", "value1");
		assertFalse(map.isModified());
	}
	
	public void testModificationViaRemove()
	{
		map.remove("key3");
		assertFalse(map.isModified());
		map.remove("key1");
		assertTrue(map.isModified());
	}
	
	public void testModificationViaPutAll()
	{
		map.putAll(Collections.<String, String> emptyMap());
		assertFalse(map.isModified());
		Map<String,String> map2 = new HashMap<>();
		map2.put("key3","value3");
		map.putAll(map2);
		assertTrue(map.isModified());
	}
	
	// --------------------------- Modification via values() --------------------------------------

	public void testModificationViaValuesClear()
	{
		map.values().clear();
		assertTrue(map.isModified());
	}
	
	public void testModificationViaValuesRemove()
	{
		map.values().remove("value3");
		assertFalse(map.isModified());
		map.values().remove("value1");
		assertTrue(map.isModified());
	}
	
	public void testModificationViaValuesRemoveAll()
	{
		map.values().removeAll(Collections.EMPTY_LIST);
		assertFalse(map.isModified());
		map.values().removeAll(Collections.singletonList("value1"));
		assertTrue(map.isModified());
	}
	
	public void testModificationViaValuesRetainAll()
	{
		map.values().retainAll(map.values());
		assertFalse(map.isModified());
		map.values().retainAll(Collections.EMPTY_LIST);
		assertTrue(map.isModified());
	}
	
	// addAll and add are not supported by Map.values()
	
	// --------------------------- Modification via entrySet() -------------------------------------
	
	public void testModificationViaEntrySetClear()
	{
		map.entrySet().clear();
		assertTrue(map.isModified());
	}
	
	public void testModificationViaEntrySetRemove()
	{
		map.entrySet().remove(makeExistingEntry());
		assertTrue(map.isModified());
	}
	
	public void testModificationViaEntrySetRemoveAll()
	{
		map.entrySet().removeAll(Collections.singletonList(makeNewEntry()));
		assertFalse(map.isModified());
		map.entrySet().removeAll(Collections.singletonList(makeExistingEntry()));
		assertTrue(map.isModified());
	}
	
	public void testModificationViaEntrySetRetainAll()
	{
		map.entrySet().retainAll(map.entrySet());
		assertFalse(map.isModified());
		map.entrySet().retainAll(Collections.EMPTY_LIST);
		assertTrue(map.isModified());
	}
	
	// addAll and add are not supported by Map.entrySet()
	
	// --------------------------- Modification via keySet() -------------------------------------
	
	public void testModificationViaKeySetClear()
	{
		map.keySet().clear();
		assertTrue(map.isModified());
	}
	
	public void testModificationViaKeySetRemove()
	{
		map.keySet().remove("key3");
		assertFalse(map.isModified());
		map.keySet().remove("key1");
		assertTrue(map.isModified());
	}
	
	public void testModificationViaKeySetRemoveAll()
	{
		map.keySet().removeAll(Collections.EMPTY_LIST);
		assertFalse(map.isModified());
		map.keySet().removeAll(Collections.singletonList("key1"));
		assertTrue(map.isModified());
	}
	
	public void testModificationViaKeySetRetainAll()
	{
		map.keySet().retainAll(map.keySet());
		assertFalse(map.isModified());
		map.keySet().retainAll(Collections.EMPTY_LIST);
		assertTrue(map.isModified());
	}
	
	// addAll and add are not supported by Map.keySet()
	
	// ---------------------- Modification via entrySet().iterator() -------------------------------
	
	public void testModificationViaEntrySetIterator()
	{
		Iterator<Entry<String, String>> it = map.entrySet().iterator();
		it.next();
		it.remove();
		assertTrue(map.isModified());
	}
	
	public void testModificationViaKeySetIterator()
	{
		Iterator<String> it = map.keySet().iterator();
		it.next();
		it.remove();
		assertTrue(map.isModified());
	}

	public void testModificationViaValuesIterator()
	{
		Iterator<String> it = map.values().iterator();
		it.next();
		it.remove();
		assertTrue(map.isModified());
	}
	
	// --------------------------------------------------------------------------------------------

	
	private ModificationAwareMap<String,String> makeMap()
	{
		Map<String, String> map = new HashMap<>();
		map.put("key1", "value1");
		map.put("key2", "value2");
		return new ModificationAwareMap<>(map);
	}
	
	private Entry<String,String> makeExistingEntry()
	{
		return new AbstractMap.SimpleEntry<>("key1","value1");
	}
	
	private Entry<String,String> makeNewEntry()
	{
		return new AbstractMap.SimpleEntry<>("key3","value3");
	}
}
