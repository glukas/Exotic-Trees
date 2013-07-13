package ch.ethz.glukas.orderedset;

import java.util.Arrays;

public class ImmutableOrderedSet {
	//Cache-oblivious Corona tree
	//The memory layout is van Embde Boas
	//all keys are stored in the leafs of the tree
	//this allows for simple recursive algorithms that don't involve the direct calculation of child indexes in the implicit tree array
	
	
	//TODO: investigate better base cases
	//TODO: method to update individual keys (and sequences of adjacent keys)
	
	public ImmutableOrderedSet(int[] content)
	{
		rebuild(content, 0, content.length);
	}

	public boolean contains(int key)
	{
		/*int idx = Arrays.binarySearch(internalKeys, key);
		if (idx >= 0) {
			return true;
		} else {
			return false;
		}*/
		
		if (treeHeight == 1) {
			internalLastFound = tree[0];
		} else if (treeHeight == 2) {
			baseCaseHeight2Find(key, 0);
		} else {
			find(key, 0, treeHeight);
		}
		return internalLastFound == key;
	}
	
	
	//fromIndex inclusive, toIndex exclusive
	private void rebuild(int[] keys, int fromIndex, int toIndex)
	{
		assert BinaryMath.isPowerOfTwo(toIndex-fromIndex);
		
		internalKeys = keys;
		
		treeHeight = BinaryMath.log(toIndex-fromIndex)+1;
		tree = new int[numberOfNodesForHeight(treeHeight)];
		
		rebuildUpwards(0, treeHeight, keys, keys, 0);
		//assert treeConsistent();
	}
	
	
	//T(K^2) = KT(K) + O(1) = O(logK)
	//returns the index of the leaf the search ended in (with respect to the given subtree at rootIndex of size numberOfNodes(rank)
	//the key of this leaf is put into 'foundkey'
	private int find(int key, int rootIndex, int height)
	{
		assert height > 2;
		
		//base cases
		//large base cases are needed for performance reasons
		//the base cases assume that the tree has at least height 3
		if (height == 5) {
			return baseCaseHeight5Find(key, rootIndex);
		} else if (height == 4) {
			return baseCaseHeight4Find(key, rootIndex);
		} else if (height == 3) {
			return baseCaseHeight3Find(key, rootIndex);
		}
		
		//subtree properties
		int topTreeHeight = height/2;
		int bottomTreeHeight =  height-topTreeHeight;
		
		//top
		int topindex = find(key, rootIndex, topTreeHeight);
		assert topindex < numberOfLeavesForHeight(topTreeHeight);
		assert topindex >= 0;
		
		//bottom
		//decide where to continue the search
		int bottomTreeIndex =  2*topindex;
		if (key >= internalLastFound) {
			bottomTreeIndex++;
		}
		
		int bottomFoundIndex = find(key, rootIndex+numberOfNodesForHeight(topTreeHeight)+bottomTreeIndex*numberOfNodesForHeight(bottomTreeHeight), bottomTreeHeight);
		assert bottomFoundIndex < numberOfLeavesForHeight(bottomTreeHeight);
		assert bottomFoundIndex >= 0;
		
		return bottomFoundIndex+(bottomTreeIndex*numberOfLeavesForHeight(bottomTreeHeight));
	}

	
	//T(K^2) = KT(K) + O(K) = O(K^2)
	//proof: assume P(K^2): T(K^2)<=c1*K^2-c2
	//we know T(K^2) = KT(K) + c0*K and T(1) = c3
	//P(1) holds for c1-c2>=c3
	//P(k^2) implies P(k^4):
	//T(k^4) = K^2T(k^2)+c0K <= K^2(c1K^2-c2)-c0*K = c1*K^4-c2*K^2+c0*K = c1*K^4-c2-(c2+c2*K^2-c0*K) <= c1*K^4-c2 for c2<=c0, k>=1
	
