package ch.ethz.glukas.orderedset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public class PackedMemoryStructure {
	//The packed memory structure maintains an ordered set of keys
	//the keys are arranged to allow fast O(K/B) traversal, where K is the number of keys to scan and B is the cache line size
	//searches are O(log(N)) worst case time
	//insertions are the cost of searching plus O(log^2(N)/B) amortized time, where N is the number of keys in the structure
	
	
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
		//auxiliary = new int[capacity];
		sectionSize = sectionSizeForCapacity(capacity);
		firstKeyOfSection = new int[capacity/sectionSize];
		depth = BinaryMath.log(numberOfSections());
		//usedSlotsPerSection = new int[numberOfSectionsForLevel(0)];
	}
	
	public int size()
	{
		return count;
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
		if (contains(key, section)) return;
		count++;
		insert(key, section);
		assert checkInvariants();
		assert contains(key);
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
		int arrayIndexOfSection = arrayIndexForSection(section);
		//there is at least one free slot in the section
		assert keys[arrayIndexOfSection+sectionSize-1] == 0;
		
		//find insertion index
		int index = arrayIndexOfSection;
		while (keys[index] < key && keys[index] != 0) {
			index++;
		}
		//push the part of the section that is larger than the key to the right
		System.arraycopy(keys, index, keys, index+1, sectionSize-index+arrayIndexOfSection-1);
		
		//insert the key in the now free spot
		keys[index] = key;
		firstKeyOfSection[section] = keys[arrayIndexOfSection];
		
		//usedSlotsPerSection[arrayIndexOfSection/sectionSize]++;
		assert sectionIsSorted(section);
		
		//ensure there is enough space
		ensureDensity(section);
	}
	
	
	private int sectionForKey(int key)
	{
		if (key <= firstKeyOfSection[0]) return 0;
		int index = Arrays.binarySearch(firstKeyOfSection, 0, firstKeyOfSection.length, key);
		if (index < 0) {
			index = -(index+1)-1;
		}
		assert (index == directSectionForKey(key));
		return index;
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
	
	
	//////
	//Sequential restructuring algorithms
	/////
	
	private void redistribute(int startIndex, int level)
	{
		//assert usedSlotsConsistent();
		assert startIndex >= 0;
		
		int numberOfSections = numberOfSectionsForLevel(level);
		int numberOfKeys = crunch(startIndex, numberOfSections);
		int [] sourceArray = keys;
		if (level < 0) {
			assert numberOfKeys == exhaustiveCountNonZeroEntries(keys);
			init(2*capacity());
			numberOfSections = numberOfSections();
		}
		distributeBlock(startIndex, sourceArray, numberOfSections, numberOfKeys);
		
		assert (level >= 0) || count == numberOfKeys;
		assert isWithinCapacity();
		//assert usedSlotsConsistent();
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
		int currentSection = currentSectionIndex/sectionSize;
		
		for (int i=targetNumberOfSections; i>0; i--) {
			
			if (i == leftover) {//the first few sections might have some more elements
				keysPerSection++;
			}
			
			moveKeys(currentBlockIndex, fromArray, currentSectionIndex+keysPerSection-1, keys, keysPerSection);
			
			//usedSlotsPerSection[currentSectionIndex/sectionSize] = keysPerSection;
			firstKeyOfSection[currentSection] = keys[currentSectionIndex];
			currentSection--;
			currentBlockIndex -= keysPerSection;
			currentSectionIndex -= sectionSize;
			
		}
		
		assert countConsistent();
		assert firstKeyOfSectionsConsistent();
	}
	
	//from index refers to the last element to be moved, toIndex to where the last element should go
	//keys are copied scanning right to left
	//the copied segment of the fromArray is zeroed out
	private void moveKeys(int fromIndex, int[] fromArray, int toIndex, int[]toArray, int length)
	{
		//TODO: consider using system.arraycopy and arrays.fill
		if (fromArray == toArray && fromIndex == toIndex) return;
		assert fromArray != toArray || fromIndex < toIndex;
		;
		for (int minToIndex = toIndex-length; toIndex>minToIndex; toIndex--) {
			toArray[toIndex] = fromArray[fromIndex];
			fromArray[fromIndex] = 0;
			fromIndex--;
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
			keys[sectionIndex+i] = 0;
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
		//assert usedSlotsConsistent();
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
	
	private void swapKeys(int index1, int index2) {
		int key1 = keys[index1];
		keys[index1] = keys[index2];
		keys[index2] = key1;
	}
	
	
	private int numberOfUsedSlotsInSectionAtIndex(int index)
	{
		return countNumberOfUsedSlotsInSectionAtIndex(index);
		//return usedSlotsPerSection[index/sectionSize];
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
	
	//private int[] usedSlotsPerSection;
	private int[] keys;
	private int[] firstKeyOfSection;
	//private int[] auxiliary;
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

	//private final static int dummy = Integer.MIN_VALUE;
	
	//private static final int numberOfSectionsPerChunk = BinaryMath.powerOfTwo(11);//used as a base case for the parallelized algorithms
	//private static final int minimumParallelism = 4;
	
	//private static final ForkJoinPool threadPool = new ForkJoinPool();
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
		result = result && firstKeyOfSectionsConsistent();
		assert result;
		//result = result && usedSlotsConsistent();
		//assert result;
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
	
	protected int exhaustiveCountNonZeroEntries(int[] array)
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
	
	protected boolean firstKeyOfSectionsConsistent()
	{
		boolean result = true;
		for (int i=0; i<numberOfSections(); i++) {
			result = result && keys[arrayIndexForSection(i)] == firstKeyOfSection[i];
		}
		return result;
	}
	
	//calculates the section for a given key based soley on the 'keys' array
	protected int directSectionForKey(int key)
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
	
	/*
	protected boolean usedSlotsConsistent()
	{
		int numberOfSections = numberOfSectionsForLevel(0);
		boolean result = true;
		for (int i=0; i<numberOfSections; i++) {
			result = result && usedSlotsPerSection[i] == countNumberOfUsedSlotsInSectionAtIndex(i*sectionSize);
			assert result;
		}
		return result;
	}*/
	
	

	/////
	//Parallel restructuring algorithms
	////
	
	
	/*
	private void parallelRedistribute(int startIndex, int level)
	{
		int numberOfSections = numberOfSectionsForLevel(level);
		int numberOfKeys = parallelCrunch(startIndex, numberOfSections);
		int [] sourceArray = auxiliary;
		if (level < 0) {
			init(2*capacity());
			numberOfSections = numberOfSections();
			assert startIndex == 0;
		}
		int [] targetArray = keys;
		parallelDistributeBlock(sourceArray, numberOfKeys, targetArray, startIndex, numberOfSections);
		
	}
	
	private int parallelCrunch(int indexOfFirstSection, int numberOfSections)
	{
		assert countConsistent();
		
		
		int chunkSize = numberOfSections*sectionSize/numberOfTasks;
		int numberOfSectionsPerTask = numberOfSections/numberOfTasks;
		assert chunkSize*numberOfTasks == numberOfSections*sectionSize;
		
		int currentSourceSectionIndex = indexOfFirstSection;
		
		//partially crunch each chunk in parallel
		
		for (int i=0; i<numberOfTasks; i++) {
			ParallelPartialCrunch task = new ParallelPartialCrunch(keys, currentSourceSectionIndex, sectionSize, numberOfSectionsPerTask);
			threadPool.execute(task);
			ppcTasks[i] = task;
			currentSourceSectionIndex += chunkSize;
		}
		
		
		
		//copy the keys from the chunks to the destination array (in parallel)
		int currentTargetIndex = 0;
		currentSourceSectionIndex = indexOfFirstSection;
		for (int i=0; i<numberOfTasks; i++) {
			int nonzeroEntries = ppcTasks[i].join();
			ParallelArrayCopy task = new ParallelArrayCopy(auxiliary, currentTargetIndex, keys, currentSourceSectionIndex, nonzeroEntries);
			threadPool.execute(task);
			kmtasks[i] = task;
			currentTargetIndex += nonzeroEntries;
			currentSourceSectionIndex += chunkSize;
		}
		
		//wait for completion
		for (ForkJoinTask<Void> task : kmtasks) {
			task.join();
		}
		return currentTargetIndex;
	}
	
	private static final int numberOfTasks = 256;
	private ParallelPartialCrunch[] ppcTasks = new ParallelPartialCrunch[numberOfTasks];
	private ParallelArrayCopy[] kmtasks = new ParallelArrayCopy[numberOfTasks];
	
	
	private void parallelDistributeBlock(int[]fromArray, int numberOfKeys, int[]toArray, int toStartIndex, int targetNumberOfSections)
	{
		//TODO: parallelize
		int keysPerSection = numberOfKeys/targetNumberOfSections;
		int leftover = numberOfKeys-(targetNumberOfSections*keysPerSection);//the remaining keys are distributed evenly among the first few sections
		
		int currentBlockIndex = 0;
		int currentSectionIndex = toStartIndex;
		
		for (int i=targetNumberOfSections; i>0; i--) {
			
			if (i == leftover) {//the last few sections might have some more elements
				keysPerSection++;
			}
			assert currentBlockIndex <= numberOfKeys;
			
			System.arraycopy(fromArray, currentBlockIndex, toArray, currentSectionIndex, keysPerSection);
			Arrays.fill(toArray, currentSectionIndex+keysPerSection, currentSectionIndex+sectionSize, 0);
			
			currentBlockIndex += keysPerSection;
			currentSectionIndex += sectionSize;
			
		}
	}*/

}
