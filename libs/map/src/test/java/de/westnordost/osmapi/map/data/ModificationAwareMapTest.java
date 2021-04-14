package de.westnordost.osmapi.map.data;

import org.junit.Before;
import org.junit.Test;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.Assert.*;

public class ModificationAwareMapTest
{
	private ModificationAwareMap<String, String> map;
	
	@Before	public void setUp()
	{
		map = makeMap();
	}
	
	@Test public void initiallyNoModification()
	{
		assertFalse(map.isModified());
	}
	
	@Test public void modificationViaClear()
	{
		map.clear();
		assertTrue(map.isModified());
	}
	
	@Test public void modificationViaPut()
	{
		map.put("key3", "value3");
		assertTrue(map.isModified());
	}
	
	@Test public void modificationViaPutExistingValue()
	{
		map.put("key1", "value1");
		assertFalse(map.isModified());
	}
	
	@Test public void modificationViaRemove()
	{
		map.remove("key3");
		assertFalse(map.isModified());
		map.remove("key1");
		assertTrue(map.isModified());
	}
	
	@Test public void modificationViaPutAll()
	{
		map.putAll(Collections.<String, String> emptyMap());
		assertFalse(map.isModified());
		Map<String,String> map2 = new HashMap<>();
		map2.put("key3","value3");
		map.putAll(map2);
		assertTrue(map.isModified());
	}
	
	// --------------------------- Modification via values() --------------------------------------

	@Test public void modificationViaValuesClear()
	{
		map.values().clear();
		assertTrue(map.isModified());
	}
	
	@Test public void modificationViaValuesRemove()
	{
		map.values().remove("value3");
		assertFalse(map.isModified());
		map.values().remove("value1");
		assertTrue(map.isModified());
	}
	
	@Test public void modificationViaValuesRemoveAll()
	{
		map.values().removeAll(Collections.EMPTY_LIST);
		assertFalse(map.isModified());
		map.values().removeAll(Arrays.asList("value1"));
		assertTrue(map.isModified());
	}
	
	@Test public void modificationViaValuesRetainAll()
	{
		map.values().retainAll(map.values());
		assertFalse(map.isModified());
		map.values().retainAll(Collections.EMPTY_LIST);
		assertTrue(map.isModified());
	}
	
	// addAll and add are not supported by Map.values()
	
	// --------------------------- Modification via entrySet() -------------------------------------
	
	@Test public void modificationViaEntrySetClear()
	{
		map.entrySet().clear();
		assertTrue(map.isModified());
	}
	
	@Test public void modificationViaEntrySetRemove()
	{
		map.entrySet().remove(makeExistingEntry());
		assertTrue(map.isModified());
	}
	
	@Test public void modificationViaEntrySetRemoveAll()
	{
		map.entrySet().removeAll(Arrays.asList(makeNewEntry()));
		assertFalse(map.isModified());
		map.entrySet().removeAll(Arrays.asList(makeExistingEntry()));
		assertTrue(map.isModified());
	}
	
	@Test public void modificationViaEntrySetRetainAll()
	{
		map.entrySet().retainAll(map.entrySet());
		assertFalse(map.isModified());
		map.entrySet().retainAll(Collections.EMPTY_LIST);
		assertTrue(map.isModified());
	}
	
	// addAll and add are not supported by Map.entrySet()
	
	// --------------------------- Modification via keySet() -------------------------------------
	
	@Test public void modificationViaKeySetClear()
	{
		map.keySet().clear();
		assertTrue(map.isModified());
	}
	
	@Test public void modificationViaKeySetRemove()
	{
		map.keySet().remove("key3");
		assertFalse(map.isModified());
		map.keySet().remove("key1");
		assertTrue(map.isModified());
	}
	
	@Test public void modificationViaKeySetRemoveAll()
	{
		map.keySet().removeAll(Collections.EMPTY_LIST);
		assertFalse(map.isModified());
		map.keySet().removeAll(Arrays.asList("key1"));
		assertTrue(map.isModified());
	}
	
	@Test public void modificationViaKeySetRetainAll()
	{
		map.keySet().retainAll(map.keySet());
		assertFalse(map.isModified());
		map.keySet().retainAll(Collections.EMPTY_LIST);
		assertTrue(map.isModified());
	}
	
	// addAll and add are not supported by Map.keySet()
	
	// ---------------------- Modification via entrySet().iterator() -------------------------------
	
	@Test public void modificationViaEntrySetIterator()
	{
		Iterator<Entry<String, String>> it = map.entrySet().iterator();
		it.next();
		it.remove();
		assertTrue(map.isModified());
	}
	
	@Test public void modificationViaKeySetIterator()
	{
		Iterator<String> it = map.keySet().iterator();
		it.next();
		it.remove();
		assertTrue(map.isModified());
	}

	@Test public void modificationViaValuesIterator()
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
