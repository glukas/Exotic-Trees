package ch.ethz.glukas.orderedset;

import java.util.Arrays;

public class ImmutableOrderedSet {
	//Cache-oblivious Corona tree
	//The memory layout is van Embde Boas
	//all values are stored in the leafs of the tree
	//this allows for simple recursive algorithms that don't involve the direct calculation of child indexes in the implicit tree array
	
	
	public ImmutableOrderedSet(int[] content)
	{
		rebuild(content);
	}

	public boolean contains(int value)
	{
		/*int index = Arrays.binarySearch(values, value);//simple binary search
		if (index >= 0) {
			return true;
		} else {
			return false;
		}*/
		find(value, 0, rootRank);
		return internalLastFound == value;
	}
	
	
	//precondition: the content length is of the form Math.pow(2, Math.pow(2, testMagnitude))/2;
	private void rebuild(int[] content)
	{
		values = Arrays.copyOf(content, content.length);
		Arrays.sort(values);
		rootRank = values.length*2;
		tree = new int[numberOfNodes(rootRank)];
		
		rebuildUpwards(0, rootRank, values, values, 0);
		assert treeConsistent();
	}
	
	
	//T(K^2) = KT(K) + O(1) = O(logK)
	//returns the index of the leaf the search ended in (with respect to the given subtree at rootIndex of size numberOfNodes(rank)
	//the value of this leaf is put into 'foundvalue'
	private int find(int value, int rootIndex, int rank)
	{
		
		if (rank == 16) {
			return baseCase16Find(value, rootIndex);
		}
		
		//subtree properties
		int subrank = subrank(rank);

		//top
		int topindex = find(value, rootIndex, subrank);
		assert topindex < numberOfLeaves(subrank);
		
		//bottom
		//decide where to continue the search
		int bottomTreeIndex =  2*topindex;
		if (value >= internalLastFound) {
			bottomTreeIndex++;
		}
		
		int bottomFoundIndex = find(value, bottomSubtreeIndex(rootIndex, subrank, bottomTreeIndex), subrank);
		
		return (bottomTreeIndex*numberOfLeaves(subrank))+bottomFoundIndex;
	}

	
	//T(K^2) = KT(K) + O(K) = O(K^2)
	//proof: assume P(K^2): T(K^2)<=c1*K^2-c2
	//we know T(K^2) = KT(K) + c0*K and T(1) = c3
	//P(1) holds for c1-c2>=c3
	//P(k^2) implies P(k^4):
	//T(k^4) = K^2T(k^2)+c0K <= K^2(c1K^2-c2)-c0*K = c1*K^4-c2*K^2+c0*K = c1*K^4-c2-(c2+c2*K^2-c0*K) <= c1*K^4-c2 for c2<=c0, k>=1
	
	//returns the minimum value in the subtree
	//lower denotes the first value of the minimumSubtreeValues array that are relevant for the current step
	//the end of the relevant part is implicit in the rank of the current subtree
	private int rebuildUpwards(int rootIndex, int rank, int[] leftMinimumSubtreeValues, int[] rightMinimumSubtreeValues, int offset)
	{
		//base case - singleton tree : write value at root index
		if (rank == 2) {
			tree[rootIndex] = rightMinimumSubtreeValues[offset];//keys are the min of the right subtree
			return Math.min(leftMinimumSubtreeValues[offset], rightMinimumSubtreeValues[offset]);//return the minimum of the whole subtree
		}
		
		//subtree properties
		int subrank = subrank(rank);
		int subcoronaSize = numberOfNodes(subrank);
		int subcoronaLeaves = numberOfLeaves(subrank);
		int subcoronas = 2*subcoronaLeaves;
		
		//bottom trees
		int [] leftBottomTreeMins = new int[subcoronaLeaves];
		int [] rightBottomTreeMins = new int[subcoronaLeaves];
		
		int currentIndex = rootIndex;
		for (int i=0; i<subcoronas; i+=2) {
			currentIndex += subcoronaSize;
			leftBottomTreeMins[i/2] = rebuildUpwards(currentIndex, subrank, leftMinimumSubtreeValues, rightMinimumSubtreeValues, offset+i*subcoronaLeaves);
			currentIndex += subcoronaSize;
			rightBottomTreeMins[i/2] = rebuildUpwards(currentIndex, subrank, leftMinimumSubtreeValues, rightMinimumSubtreeValues, offset+(i+1)*subcoronaLeaves);
		}
		
		//top tree: push up the values from the bottom trees
		//the key of a node is the smallest key of its right subtree
		rebuildUpwards(rootIndex, subrank, leftBottomTreeMins, rightBottomTreeMins, 0);
		
		return leftBottomTreeMins[0];//because the input is presorted, the min is always the leftmost entry
	}
	
	
	private void inOrder(int rootIndex, int rank, int[]output, int outputIndex)
	{
		
		//base case - singleton tree : write value to output array
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
	}
	
	
	//a base case of this size is needed for performance reasons
	//a two level search is performed
	private int baseCase16Find(int value, int rootIndex)
	{
		int current = rootIndex+3;
		int i=0;
		while(i < 3) {
			if (tree[current]>=value) break;
			current+=3;
			i++;
		}
		i = 2*i;
		current++;
		if (tree[current] < value) {
			current++;
			i++;
		}
		internalLastFound = tree[current];
		return i;
	}
	
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
	}
	
	private int[] inOrder()
	{
		int[] traversal = new int[numberOfLeaves(rootRank)];
		inOrder(0, rootRank, traversal, 0);
		return traversal;
	}

	private int subrank(int rank)
	{
		//square root operations are very expensive, use a small lookup table
		if (rank == 256) return 16;
		if (rank == 65536) return 256;
		return (int)Math.sqrt(rank);
	}
	
	private int numberOfLeaves(int rank)
	{
		return rank/2;
	}
	
	private int numberOfNodes(int rank)
	{
		return rank-1;
	}
	
	private int bottomSubtreeIndex(int parentRootIndex, int rank, int i)
	{
		return parentRootIndex+(i+1)*numberOfNodes(rank);
	}
	
	
	
	private int[] values;//for debug purposes
	private int[] tree;
	
	//The rank is the number of nodes + 1 or equivalently twice the number of leaves
	//the rank of a sub-corona is 4 times smaller
	private int rootRank;
	
	//used internally by the find method
	private int internalLastFound;
	
	////
	//INVARIANTS & ASSERTIONS
	////
	
	private boolean treeConsistent()
	{
		int[] traversal = inOrder();
		boolean result = true;
		for (int i=0; i<values.length; i++) {
			result = result && values[i]==traversal[i];
			assert result;
		}
		return result;
	}
}
