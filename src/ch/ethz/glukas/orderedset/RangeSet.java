package ch.ethz.glukas.orderedset;

import java.util.NavigableSet;

public interface RangeSet<T> extends NavigableSet<T> {

	
	public int sizeOfRange(T lowerbound, T upperbound, boolean fromInclusive, boolean toInclusive);
	
	
	public void removeRange(T lowerbound, T upperbound, boolean fromInclusive, boolean toInclusive);
	
	public T get(int index);
	
	public T poll(int index);
	
	public void remove(int index);
	
	public int indexOf(T value);
}
