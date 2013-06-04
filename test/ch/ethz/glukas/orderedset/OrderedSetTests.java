package ch.ethz.glukas.orderedset;
import java.util.Date;
import java.util.HashSet;
import java.util.NavigableSet;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.Assert.*;

import org.junit.Test;

public class OrderedSetTests {


	
	public static void testSubsets(NavigableSet<Integer> set)
	{
		TreeSet<Integer> control = new TreeSet<Integer>();
		int testSize = 200;
		SetTests.randomAdd(set, control, testSize);
		
		for (int i=1; i<testSize; i++) {
			assertSubsetsEqual(set, control, 0, i);
		}
		System.out.println("OrderedSetTests: testSubsets done.");
	}
	
	private static void assertSubsetsEqual(NavigableSet<Integer> set, NavigableSet<Integer> control, int lower, int upper)
	{
		SortedSet<Integer> subset = set.subSet(lower, upper);
		SortedSet<Integer> controlSubset = control.subSet(lower, upper);
		SetTests.assertEqualSets(subset, controlSubset);
	}
	
	
	public static void testPolling(NavigableSet<Integer> set)
	{
		TreeSet<Integer> control = new TreeSet<Integer>();
		int testSize = 100;
		SetTests.randomAdd(set, control, testSize);
		while (set.size() >= 2) {
			assertEquals(set.pollFirst(), control.pollFirst());
			assertEquals(set.pollLast(), control.pollLast());
		}
		System.out.println("OrderedSetTests: testPolling done.");
	}
	
	public static void testNavigation(NavigableSet<Integer> set)
	{
		TreeSet<Integer> control = new TreeSet<Integer>();
		int testSize = 100;
		SetTests.sequenceAdd(set, control, testSize);
		for (int i=0; i<testSize; i++) {
			testSetsReturnSameNeighborhoods(control, set, i);
		}
		Random rand = new Random(0);
		for (int i=0; i<testSize; i++) {
			int next = rand.nextInt();
			set.add(next);
			control.add(next);
			testSetsReturnSameNeighborhoods(control, set, next);
		}
		System.out.println("OrderedSetTests: testNavigation done.");
	}
	
	
	private static void testSetsReturnSameNeighborhoods(NavigableSet<Integer> control, NavigableSet<Integer> set, int next)
	{
		assertEquals("floor - error at " + next, control.floor(next), set.floor(next));
		assertEquals("higher - error at " + next, control.higher(next), set.higher(next));
		assertEquals("ceiling - error at " + next, control.ceiling(next), set.ceiling(next));
		assertEquals("lower - error at " + next, control.lower(next), set.lower(next));
	}

	
	
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
	
	

	//tests add, remove, contains and size
	public static void testSortedSet(SortedSet<Integer> set) {
		SortedSet<Integer> controlSet = new TreeSet<Integer>();//golden model
		
		int testSize = 500;
		int testRange = testSize/5;
		Random random = new Random(38);

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
			if (controlSet.size() > 0) {
				assertFalse (!set.first().equals(controlSet.first())) ;
				assertFalse (!set.last().equals(controlSet.last())) ;
			}
		}
		
		SetTests.assertEqualSets(set, controlSet);
		System.out.println("OrderedSetTests: testSortedSet done.");
	}
	
	


	
}
