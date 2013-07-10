import java.io.IOException;
import java.util.*;

import ch.ethz.glukas.orderedset.*;

class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		
		testPackedMemoryStructure();
		
		/*
		SetTests.testSet(new RandomizedBST<Integer>());
		OrderedSetTests.testNavigation(new RandomizedBST<Integer>());
		OrderedSetTests.testSortedSet(new RandomizedBST<Integer>());
		OrderedSetTests.testSubsets(new RandomizedBST<Integer>());
		RangeSetTest.testRangeSizes(new RandomizedBST<Integer>());
		
		
		OrderedSetTests.testIterator(new SplayTree<Integer>());
		OrderedSetTests.testIterator(new RandomizedBST<Integer>());
		
		
		RangeSetTest.testRangeSizes(new SplayTree<Integer>());
		OrderedSetTests.testNavigation(new SplayTree<Integer>());
		RangeSetTest.testPolling(new SplayTree<Integer>());
		OrderedSetTests.testSubsets(new SplayTree<Integer>());
		SetTests.testSet(new SplayTree<Integer>());
		SetTests.randomizedTestSet(new SplayTree<Integer>(), 1000);*/
		
		/*
		int performanceTestSize = 1000000;
		
		
		performanceTestSet(new SplayTree<Integer>(), performanceTestSize);
		performanceTestSet(new TreeSet<Integer>(), performanceTestSize);
		
		performanceTestRepeatedAccess(new SplayTree<Integer>(), performanceTestSize);
		performanceTestRepeatedAccess(new TreeSet<Integer>(), performanceTestSize);
		//testImmutableSet();
		//performanceTestImmutableSet();
		
		//performanceTestNavigableSet(new RandomizedBST<Integer>(), performanceTestSize);
		performanceTestNavigableSet(new SplayTree<Integer>(), performanceTestSize);
		performanceTestNavigableSet(new TreeSet<Integer>(), performanceTestSize);
		
		performanceTestSet(new RandomizedBST<Integer>(), performanceTestSize);
		performanceTestSet(new Treap<Integer>(), performanceTestSize);
		performanceTestSet(new SplayTree<Integer>(), performanceTestSize);
		performanceTestSet(new TreeSet<Integer>(), performanceTestSize);*/
		//performanceTestSet(new HashSet<Integer>(), performanceTestSize);
	}
	

	
	static void performanceTestSet(Set<Integer> set, int testSize)
	{
		int testRange = testSize/5;
		Random random = new Random(2);
		Date start = new Date();
		
		
		for (int i=0; i<testSize; i++) {
			set.add(i);
			set.contains(i);
		}
		for (int i=0; i<testSize; i++) {
			set.remove(i);
			set.contains(i);
		}
		
		
		for (int i=0; i< testSize; i++) {
			int nextOperation = random.nextInt(10);
			int nextNumber = random.nextInt(testRange);
			if (nextOperation < 5) {
				set.add(nextNumber);
			} else if (nextOperation < 7) {
				set.remove(nextNumber);
			} else {
				set.contains(nextNumber);
			}
			
		}
		Date end = new Date();
		System.out.println("randomized performance test set done!" + " took " + (end.getTime()-start.getTime())/ 1000.0 + " s");
	}

	
	static void performanceTestRepeatedAccess(Set<Integer> set, int testSize)
	{
		SetTests.sequenceAdd(set, testSize);
		
		Date start = new Date();
		for (int i=0; i<testSize; i++) {
			for (int j=0; j<30; j++) {
				set.contains(i);
			}
			for (int j=0; j<10; j++) {
				set.contains(i-1);
			}
		}
		Date end = new Date();
		System.out.println("performance test repeated access set done!" + " took " + (end.getTime()-start.getTime())/ 1000.0 + " s");
	}
	
	static void performanceTestNavigableSet(NavigableSet<Integer> set, int testSize)
	{
		SetTests.sequenceAdd(set, testSize/2);
		SetTests.randomAdd(set, testSize/2, testSize);
		
		Date start = new Date();
		for (int i=-testSize/4; i<1.2*testSize; i++) {
			set.floor(i);
			set.ceiling(i);
			set.higher(i);
			set.lower(i);
		}
		Date end = new Date();
		System.out.println("test navigation done!" + " took " + (end.getTime()-start.getTime())/ 1000.0 + " s");
	}
	
	
	static void testPackedMemoryStructure()
	{
		PackedMemoryStructure pma = new PackedMemoryStructure();
		Date start = new Date();
		/*for (int i=1; i<1000; i++) {
			pma.insert(i);
			if (!pma.contains(i)) throw new Error();
		}*/
		pma = new PackedMemoryStructure();
		Random rand = new Random(2);
		for (int i=0; i<1000; i++) {
			int next = rand.nextInt();
			if (next == 0) continue;
			pma.insert(next);
			if (!pma.contains(next)) throw new Error();
		}
		
		
		Date end = new Date();
		System.out.println("test pma done!" + " took " + (end.getTime()-start.getTime())/ 1000.0 + " s");
		
	}
	
	/*
	static void performanceTestImmutableSet()
	{
		TreeSet<Integer> control = new TreeSet<Integer>();
		int testMagnitude = 4;
		int testSize = (int)Math.pow(2, Math.pow(2, testMagnitude))/2;
		
		int[] input = new int[testSize];
		int testRange = 2*testSize;
		Random random = new Random();
		
		for (int i=0; i<testSize; i++) {
			input[i] = random.nextInt(testRange);
			control.add(input[i]);
		}
		
		
		Date cstart = new Date();
		for (int j=0; j<400; j++) {
			for (int i=0; i<testRange; i++) {
				control.contains(i);
			}
		}
		Date cend = new Date();
		System.out.println("test control set done!" + " took " + (cend.getTime()-cstart.getTime())/ 1000.0 + " s");
		
		
		ImmutableOrderedSet corona = new ImmutableOrderedSet(input);
		Date start = new Date();
		for (int j=0; j<400; j++) {
			for (int i=0; i<testRange; i++) {
				corona.contains(i);
			}
		}
		Date end = new Date();
		System.out.println("test immutable set done!" + " took " + (end.getTime()-start.getTime())/ 1000.0 + " s");
		

	}
	
	static void testImmutableSet()
	{
		TreeSet<Integer> control = new TreeSet<Integer>();
		int testMagnitude = 2;
		int testSize = (int)Math.pow(2, Math.pow(2, testMagnitude))/2;
		int testRange = testSize*5;
		Random random = new Random(1);
		int[] input = new int[testSize];
		
		for (int i=0; i<testSize; i++) {
			input[i] = random.nextInt(testRange);
			control.add(input[i]);
		}
		
		ImmutableOrderedSet corona = new ImmutableOrderedSet(input);
		for (int i=0; i<testRange; i++) {
			if (corona.contains(i) != control.contains(i)) throw new Error();
		}
	}
	*/

}
