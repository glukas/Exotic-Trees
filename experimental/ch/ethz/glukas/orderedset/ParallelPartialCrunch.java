package ch.ethz.glukas.orderedset;

import java.util.concurrent.RecursiveTask;

public class ParallelPartialCrunch extends RecursiveTask<Integer> {

	public ParallelPartialCrunch(int[] keys, int firstSectionIndex, int sectionSize, int numberOfSections)
	{
		keysArray = keys;
		indexOfFirstSection = firstSectionIndex;
		numSections = numberOfSections;
		sizeOfSections = sectionSize;
	}

	@Override
	protected Integer compute() {
		int result = crunch(indexOfFirstSection, numSections);
		return result;
	}
	
	//copies all the elements to the left to form one coherent block
	//returns the number of elements in the block
	//the remaining part of the modified chunk has arbitrary values (the unused portions are not cleaned up)
	private int crunch(int indexOfFirstSection, int numberOfSections)
	{
		int currentDestinationIndex = indexOfFirstSection;
		int sectionIndex = indexOfFirstSection;
		
		for (int i=0; i<numberOfSections; i++) {
			currentDestinationIndex = crunchSection(sectionIndex, currentDestinationIndex);
			sectionIndex += sizeOfSections;
		}
		return currentDestinationIndex-indexOfFirstSection;
	}
	
	//cuts&copies a section to the destination index
	private int crunchSection(int sectionIndex, int destinationIndex)
	{
		int i=0;
		for(; i < sizeOfSections && keysArray[sectionIndex+i] != 0; i++) {
			keysArray[destinationIndex+i] = keysArray[sectionIndex+i];
		}
		return destinationIndex+i;
	}
	
	int[] keysArray;
	int indexOfFirstSection;
	int sizeOfSections;
	int numSections;
	
}
