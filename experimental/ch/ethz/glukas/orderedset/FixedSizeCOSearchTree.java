package ch.ethz.glukas.orderedset;

public class FixedSizeCOSearchTree implements StaticSearchTree {

	
	public FixedSizeCOSearchTree(int[] content)
	{
		assert BinaryMath.isPowerOfTwo(content.length);
		assert isSorted(content);
		assert content.length > 0;//TODO: generalize to handle 0 length content
	
		internalKeys = content;
		
		int magnitude = BinaryMath.log(content.length);
		int height = magnitude/nodeMagnitude;
		boolean leftover = magnitude == 0 || height*nodeMagnitude<magnitude;
		
		if (leftover) {
			height++;
		}
		
		
		treeHeight = height;
		
		int numberOfNodes = numberOfNodesForHeight(height);
		
		assert numberOfNodes > 0;
		assert treeHeight > 0;
		
		if (height == 1) {
			children = null;
			tree = content;
			numberOfKeysInTheRootNode = content.length;
		} else {
			children = new int[numberOfNodes];
			tree = new int[numberOfNodes];
			numberOfKeysInTheRootNode = leftover ? BinaryMath.powerOfTwo(magnitude-(magnitude/nodeMagnitude)*nodeMagnitude) : nodeSize;
			rebuild();
		}
		
		assert height == 1 || numberOfKeysInTheRootNode > 1;
		assert numberOfKeysInTheRootNode <= nodeSize;
	}
	
	public boolean contains(int key)
	{
		int index = indexOf(key);
		return internalKeys[index] == key;
	}
	
	public int indexOf(int key)
	{
		return find(key, 0, treeHeight);
	}
	

	//T(K^2) = T(K) + O(1) = O(log(K)) (note that if we divide a tree of size K^2 in half by height, the resulting subtrees have size K)
	//returns the index of the leaf the search should continue with (with respect to the parent tree)
	private int find(int key, int rootIndex, int height)
	{
		//base cases
		//large base cases are needed for performance reasons
		if (height == 1) {
			return baseCaseFind(key, rootIndex);
		}
		
		//subtree properties
		int topTreeHeight = height/2;
		int bottomTreeHeight =  height-topTreeHeight;
		
		int topTreeSize = numberOfNodesForHeight(topTreeHeight);
		int bottomTreeSize = numberOfNodesForHeight(bottomTreeHeight);
		
		//recurse on the top half of the tree
		int topindex = find(key, rootIndex, topTreeHeight);
		//check if result is within range
		assert topindex >= 0;
		assert topindex < numberOfLeavesForHeight(bottomTreeHeight);
		
		
		//recurse on one of the bottom trees (the topindex-th bottom tree)
		int bottomFoundIndex = find(key, rootIndex+topTreeSize+topindex*bottomTreeSize, bottomTreeHeight);
		//check if result is within range
		assert bottomFoundIndex >= 0;
		assert bottomFoundIndex < numberOfLeavesForHeight(bottomTreeHeight);
		
		//the index the search should continue with is the sum of the index relative to the bottom tree plus 2*the numberOfLeaves in a bottom tree * the number of trees to the left of the bottom tree
		return bottomFoundIndex+(topindex*numberOfLeavesForHeight(bottomTreeHeight));//(2^bottomTreeHeight) == 2*numberOfLeavesForHeight(bottomTreeHeight)
	}
	
	private int baseCaseFind(int key, int rootIndex)
	{
		int maxIndex = rootIndex+sizeOfNodeAtIndex(rootIndex);
		int currentIndex = rootIndex+1;
		while (currentIndex < maxIndex && tree[currentIndex] <= key) {
			currentIndex++;
		}
		return currentIndex-rootIndex-1;
	}

	private void rebuild()
	{
		rebuildChildrenPointers();
		totalRebuildKeys(0, 0);
		assert checkInvariants();
	}
	
	
	private void rebuildChildrenPointers()
	{
		rebuildChildrenPointers(0, treeHeight, null, 0);
	}
	
	
	//(re)builds a minimum tree
	//assumes all internalKeys are distinct and sorted in ascending order
	private void rebuildKeys(int rootIndex, int layerIndex, int smallestValueToUpdate, int largestValueToUpdate)
	{
		if (isLeafIndex(rootIndex)) {//base case
			assert rootIndex > 0;
			System.arraycopy(internalKeys, layerIndex, tree, rootIndex, nodeSize);
		} else {
			
			int currentChild = 0;
			int currentChildIndex = rootIndex;
			int length = sizeOfNodeAtIndex(rootIndex);
			while (tree[currentChildIndex] <= largestValueToUpdate && currentChild < length) {
				
				if (tree[currentChildIndex+1] > smallestValueToUpdate) {//Probably BUGGY
					//update:
					rebuildKeys(children[currentChildIndex], (layerIndex+currentChild)*nodeSize, smallestValueToUpdate, largestValueToUpdate);
					tree[currentChildIndex] = tree[children[currentChildIndex]];
				}
				
				currentChild++;
				currentChildIndex++;
			}
			
		}
	}
	
