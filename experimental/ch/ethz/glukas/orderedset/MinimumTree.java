package ch.ethz.glukas.orderedset;

public class MinimumTree {

	
	public MinimumTree(int[]keys)
	{
		int leaves = keys.length;
		int size = 2*leaves-1;
		int height = BinaryMath.log(size);
		arrays = new Object[height+1];
		arrays[height] = keys;
	}
	
	
	public int[] arrayForDepth(int depth)
	{
		if (arrays[depth] == null) {
			buildDepth(depth);
		}
		return (int[])arrays[depth];
	}
	
	
	private void rebuildFromDepth(int depth)
	{
		if (arrays[depth+1] == null) {
			rebuildFromDepth(depth+1);
		}
		int[] predecessor = (int[])arrays[depth+1];
		
		int size = BinaryMath.powerOfTwo(depth);
		int[] newArray = new int[size];
		int j = 0;
		//copy entries
		for (int i=0; i<size; i++) {
			newArray[i] = predecessor[j];
			j+=2;
		}
		arrays[depth] = newArray;
	}
	
	private void buildDepth(int depth)
	{
		int predecessorsDepth = depth+1;
		while (arrays[predecessorsDepth] == null) {
			predecessorsDepth++;
		}
		int[] predecessor = (int[])arrays[predecessorsDepth];
		
		int size = BinaryMath.powerOfTwo(depth);
		int[] newArray = new int[size];
		int j = 0;
		int stepSize = BinaryMath.powerOfTwo(predecessorsDepth-depth);
		//copy entries
		for (int i=0; i<size; i++) {
			newArray[i] = predecessor[j];
			j+=stepSize;
		}
		arrays[depth] = newArray;
	}
	
	private Object[] arrays;
}
