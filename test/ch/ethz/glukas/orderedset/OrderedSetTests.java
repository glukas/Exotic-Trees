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


	public static void testNavigableSet(NavigableSet<Integer> set)
	{
		TreeSet<Integer> control = new TreeSet<Integer>();
		int testSize = 30;
		for (int i=0; i<testSize; i++) {
			set.add(i);
			control.add(i);
		}
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
	}
	
	public static void testSetsReturnSameNeighborhoods(NavigableSet<Integer> control, NavigableSet<Integer> set, int next)
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
		
		int testSize = 1000;
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
		
		assertFalse (controlSet.size() != set.size());
		for (Integer number : controlSet) {
			assertFalse (!controlSet.contains(number));
		}
		
	}
	
	

	//tests add, remove, contains and size
	public static void testSortedSet(SortedSet<Integer> set) {
		SortedSet<Integer> controlSet = new TreeSet<Integer>();//golden model
		
		int testSize = 1000;
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
		
		assertFalse (controlSet.size() != set.size()) ;
		for (Integer number : controlSet) {
			assertFalse (!controlSet.contains(number)) ;
		}

	}
	
	
	//basic test that can be used for early implementation testing
	//tests add and contains
	public static void testAddAndContainsSet(Set<Integer> set)
	{
		Set<Integer> controlSet = new HashSet<Integer>();//golden model
		
		int testSize = 1000;
		Random random = new Random(5);

		for (int i=0; i<testSize; i++) {
			set.add(i);
			controlSet.add(i);
			assertFalse (!set.contains(i)) ;
		}
		assertFalse (set.size() != controlSet.size()) ;

		set.clear();
		assertFalse (set.size() > 0) ;
	}
	
	public static void testSet(Set<Integer> set)
	{
		Set<Integer> controlSet = new HashSet<Integer>();//golden model
		
		int testSize = 1000;
		Random random = new Random(5);

		
		for (int i=0; i<testSize; i++) {
			assertFalse (set.add(i) == false) ;
			controlSet.add(i);
			assertFalse (set.add(i) != false) ;
			assertFalse (!set.contains(i)) ;
		}
		assertFalse (set.size() != controlSet.size()) ;
		
		
		for (int i=0; i<testSize; i++) {
			int next = random.nextInt();
			set.add(next);
			controlSet.add(next);
			assertFalse (!set.contains(next)) ;
		}
		for (int i=0; i<testSize; i++) {
			assertFalse (!set.contains(i)) ;
		}
		for (int i=0; i<testSize; i++) {
			set.remove(i);
			controlSet.remove(i);
			assertFalse (set.contains(i)) ;
		}
		assertFalse (set.size() != controlSet.size()) ;
		
		for (Integer i : controlSet) {
			assertFalse (!set.contains(i)) ;
		}
		set.clear();
		assertFalse (set.size() > 0) ;
		
	}


}
