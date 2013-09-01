package priorityQueue;

import java.util.ArrayList;
import java.util.Arrays;

import ch.ethz.glukas.orderedset.BinaryMath;

public class COPriorityQueue {
	//this structure uses ideas from Cache Oblivious Lookahead Arrays to develop a practical priority queue supporting O(logN/B) amortized updates and O(log^2(N)) worst case searches
	//this is a prototype
	
	
	public COPriorityQueue()
	{
		init(1);
		assert checkInvariants();
	}
	
	
	public int peek()
	{
		assert !isEmpty();
		
		return index[chunkContainingMinimum()];
	}
	
	
	public int poll()
	{
		assert !isEmpty();
		
		int chunk = chunkContainingMinimum();
		int result = index[chunk];
		deleteMin(chunk);
		return result;
	}
	
	public void deleteMin()
	{
		int chunk = chunkContainingMinimum();
		deleteMin(chunk);
	}
	
	
	public boolean isEmpty()
	{
		return size() == 0;
	}
	
	public int size()
	{
		return count-numberOfZombies;
	}
	
	
	public boolean contains(int key)
	{
		boolean result = internalContains(key);
		assert checkInvariants();
		return result;
	}
	
	
	//O(log^2(N)): perform binary search on all of the chunks
	protected boolean internalContains(int key)
	{
		int numberOfChunks = numberOfChunks();
		for (int i=0; i<numberOfChunks; i++) {
			if (chunkContainsKey(i, key)) return true;
		}
		return false;
	}
	
	private boolean chunkContainsKey(int chunk, int key)
	{
		int index = Arrays.binarySearch(keys, firstValidKeyIndexForChunk[chunk], indexForChunk(chunk+1), key);
		return index >= 0;
	}
	
	public boolean add(int key)
	{
		internalAdd(key);
		assert checkInvariants();
		return true;
	}
	
	
	////
	//IMPLEMENTATION
	////
	
	private int chunkContainingMinimum()
	{	
		int min = Integer.MAX_VALUE;
		int minIndex = 0;
		for (int i=0; i<index.length; i++) {
			if (chunkIsValid(i) && index[i] < min) {
				min = index[i];
				minIndex = i;
			}
		}
		return minIndex;
	}
	
	private void deleteMin(int chunk)
	{
		keys[firstValidKeyIndexForChunk[chunk]] = Integer.MIN_VALUE;
		firstValidKeyIndexForChunk[chunk]++;
		if (chunkIsValid(chunk)) {
			index[chunk] = keys[firstValidKeyIndexForChunk[chunk]];
		}
		numberOfZombies++;
		
		if (tooManyZombies()) {
			rebuild();
		}
		
		assert checkInvariants();
	}
	
	private boolean tooManyZombies()
	{
		return (float)numberOfZombies/count > maximumZombieFraction;
	}
	
	
	private void rebuild()
	{
		int numberOfKeys = size();
		
		auxiliaryKeys[0] = Integer.MIN_VALUE;
		int afterwards = mergeValidPartsOfChunksIntoAuxiliary(numberOfChunks());//BUGGY: this method assumes that all chunks have valid information, or that they are filled with -infinity if they are invalid
		
		int[] source = auxiliaryKeys;
		int firstValidIndex = afterwards-numberOfKeys;
		init(numberOfChunks()-1);
		
		buildFromKeys(source, firstValidIndex, numberOfKeys, numberOfChunks());
		numberOfZombies = 0;
		count = numberOfKeys;
	}
	
	private void buildFromKeys(int[] source, int firstValidIndex, int numberOfKeys, int numberOfChunks)
	{
		int currentBit = 1;
		for (int i=0; i<numberOfChunks; i++) {
			//three parallel scans
			if ((currentBit & numberOfKeys) == currentBit) {
				System.arraycopy(source, firstValidIndex, keys, indexForChunk(i), sizeOfChunk(i));
				firstValidIndex += sizeOfChunk(i);
				firstValidKeyIndexForChunk[i] = indexForChunk(i);
				index[i] = keys[indexForChunk(i)];
			}
			
			currentBit = currentBit << 1;
		}
		
	}
	
