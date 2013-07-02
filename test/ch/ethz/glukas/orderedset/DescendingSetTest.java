package ch.ethz.glukas.orderedset;

import static org.junit.Assert.*;

import java.util.NavigableSet;
import java.util.TreeSet;

import org.junit.*;

public class DescendingSetTest {

	@Test
	public void testBasicSetConsistency()
	{
		int testSize = 500;
		
		NavigableSet<Integer> set1 = new TreeSet<Integer>();
		NavigableSet<Integer> set2 = new TreeSet<Integer>();
		
		SetTests.sequenceAdd(set1, set2, testSize/2);
		SetTests.randomAdd(set1, set2, testSize);
		
		NavigableSet<Integer> uut = new DescendingSet<Integer>(set1);
		NavigableSet<Integer> control = set2.descendingSet();
		
		OrderedSetTests.assertEqualNavigableSets(uut, control);
		SetTests.randomAdd(uut, control, testSize);
		OrderedSetTests.assertEqualNavigableSets(uut, control);
		
		uut.clear();
		assertEquals(uut.size(), 0);
		
	}
	
	@Test
	public void testSubsets()
	{
		int testSize = 100;
		
		NavigableSet<Integer> set1 = new TreeSet<Integer>();
		NavigableSet<Integer> set2 = new TreeSet<Integer>();
		SetTests.sequenceAdd(set1, set2, testSize/2);
		SetTests.randomAdd(set1, set2, testSize/2);
		
		NavigableSet<Integer> uut = new DescendingSet<Integer>(set1);
		NavigableSet<Integer> control = set2.descendingSet();
		
		for (int j=0; j<2*testSize; j++) {
			for (int i=j-1; i>-10; i--) {
				OrderedSetTests.assertEqualSubsets(uut, control, j, i);
			}
		}
	}
	
	@Test
	public void testIterator()
	{
		NavigableSet<Integer> set1 = new SplayTree<Integer>();
		NavigableSet<Integer> set2 = new TreeSet<Integer>();
		OrderedSetTests.testIterator(new DescendingSet<Integer>(set1), set2.descendingSet());
	}
	
}
