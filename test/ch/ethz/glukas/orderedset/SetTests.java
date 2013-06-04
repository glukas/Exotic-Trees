package ch.ethz.glukas.orderedset;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class SetTests {

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
	
	public static void sequenceAdd(Set<Integer> set1, Set<Integer> set2, int testSize)
	{
		for (int i=0; i<testSize; i++) {
			set1.add(i);
			set2.add(i);
		}
	}
	
	public static void randomAdd(Set<Integer> set1, Set<Integer> set2, int testSize, int upperbound)
	{
		Random rand = new Random(testSize);
		for (int i=0; i<testSize; i++) {
			int next = rand.nextInt(upperbound);
			set1.add(next);
			set2.add(next);
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