	protected void internalAdd(int key)
	{
		int firstFreeChunk = firstFreeChunk();
		auxiliaryKeys[0] = key;
		//merge
		int numberOfValidKeys = mergeChunksIntoAuxiliary(firstFreeChunk);
		//clear old chunks
		setChunksEmpty(firstFreeChunk);
		
		int[] nextKeys = auxiliaryKeys;
		if (firstFreeChunk == numberOfChunks()) {
			init(numberOfChunks()+1, false);
			keys = nextKeys;
		} else {
			//copy merged keys into the next free node
			System.arraycopy(nextKeys, indexForChunk(firstFreeChunk), keys, indexForChunk(firstFreeChunk), sizeOfChunk(firstFreeChunk));
		}
		
		firstValidKeyIndexForChunk[firstFreeChunk] = indexForChunk(firstFreeChunk+1)-numberOfValidKeys;
		index[firstFreeChunk] = keys[firstValidKeyIndexForChunk[firstFreeChunk]];
		count++;
	}
	
	private int sizeOfChunk(int chunk)
	{
		assert chunk >= 0;
		return BinaryMath.powerOfTwo(chunk);
	}
	
	
	//accumulates all the valid keys of the first numberOfChunks in the auxiliary array
	//returns the index+1 of the last element that is relevant
	private int mergeValidPartsOfChunksIntoAuxiliary(int numberOfChunks)
	{
		int validKeys = 1;
		int currentChunkIndex = 0;
		int auxIndex = 0;
		int keyIndex = 0;
		int currentDestinationIndex = 1;
		int nextDestinationIndex = 1;
		
		for (int currentChunk=0; currentChunk<numberOfChunks; currentChunk++) {
			//merge step
			//advance pointers
			
			keyIndex = firstValidKeyIndexForChunk[currentChunk];
			
			currentChunkIndex += sizeOfChunk(currentChunk);
			assert currentDestinationIndex == nextDestinationIndex;
			
			validKeys += numberOfValidKeysForChunk(currentChunk);
			int startOfCurrentWriteChunk = nextDestinationIndex;
			nextDestinationIndex = nextDestinationIndex + validKeys;
			
			//actual merging (analogous to a merge step from merge-sort)
			
			for (; currentDestinationIndex<nextDestinationIndex; currentDestinationIndex++) {
				
				if (keyIndex >= currentChunkIndex || (auxIndex < startOfCurrentWriteChunk && auxiliaryKeys[auxIndex] <= keys[keyIndex])) {
					auxiliaryKeys[currentDestinationIndex] = auxiliaryKeys[auxIndex];
					auxIndex++;
				} else {
					auxiliaryKeys[currentDestinationIndex] = keys[keyIndex];
					keyIndex++;
				}
			}
			
			
		}
		
		return currentDestinationIndex;
		
	}
	
	
	//TODO: migrate adds to other merge method
	private int mergeChunksIntoAuxiliary(int numberOfChunks)
	{
		//algorithm performs 4 parallel scans (2 read and 2 write scans)
		int validKeys = 1;
		int currentChunkIndex = 0;
		int auxIndex = 0;
		int keyIndex = 0;
		int currentDestinationIndex = 1;
		int nextDestinationIndex = 1;
		
		for (int currentChunk=0; currentChunk<numberOfChunks; currentChunk++) {
			//merge step
			//advance pointers
			
			currentChunkIndex += sizeOfChunk(currentChunk);
			assert currentDestinationIndex == nextDestinationIndex;
			nextDestinationIndex = currentChunkIndex + sizeOfChunk(currentChunk+1);
		
			//actual merging (analogous to a merge step from merge-sort)
	
			for (; currentDestinationIndex<nextDestinationIndex; currentDestinationIndex++) {
				
				if (keyIndex >= currentChunkIndex || (auxIndex < currentChunkIndex && auxiliaryKeys[auxIndex] <= keys[keyIndex])) {
					auxiliaryKeys[currentDestinationIndex] = auxiliaryKeys[auxIndex];
					auxIndex++;
				} else {
					auxiliaryKeys[currentDestinationIndex] = keys[keyIndex];
					keyIndex++;
				}
			}
			
			assert keyIndex == currentChunkIndex;
			assert auxIndex == currentChunkIndex;
			
			//keep track of valid keys
			validKeys += numberOfValidKeysForChunk(currentChunk);
			
		}
		
		return validKeys;
	}
	
