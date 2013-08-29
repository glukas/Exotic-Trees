import java.io.IOException;
import java.util.*;

import ch.ethz.glukas.orderedmap.TrieTest;
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
		
		//TrieTest.basicTestTrie();
		//ImmutableOrderedSetTest.testImmutableSet();
		
		//CocoTreeTest.testUpdates();
		//CocoTreeTest.testImmutableSet();
		
		//COBTreeTest.testPackedMemoryStructure();
		
		performanceTestCOBTree(25);
		
		//performanceTestFixedSizeCoSearchTree(26);
		//performanceTestImmutableSet(25);
		
		//performanceTestSet(new TreeSet<Integer>(), BinaryMath.powerOfTwo(20));
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
		int testRange = testSize;
		Random random = new Random(2);
		Date start = new Date();
		for (int i=0; i<testSize; i++) {
			set.add(i);
		}
		Date end = new Date();
		System.out.println("performanceTestSet: in order insertion test done!" + " took " + (end.getTime()-start.getTime())/ 1000.0 + " s");
		
		set.clear();
		start = new Date();
		for (int i=0; i<testSize; i++) {
			set.add(random.nextInt(testRange));
		}
		end = new Date();
		System.out.println("performanceTestSet: random insertion test done!" + " took " + (end.getTime()-start.getTime())/ 1000.0 + " s");
		
		start = new Date();
		for (int i=0; i<testSize; i++) {
			set.contains(random.nextInt(testSize));
		}
		end = new Date();
		System.out.println("performanceTestSet: random search test done!" + " took " + (end.getTime()-start.getTime())/ 1000.0 + " s");
		
		/*
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
			
		}*/

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
	

	
	static void performanceTestCOBTree(int testMagnitude)
	{
		int testSize = (int)Math.pow(2, testMagnitude);

		int testRange = testSize;
		Random random = new Random(2);
		
		Date start = new Date();

		COBTree pma = new COBTree();
		//force worst case behaviour: sequential inserts
		for (int i=1; i<testSize; i++) {
			pma.insert(i);
		}
		Date end = new Date();
		System.out.println("in-order-building cobtree done!" + " took " + (end.getTime()-start.getTime())/ 1000.0 + " s");
		
		//best case insertion: random inserts
		start = new Date();
		pma = new COBTree();
		for (int i=1; i<testSize; i++) {
			pma.insert(random.nextInt(testRange));
		}
		end = new Date();
		System.out.println("randomly building cobtree done!" + " took " + (end.getTime()-start.getTime())/ 1000.0 + " s");
		
		//worst case searches: random searches
		start = new Date();
		for (int i=0; i<testSize; i++) {
			pma.contains(random.nextInt(testSize));
		}
		end = new Date();
		System.out.println("searching in cobtree done!" + " took " + (end.getTime()-start.getTime())/ 1000.0 + " s");
		
	}
	
	
	static void performanceTestFixedSizeCoSearchTree(int testMagnitude)
	{
		int testSize = (int)Math.pow(2, testMagnitude);
		
		Date start = new Date();
		int[] input = new int[testSize];
		
		int testRange = testSize;
		Random random = new Random(2);
		
		for (int i=0; i<testSize; i++) {
			input[i] = random.nextInt(testRange);
		}
		Arrays.sort(input);
		Date end = new Date();
		System.out.println("setting up immutable set done!" + " took " + (end.getTime()-start.getTime())/ 1000.0 + " s");
		
		start = new Date();
		StaticSearchTree uut = new CocoTree(input);
		end = new Date();
		
		System.out.println("building immutable set done!" + " took " + (end.getTime()-start.getTime())/ 1000.0 + " s");
		
		performanceTestStaticSearchTree(uut, testMagnitude);
		
	}
	
	static void performanceTestStaticSearchTree(StaticSearchTree uut, int testMagnitude)
	{
		int testSize = (int)Math.pow(2, testMagnitude);
		
		int testRange = testSize;
		Random random = new Random(2);

		Date start = new Date();
		for (int j=0; j<1; j++) {
			for (int i=0; i<testSize; i++) {
				uut.contains(random.nextInt(testRange));
			}
		}
		Date end = new Date();
		System.out.println("testing static set done!" + " took " + (end.getTime()-start.getTime())/ 1000.0 + " s");
		

	}
	
	static void performanceTestImmutableSet(int testMagnitude)
	{
		int testSize = (int)Math.pow(2, testMagnitude);
		
		Date start = new Date();
		int[] input = new int[testSize];
		
		int testRange = testSize;
		Random random = new Random(2);
		
		
		for (int i=0; i<testSize; i++) {
			input[i] = random.nextInt(testRange);
		}
		Arrays.sort(input);
		Date end = new Date();
		System.out.println("setting up immutable set done!" + " took " + (end.getTime()-start.getTime())/ 1000.0 + " s");
		
		start = new Date();
		ImmutableOrderedSet corona = new ImmutableOrderedSet(input);
		end = new Date();
		
		System.out.println("building immutable set done!" + " took " + (end.getTime()-start.getTime())/ 1000.0 + " s");
		
		performanceTestStaticSearchTree(corona, testMagnitude);
	}
	

}
