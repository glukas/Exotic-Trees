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
		RangeSetTest.testRangeSet(new RandomizedBST<Integer>());
		RangeSetTest.testPolling(new RandomizedBST<Integer>());
	}
	
	
	
}
