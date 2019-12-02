package de.westnordost.osmapi.map.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/** A List that registers if it has been changed from its original state. */
public class ModificationAwareList<T> implements List<T>, Serializable
{
	private static final long serialVersionUID = 1L;

	private List<T> list;
	private boolean modified;

	public ModificationAwareList(List<T> list)
	{
		this.list = list;
	}

	/** @return Whether the list has been modified. Note that this will always return true once the
	 *          list has once been modified, regardless of whether it has been restored to its
	 *          original state later on. */
	public boolean isModified()
	{
		return modified;
	}

	protected void onModification()
	{
		modified = true;
	}

	/* Everything below this comment: implementation of the list interface */

	@Override
	public void add(int location, T object)
	{
		list.add(location, object);
		onModification();
	}

	@Override
	public boolean add(T object)
	{
		boolean result = list.add(object);
		if(result) onModification();
		return result;
	}

	@Override
	public boolean addAll(int location, Collection<? extends T> collection)
	{
		boolean result = list.addAll(location, collection);
		if(result) onModification();
		return result;
	}

	@Override
	public boolean addAll(Collection<? extends T> collection)
	{
		boolean result = list.addAll(collection);
		if(result) onModification();
		return result;
	}

	@Override
	public void clear()
	{
		list.clear();
		onModification();
	}

	@Override
	public boolean contains(Object object)
	{
		return list.contains(object);
	}

	@Override
	public boolean containsAll(Collection<?> collection)
	{
		return list.containsAll(collection);
	}

	@Override
	public T get(int location)
	{
		return list.get(location);
	}

	@Override
	public int indexOf(Object object)
	{
		return list.indexOf(object);
	}

	@Override
	public boolean isEmpty()
	{
		return list.isEmpty();
	}

	@Override
	public Iterator<T> iterator()
	{
		return new IteratorWrapper(list.iterator());
	}

	@Override
	public int lastIndexOf(Object object)
	{
		return list.lastIndexOf(object);
	}

	@Override
	public ListIterator<T> listIterator()
	{
		return new ListIteratorWrapper(list.listIterator());
	}

	@Override
	public ListIterator<T> listIterator(int location)
	{
		return new ListIteratorWrapper(list.listIterator(location));
	}

	@Override
	public T remove(int location)
	{
		T result = list.remove(location);
		onModification();
		return result;
	}

	@Override
	public boolean remove(Object object)
	{
		boolean result = list.remove(object);
		if(result) onModification();
		return result;
	}

	@Override
	public boolean removeAll(Collection<?> collection)
	{
		boolean result = list.removeAll(collection);
		if(result) onModification();
		return result;
	}

	@Override
	public boolean retainAll(Collection<?> collection)
	{
		boolean result = list.retainAll(collection);
		if(result) onModification();
		return result;
	}

	@Override
	public T set(int location, T object)
	{
		T result = list.set(location, object);
		if(result != object) onModification();
		return result;
	}

	@Override
	public int size()
	{
		return list.size();
	}

	@Override
	public List<T> subList(int start, int end)
	{
		return new ModificationAwareSubList(list.subList(start, end), this);
	}

	@Override
	public Object[] toArray()
	{
		return list.toArray();
	}

	@Override
	public <U> U[] toArray(U[] array)
	{
		return list.toArray(array);
	}

	@Override
	public boolean equals(Object other)
	{
		return list.equals(other);
	}

	@Override
	public int hashCode()
	{
		return list.hashCode();
	}
	
	private class ModificationAwareSubList extends ModificationAwareList<T>
	{
		private static final long serialVersionUID = 1L;
		
		ModificationAwareList<T> master;
		
		public ModificationAwareSubList(List<T> list, ModificationAwareList<T> master)
		{
			super(list);
			this.master = master;
		}
		
		@Override
		protected void onModification()
		{
			master.onModification();
		}
		
		@Override
		public List<T> subList(int start, int end)
		{
			return new ModificationAwareSubList(list.subList(start, end), master);
		}
	}
	
	private class IteratorWrapper implements Iterator<T>
	{
		private Iterator<T> it;
		public IteratorWrapper(Iterator<T> it) { this.it = it; }

		public boolean hasNext() { return it.hasNext(); }
		public T next() { return it.next(); }
		public void remove()
		{
			it.remove();
			onModification();
		}
	}

	private class ListIteratorWrapper implements ListIterator<T>
	{
		private ListIterator<T> it;
		public ListIteratorWrapper(ListIterator<T> it) { this.it = it; }

		public boolean hasNext() { return it.hasNext(); }
		public boolean hasPrevious() { return it.hasPrevious(); }

		public T next() { return it.next(); }
		public T previous() { return it.previous(); }

		public int nextIndex() { return it.nextIndex(); }
		public int previousIndex() { return it.previousIndex(); }

		public void add(T object)
		{
			it.add(object);
			onModification();
		}
		public void remove()
		{
			it.remove();
			onModification();
		}
		public void set(T object)
		{
			it.set(object);
			onModification();
		}
	}
}
