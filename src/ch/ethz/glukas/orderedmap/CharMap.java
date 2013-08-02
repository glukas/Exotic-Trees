package ch.ethz.glukas.orderedmap;

import java.util.concurrent.ForkJoinPool;

import ch.ethz.glukas.orderedset.BinaryMath;

public class CharMap<T> {


	public CharMap()
	{
		init(1);
		assert checkInvariants();
	}
	
	@SuppressWarnings("unchecked")
	private void init(int capacity)
	{
		//capacity should be a power of 2
		keys = new char[capacity];
		values = (T[]) new Object[capacity];
		sectionSize = sectionSizeForCapacity(capacity);
		depth = BinaryMath.log(numberOfSections());
	}
	
	public int size()
	{
		return count;
	}
	
	//O(log(N)) worst case
	public boolean containsKey(char key)
	{
		return get(key) != null;
	}
	
	//O(log(N) + log^2(N)/B amortized
	public T put(char key, T value)
	{
		if (key == 0) throw new IllegalArgumentException();
		
		int section = sectionForKey(key);
		T oldValue = put(key, value, section);
		
		assert checkInvariants();
		assert get(key) == value;
		
		return oldValue;
	}
	
	//O(log(N)) worst case
	public T get(char key)
	{
		int section = sectionForKey(key);
		T result = get(key, section);
		
		assert checkInvariants();
		return result;
	}
	
	private T get(char key, int section)
	{
		int index = arrayIndexForSection(section);
		while (keys[index] != 0) {
			if (keys[index] == key) return values[index];
			index++;
		}
		return null;
	}
	
	private boolean contains(char key, int section)
	{
		return get(key, section) != null;
	}
	
	
	private T put(char key, T value, int section)
	{
		int arrayIndexOfSection = arrayIndexForSection(section);
		T oldValue = null;
		//there is at least one free slot in the section
		assert keys[arrayIndexOfSection+sectionSize-1] == 0;
		
		//find insertion index
		int index = arrayIndexOfSection;
		while (keys[index] < key && keys[index] != 0) {
			index++;
		}
		//insert if necessary
		if (keys[index] != key) {
			//push the part of the section that is larger than the key to the right
			int rightEndOfSection = sectionSize-index+arrayIndexOfSection-1;
			System.arraycopy(keys, index, keys, index+1, rightEndOfSection);
			System.arraycopy(values, index, values, index+1, rightEndOfSection);
			//insert the key in the now free spot
			keys[index] = key;
			count++;
		} else {
			oldValue = values[index];
		}
		assert keys[index] == key;
		//(over)write the value at that position
		values[index] = value;
		
		assert sectionIsSorted(section);
		
		//ensure there is enough space
		ensureDensity(section);
		
		return oldValue;
	}
	
	
	private int sectionForKey(char key)
	{
		//binary search on the first element of the sections (first element is always non-null, except when capacity == 1)
		int lefthand = 0;
		int righthand = numberOfSections()-1;
		
		while (righthand-lefthand > 1) {
			int middle = lefthand+(righthand-lefthand)/2;
			int sectionIndex = arrayIndexForSection(middle);
			if (key > keys[sectionIndex]) {
				lefthand = middle;
			} else if (key < keys[sectionIndex]) {
				righthand = middle;
			} else {
				return middle;
			}
		}
		
		//we might be off by one, choose between the two remaining sections
		if (key >= keys[arrayIndexForSection(righthand)]) return righthand;
		assert lefthand == 0 || key >= keys[arrayIndexForSection(lefthand)];
		return lefthand;
	}
	
