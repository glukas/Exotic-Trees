package ch.ethz.glukas.orderedset;

import java.util.Arrays;
import java.util.concurrent.RecursiveTask;

class ParallelSubarrayCut extends RecursiveTask<int[]> {
	//copies all nonzero entries in the specified range to a new array
	//the source section is zeroed out
	//the numberOfNonzeroEntries is valid when it isDone
	
	//start index inclusive, end index exclusive
	public ParallelSubarrayCut(int[]source, int startIndex, int endIndex)
	{
		sourceArray = source;
		from = startIndex;
		to = endIndex;
	}
	
	@Override
	protected int[] compute() {
		int[] result = new int[to-from];
		int count = 0;
		for (int i=from; i<to; i++) {
			int value = sourceArray[i];
			if (value != 0) {
				result[count] = value;
				count++;
			}
			sourceArray[i] = 0;
		}
		numberOfNonzeroEntries = count;
		//int[] result = Arrays.copyOfRange(sourceArray, from, to);
		//Arrays.fill(sourceArray, from, to, 0);
		return result;
	}
	
	private int to;
	private int from;
	private int[] sourceArray;
	public int numberOfNonzeroEntries;//only valid if isdone
}
