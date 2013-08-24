package ch.ethz.glukas.orderedset;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;

public class ImmutableOrderedSet implements StaticSearchTree {
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
			mainPool.invoke(new ParallelTreeBuilder(0, treeHeight, content, 0, tree));
		} else {
			//create a dummy tree that always returns index 0
			tree = new int[1];
			tree[0] = Integer.MAX_VALUE;
		}
	}

	public boolean contains(int key)
	{	
		//return simpleContains(key);
		//find the index where the key should reside
		int idx = find(key, 0, treeHeight);
		boolean result = internalKeys[idx] == key;
		
		assert internalKeys[idx] == key || ((idx == 0 || internalKeys[idx-1] < key) && (idx == internalKeys.length-1 || internalKeys[idx+1] > key));
		assert simpleContains(key) == result;
		
		return result;
	}
	
	
	//Asymptotics:
	
	//T(K^2) = T(K) + O(1) = O(log(K)) (note that if we divide a tree of size K^2 in half by height, the resulting subtrees have size K)
	//returns the index of the leaf the search should continue with (with respect to the parent tree)
	private int find(int key, int rootIndex, int height)
	{
		//base cases
		//large base cases are needed for performance reasons
		if (height <= baseCaseSize) {
			return baseCaseFind(key, rootIndex, height);
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
		assert topindex < 2*numberOfLeavesForHeight(topTreeHeight);
		
		
		//recurse on one of the bottom trees (the topindex-th bottom tree)
		int bottomFoundIndex = find(key, rootIndex+topTreeSize+topindex*bottomTreeSize, bottomTreeHeight);
		//check if result is within range
		assert bottomFoundIndex >= 0;
		assert bottomFoundIndex < 2*numberOfLeavesForHeight(bottomTreeHeight);
		
		//the index the search should continue with is the sum of the index relative to the bottom tree plus 2*the numberOfLeaves in a bottom tree * the number of trees to the left of the bottom tree
		return bottomFoundIndex+(topindex*BinaryMath.powerOfTwo(bottomTreeHeight));//(2^bottomTreeHeight) == 2*numberOfLeavesForHeight(bottomTreeHeight)
	}
	
	//MT(N) = O(N/B)
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
	
	
	protected void rebuild(int fromIndex, int[] newKeys)
	{
		partialRebuildUpwards(0, treeHeight, newKeys, 0, fromIndex, fromIndex+newKeys.length-1);
	}
	
	
	//returns the new minimum of the subtree
	//if the tree minimum didn't change, the value is undefined
	//possible simplification: encapsulate subtreeMinima and subtreeMinimaStartIndex into a single object that behaves like a simple array (the startIndex is mutable)
	private int partialRebuildUpwards(int rootIndex, int height, int[] subtreeMinima, int subtreeMinimaStartIndex, int indexOfFirstModifiedSubtree, int indexOfLastModifiedSubtree)
	{
		assert indexOfFirstModifiedSubtree < 2*numberOfLeavesForHeight(height);
		assert indexOfLastModifiedSubtree < 2*numberOfLeavesForHeight(height);
		
		if (height <= 7) {
			copySomeEntries(subtreeMinima, tree, subtreeMinimaStartIndex+1, rootIndex+indexOfFirstModifiedSubtree, 1, 1, indexOfLastModifiedSubtree-indexOfFirstModifiedSubtree);
			//the leftmost entry is the minimum overall if the minimum changed (indexOfFirstModifiedSubtree==0)
			return subtreeMinima[subtreeMinimaStartIndex];
		}
		
		//subtree properties
		int topTreeHeight = height/2;
		int bottomTreeHeight =  height-topTreeHeight;
		
		int topTreeSize = numberOfNodesForHeight(topTreeHeight);
		int bottomTreeSize = numberOfNodesForHeight(bottomTreeHeight);
		
		//int numberOfChildrenOfTheTopTree = numberOfLeavesForHeight(topTreeHeight+1);//==topTreeSize+1;
		int numberOfChildrenOfEachBottomTree = numberOfLeavesForHeight(bottomTreeHeight+1);//==bottomTreeSize+1
		
		//find out which bottom trees are affected and update those recursively
		int firstAffectedBottomTree = indexOfFirstModifiedSubtree/numberOfChildrenOfEachBottomTree;
		int lastAffectedBottomTree = indexOfLastModifiedSubtree/numberOfChildrenOfEachBottomTree;
		
		//TODO: if the first affected tree is not fully affected, it doesn't propagate the minimum, as it did not change
		int numberOfAffectedBottomTrees = lastAffectedBottomTree-firstAffectedBottomTree+1;
		int[] bottomTreeMinima = new int[numberOfAffectedBottomTrees];
		
		
		//TODO: special calculation for the first and last trees (or find general formula)
		int indexOfFirstAffectedSubtreeInTheFirstAffectedBottomTree;
		int indexOfLastAfffectedSubtreeInTheLastAffectedBottomTree;
		
		int currentIndex = rootIndex+topTreeSize+bottomTreeSize*firstAffectedBottomTree;
		for (int i=0; i<numberOfAffectedBottomTrees; i++) {
			bottomTreeMinima[i] = partialRebuildUpwards(currentIndex, bottomTreeHeight, subtreeMinima, subtreeMinimaStartIndex, 0, numberOfChildrenOfEachBottomTree-1);
			currentIndex += bottomTreeSize;
			subtreeMinimaStartIndex += numberOfChildrenOfEachBottomTree;
		}
		
		
		//recursively build the top tree and return the minimum overall (if defined)
		return partialRebuildUpwards(rootIndex, topTreeHeight, bottomTreeMinima, 0, firstAffectedBottomTree, lastAffectedBottomTree);
	}
	
	
	
	private int baseCasePartialRebuild(int rootIndex, int[] subtreeMinima, int offset, int indexOfFirstModifiedSubtree, int numberOfModifiedSubtrees)
	{
		copySomeEntries(subtreeMinima, tree, offset+1, rootIndex+indexOfFirstModifiedSubtree, 1, 1, numberOfModifiedSubtrees);
		//the leftmost entry is the minimum overall if the minimum changed (indexOfFirstModifiedSubtree==0)
		return subtreeMinima[offset];
	}
	
	private static int numberOfLeavesForHeight(int height)
	{
		return BinaryMath.powerOfTwo(height-1);
	}
	
	private static int numberOfNodesForHeight(int height)
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
	
	static final ForkJoinPool mainPool = new ForkJoinPool();
	
	private final int[] internalKeys;
	private final int[] tree;
	private final int treeHeight;
	private static final int baseCaseSize = 7;
	
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
	
	
	///
	///ALTERNATIVE ALGORITHMS
	///
	
	/*
	//O(N/B) runtime
	private int[] rebuildBFMinimumTree(int[] keys)
	{
		int leaves = keys.length;
		int size = 2*(leaves)-1;	
		
		int[] result = new int[size];
		
		int height = BinaryMath.log(size);
		//copy last level
		
		int targetIndex = BinaryMath.powerOfTwo(height)-1;
		copySomeEntries(keys, result, 0, targetIndex, 1, 1, leaves);
		//copy previous levels
		for (int depth = height-1; depth >= 0; depth--)
		{
			int sourceIndex = targetIndex;
			targetIndex = BinaryMath.powerOfTwo(depth) - 1;
			copySomeEntries(result, result, sourceIndex, targetIndex, 2, 1, BinaryMath.powerOfTwo(depth));
		}
		
		assert (keys[0] == result[0]);
		return result;
	}*/
	
	/*
	private void rebuildUpwards(int rootIndex, int height, MinimumTree BFMinimumTree, int BFDepth, int BFOffset)
	{
		if (height <= baseCaseSize) {
			copySomeEntries(BFMinimumTree.arrayForDepth(BFDepth+height), tree, (BFOffset<<height)+1, rootIndex, 1, 1, numberOfNodesForHeight(height));
			return;
		}
		
		//subtree properties
		int topTreeHeight = height/2;
		int bottomTreeHeight =  height-topTreeHeight;
		
		int topTreeSize = numberOfNodesForHeight(topTreeHeight);
		int bottomTreeSize = numberOfNodesForHeight(bottomTreeHeight);
		
		int numberOfChildrenOfTheTopTree = 2*numberOfLeavesForHeight(topTreeHeight);//==topTreeSize+1;

		int bottomDepth = BFDepth+topTreeHeight;
		int bottomOffset = BFOffset<<topTreeHeight;
		
		//recursively build the bottom trees
		int currentIndex = rootIndex+topTreeSize;
		
		for (int i=0; i<numberOfChildrenOfTheTopTree; i++) {
			rebuildUpwards(currentIndex, bottomTreeHeight, BFMinimumTree, bottomDepth, bottomOffset);
			currentIndex += bottomTreeSize;
			bottomOffset++;
		}
		
		//recursively build the top tree
		rebuildUpwards(rootIndex, topTreeHeight, BFMinimumTree, BFDepth, BFOffset);
	}*/
	
	
	

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
	
	/*
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
		
		int numberOfChildrenOfTheTopTree = 2*numberOfLeavesForHeight(topTreeHeight);//==topTreeSize+1;
		int numberOfChildrenOfEachBottomTree = 2*numberOfLeavesForHeight(bottomTreeHeight);//==bottomTreeSize+1
		
		//recursively build the bottom trees and store the minimum elements of those subtrees
		int [] bottomTreeMinima = new int[numberOfChildrenOfTheTopTree];
		
		int currentIndex = rootIndex+topTreeSize;
		for (int i=0; i<numberOfChildrenOfTheTopTree; i++) {
			bottomTreeMinima[i] = rebuildUpwards(currentIndex, bottomTreeHeight, subtreeMinima, offset);
			currentIndex += bottomTreeSize;
			offset += numberOfChildrenOfEachBottomTree;
		}
		
		//recursively build the top tree and return the minimum overall
		return rebuildUpwards(rootIndex, topTreeHeight, bottomTreeMinima, 0);
	}
	
	
	
	
	//stores the index subtree in order starting at the rootIndex.
	//the offset refers to the first index in the arrays that is relevant
	//the height determines the size of the subtree to be constructed
	//the method returns the minimum of the whole subtree rooted at 'rootIndex' (this is the entry of the subtreeMinima array at index 'offset)
	//TODO: generalize to handle partial updates
	private int baseCaseRebuild(int rootIndex, int height, int[] subtreeMinima, int offset)
	{
		copySomeEntries(subtreeMinima, tree, offset+1, rootIndex, 1, 1, numberOfNodesForHeight(height));
		//the leftmost entry is the minimum overall
		return subtreeMinima[offset];
	}*/
	
}
