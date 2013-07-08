package ch.ethz.glukas.orderedset;

import java.util.Arrays;

public class PackedMemoryStructure {
	//The packed memory structure maintains an ordered set of keys
	//the keys are arranged to allow fast O(K/B) traversal, where K is the number of keys to scan and B is the cache line size
	//searches are O(log(N)) worst case time
	//insertions are the cost of searching plus O(1 + log^2(N)/B) amortized time, where N is the number of keys in the structure
	
	
	//Implementation details:
	//the size of the array ("capacity") is a power of two.
	//the section size is the next highest power of two of log2(capacity). therefore also the number of sections is a number of two.
	//in each section, the keys are stored left-aligned.
	//there is always at least one free slot in each section
	
	
	
	public PackedMemoryStructure()
	{
		init(1);
		assert checkInvariants();
	}
	
	private void init(int capacity)
	{
		//capacity should be a power of 2
		keys = new int[capacity];
		sectionSize = sectionSizeForCapacity(capacity);

	}
	
	
	public boolean contains(int key)
	{
		int section = sectionForKey(key);
		boolean result = contains(key, section);
		
		assert checkInvariants();
		return result;
	}
	
	public void insert(int key)
	{
		int section = sectionForKey(key);
		insert(key, section);
		assert checkInvariants();
	}
	
	private boolean contains(int key, int section)
	{
		//linear scan through the section
		int index = arrayIndexForSection(section);
		while (keys[index] != 0) {
			if (keys[index] == key) return true;
			index++;
		}
		return false;
	}
	
	
	private void insert(int key, int section)
	{
		int sectionIndex = arrayIndexForSection(section);
		insertIntoSectionAtIndex(key, sectionIndex);
		assert sectionIsSorted(section);
		
		//ensure there is enough space
		ensureDensity(section);
	}
	
	
	private int sectionForKey(int key)
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
		
		if (key > keys[righthand]) return righthand;
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
		int [] sourceArray = keys;
		if (level < 0) {
			init(2*capacity());
			numberOfSections = numberOfSections();
		}
		distributeBlock(startIndex, sourceArray, numberOfSections, numberOfKeys);
		
