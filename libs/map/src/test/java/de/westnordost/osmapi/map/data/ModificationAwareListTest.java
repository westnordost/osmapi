package de.westnordost.osmapi.map.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import junit.framework.TestCase;

public class ModificationAwareListTest extends TestCase
{
	private ModificationAwareList<String> list;
	
	public void setUp() { list = makeList(); }
	
	public void testInitiallyNoModification()	{ assertFalse(list.isModified()); }

	public void testModificationOnAdd()			{ checkModificationOnAdd(list); }
	public void testModificationOnAddAt()		{ checkModificationOnAddAt(list); }
	public void testModificationOnAddAll()		{ checkModificationOnAddAll(list); }
	public void testModificationOnAddAllAt()	{ checkModificationOnAddAllAt(list); }
	public void testModificationOnRemove()		{ checkModificationOnRemove(list); }
	public void testModificationOnRemoveObj()	{ checkModificationOnRemoveObj(list); }
	public void testModificationOnRemoveAll()	{ checkModificationOnRemoveAll(list); }
	public void testModificationOnRetainAll()	{ checkModificationOnRetainAll(list); }
	public void testModificationOnSet()			{ checkModificationOnSet(list); }
	public void testModificationOnClear()		{ checkModificationOnClear(list); }

	// --------------------------- Modification via sublist() --------------------------------------

	public void testModificationOnSublistAdd()			{ checkModificationOnAdd(sublist()); }
	public void testModificationOnSublistAddAt()		{ checkModificationOnAddAt(sublist()); }
	public void testModificationOnSublistAddAll()		{ checkModificationOnAddAll(sublist()); }
	public void testModificationOnSublistAddAllAt()		{ checkModificationOnAddAllAt(sublist()); }
	public void testModificationOnSublistRemove()		{ checkModificationOnRemove(sublist()); }
	public void testModificationOnSublistRemoveObj()	{ checkModificationOnRemoveObj(sublist()); }
	public void testModificationOnSublistRemoveAll()	{ checkModificationOnRemoveAll(sublist()); }
	public void testModificationOnSublistRetainAll()	{ checkModificationOnRetainAll(sublist()); }
	public void testModificationOnSublistSet()			{ checkModificationOnSet(sublist()); }
	public void testModificationOnSublistClear()		{ checkModificationOnClear(sublist()); }
	
	private List<String> sublist() { return list.subList(0, 2); }
	
	// --------------------------- Modification via sublist().sublist()-----------------------------

	public void testModificationOnSublistSublistClear()
	{
		checkModificationOnClear(sublist().subList(0, 1));
	}
	
	// one is enough to check that recursion
	
	// --------------------------- Modification via iterator() -------------------------------------

	public void testModificationViaIterator()
	{
		Iterator<String> it = list.iterator();
		it.next();
		it.remove();
		assertTrue(list.isModified());
	}
	
	public void testModificationViaListIteratorRemove()
	{
		ListIterator<String> it = list.listIterator();
		it.next();
		it.remove();
		assertTrue(list.isModified());
	}
	
	public void testModificationViaListIteratorSet()
	{
		ListIterator<String> it = list.listIterator();
		it.next();
		it.set("c");
		assertTrue(list.isModified());
	}
	
	public void testModificationViaListIteratorAdd()
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
		l.addAll(Collections.singletonList("c"));
		assertTrue(list.isModified());
	}
	
	private void checkModificationOnAddAllAt(List<String> l)
	{
		l.addAll(1,Collections.<String> emptyList());
		assertFalse(list.isModified());
		l.addAll(1, Collections.singletonList("c"));
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
		l.removeAll(Collections.singletonList("a"));
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
