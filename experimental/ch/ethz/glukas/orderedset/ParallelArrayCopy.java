package ch.ethz.glukas.orderedset;

import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

public class ParallelArrayCopy extends RecursiveAction {

	
	public ParallelArrayCopy(int[]targetArray, int targetIndex, int[]sourceArray, int sourceIndex, int sequenceLength)
	{
		assert sequenceLength > 0;
		
		this.target = targetArray;
		this.source = sourceArray;
		this.toIndex = targetIndex;
		this.fromIndex = sourceIndex;
		this.sequenceLength = sequenceLength;
	}
	
	@Override
	protected void compute() {
		System.arraycopy(source, fromIndex, target, toIndex, sequenceLength);
		/*int currentDestinationIndex = toIndex;
		int currentSourceIndex = fromIndex;
		
		for (int i=0; i<sequenceLength; i++) {
			target[currentDestinationIndex] = source[currentSourceIndex];
			currentDestinationIndex++;
			currentSourceIndex++;
		}*/
	}
	
	
	private int[] target;
	private int toIndex;
	private int fromIndex;
	private int[] source;
	private int sequenceLength;
}
