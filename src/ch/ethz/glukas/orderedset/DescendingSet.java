package ch.ethz.glukas.orderedset;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;

//simple wrapper that provides a reverse order view to a navigable set
//assumes that the comparator of the undelying set in non-null
public class DescendingSet<T> extends AbstractCollection<T> implements NavigableSet<T>{

	public DescendingSet(NavigableSet<T> set)
	{
		if (set == null) throw new NullPointerException();
		implementingSet = set;
	}
	

	@Override
	public Comparator<? super T> comparator() {
		if (internalComparator == null && implementingSet.comparator() != null) {
			internalComparator = new Comparator<T>() {
				@Override
				public int compare(T arg0, T arg1) {
					return implementingSet.comparator().compare(arg1, arg0);
				}
			};
		}
		return internalComparator;
	}


	@Override
	public T first() {
		return implementingSet.last();
	}


	@Override
	public T last() {
		return implementingSet.first();
	}


	@Override
	public boolean add(T e) {
		return implementingSet.add(e);
	}

	
	@Override
	public void clear() {
		implementingSet.clear();
	}

	
	@Override
	public boolean contains(Object o) {
		return implementingSet.contains(o);
	}


	@Override
	public boolean remove(Object o) {
		return implementingSet.remove(o);
	}
	

	@Override
	public int size() {
		return implementingSet.size();
	}

	
	@Override
	public T ceiling(T arg0) {
		return implementingSet.floor(arg0);
	}


	@Override
	public Iterator<T> descendingIterator() {
		return implementingSet.iterator();
	}


	@Override
	public NavigableSet<T> descendingSet() {
		return implementingSet;
	}


	@Override
	public T floor(T arg0) {
		return implementingSet.ceiling(arg0);
	}

	
	@Override
	public SortedSet<T> headSet(T to) {
		return headSet(to, false);
	}


	@Override
	public NavigableSet<T> headSet(T to, boolean inclusive) {
		return implementingSet.tailSet(to, inclusive).descendingSet();
	}


	@Override
	public T higher(T arg0) {
		return implementingSet.lower(arg0);
	}


	@Override
	public Iterator<T> iterator() {
		return new NavigableSetIterator<T>(this, first(), last());
	}


	@Override
	public T lower(T arg0) {
		return implementingSet.higher(arg0);
	}


	@Override
	public T pollFirst() {
		return implementingSet.pollLast();
	}


	@Override
	public T pollLast() {
		return implementingSet.pollFirst();
	}


	@Override
	public SortedSet<T> subSet(T from, T to) {
		return subSet(from, true, to, false);
	}


	@Override
	public NavigableSet<T> subSet(T from, boolean fromInclusive, T to, boolean toInclusive) {
		return implementingSet.subSet(to, toInclusive, from, fromInclusive).descendingSet();
	}


	@Override
	public SortedSet<T> tailSet(T from) {
		return tailSet(from, true);
	}


	@Override
	public NavigableSet<T> tailSet(T from, boolean inclusive) {
		return implementingSet.headSet(from, inclusive).descendingSet();
	}
	
	
	/////
	//instance variables
	////
	
	private NavigableSet<T> implementingSet;
	private Comparator<? super T> internalComparator;
	
}
