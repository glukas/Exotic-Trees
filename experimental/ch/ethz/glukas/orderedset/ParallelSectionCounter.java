package ch.ethz.glukas.orderedset;

import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class ParallelSectionCounter extends RecursiveTask<Integer> {

	
	public ParallelSectionCounter(int[] array, int sectionSize, int fromSection, int toSection)
	{
		_keys = array;
		_sectionSize = sectionSize;
		_fromSection = fromSection;
		_toSection = toSection;
	}
	
	
	private static final int baseCaseNumberOfSections = BinaryMath.powerOfTwo(6);
	
	@Override
	protected Integer compute() {
		
		if (_toSection-_fromSection<=baseCaseNumberOfSections) {
			return numberOfUsedSlots();
		}
		
		int half = (_toSection-_fromSection+1)/2;
		
		ForkJoinTask<Integer> right = new ParallelSectionCounter(_keys, _sectionSize, _fromSection+half, _toSection).fork();
		Integer leftHalf = new ParallelSectionCounter(_keys, _sectionSize, _fromSection, _toSection-half).compute();
		Integer rightHalf = right.join();

		return leftHalf+rightHalf;
	}
	
	
	private int numberOfUsedSlots()
	{
		int count = 0;
		int currentSectionIndex = _fromSection*_sectionSize;
		for (int i=_fromSection; i<=_toSection; i++) {
			count += numberOfUsedSlotsInSectionAtIndex(currentSectionIndex);
			currentSectionIndex += _sectionSize;
		}
		return count;
	}
	
	private int numberOfUsedSlotsInSectionAtIndex(int index)
	{
		if (_keys[index+_sectionSize-1] != 0) return _sectionSize;
		int count = 0;
		if (_keys[index+_sectionSize/2] != 0) {//perform one step of binary search
			index+=_sectionSize/2;
			count+=_sectionSize/2;
		}
		while (_keys[index] != 0) {//scan rightwards until finding a null element
			index++;
			count++;
		}
		return count;
	}
	
	private int[] _keys;
	private int _sectionSize;
	private int _fromSection;
	private int _toSection;
	
}
