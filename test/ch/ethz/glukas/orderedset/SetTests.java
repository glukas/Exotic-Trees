package ch.ethz.glukas.orderedset;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class SetTests {

	
	//tests add, contains, remove and size
	//uses a proven java.util set as golden model
	public static void randomTestSet(Set<Integer> set)
	{
		
		Set<Integer> controlSet = new HashSet<Integer>();
		
		int testSize = 500;
		int testRange = testSize/5;
		Random random = new Random(9);
		
		for (int i=0; i< testSize; i++) {
			int nextOperation = random.nextInt(3);
			int nextNumber = random.nextInt(testRange);
			if (nextOperation > 0) {
				controlSet.add(nextNumber);
				set.add(nextNumber);
			} else {
				controlSet.remove(nextNumber);
				set.remove(nextNumber);
			}
		}
		
		SetTests.assertEqualSets(set, controlSet);
		System.out.println("OrderedSetTests: randomTestSet done.");
	}
	
	public static void testSet(Set<Integer> set)
	{
		Set<Integer> controlSet = new HashSet<Integer>();//golden model
		
		int testSize = 300;
		
		//sequence test
		for (int i=0; i<testSize; i++) {
			assertTrue (set.add(i) == controlSet.add(i)) ;
			assertTrue (set.add(i) == controlSet.add(i)) ;
			assertEqualSets(set, controlSet);
		}
		
		//randomized test
		Random random = new Random(5);
		for (int i=0; i<testSize; i++) {
			int next = random.nextInt();
			assertTrue(set.add(next) == controlSet.add(next));
			assertEqualSets(set, controlSet);
		}
		
		//removal test
		for (int i=0; i<testSize; i++) {
			assertTrue(set.remove(i) == controlSet.remove(i));
			assertEqualSets(set, controlSet);
		}
		
		set.clear();
		controlSet.clear();
		
		assertTrue (set.size() == 0) ;
		System.out.println("SetTests: testSet done.");
	}
	
	public static void randomizedTestSet(Set<Integer> set, int testSize)
	{
		int testRange = testSize/5;
		Random random = new Random(2);
		Set<Integer> control = new HashSet<Integer>();//golden model
		
		for (int i=0; i< testSize; i++) {
			int nextOperation = random.nextInt(10);
			int nextNumber = random.nextInt(testRange);
			if (nextOperation < 5) {
				assert(set.add(nextNumber) == control.add(nextNumber));
			} else if (nextOperation < 7) {
				assert(set.remove(nextNumber) == control.remove(nextNumber));
			} else {
				assert(set.contains(nextNumber) == control.contains(nextNumber));
			}
		}
	}

	//basic test that can be used for early implementation testing
	//tests add and contains, also makes sure that size is 0 after clear
	public static void testAddAndContains(Set<Integer> set)
	{
		Set<Integer> controlSet = new HashSet<Integer>();//golden model
		
		int testSize = 310;

		sequenceAdd(set, controlSet, testSize);
		assertEqualSets(set, controlSet);
		set.clear();
		controlSet.clear();
		assertTrue (set.size() == 0) ;
		randomAdd(set, controlSet, testSize);
		assertEqualSets(set, controlSet);
		
		System.out.println("SetTests: testAddAndContains done.");
	}
	
	
	////
	//HELPERS
	////
	
	public static void sequenceAdd(Set<Integer> set1, int testSize)
	{
		sequenceAdd(set1, null, testSize);
	}
	
	public static void sequenceAdd(Set<Integer> set1, Set<Integer> set2, int testSize)
	{
		for (int i=0; i<testSize; i++) {
			set1.add(i);
			if (set2 != null) set2.add(i);
		}
	}
	
	
	public static void randomAdd(Set<Integer> set, int testSize, int upperbound)
	{
		randomAdd(set, null, testSize, upperbound);
	}
	
	
	//second parameter may be null
	public static void randomAdd(Set<Integer> set1, Set<Integer> set2, int testSize, int upperbound)
	{
		Random rand = new Random(testSize);
		for (int i=0; i<testSize; i++) {
			int next = rand.nextInt(upperbound);
			set1.add(next);
			if (set2 != null) set2.add(next);
		}
	}
	
	
	public static void randomAdd(Set<Integer> set1, Set<Integer> set2, int testSize)
	{
		randomAdd(set1, set2, testSize, Integer.MAX_VALUE);
	}
	
	public static void assertEqualSets(Set<Integer> set, Set<Integer> control)
	{
		assertTrue(set.size() == control.size());
		for (Integer value : control) {
			assertTrue(set.contains(value));
		}
	}
}
