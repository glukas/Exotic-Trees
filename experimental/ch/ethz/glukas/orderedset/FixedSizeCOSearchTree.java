package ch.ethz.glukas.orderedset;

public class FixedSizeCOSearchTree implements StaticSearchTree {

	
	public FixedSizeCOSearchTree(int[] content)
	{
		assert BinaryMath.isPowerOfTwo(content.length);
		assert isSorted(content);
		assert content.length > 0;//TODO: generalize to handle 0 length content
		
		treeHeight = BinaryMath.log(content.length);
		internalKeys = content;

		if (treeHeight > 0) {
			int numberOfNodes = numberOfNodesForHeight(treeHeight);
			tree = new int[numberOfNodes];
			leftChild = new int[numberOfNodes];
			rightChild = new int[numberOfNodes];
			rebuild();
		} else {
			//create a dummy tree that always returns index 0
			tree = new int[1];
			leftChild = new int[1];
			rightChild = new int[1];
			tree[0] = Integer.MAX_VALUE;
		}
	}
	
	public boolean contains(int key)
	{
		return internalKeys[indexOf(key)] == key;
	}
	
	public int indexOf(int key)
	{
		if (treeHeight == 0) {
			return 0;
		}
		
		int currentIndex = 0;
		int layerIndex = 0;
		int depth = 0;
		int maxDepth = treeHeight-1;
		
		while (depth < maxDepth) {
			layerIndex *= 2;
			int rightChildIndex = rightChild[currentIndex];
			if (key < tree[rightChildIndex]) {//smaller than the minimum of the right tree
				currentIndex = leftChild[currentIndex];
			} else {
				currentIndex = rightChildIndex;
				layerIndex++;
			}
			depth++;
		}
		
		if (key >= internalKeys[2*layerIndex+1]) {
			return 2*layerIndex+1;
		} else {
			return 2*layerIndex;
		}
	}
	
	private void rebuild()
	{
		rebuildChildrenPointers();
		rebuildKeys(0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
		assert checkInvariants();
	}
	
	
	private void rebuildChildrenPointers()
	{
		rebuildChildrenPointers(0, treeHeight, null, 0);
	}
	
	private boolean isLeafIndex(int index)
	{
		return leftChild[index] == 0;
	}
	
	
	//(re)builds a minimum tree
	//assumes all internalKeys are distinct and sorted in ascending order
	private void rebuildKeys(int rootIndex, int layerindex, int smallestValueToUpdate, int largestValueToUpdate)
	{
		if (isLeafIndex(rootIndex)) {//base case
			tree[rootIndex] = internalKeys[2*layerindex];
		} else {
			
			int rightChildIndex = rightChild[rootIndex];
			int rightChildValue = tree[rightChildIndex];
			
			assert smallestValueToUpdate < rightChildValue || largestValueToUpdate >= rightChildValue;
			
			if (smallestValueToUpdate < rightChildValue) {
				int leftChildIndex = leftChild[rootIndex];
				int leftChildValue = tree[leftChildIndex];
				
				rebuildKeys(leftChildIndex, layerindex*2, smallestValueToUpdate, largestValueToUpdate);
				tree[rootIndex] = tree[leftChildIndex];
			}
			if (largestValueToUpdate >= rightChildValue) {
				rebuildKeys(rightChildIndex, layerindex*2+1, smallestValueToUpdate, largestValueToUpdate);
			}
			
			assert tree[rootIndex] == tree[leftChild[rootIndex]];
		}
	}
	
	private void rebuildChildrenPointers(int rootIndex, int height, int[] indexesOfChildren, int offset)
	{
		if (height == 1) {
			if (indexesOfChildren != null) {//if indexesOfChildren is null, the nodes is a leaf: no children
				leftChild[rootIndex] = indexesOfChildren[offset];
				rightChild[rootIndex] = indexesOfChildren[offset+1];
			}
			return;
		}
		
		//subtree properties
		int topTreeHeight = height/2;
		int bottomTreeHeight =  height-topTreeHeight;
		
		int topTreeSize = numberOfNodesForHeight(topTreeHeight);
		int bottomTreeSize = numberOfNodesForHeight(bottomTreeHeight);
		
		int numberOfChildrenOfTheTopTree = 2*numberOfLeavesForHeight(topTreeHeight);//==topTreeSize+1;
		int numberOfChildrenOfEachBottomTree = 2*numberOfLeavesForHeight(bottomTreeHeight);//==bottomTreeSize+1
		
		//recursively build the bottom trees and save their indexes
		int [] bottomTreeIndeces = new int[numberOfChildrenOfTheTopTree];
		
		int currentIndex = rootIndex+topTreeSize;
		for (int i=0; i<numberOfChildrenOfTheTopTree; i++) {
			bottomTreeIndeces[i] = currentIndex;
			rebuildChildrenPointers(currentIndex, bottomTreeHeight, indexesOfChildren, offset);//maybe it would be more cache efficient to first write the bottomTreeIndeces array before recursing?
			currentIndex += bottomTreeSize;
			offset += numberOfChildrenOfEachBottomTree;
		}
		
		//recursively build the top trees
		rebuildChildrenPointers(rootIndex, topTreeHeight, bottomTreeIndeces, 0);
	}
	
	
	private static int numberOfLeavesForHeight(int height)
	{
		return BinaryMath.powerOfTwo(height-1);
	}
	
	private static int numberOfNodesForHeight(int height)
	{
		return BinaryMath.powerOfTwo(height)-1;
	}
	
	private final int[] leftChild;
	private final int[] rightChild;
	private final int[] internalKeys;
	private final int[] tree;
	private final int treeHeight;
	
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
		
		boolean result = tree[rootIndex] == tree[leftChild[rootIndex]];
		assert result;
		result = result && subtreesConsistent(leftChild[rootIndex]);
		assert result;
		result = result && subtreesConsistent(rightChild[rootIndex]);
		assert result;
		return result;
	}
	
}
