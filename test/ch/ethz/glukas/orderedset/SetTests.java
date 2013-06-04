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
		
		int testSize = 500;
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
		System.out.println("SetTests: testSet done.");
	}

	//basic test that can be used for early implementation testing
	//tests add and contains
	public static void testAddAndContains(Set<Integer> set)
	{
		Set<Integer> controlSet = new HashSet<Integer>();//golden model
		
		int testSize = 500;
		Random random = new Random(8);

		for (int i=0; i<testSize; i++) {
			set.add(i);
			controlSet.add(i);
			assertFalse (!set.contains(i)) ;
		}
		assertFalse (set.size() != controlSet.size()) ;

		set.clear();
		assertFalse (set.size() > 0) ;
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
	
	public static void randomAdd(Set<Integer> set1, Set<Integer> set2, int testSize)
	{
		Random rand = new Random(testSize);
		for (int i=0; i<testSize; i++) {
			int next = rand.nextInt();
			set1.add(next);
			set2.add(next);
		}
	}
	
	public static void assertEqualSets(Set<Integer> set, Set<Integer> control)
	{
		assertTrue(set.size() == control.size());
		for (Integer value : control) {
			assertTrue(set.contains(value));
		}
	}
}