	//this is the meat of the structure
	private void ensureDensity(int section)
	{
		int sectionIndex = arrayIndexForSection(section);
		if (keys[sectionIndex+sectionSize-1] != 0) {//section is all full - a redistribution is required
			
			int level = depth();
			//A) find the first level that is within capacity
			while (level >= 0 && !isWithinUpperbound(arrayIndexForNode(level, section), level)) {
				section = section/2;
				level--;
			}

			//B) redistribute the keys among the sections
			redistribute(arrayIndexForNode(level, section), level);
		}
		assert isWithinCapacity();
	}

	
	private void redistribute(int startIndex, int level)
	{
		assert startIndex >= 0;
		
		int numberOfSections = numberOfSectionsForLevel(level);
		int numberOfKeys = crunch(startIndex, numberOfSections);
		char [] sourceKeys = keys;
		T [] sourceValues = values;
		if (level < 0) {
			assert numberOfKeys == exhaustiveCountNonZeroEntries(keys);
			init(2*capacity());
			numberOfSections = numberOfSections();
		}
		distributeBlock(startIndex, sourceKeys, sourceValues, numberOfSections, numberOfKeys);
		
		
		assert (level >= 0) || count == numberOfKeys;
		assert isWithinCapacity();
	}
	
	
	private void distributeBlock(int fromStartIndex, char[]sourceKeys, T[]sourceValues, int targetNumberOfSections, int numberOfKeys)
	{
		assert numberOfKeys >= targetNumberOfSections; //there must be enough keys
		assert targetNumberOfSections*(sectionSize-1) >= numberOfKeys; //there should not be too many keys
		
		int keysPerSection = numberOfKeys/targetNumberOfSections;
		int leftover = numberOfKeys-(targetNumberOfSections*keysPerSection);//the remaining keys are distributed evenly among the first few sections
		
		int currentBlockIndex = fromStartIndex+numberOfKeys-1;//points to the right end of the crunched block that still need to be distributed
		int currentSectionIndex = fromStartIndex+sectionSize*(targetNumberOfSections-1);//points to the first element of the next section that the keys will move to
		//the distribution proceeds from right to left
		
		for (int i=targetNumberOfSections; i>0; i--) {
			
			if (i == leftover) {//the first few sections might have some more elements
				keysPerSection++;
			}
			
			moveKeyValuePairs(currentBlockIndex, sourceKeys, sourceValues, currentSectionIndex+keysPerSection-1, keys, values, keysPerSection);
			currentBlockIndex -= keysPerSection;
			currentSectionIndex -= sectionSize;
		}
		
		assert countConsistent();
	}
	
	//from index refers to the last element to be moved, toIndex to where the last element should go
	//keys are copied scanning right to left
	//the copied segment of the fromArray is zeroed out
	private void moveKeyValuePairs(int fromIndex, char[] sourceKeys, T[]sourceValues, int toIndex, char[]targetKeys, T[] targetValues, int length)
	{
		if (sourceKeys == targetKeys && fromIndex == toIndex) return;
		assert sourceKeys != targetKeys || fromIndex < toIndex;
		
		for (int i=0; i< length; i++) {
			targetKeys[toIndex-i] = sourceKeys[fromIndex-i];
			sourceKeys[fromIndex-i] = 0;
			targetValues[toIndex-i] = sourceValues[fromIndex-i];
			sourceValues[fromIndex-i] = null;
		}
	}
	
	
	//moves all the elements to the left to form one coherent block
	//returns the number of elements in the block
	private int crunch(int indexOfFirstSection, int numberOfSections)
	{
		assert countConsistent();
		
		int currentDestinationIndex = indexOfFirstSection+numberOfUsedSlotsInSectionAtIndex(indexOfFirstSection);
		int sectionIndex = indexOfFirstSection;
		
		for (int i=1; i<numberOfSections; i++) {
			sectionIndex+=sectionSize;
			currentDestinationIndex = crunchSection(sectionIndex, currentDestinationIndex);
		}
		
		assert countConsistent();
		return currentDestinationIndex-indexOfFirstSection;
	}
	
	//cuts&copies a section to the destination index
	private int crunchSection(int sectionIndex, int destinationIndex)
	{
		if (sectionIndex == destinationIndex) return firstFreeIndexForSectionAtIndex(sectionIndex);//if the section is already at the right place, don't crunch as this would zero out the section
		int i=0;
		for(; i < sectionSize && keys[sectionIndex+i] != 0; i++) {
			keys[destinationIndex+i] = keys[sectionIndex+i];
			values[destinationIndex+i] = values[sectionIndex+i];
			keys[sectionIndex+i] = 0;
			values[sectionIndex+i] = null;
		}
		return destinationIndex+i;
	}
	
	///////
	///////
	
	private boolean isWithinUpperbound(int arrayIndexOfNode, int level)
	{
		double count = numberOfUsedSlotsForNode(arrayIndexOfNode, level);
		if (count > numberOfSectionsForLevel(level)*(sectionSize-1)) return false;//there needs to be at least one empty slot in each section
		
		double capacity = capacityOfNodeAtLevel(level);
		double maxDensity = maxDensityForLevel(level);
		double density = count/capacity;
		return density < maxDensity;
	}
	
	private int numberOfUsedSlotsForNode(int arrayIndex, int level)
	{
		int numberOfSections = numberOfSectionsForLevel(level);

		int count = 0;
		for (int i=0; i<numberOfSections; i++) {
			count += numberOfUsedSlotsInSectionAtIndex(arrayIndex+i*sectionSize);
		}
		return count;

	}
	
	private double maxDensityForLevel(int level)
	{
		return rootDensityUpperbound + (level)*(leafDensityUpperbound-rootDensityUpperbound)/(depth()+1);
	}
	
