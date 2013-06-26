package ch.ethz.glukas.orderedset;

import static org.junit.Assert.*;

import org.junit.Test;

public class SplayTreeTest {

	@Test
	public void testSet()
	{
		SetTests.testSet(new SplayTree<Integer>());
	}
	
	@Test
	public void randomizedTestSet()
	{
		SetTests.randomizedTestSet(new SplayTree<Integer>(), 2000);
	}
	
	@Test
	public void navigationTest()
	{
		OrderedSetTests.testNavigation(new SplayTree<Integer>());
	}
	
	@Test
	public void testSortedSet()
	{
		OrderedSetTests.testSortedSet(new SplayTree<Integer>());
	}
	
	@Test
	public void testPolling()
	{
		OrderedSetTests.testPolling(new SplayTree<Integer>());
	}
	
	@Test
	public void testAccessByRank()
	{
		RangeSetTest.testRangeSet(new SplayTree<Integer>());
		RangeSetTest.testPolling(new SplayTree<Integer>());
	}
	
	
	 @Test
	 public void testSubsets()
	 {
		 OrderedSetTests.testSubsets(new SplayTree<Integer>());
	 }
	 
	 @Test
	 public void testTailsets()
	 {
		 OrderedSetTests.testTailSets(new SplayTree<Integer>());
	 }
	 
	 @Test
	 public void testSubsetModification()
	 {
		 OrderedSetTests.testSubsetModification(new SplayTree<Integer>());
	 }
	
	
}
