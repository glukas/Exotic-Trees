package ch.ethz.glukas.orderedset;

import static org.junit.Assert.*;

import java.util.NavigableSet;
import java.util.TreeSet;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class RandomizedBSTTest {

	
	 @BeforeClass
	 public static void testSetup() {
	 }

	 @AfterClass
	 public static void testCleanup() {
	 }
	  
	  
	  
	@Test
	public void testPolling()
	{
		OrderedSetTests.testPolling(new RandomizedBST<Integer>());
	}
	
	 @Test
	 public void testNavigable()
	 {
		 OrderedSetTests.testNavigation(new RandomizedBST<Integer>());
	 }
	 

	 @Test
	 public void testSet()
	 {
		 SetTests.testSet(new RandomizedBST<Integer>());
	 }
	 
	 @Test
	 public void testAddAndContains()
	 {
		 SetTests.testAddAndContains(new RandomizedBST<Integer>());
	 }
	 
	 @Test
	 public void testSortedSet()
	 {
		 OrderedSetTests.testSortedSet(new RandomizedBST<Integer>());
	 }
	 
	 @Test
	 public void testSetRandomized()
	 {
		 OrderedSetTests.randomTestSet(new RandomizedBST<Integer>());
	 }
	 
	 @Test
	 public void testAccessByRank()
	 {
		 int testSize = 1000;
		 RandomizedBST<Integer> set = new RandomizedBST<Integer>();
			
		 for (int i=0; i < testSize; i++) {
			 set.add(i);
		 }
		
		 for (int i=0; i < testSize; i++) {
			 assertTrue(set.get(i) == i);
			 assertEquals(set.indexOf(i), i);
		 }
		 for (int i=0; i< testSize; i++) {
			 assertFalse (set.remove(0) == false);
		 }
		 assertFalse (set.isEmpty() == false);
	 }
	
}
