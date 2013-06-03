package ch.ethz.glukas.orderedset;

import java.util.NavigableSet;

interface RangeSet<T> extends NavigableSet<T> {

	
	public int sizeOfRange(T lowerbound, T upperbound, boolean fromInclusive, boolean toInclusive);
	
	
	public void removeRange(T lowerbound, T upperbound, boolean fromInclusive, boolean toInclusive);
	
	
}