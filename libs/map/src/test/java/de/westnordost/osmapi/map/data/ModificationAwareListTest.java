package de.westnordost.osmapi.map.data;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static org.junit.Assert.*;

public class ModificationAwareListTest
{
	private ModificationAwareList<String> list;
	
	@Before	public void setUp() { list = makeList(); }
	
	@Test public void initiallyNoModification()	{ assertFalse(list.isModified()); }

	@Test public void modificationOnAdd()		{ checkModificationOnAdd(list); }
	@Test public void modificationOnAddAt()		{ checkModificationOnAddAt(list); }
	@Test public void modificationOnAddAll()	{ checkModificationOnAddAll(list); }
	@Test public void modificationOnAddAllAt()	{ checkModificationOnAddAllAt(list); }
	@Test public void modificationOnRemove()	{ checkModificationOnRemove(list); }
	@Test public void modificationOnRemoveObj()	{ checkModificationOnRemoveObj(list); }
	@Test public void modificationOnRemoveAll()	{ checkModificationOnRemoveAll(list); }
	@Test public void modificationOnRetainAll()	{ checkModificationOnRetainAll(list); }
	@Test public void modificationOnSet()		{ checkModificationOnSet(list); }
	@Test public void modificationOnClear()		{ checkModificationOnClear(list); }

	// --------------------------- Modification via sublist() --------------------------------------

	@Test public void modificationOnSublistAdd()		{ checkModificationOnAdd(sublist()); }
	@Test public void modificationOnSublistAddAt()		{ checkModificationOnAddAt(sublist()); }
	@Test public void modificationOnSublistAddAll()		{ checkModificationOnAddAll(sublist()); }
	@Test public void modificationOnSublistAddAllAt()	{ checkModificationOnAddAllAt(sublist()); }
	@Test public void modificationOnSublistRemove()		{ checkModificationOnRemove(sublist()); }
	@Test public void modificationOnSublistRemoveObj()	{ checkModificationOnRemoveObj(sublist()); }
	@Test public void modificationOnSublistRemoveAll()	{ checkModificationOnRemoveAll(sublist()); }
	@Test public void modificationOnSublistRetainAll()	{ checkModificationOnRetainAll(sublist()); }
	@Test public void modificationOnSublistSet()		{ checkModificationOnSet(sublist()); }
	@Test public void modificationOnSublistClear()		{ checkModificationOnClear(sublist()); }
	
	private List<String> sublist() { return list.subList(0, 2); }
	
	// --------------------------- Modification via sublist().sublist()-----------------------------

	@Test public void modificationOnSublistSublistClear()
	{
		checkModificationOnClear(sublist().subList(0, 1));
	}
	
	// one is enough to check that recursion
	
	// --------------------------- Modification via iterator() -------------------------------------

	@Test public void modificationViaIterator()
	{
		Iterator<String> it = list.iterator();
		it.next();
		it.remove();
		assertTrue(list.isModified());
	}
	
	@Test public void modificationViaListIteratorRemove()
	{
		ListIterator<String> it = list.listIterator();
		it.next();
		it.remove();
		assertTrue(list.isModified());
	}
	
	@Test public void modificationViaListIteratorSet()
	{
		ListIterator<String> it = list.listIterator();
		it.next();
		it.set("c");
		assertTrue(list.isModified());
	}
	
	@Test public void modificationViaListIteratorAdd()
	{
		ListIterator<String> it = list.listIterator();
		it.next();
		it.add("c");
		assertTrue(list.isModified());
	}
	
	// ---------------------------------------------------------------------------------------------

	
	private void checkModificationOnAdd(List<String> l)
	{
		l.add("c");
		assertTrue(list.isModified());
	}
	
	private void checkModificationOnAddAt(List<String> l)
	{
		l.add(1,"c");
		assertTrue(list.isModified());
	}
	
	private void checkModificationOnAddAll(List<String> l)
	{
		l.addAll(Collections.<String> emptyList());
		assertFalse(list.isModified());
		l.addAll(Arrays.asList("c"));
		assertTrue(list.isModified());
	}
	
	private void checkModificationOnAddAllAt(List<String> l)
	{
		l.addAll(1,Collections.<String> emptyList());
		assertFalse(list.isModified());
		l.addAll(1,Arrays.asList("c"));
		assertTrue(list.isModified());
	}
	
	private void checkModificationOnRemove(List<String> l)
	{
		l.remove(0);
		assertTrue(list.isModified());
	}
	
	private void checkModificationOnRemoveObj(List<String> l)
	{
		l.remove("c");
		assertFalse(list.isModified());
		l.remove("a");
		assertTrue(list.isModified());
	}
	
	private void checkModificationOnRemoveAll(List<String> l)
	{
		l.removeAll(Collections.EMPTY_LIST);
		assertFalse(list.isModified());
		l.removeAll(Arrays.asList("a"));
		assertTrue(list.isModified());
	}
	
	private void checkModificationOnRetainAll(List<String> l)
	{
		l.retainAll(l);
		assertFalse(list.isModified());
		l.retainAll(Collections.EMPTY_LIST);
		assertTrue(list.isModified());
	}
	
	private void checkModificationOnSet(List<String> l)
	{
		l.set(0,"a");
		assertFalse(list.isModified());
		l.set(0,"c");
		assertTrue(list.isModified());
	}
	
	private void checkModificationOnClear(List<String> l)
	{
		l.clear();
		assertTrue(list.isModified());
	}
	
	
	private ModificationAwareList<String> makeList()
	{
		List<String> list = new ArrayList<>();
		list.add("a");
		list.add("b");
		list.add("d");
		return new ModificationAwareList<>(list);
	}
}