	private int arrayIndexForNode(int level, int windowIndex)
	{
		if (level < 0) return 0;
		return capacityOfNodeAtLevel(level)*windowIndex;
	}
	
	private int capacityOfNodeAtLevel(int level)
	{
		return capacity()/BinaryMath.powerOfTwo(level);
	}
	
	private int numberOfSectionsForLevel(int level)
	{
		if (level < 0) return numberOfSections();
		return capacityOfNodeAtLevel(level)/sectionSize;
	}
	
	
	private int numberOfUsedSlotsInSectionAtIndex(int index)
	{
		return countNumberOfUsedSlotsInSectionAtIndex(index);
	}
	
	private int countNumberOfUsedSlotsInSectionAtIndex(int index)
	{
		if (keys[index+sectionSize-1] != 0) return sectionSize;
		int count = 0;
		if (keys[index+sectionSize/2] != 0) {//perform one step of binary search
			index+=sectionSize/2;
			count+=sectionSize/2;
		}
		while (keys[index] != 0) {//scan rightwards until finding a null element
			index++;
			count++;
		}
		return count;
	}
	
	private int firstFreeIndexForSectionAtIndex(int arrayIndex)
	{
		return arrayIndex+numberOfUsedSlotsInSectionAtIndex(arrayIndex);
	}
	
	private int arrayIndexForSection(int section)
	{
		return section*sectionSize;
	}
	
	
	private int numberOfSections()
	{
		return capacity()/sectionSize;
	}
	
	private int depth()
	{
		return depth;
	}
	
	private int capacity()
	{
		return keys.length;
	}
	
	//section sizes are always powers of twos
	private int sectionSizeForCapacity(int capacity)
	{
		assert BinaryMath.isPowerOfTwo(capacity);
		
		if (capacity <= 1) return 1;
		if (capacity == 2) return 2;
		return  BinaryMath.nextHighestPowerOfTwo(BinaryMath.log(capacity));
	}
	
	////
	//INSTANCE VARIABLES
	////
	
	private T[] values;
	private char[] keys;
	private int sectionSize;
	private int count = 0;
	private int depth;
	
	////
	//CONSTANTS
	////
	
	private final static double rootDensityUpperbound = 0.8;
	private final static double rootDensityLowerbound = 0.35;
	private final static double leafDensityLowerbound = 0.1;
	private final static double leafDensityUpperbound = 1.0;

	/////
	//INVARIANTS & ASSERTIONS
	////
	
	protected boolean checkInvariants()
	{
		boolean result;
		result = BinaryMath.isPowerOfTwo(numberOfSections());
		assert result;
		result = result && BinaryMath.isPowerOfTwo(keys.length);
		assert result;
		result = result &&  BinaryMath.isPowerOfTwo(sectionSize);
		assert result;
		result = result && (keys.length == sectionSize*numberOfSections());
		assert result;
		result = result && isOrdered();
		assert result;
		result = result && countConsistent();
		assert result;
		result = result && isWithinCapacity();
		assert result;
		result = result && depth == BinaryMath.log(numberOfSections());
		assert result;
		return result;
	}
	
	protected boolean isOrdered()
	{
		boolean result = true;
		for (int i=0; i<numberOfSections(); i++) {
			result = result && sectionIsSorted(i);
			assert result;
		}
		return result;
	}
	

	protected boolean isWithinCapacity()
	{
		//assert usedSlotsConsistent();
		boolean result = true;
		for (int i=0; i<numberOfSections(); i++) {
			result = result && keys[arrayIndexForSection(i)+sectionSize-1] == 0; //last slot of a section should always be empty
			assert result;
			result = result && keys[arrayIndexForSection(i)] != 0 || capacity() == 1; //first slot of a section should never be empty, except if the structure itself is empty
			assert result;
		}
		return result;
	}
	
	protected boolean sectionIsSorted(int section)
	{
		boolean result = true;
		int index = arrayIndexForSection(section);
		int lastKey = keys[index];
		while (index < capacity() && keys[index] != 0) {
			result = result && keys[index] >= lastKey;
			lastKey = keys[index];
			index++;
			assert result;
		}
		return result;
	}
	
	protected boolean countConsistent()
	{
		int exhaustiveCount = exhaustiveCount();
		boolean result = exhaustiveCount == count;
		assert result;
		return result;
	}
	
	protected int exhaustiveCountNonZeroEntries(char[] array)
	{
		int result = 0;
		for (int i=0; i<array.length; i++) {
			if (array[i] != 0) result++;
		}
		return result;
	}
	
	protected int exhaustiveCount()
	{
		return exhaustiveCountNonZeroEntries(keys);
	}
	
	
}