		assert isWithinCapacity();
	}
	
	
	private void distributeBlock(int fromStartIndex, int[]fromArray, int targetNumberOfSections, int numberOfKeys)
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
			
			moveKeys(currentBlockIndex, fromArray, currentSectionIndex+keysPerSection-1, keys, keysPerSection);
			
			currentBlockIndex -= keysPerSection;
			currentSectionIndex -= sectionSize;
			
		}
	}
	
	//from index refers to the last element to be moved, toIndex to where the last element should go
	//keys are copied scanning right to left
	//the copied segment of the fromArray is zeroed out
	private void moveKeys(int fromIndex, int[] fromArray, int toIndex, int[]toArray, int length)
	{
		if (fromArray == toArray && fromIndex == toIndex) return;
		assert fromArray != toArray || fromIndex < toIndex;
		
		for (int i=0; i< length; i++) {
			toArray[toIndex-i] = fromArray[fromIndex-i];
			fromArray[fromIndex-i] = 0;
		}
	}
	
	
	//moves all the elements to the left to form one coherent block
	//returns the number of elements in the block
	private int crunch(int indexOfFirstSection, int numberOfSections)
	{
		int currentDestinationIndex = indexOfFirstSection+numberOfUsedSlotsInSectionAtIndex(indexOfFirstSection);
		int sectionIndex = indexOfFirstSection;
		
		for (int i=1; i<numberOfSections; i++) {
			sectionIndex+=sectionSize;
			currentDestinationIndex = crunchSection(sectionIndex, currentDestinationIndex);
		}
		
		return currentDestinationIndex-indexOfFirstSection;
	}
	
	//cuts&copies a section to the destination index
	private int crunchSection(int sectionIndex, int destinationIndex)
	{
		int i=0;
		for(; i < sectionSize && keys[sectionIndex+i] != 0; i++) {
			keys[destinationIndex+i] = keys[sectionIndex+i];
			keys[sectionIndex+i] = 0;
		}
		return destinationIndex+i;
	}
	
	private void insertIntoSectionAtIndex(int key, int arrayIndexOfSection)
	{
		int index = firstFreeIndexForSectionAtIndex(arrayIndexOfSection);
		assert keys[index] == 0;
		
		keys[index] = key;
		//insertion sort step
		while (index > arrayIndexOfSection && keys[index-1] > keys[index]) {
			swapKeys(index, index-1);
			index--;
		}
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
		int count = 0;
		int numberOfSections = numberOfSectionsForLevel(level);
		for (int i=0; i<numberOfSections; i++) {
			count += numberOfUsedSlotsInSectionAtIndex(arrayIndex+i*sectionSize);
		}
		return count;
	}
	
	private double maxDensityForLevel(int level)
	{
		return rootDensityUpperbound + (level)*(leafDensityUpperbound-rootDensityUpperbound)/(depth()+1);
	}
	
	private int arrayIndexForNode(int level, int index)
	{
		if (level < 0) return 0;
		return capacityOfNodeAtLevel(level)*index;
	}
	
	private int capacityOfNodeAtLevel(int level)
	{
		return capacity()/(1 << level);
	}
	
	private int numberOfSectionsForLevel(int level)
	{
		if (level < 0) return numberOfSections();
		return capacityOfNodeAtLevel(level)/sectionSize;
	}
	
	private void swapKeys(int index1, int index2) {
		int key1 = keys[index1];
		keys[index1] = keys[index2];
		keys[index2] = key1;
	}
	
	
	private int numberOfUsedSlotsInSectionAtIndex(int index)
	{
		if (keys[index+sectionSize-1] != 0) return sectionSize;
		int count = 0;
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
		return binlog(numberOfSections());
	}
	
	private int capacity()
	{
		return keys.length;
	}
	
	//section sizes are always powers of twos
	private int sectionSizeForCapacity(int capacity)
	{
		assert isPowerOfTwo(capacity);
		
		if (capacity <= 1) return 1;
		if (capacity == 2) return 2;
		return  nextHighestPowerOfTwo(binlog(capacity));
	}
	
	////
	//MATH HELPERS
	////
	
	//fast binary logarithm by x4u at http://stackoverflow.com/questions/3305059/how-do-you-calculate-log-base-2-in-java-for-integers
	private static int binlog( int bits ) // returns 0 for bits=0
	{
	    int log = 0;
	    if( ( bits & 0xffff0000 ) != 0 ) { bits >>>= 16; log = 16; }
	    if( bits >= 256 ) { bits >>>= 8; log += 8; }
	    if( bits >= 16  ) { bits >>>= 4; log += 4; }
	    if( bits >= 4   ) { bits >>>= 2; log += 2; }
	    return log + ( bits >>> 1 );
	}
	
	private static int nextHighestPowerOfTwo(int num)
	{
		assert num >= 0;
		if (num <= 1) return 1;
		
		int result = 2;
		while (num > result) {
			result = result*2;
		}
		return result;
	}

	
	/////
	//INVARIANTS & ASSERTIONS
	////
	
	protected boolean checkInvariants()
	{
		boolean result;
		result = isPowerOfTwo(numberOfSections());
		assert result;
		result = result && isPowerOfTwo(keys.length);
		assert result;
		result = result &&  isPowerOfTwo(sectionSize);
		assert result;
		result = result && (keys.length == sectionSize*numberOfSections());
		assert result;
		result = result && isOrdered();
		assert result;
		result = result && isWithinCapacity();
		assert result;
		return result;
	}
	
	protected boolean isPowerOfTwo(int num)
	{
		return nextHighestPowerOfTwo(num) == num;
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
	
	
	////
	//INSTANCE VARIABLES
	////
	
	private int[] keys;
	private int sectionSize;
	
	////
	//CONSTANTS
	////
	
	private final static double rootDensityUpperbound = 0.8;
	private final static double rootDensityLowerbound = 0.35;
	private final static double leafDensityLowerbound = 0.1;
	private final static double leafDensityUpperbound = 1.0;
}
