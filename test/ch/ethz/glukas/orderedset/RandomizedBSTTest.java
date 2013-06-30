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
	 public void testIterator()
	 {
		 OrderedSetTests.testIterator(new SplayTree<Integer>());
	 }
	 
	 @Test
	 public void testAccessByRank()
	 {
		 RangeSetTest.testRangeSet(new RandomizedBST<Integer>());
		 RangeSetTest.testPolling(new RandomizedBST<Integer>());
		 RangeSetTest.testRangeSizes(new RandomizedBST<Integer>());
	 }
	
}