	//returns the minimum key in the subtree
	//lower denotes the first key of the minimumSubtreekeys array that are relevant for the current step
	//the end of the relevant part is implicit in the rank of the current subtree
	private int rebuildUpwards(int rootIndex, int height, int[] leftMinimumSubtreeKeys, int[] rightMinimumSubtreeKeys, int offset)
	{
		//base case - singleton tree : write key at root index
		if (height == 1) {
			tree[rootIndex] = rightMinimumSubtreeKeys[offset];//keys are the min of the right subtree
			return Math.min(leftMinimumSubtreeKeys[offset], rightMinimumSubtreeKeys[offset]);//return the minimum of the whole subtree
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
	
	
	/*
	private void inOrder(int rootIndex, int rank, int[]output, int outputIndex)
	{
		
		//base case - singleton tree : write key to output array
		if (rank == 16) {
			baseCase16InOrder(rootIndex, output, outputIndex);
		}
		
		//subtree properties
		int subrank = subrank(rank);
		int subcoronaSize = numberOfNodes(subrank);
		int subcoronaLeaves = numberOfLeaves(subrank);
		int subcoronas = 2*subcoronaLeaves;
		
		//bottom trees
		int currentIndex = rootIndex;
		for (int i=0; i<subcoronas; i++) {
			currentIndex += subcoronaSize;
			inOrder(currentIndex, subrank, output, outputIndex+i*subcoronaLeaves);
		}
	}*/
	
	
	
	//the currentIndex is the index in the tree of the subtree
	private int baseCaseHeight4Find(int key, int rootIndex)
	{
		int leaf = 0;
		int currentIndex = rootIndex;
		//level 1
		if (key >= tree[currentIndex]) {
			leaf += 4;
			currentIndex += 2;
		} else {
			currentIndex ++;
		}
		
		//level 2
		if (key >= tree[currentIndex]) {
			leaf += 2;
		}
		currentIndex = rootIndex+3+(leaf/2)*3;
		
		//level 3
		if (key >= tree[currentIndex]) {
			leaf++;
			currentIndex+=2;
		} else {
			currentIndex++;
		}
		
		internalLastFound = tree[currentIndex];
		
		return leaf;
	}
	
	private int baseCaseHeight3Find(int key, int rootIndex)
	{
		int leaf = 0;
		//level 1
		if (key >= tree[rootIndex]) {
			rootIndex += 4;
			leaf += 2;
		} else {
			rootIndex++;
		}
		//level 2
		if (key >= tree[rootIndex]) {
			rootIndex += 2;
			leaf++;
		} else {
			rootIndex++;
		}
		internalLastFound = tree[rootIndex];
		return leaf;
	}
	
	private int baseCaseHeight2Find(int key, int rootIndex)
	{
		if (key >= tree[rootIndex]) {
			internalLastFound = tree[rootIndex+2];
			return 1;
		} else {
			internalLastFound = tree[rootIndex+1];
			return 0;
		}
	}
	
	
	
	private int baseCaseHeight5Find(int key, int rootIndex)
	{
		int leaf = 0;
		int currentIndex = rootIndex;
		//level 1
		if (key >= tree[currentIndex]) {
			leaf += 8;
			currentIndex += 2;
		} else {
			currentIndex ++;
		}
		//level 2
		if (key >= tree[currentIndex]) {
			leaf += 4;
		}
		currentIndex = rootIndex+3+(leaf/4)*7;
		
		//level 3
		if (key >= tree[currentIndex]) {
			currentIndex += 4;
			leaf += 2;
		} else {
			currentIndex++;
		}
		//level 4
		if (key >= tree[currentIndex]) {
			currentIndex += 2;
			leaf++;
		} else {
			currentIndex++;
		}
		internalLastFound = tree[currentIndex];
		return leaf;
	}
	
	/*
	private void baseCase16InOrder(int rootIndex, int[]output, int offset)
	{
		output[offset] = tree[rootIndex+4];
		output[offset+1] = tree[rootIndex+5];
		output[offset+2] = tree[rootIndex+7];
		output[offset+3] = tree[rootIndex+8];
		output[offset+4] = tree[rootIndex+10];
		output[offset+5] = tree[rootIndex+11];
		output[offset+6] = tree[rootIndex+13];
		output[offset+7] = tree[rootIndex+14];
	}*/
	
	/*
	private int[] inOrder()
	{
		int[] traversal = new int[numberOfLeaves(rootRank)];
		inOrder(0, rootRank, traversal, 0);
		return traversal;
	}*/
	
	private int numberOfLeavesForHeight(int height)
	{
		return BinaryMath.powerOfTwo(height-1);
	}
	
	private int numberOfNodesForHeight(int height)
	{
		return BinaryMath.powerOfTwo(height)-1;
	}
	
	private int[] internalKeys;//for debug purposes
	private int[] tree;
	
	
	private int treeHeight;
	
	//used internally by the find method
	private int internalLastFound;
	
	////
	//INVARIANTS & ASSERTIONS
	////
	
	/*
	private boolean treeConsistent()
	{
		int[] traversal = inOrder();
		boolean result = true;
		for (int i=0; i<keys.length; i++) {
			result = result && keys[i]==traversal[i];
			assert result;
		}
		return result;
	}*/
}
