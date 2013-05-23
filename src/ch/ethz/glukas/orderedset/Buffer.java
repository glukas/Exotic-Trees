package ch.ethz.glukas.orderedset;

import java.lang.reflect.Array;

public class Buffer<T> {

	
	public Buffer(int capacity)
	{
		numberOfSlotsUsed = 0;
		nextInsertionIndex = 0;
		bufferSize = capacity;
		buffer = new Object[capacity];
	}
	
	public void add(T value) {
		buffer[nextInsertionIndex] = value;
		nextInsertionIndex = (nextInsertionIndex+1)%bufferSize;
		if (size() == bufferSize) {
			currentZero = (currentZero+1);
		} else {
			numberOfSlotsUsed = numberOfSlotsUsed+1;
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public T get(int index)
	{
		return (T)buffer[internalIndexForIndex(index)];
	}
	
	public int internalIndexForIndex(int index)
	{
		return (currentZero+index)%bufferSize;
	}
	
	public void swap(int index1, int index2)
	{
		T temp = get(index1);
		set(index1, get(index2));
		set(index2, temp);
	}
	
	public void set(int index, T value)
	{
		buffer[internalIndexForIndex(index)] = value;
	}
	
	public int size()
	{
		return numberOfSlotsUsed;
	}
	
	public int numberOfUsedSlots()
	{
		return numberOfSlotsUsed;
	}
	
	
	private int currentZero = 0;
	private int bufferSize;
	private int nextInsertionIndex;
	private Object[] buffer;
	private int numberOfSlotsUsed;
}
