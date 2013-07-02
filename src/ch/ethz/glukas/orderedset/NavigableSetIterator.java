package ch.ethz.glukas.orderedset;

import java.util.Iterator;
import java.util.NavigableSet;

class NavigableSetIterator<T> implements Iterator<T> {

	
	//the empty iterator
	public NavigableSetIterator()
	{
	}
	
	//both first and last are inclusive bounds. 'first' is the first, 'last' the last element returned by the iterator
	//precondition: set.contains(first), set.contains(last);
	public NavigableSetIterator(NavigableSet<T> set, T first, T last)
	{
		if (first == null) throw new NullPointerException();
		if (last == null) throw new NullPointerException();
		assert set.contains(first);
		assert set.contains(last);
		internalSet = set;
		lowerbound = first;
		upperbound = last;
		current = first;
	}
	

	private void advance()
	{
		current = internalSet.higher(current);
	}

	///
	//LIST INTERFACE
	///
	
	@Override
	public boolean hasNext() {
		if (internalSet == null) return false;
		if (current == null) return false;
		return internalSet.comparator().compare(current, upperbound) <= 0;
	}
	
	@Override
	public T next() {
		T result = current;
		advance();
		return result;
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	////
	//INSTANCE VARIABLES
	////
	
	private T current;
	private NavigableSet<T> internalSet;
	private T upperbound;
	private T lowerbound;
}
