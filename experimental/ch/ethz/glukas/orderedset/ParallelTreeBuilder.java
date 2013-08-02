package ch.ethz.glukas.orderedset;
import java.util.ArrayList;
import java.util.concurrent.*;

class ParallelTreeBuilder extends RecursiveTask<Integer>{

	
	public ParallelTreeBuilder (int arootIndex, int aheight, int[] asubtreeMinima, int aoffset, int[] atree)
	{
		rootIndex = arootIndex;
		offset = aoffset;
		subtreeMinima = asubtreeMinima;
		height = aheight;
		tree = atree;
	}
	
	@Override
	//span: S(K^2) = 2*S(K) + K = O(K)
	//work: W(K^2) = (K+1)W(K) + K = O(K^2)
	protected Integer compute() {
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
		ArrayList<ParallelTreeBuilder> tasks = new ArrayList<ParallelTreeBuilder>(numberOfChildrenOfTheTopTree);
		
		int currentIndex = rootIndex+topTreeSize;
		for (int i=0; i<numberOfChildrenOfTheTopTree; i++) {
			tasks.add(new ParallelTreeBuilder(currentIndex, bottomTreeHeight, subtreeMinima, offset, tree));
			tasks.get(i).fork();
			currentIndex += bottomTreeSize;
			offset += numberOfChildrenOfEachBottomTree;
		}
		for (int i=0; i<numberOfChildrenOfTheTopTree; i++) {
			bottomTreeMinima[i] = (Integer)tasks.get(i).join();
		}
		
		//recursively build the top tree and return the minimum overall
		ParallelTreeBuilder top = new ParallelTreeBuilder(rootIndex, topTreeHeight, bottomTreeMinima, 0, tree);
		return (Integer)top.compute();
	}
	
	private int baseCaseRebuild(int rootIndex, int height, int[] subtreeMinima, int offset)
	{
		System.arraycopy(subtreeMinima, offset+1, tree, rootIndex, numberOfNodesForHeight(height));
		//the leftmost entry is the minimum overall
		return subtreeMinima[offset];
	}
	
	int rootIndex;
	int offset;
	int[] subtreeMinima;
	int height;
	int[] tree;
	
	private static int numberOfLeavesForHeight(int height)
	{
		return BinaryMath.powerOfTwo(height-1);
	}
	
	private static int numberOfNodesForHeight(int height)
	{
		return BinaryMath.powerOfTwo(height)-1;
	}
	
	private static void copySomeEntries(int[]source, int[]target, int sourceIndex, int targetIndex, int fromStepSize, int toStepSize, int numberOfSteps)
	{
		for (int i=0; i<numberOfSteps; i++) {
			target[targetIndex] = source[sourceIndex];
			sourceIndex += fromStepSize;
			targetIndex += toStepSize;
		}
	}

}
