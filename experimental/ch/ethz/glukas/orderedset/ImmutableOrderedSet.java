package ch.ethz.glukas.orderedset;

import java.util.Arrays;

public class ImmutableOrderedSet {
	//Cache-oblivious Corona tree
	//The memory layout is van Embde Boas
	//all keys are stored in the leafs of the tree
	//this allows for simple recursive algorithms that don't involve the direct calculation of child indexes in the implicit tree array
	
	
	//TODO: method to update individual keys (and sequences of adjacent keys)
	
	public ImmutableOrderedSet(int[] content)
	{
		assert BinaryMath.isPowerOfTwo(content.length);
		assert isSorted(content);
		
		treeHeight = BinaryMath.log(content.length);
		internalKeys = content;
		
		if (treeHeight > 0) {
			tree = new int[numberOfNodesForHeight(treeHeight)];
			int[]leftInput = evenEntries(content, 0, content.length);
			int[]rightInput = oddEntries(content, 0, content.length);
			
			rebuildUpwards(0, treeHeight, leftInput, rightInput, 0);
		} else {
			tree = new int[1];//create a dummy tree that always return index 0
			tree[0] = Integer.MAX_VALUE;
		}
	}

	public boolean contains(int key)
	{
		/*int idx = Arrays.binarySearch(internalKeys, key);
		if (idx >= 0) {
			return true;
		} else {
			return false;
		}*/
		
		int idx = find(key, 0, treeHeight);
		assert internalKeys[idx] == key || ((idx == 0 || internalKeys[idx-1] < key) && (idx == internalKeys.length-1 || internalKeys[idx+1] > key));
		return internalKeys[idx] == key;
	}

	
	//Asymptotics:
	//T(K^2) = KT(K) + O(1) = O(logK) (note that if we divide a tree of size K^2 in half by height, the resulting subtrees have size K)
	//returns the index of the leaf the search ended in (with respect to the given subtree at rootIndex of size numberOfNodes(rank)
	//the key of this leaf is put into 'foundkey'
	private int find(int key, int rootIndex, int height)
	{
		//base cases
		//large base cases are needed for performance reasons
		if (height <= 7) {
			return baseCaseFind(key, rootIndex, height);
		}
		
		//subtree properties
		int topTreeHeight = height/2;
		int bottomTreeHeight =  height-topTreeHeight;
		
		//top
		int topindex = find(key, rootIndex, topTreeHeight);
		
		assert topindex < 2*numberOfLeavesForHeight(topTreeHeight);
		assert topindex >= 0;
		
		int topTreeSize = numberOfNodesForHeight(topTreeHeight);
		int bottomTreeSize = numberOfNodesForHeight(bottomTreeHeight);
		int bottomFoundIndex = find(key, rootIndex+topTreeSize+topindex*bottomTreeSize, bottomTreeHeight);
		
		
		assert bottomFoundIndex < 2*numberOfLeavesForHeight(bottomTreeHeight);
		assert bottomFoundIndex >= 0;
		
		return bottomFoundIndex+(topindex*BinaryMath.powerOfTwo(bottomTreeHeight));//(2^bottomTreeHeight) == 2*numberOfLeavesForHeight(bottomTreeHeight)
	}

	
	//Asymptotics:
	//T(K^2) = KT(K) + O(K) = O(K^2)
	//proof: assume P(K^2): T(K^2)<=c1*K^2-c2 
	//we know T(K^2) = KT(K) + c0*K and T(1) = c3 (note that if we divide a tree of size K^2 in half by height, the resulting subtrees have size K)
	//P(1) holds for c1-c2>=c3
	//P(k^2) implies P(k^4):
	//T(k^4) = K^2T(k^2)+c0K <= K^2(c1K^2-c2)-c0*K = c1*K^4-c2*K^2+c0*K = c1*K^4-c2-(c2+c2*K^2-c0*K) <= c1*K^4-c2 for c2<=c0, k>=1
	
