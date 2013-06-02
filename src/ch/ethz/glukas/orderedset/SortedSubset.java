package ch.ethz.glukas.orderedset;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

class SortedSubset<T> extends AbstractCollection<T> implements SortedSet<T> {

	
	public SortedSubset(RangeSet<T> constitutingSuperset, T lowerbound, T upperbound)
	{
		this.superset = constitutingSuperset;
		this.lower = lowerbound;
		this.upper = upperbound;
	}

	@Override
	/**
	 * The set will throw an IllegalArgumentException on an attempt to insert an element outside its range.
	 */
	public boolean add(T e) {
		if (comparator().compare(e, lower) < 0) throw new IllegalArgumentException();
		if (comparator().compare(e, upper) >= 0) throw new IllegalArgumentException();
		
		return superset.add(e);
	}


	@Override
	public void clear() {
		superset.removeRange(lower, upper);
	}

	
	
	@Override
	public boolean contains(Object o) {
		//TODO
		
		return false;
	}
	

	@Override
	public Iterator<T> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}
	

	@Override
	public int size() {
		return superset.sizeOfRange(lower, upper);
	}

	@Override
	public Comparator<? super T> comparator() {
		return superset.comparator();
	}

	@Override
	public T first() {
		T found = superset.ceiling(lower);
		if (comparator().compare(found, lower) < 0) {//check if in range
			return found;
		}
		return null;
	}

	@Override
	public SortedSet<T> headSet(T arg0) {
		return superset.subSet(lower, arg0);
	}

	@Override
	public T last() {
		T found = superset.floor(upper);
		if (comparator().compare(found, lower) >= 0) {//check if in range
			return found;
		}
		return null;
	}

	@Override
	/**
	 * Throws an IllegalArgumentException if the lowerbound is smaller or the upperbound is larger than that of this subset
	 */
	public SortedSet<T> subSet(T arg0, T arg1) {
		if (comparator().compare(arg0, lower) < 0) throw new IllegalArgumentException();
		if (comparator().compare(arg1, upper) > 0) throw new IllegalArgumentException();
		return superset.subSet(arg0, arg1);
	}

	@Override
	public SortedSet<T> tailSet(T arg0) {
		return superset.subSet(arg0, lower);
	}
	
	
	private RangeSet<T> superset;
	private T lower;
	private T upper;
	
	
}
