package ch.ethz.glukas.orderedset;

import java.util.ArrayList;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class ParallelCruncher extends RecursiveTask<Integer> {
	

	
	
	public ParallelCruncher (int[] keys, int numberOfSections, int indexOfFirstSection, int sectionSize, int numberOfSectionsPerChunk)
	{
		assert BinaryMath.isPowerOfTwo(numberOfSections);
		assert numberOfSectionsPerChunk <= numberOfSections;
		
		this.numberOfSections = numberOfSections;
		this.indexOfFirstSection= indexOfFirstSection;
		this.sectionSize = sectionSize;
		this.numberOfSectionsPerChunk = numberOfSectionsPerChunk;
		this.keys = keys;
	}
	
	@Override
	protected Integer compute() {
		
		int numberOfTasks = numberOfSections/numberOfSectionsPerChunk;
		int chunkSize = numberOfSectionsPerChunk*sectionSize;
		int currentSourceSectionIndex = indexOfFirstSection;
		
		//crunch each chunk in parallel (copying to a buffer array)
		ArrayList<ParallelSubarrayCut> tasks = new ArrayList<ParallelSubarrayCut>(numberOfTasks);
		for (int i=0; i<numberOfTasks; i++) {
			ParallelSubarrayCut task = new ParallelSubarrayCut(keys, currentSourceSectionIndex, currentSourceSectionIndex+chunkSize);
			task.fork();
			tasks.add(task);
			currentSourceSectionIndex += chunkSize;
		}
		
		int[][] buffers = new int[numberOfTasks][chunkSize];
		for (int i=0; i<numberOfTasks; i++) {
			buffers[i] = tasks.get(i).join();
		}
		
		//copy the keys from the buffers back to the main array (in parallel)
		ArrayList<ForkJoinTask<Void>> kmtasks = new ArrayList<ForkJoinTask<Void>>(numberOfTasks);
		int currentTargetIndex = indexOfFirstSection;
		for (int i=0; i<numberOfTasks; i++) {
			int nonzeroEntries = tasks.get(i).numberOfNonzeroEntries;
			assert buffers[i][0] != 0;
			ParallelArrayCopy task = new ParallelArrayCopy(keys, currentTargetIndex, buffers[i], 0, nonzeroEntries);
			kmtasks.add(task.fork());
			currentTargetIndex += nonzeroEntries;
		}
		//wait for completion
		for (ForkJoinTask<Void> task : kmtasks) {
			task.join();
		}
		return currentTargetIndex-indexOfFirstSection;
	}
	
	
	private int indexOfFirstSection;
	private int sectionSize;
	private int numberOfSections;
	private int numberOfSectionsPerChunk;
	private int[] keys;
	private int[] usedSlotsPerSection;
}
