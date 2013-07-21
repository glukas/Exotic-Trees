package ch.ethz.glukas.orderedset;

import java.util.Arrays;

public class ImmutableOrderedSet {
	//Cache-oblivious Corona tree
	//The structure is a cache oblivious static search tree (sometimes called "Van Embde Boas" layout.
	
	//all keys are stored in the leafs of the tree. the value of the parent is the minimum key in the right subtree.
	//the leafs are stored in the internalKeys array (consecutively).
	//the index structure is stored in the tree array. the tree is implicit.
	//the algorithms operate recursively on the top and bottom halves of a subtree
	//the base cases are large for performance reasons. In the base case, the tree is stored in-order.
	
	//TODO: method to update individual keys (and sequences of adjacent keys)
	
	public ImmutableOrderedSet(int[] content)
	{
		assert BinaryMath.isPowerOfTwo(content.length);
		assert isSorted(content);
		assert content.length > 0;//TODO: generalize to handle 0 length content
		
		treeHeight = BinaryMath.log(content.length);
		internalKeys = content;

		if (treeHeight > 0) {
			tree = new int[numberOfNodesForHeight(treeHeight)];
			rebuildUpwards(0, treeHeight, internalKeys, 0);
		} else {
			//create a dummy tree that always returns index 0
			tree = new int[1];
			tree[0] = Integer.MAX_VALUE;
		}
	}

	public boolean contains(int key)
	{
		//find the index where the key should reside
		int idx = find(key, 0, treeHeight);
		boolean result = internalKeys[idx] == key;
		
		assert internalKeys[idx] == key || ((idx == 0 || internalKeys[idx-1] < key) && (idx == internalKeys.length-1 || internalKeys[idx+1] > key));
		assert simpleContains(key) == result;
		
		return result;
	}

	
	//Asymptotics:
	//T(K^2) = KT(K) + O(1) = O(logK) (note that if we divide a tree of size K^2 in half by height, the resulting subtrees have size K)
	//returns the index of the leaf the search should continue with (with respect to the parent tree)
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
		
		//recurse on the top half of the tree
		int topindex = find(key, rootIndex, topTreeHeight);
		//check if result is within range
		assert topindex >= 0;
		assert topindex < 2*numberOfLeavesForHeight(topTreeHeight);
		
		
		//recurse on one of the bottom trees (the topindex-th bottom tree)
		int topTreeSize = numberOfNodesForHeight(topTreeHeight);
		int bottomTreeSize = numberOfNodesForHeight(bottomTreeHeight);
		int bottomFoundIndex = find(key, rootIndex+topTreeSize+topindex*bottomTreeSize, bottomTreeHeight);
		//check if result is within range
		assert bottomFoundIndex >= 0;
		assert bottomFoundIndex < 2*numberOfLeavesForHeight(bottomTreeHeight);
		
		//the index the search should continue with is the sum of the index relative to the bottom tree plus 2*the numberOfLeaves in a bottom tree * the number of trees to the left of the bottom tree
		return bottomFoundIndex+(topindex*BinaryMath.powerOfTwo(bottomTreeHeight));//(2^bottomTreeHeight) == 2*numberOfLeavesForHeight(bottomTreeHeight)
	}

	
	//Runtime is linear:
	//T(K^2) = KT(K) + O(K) = O(K^2)
	//proof by induction (substitution method): let P(K^2) be the predicate that T(K^2)<=c1*K^2-c2
	//we know from inspecting the algorithm that T(K^2) = K*T(K) + c0*K and T(1) = c3 (note that if we divide a tree of size K^2 in half by height, the resulting subtrees have size K)
	//P(1) holds for c1-c2>=c3
	//P(k^2) implies P(k^4):
	//T(k^4) = K^2 * T(k^2)+c0K <= K^2(c1*K^2-c2)+c0*K = c1*K^4-c2*K^2+c0*K = c1*K^4-c2-(c2+c2*K^2-c0*K) <= c1*K^4-c2 for c2<=c0, k>=1
	
	//returns the minimum key in the subtree
	//the input array contain the minimum elements of the children of the leaf nodes ordered from left to right
	//offset denotes the index of the first key of the subtreeMinima array that is relevant for the current step
	//the end of the relevant part is implicit in the rank of the current subtree
	
	private int rebuildUpwards(int rootIndex, int height, int[] subtreeMinima, int offset)
	{
		if (height <= 7) {
			return baseCaseRebuild(rootIndex, height, subtreeMinima, offset);
		}
		
		//subtree properties
		int topTreeHeight = height/2;
		int bottomTreeHeight =  height-topTreeHeight;
		
		int topTreeSize = numberOfNodesForHeight(topTreeHeight);
		int bottomTreeSize = numberOfNodesForHeight(bottomTreeHeight);
		
		int numberOfChildrenOfTheTopTree = 2*numberOfLeavesForHeight(topTreeHeight);
		int numberOfChildrenForEachBottomTree = 2*numberOfLeavesForHeight(bottomTreeHeight);
		
		//recursively build the bottom trees
		//and store the minimum elements of all those subtrees
		int [] bottomTreeMinima = new int[numberOfChildrenOfTheTopTree];
		
		int currentIndex = rootIndex+topTreeSize;
		for (int i=0; i<numberOfChildrenOfTheTopTree; i++) {
			bottomTreeMinima[i] = rebuildUpwards(currentIndex, bottomTreeHeight, subtreeMinima, offset);
			currentIndex += bottomTreeSize;
			offset += numberOfChildrenForEachBottomTree;
		}
		
		//recursively build the top tree and return the minimum overall
		return rebuildUpwards(rootIndex, topTreeHeight, bottomTreeMinima, 0);
	}
	
	//stores the index subtree in order starting at the rootIndex.
	//the offset refers to the first index in the arrays that is relevant
	//the height determines the size of the subtree to be constructed
	//the method returns the minimum of the whole subtree rooted at 'rootIndex' (this is the entry of the subtreeMinima array at index 'offset)
	private int baseCaseRebuild(int rootIndex, int height, int[] subtreeMinima, int offset)
	{
		copySomeEntries(subtreeMinima, tree, offset+1, rootIndex, 1, 1, numberOfNodesForHeight(height));
		//the leftmost entry is the minimum overall
		return subtreeMinima[offset];
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
	
	
	private static void copySomeEntries(int[]source, int[]target, int sourceIndex, int targetIndex, int fromStepSize, int toStepSize, int numberOfSteps)
	{
		for (int i=0; i<numberOfSteps; i++) {
			target[targetIndex] = source[sourceIndex];
			sourceIndex += fromStepSize;
			targetIndex += toStepSize;
		}
	}
	
	
	///
	//INSTANCE VARIABLES
	////
	
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
	
	protected boolean simpleContains(int key)
	{
		//naive binary search (this is the baseline algorithm)
		int idx = Arrays.binarySearch(internalKeys, key);
		if (idx >= 0) {
			return true;
		} else {
			return false;
		}
	}
	
}
