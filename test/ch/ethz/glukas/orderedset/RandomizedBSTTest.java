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
	 public void testSubsets()
	 {
		 OrderedSetTests.testSubsets(new RandomizedBST<Integer>());
	 }
	 
	 @Test
	 public void testTailsets()
	 {
		 OrderedSetTests.testTailSets(new RandomizedBST<Integer>());
	 }
	 
	 @Test
	 public void testSubsetModification()
	 {
		 OrderedSetTests.testSubsetModification(new RandomizedBST<Integer>());
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
		 SetTests.randomTestSet(new RandomizedBST<Integer>());
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
			 assertTrue(set.poll(0) == i);
		 }
		 assertFalse (set.isEmpty() == false);
		 
		 set.add(1);
		 set.add(4);
		 set.add(6);
		 set.add(100);
		 assertTrue(set.poll(2) == 6);
		 assertTrue(set.poll(1) == 4);
		 assertTrue(set.poll(1) == 100);
		 assertTrue(set.poll(0) == 1);
	 }
	
}
