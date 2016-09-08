package de.westnordost.osmapi.map.data;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** A Map that registers if it has been changed from its original state. */
public class ModificationAwareMap<K,V> implements Map<K,V>, Serializable
{
	private static final long serialVersionUID = 1L;

	private Map<K,V> map;
	private boolean modified;

	public ModificationAwareMap(Map<K, V> map)
	{
		this.map = map;
	}

	/** @return Whether the map has been modified. Note that this will always return true once the
	 *          map has once been modified, regardless of whether it has been restored to its
	 *          original state later on. */
	public boolean isModified()
	{
		return modified;
	}

	private void onModification()
	{
		modified = true;
	}

	/* Everything below this comment: implementation of the map interface */

	@Override
	public void clear()
	{
		map.clear();
		onModification();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return map.containsValue(value);
	}

	@Override
	public Set<Entry<K, V>> entrySet()
	{
		return new SetWrapper<>(map.entrySet());
	}

	@Override
	public V get(Object key)
	{
		return map.get(key);
	}

	@Override
	public boolean isEmpty()
	{
		return map.isEmpty();
	}

	@Override
	public Set<K> keySet()
	{
		return new SetWrapper<>(map.keySet());
	}

	@Override
	public V put(K key, V value)
	{
		V result =  map.put(key, value);
		if(result != value) onModification();
		return result;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map)
	{
		this.map.putAll(map);
		if(!map.isEmpty()) onModification();
	}

	@Override
	public V remove(Object key)
	{
		V result = map.remove(key);
		if(result != null) onModification();
		return result;
	}

	@Override
	public int size()
	{
		return map.size();
	}

	@Override
	public boolean equals(Object other)
	{
		return map.equals(other);
	}

	@Override
	public Collection<V> values()
	{
		return new ValuesWrapper(map.values());
	}

	@Override
	public int hashCode()
	{
		return map.hashCode();
	}
	
	private class SetWrapper<E> extends AbstractSet<E>
	{
		private Set<E> set;
		public SetWrapper(Set<E> set) { this.set = set; }

		public Iterator<E> iterator() { return new IteratorWrapper<>(set.iterator()); }
		public boolean contains(Object o) { return set.contains(o); }
		public int size() {	return ModificationAwareMap.this.size(); }
		public void clear() { ModificationAwareMap.this.clear(); }
		public boolean remove(Object o)
		{
			boolean result = set.remove(o);
			if(result) onModification();
			return result;
		}
	}

	private class ValuesWrapper extends AbstractCollection<V>
	{
		private Collection<V> values;
		public ValuesWrapper(Collection<V> values) { this.values = values; }

		public Iterator<V> iterator() { return new IteratorWrapper<>(values.iterator()); }
		public int size() {	return ModificationAwareMap.this.size(); }
		public boolean contains(Object o) {	return containsValue(o); }
		public void clear() { ModificationAwareMap.this.clear();	}
	}

	private class IteratorWrapper<E> implements Iterator<E>
	{
		private Iterator<E> it;
		public IteratorWrapper(Iterator<E> it) { this.it = it; }

		public boolean hasNext() { return it.hasNext(); }
		public E next() { return it.next(); }
		public void remove()
		{
			it.remove();
			onModification();
		}
	}
}