	private void init(int order)
	{
		init(order, true);
	}
	
	private void init(int order, boolean initKeys)
	{
		index = new int[order];
		firstValidKeyIndexForChunk = new int[order];
		setChunksEmpty(order);
		if (initKeys) {
			keys = new int[capacityForOrder(order)];
		}
		auxiliaryKeys = new int[capacityForOrder(order+1)];
	}
	
	private void setChunksEmpty(int numberOfChunks)
	{
		for (int currentChunk=0; currentChunk<numberOfChunks; currentChunk++) {
			firstValidKeyIndexForChunk[currentChunk] = indexForChunk(currentChunk+1);
		}
	}
	
	private int numberOfValidKeysForChunk(int chunk)
	{
		return indexForChunk(chunk+1)-firstValidKeyIndexForChunk[chunk];
	}
	
	
	private int firstFreeChunk()
	{
		int result = 0;
		while (result < index.length && chunkIsValid(result)) {
			result++;
		}
		return result;
	}
	
	private int capacityForOrder(int order)
	{
		return indexForChunk(order);
	}
	
	private int indexForChunk(int chunk)
	{
		assert chunk >= 0;
		return BinaryMath.powerOfTwo(chunk)-1;
	}
	
	private boolean chunkIsValid(int chunk)
	{
		assert chunk >= 0;
		return firstValidKeyIndexForChunk[chunk] < indexForChunk(chunk+1);
	}
	
	private int numberOfChunks()
	{
		return index.length;
	}
	
	////
	//INSTANCE VARIABLES
	////
	
	private int[] index;//contains the minimum value of the chunks
	private int[] firstValidKeyIndexForChunk;//chunks can contain empty/invalid slots. These values mark the first index that is valid. This value is equal to the next chunks index if there is no valid key in this chunk
	private int[] keys;//contains the keys, conceptually ordered into exponentially increasingly sized chunks
	private int[] auxiliaryKeys;//used internally for temporarily storing elements during chunk merges
	private int numberOfZombies = 0;//counts the number of elements that are marked as deleted. if this number gets too large, the structure is rebuilt
	private int count = 0;//counts the total number of slots used (including the zombie slots)
	
	private final static float maximumZombieFraction = (float)0.6;
	
	////
	//ASSERTIONS AND INVARIANTS
	////
	
	
	protected boolean checkInvariants()
	{
		boolean result = index.length > 0;
		assert result;
		result = result && chunksAreSorted();
		assert result;
		result = result && indexIsValid();
		assert result;
		return result;
	}
	
	protected boolean indexIsValid()
	{
		boolean result = true;
		for (int i=0; i<index.length; i++) {
			result = result && (!chunkIsValid(i) || index[i] == keys[firstValidKeyIndexForChunk[i]]);
			assert result;
		}
		return result;
	}
	
	protected boolean chunksAreSorted()
	{
		boolean result = true;
		for (int i=0; i<numberOfChunks(); i++) {
			result = result && chunkIsSorted(i);
			assert result;
		}
		return result;
	}
	
	protected boolean chunkIsSorted(int chunk)
	{
		boolean result = true;
		int last = Integer.MIN_VALUE;
		for (int i=firstValidKeyIndexForChunk[chunk]; i<indexForChunk(chunk+1); i++) {
			result = result && keys[i]>=last;
			assert result;
			last = keys[i];
		}
		return result;
	}
	

}