	private void totalRebuildKeys(int rootIndex, int layerIndex)
	{
		if (isLeafIndex(rootIndex)) {//base case
			assert rootIndex > 0;
			System.arraycopy(internalKeys, layerIndex, tree, rootIndex, nodeSize);
		} else {
			
			int currentChild = 0;
			int currentChildIndex = rootIndex;
			int length = sizeOfNodeAtIndex(rootIndex);
			while (currentChild < length) {
				
				totalRebuildKeys(children[currentChildIndex], (layerIndex+currentChild)*nodeSize);
				tree[currentChildIndex] = tree[children[currentChildIndex]];
				
				currentChild++;
				currentChildIndex++;
			}
			
		}
	}
	
	private void rebuildChildrenPointers(int rootIndex, int height, int[] indexesOfChildren, int offset)
	{
		if (height == 1) {
			if (indexesOfChildren != null) {//if indexesOfChildren is null, the node is a leaf and it has no children
				int length = sizeOfNodeAtIndex(rootIndex);
				System.arraycopy(indexesOfChildren, offset, children, rootIndex, length);
			}
			return;
		}
		
		//subtree properties
		int topTreeHeight = height/2;
		int bottomTreeHeight =  height-topTreeHeight;
		
		int topTreeSize = numberOfNodesForHeight(topTreeHeight);
		int bottomTreeSize = numberOfNodesForHeight(bottomTreeHeight);
		
		int numberOfChildrenOfTheTopTree = numberOfLeavesForHeight(topTreeHeight);
		int numberOfChildrenOfEachBottomTree = numberOfLeavesForHeight(bottomTreeHeight);
		
		//recursively build the bottom trees and save their indexes
		int [] bottomTreeIndexes = new int[numberOfChildrenOfTheTopTree];
		
		int currentIndex = rootIndex+topTreeSize;
		for (int i=0; i<numberOfChildrenOfTheTopTree; i++) {
			bottomTreeIndexes[i] = currentIndex;
			rebuildChildrenPointers(currentIndex, bottomTreeHeight, indexesOfChildren, offset);//maybe it would be more cache efficient to first write the bottomTreeIndexes array before recursing?
			currentIndex += bottomTreeSize;
			offset += numberOfChildrenOfEachBottomTree;
		}
		
		//recursively build the top trees
		rebuildChildrenPointers(rootIndex, topTreeHeight, bottomTreeIndexes, 0);
	}
	
	
	private static int numberOfLeavesForHeight(int height)
	{
		return BinaryMath.powerOfTwo(nodeMagnitude*height);
	}
	
	private static int numberOfNodesForHeight(int height)
	{
		//q+q^2+...+q^k = (q-q^(k+1))/(1-q)
		//TODO: investigate more elegant solution
		long size = (long)1 << (nodeMagnitude*(height+1));
		long result = ((long)nodeSize-size)/(long)(1-nodeSize);
		assert (int)result > 0;
		return (int)result;
	}
	
	
	private int sizeOfNodeAtIndex(int index)
	{
		return (index > 0) ? nodeSize : numberOfKeysInTheRootNode;//the root might have less children
	}
	private boolean isLeafIndex(int index)
	{
		return children[index] == 0;
	}
	
	////
	//INSTANCE VARIABLES
	///
	
	private final int[] children;
	private final int[] tree;
	private final int treeHeight;
	private final int[] internalKeys;
	private final int numberOfKeysInTheRootNode;
	
	////
	//CONSTANTS
	////

	private static final int nodeMagnitude = 5;
	private static final int nodeSize = BinaryMath.powerOfTwo(nodeMagnitude);

	
	////
	//INVARIANTS & ASSERTIONS
	////
	
	protected boolean isSorted(int[] array)
	{
		if (array.length == 0) return true;
		int last = array[0];
		boolean result = true;
		for (int i=1; i<array.length; i++) {
			result = result && array[i] >= last;
			last = array[i];
		}
		return result;
	}
	
	protected boolean checkInvariants()
	{
		boolean result = subtreesConsistent(0);
		assert result;
		return result;
	}
	
	protected boolean subtreesConsistent(int rootIndex) {
		
		if (isLeafIndex(rootIndex)) {
			return true;
		}
		boolean result = true;
		for (int i=0; i<nodeSize; i++) {
			if (children[rootIndex+i] == 0) break;
			
			result = result && tree[rootIndex+i] == tree[children[rootIndex+i]];
			assert result;
			result = result && subtreesConsistent(children[rootIndex+i]);
			result = result && subtreesConsistent(children[rootIndex+i]);
		}
		assert result;
		return result;
	}
	
}