	//returns the minimum key in the subtree
	//offset denotes the first key of the minimumSubtreeKeys arrays that is relevant for the current step
	//the end of the relevant part is implicit in the rank of the current subtree
	private int rebuildUpwards(int rootIndex, int height, int[] leftMinimumSubtreeKeys, int[] rightMinimumSubtreeKeys, int offset)
	{
		if (height <= 7) {
			return baseCaseRebuild(rootIndex, height, leftMinimumSubtreeKeys, rightMinimumSubtreeKeys, offset);
		}
		
		//subtree properties
		int topTreeHeight = height/2;
		int bottomTreeHeight =  height-topTreeHeight;
		
		
		int bottomTreeSize = numberOfNodesForHeight(bottomTreeHeight);
		int numberOfBottomTreeLeaves = numberOfLeavesForHeight(bottomTreeHeight);
		
		int topTreeSize = numberOfNodesForHeight(topTreeHeight);
		int numberOfTopTreeLeaves = numberOfLeavesForHeight(topTreeHeight);
		int numberOfBottomTrees = 2*numberOfTopTreeLeaves;
		
		
		//bottom trees
		int [] leftBottomTreeMins = new int[numberOfTopTreeLeaves];
		int [] rightBottomTreeMins = new int[numberOfTopTreeLeaves];
		
		int currentIndex = rootIndex+topTreeSize;
		for (int i=0; i<numberOfBottomTrees; i+=2) {
			leftBottomTreeMins[i/2] = rebuildUpwards(currentIndex, bottomTreeHeight, leftMinimumSubtreeKeys, rightMinimumSubtreeKeys, offset+i*numberOfBottomTreeLeaves);
			currentIndex += bottomTreeSize;
			rightBottomTreeMins[i/2] = rebuildUpwards(currentIndex, bottomTreeHeight, leftMinimumSubtreeKeys, rightMinimumSubtreeKeys, offset+(i+1)*numberOfBottomTreeLeaves);
			currentIndex += bottomTreeSize;
		}
		
		//top tree: push up the keys from the bottom trees
		//the key of a node is the smallest key of its right subtree
		rebuildUpwards(rootIndex, topTreeHeight, leftBottomTreeMins, rightBottomTreeMins, 0);
		
		return leftBottomTreeMins[0];//because the input is presorted, the min is always the leftmost entry
	}
	
	
	//stores the index subtree in order starting at the rootIndex.
	//the index tree is constructed using the two input arrays
	//the offset refers to the first index in the arrays that is relevant
	//the height determines the size of the subtree to be constructed
	//the method returns the minimum of the whole subtree rooted at 'rootIndex' (this is the entry of the left key array at index 'offset)
	private int baseCaseRebuild(int rootIndex, int height, int[] leftMinimumSubtreeKeys, int[] rightMinimumSubtreeKeys, int offset)
	{
		int numberOfLeaves = numberOfLeavesForHeight(height);
		//todo: use the copy some keys subroutine
		
		//the leaves are on the even positions, they come from the right children's keys
		for (int i=0; i<numberOfLeaves; i++) {
			tree[rootIndex+2*i] = rightMinimumSubtreeKeys[offset+i];
		}
		//the inner nodes are on the odd positions, they come from the left children's keys (the leftmost one is skipped
		int numberOfInnerNodes = numberOfLeaves-1;
		for (int i=0; i<numberOfInnerNodes; i++) {
			tree[rootIndex+2*i+1] = leftMinimumSubtreeKeys[offset+i+1];
		}
		
		//the leftmost entry of the left minimum children is the minimum overall
		return leftMinimumSubtreeKeys[offset];
	}
	
	private int baseCaseFind(int key, int rootIndex, int height)
	{
		//performs a linear scan on the inner nodes of the subtree (subtrees are stored in-order for small heights)
		int startIdx = rootIndex;
		int maxRootIndex = rootIndex+numberOfNodesForHeight(height);
		//the inner nodes are on the odd positions (every second position)
		rootIndex++;
		while (rootIndex < maxRootIndex && key >= tree[rootIndex]) {
			rootIndex+=2;
		}
		
		int nextNode = rootIndex-startIdx;
		//we need to decide which subtree the search should continue on (test on the leaf the search ended on - this leaf is just behind the inner node the search ended on)
		if (key<tree[rootIndex-1]) {
			nextNode--;
		}
		//the arithmetic works out well so that this will get the index of the subtree the search should continue on
		return nextNode;
	}
	
	private int numberOfLeavesForHeight(int height)
	{
		return BinaryMath.powerOfTwo(height-1);
	}
	
	private int numberOfNodesForHeight(int height)
	{
		return BinaryMath.powerOfTwo(height)-1;
	}
	
	
	///
	//ARRAY HELPERS
	////
	
	private int[] evenEntries(int[]keys, int fromIndex, int toIndex)
	{
		int size = (toIndex-fromIndex)/2;
		int[] result = new int[size];
		copySomeEntries(keys, result, fromIndex, 0, 2, 1, size);
		return result;
	}
	
	private int[] oddEntries(int[]keys, int fromIndex, int toIndex)
	{
		int size = (toIndex-fromIndex)/2;
		int[] result = new int[size];
		copySomeEntries(keys, result, fromIndex+1, 0, 2, 1, size);
		return result;
	}
	
	private void copySomeEntries(int[]source, int[]target, int sourceIndex, int targetIndex, int fromStepSize, int toStepSize, int numberOfSteps)
	{
		for (int i=0; i<numberOfSteps; i++) {
			target[targetIndex] = source[sourceIndex];
			targetIndex += toStepSize;
			sourceIndex += fromStepSize;
		}
	}
	
	
	///
	//INSTANCE VARIABLES
	////
	
	private final int[] internalKeys;//for debug purposes
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
	
}
