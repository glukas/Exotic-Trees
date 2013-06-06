package ch.ethz.glukas.orderedset;

import java.util.ListIterator;
import java.util.NavigableSet;

class RangeSetIterator<T> implements ListIterator<T> {
	//Algorithm: repeated search by rank
	
	
	//the empty iterator
	public RangeSetIterator()
	{
	}
	
	
	//precondition: set contains first and last, first smaller than or equal to last
	public RangeSetIterator(RangeSet<T> set, T first, T last)
	{
		assert set.contains(first);
		assert set.contains(last);
		internalSet = set;
		lowerbound = first;
		upperbound = last;
		setIndexBounds();
		currentIndex = lowestIndex;
		
	}
	
	private void setIndexBounds()
	{
		lowestIndex = internalSet.indexOf(lowerbound);
		highestIndex = internalSet.indexOf(upperbound);
		assert lowestIndex <= highestIndex;
	}

	private void advance()
	{
		currentIndex++;
	}
	
	private void retreat()
	{
		currentIndex--;
	}
	
	
	///
	//LIST INTERFACE
	///
	
	@Override
	public boolean hasNext() {
		if (internalSet == null) return false;
		return currentIndex <= highestIndex;
	}

	@Override
	public boolean hasPrevious() {
		if (internalSet == null) return false;
		return currentIndex > lowestIndex;
	}

	@Override
	public T next() {
		T result = internalSet.get(currentIndex);
		advance();
		return result;
	}
	
	@Override
	public int nextIndex() {
		int result = currentIndex;
		advance();
		return result;
	}

	@Override
	public T previous() {
		retreat();
		return internalSet.get(currentIndex);
	}

	@Override
	public int previousIndex() {
		retreat();
		return currentIndex;
	}

	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(T arg0) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void add(T arg0) {
		throw new UnsupportedOperationException();
	}
	
	////
	//INSTANCE VARIABLES
	////
	
	private int currentIndex;
	private int lowestIndex;
	private int highestIndex;
	private RangeSet<T> internalSet;
	private T upperbound;
	private T lowerbound;
}
