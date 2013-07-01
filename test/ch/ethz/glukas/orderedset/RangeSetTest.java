package ch.ethz.glukas.orderedset;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.*;

public class RangeSetTest {
	
	
	public static void testRangeSizes(RangeSet<Integer>set)
	{
		set.clear();
		
		set.add(1);
		set.add(4);
		set.add(6);
		set.add(100);
		
		assertTrue(set.sizeOfRange(1, 6, true, true) == 3);
		assertTrue(set.sizeOfRange(1, 6, true, false) == 2);
		assertTrue(set.sizeOfRange(1, 6, false, true) == 2);
		assertTrue(set.sizeOfRange(1, 6, false, false) == 1);
		
		assertTrue(set.sizeOfRange(-3, 0, true, true) == 0);
		assertTrue(set.sizeOfRange(-3, 0, false, true) == 0);
		assertTrue(set.sizeOfRange(-3, 0, true, false) == 0);
		assertTrue(set.sizeOfRange(-3, 0, false, false) == 0);
		
		assertTrue(set.sizeOfRange(0, 100, false, true) == 4);
		assertTrue(set.sizeOfRange(0, 100, false, false) == 3);
		assertTrue(set.sizeOfRange(0, 100, false, false) == 3);
		assertTrue(set.sizeOfRange(0, 100, true, true) == 4);
		
		set.clear();
		
		SetTests.sequenceAdd(set, 30);
		
		for (int i=0; i<30; i++) {
			for (int j=0; j<i; j++) {
				assertTrue(set.sizeOfRange(j, i, true, true) == i-j+1);
				assertTrue(set.sizeOfRange(j, i, true, false) == i-j);
				assertTrue(set.sizeOfRange(j, i, false, true) == i-j);
				assertTrue(set.sizeOfRange(j, i, false, false) == i-j-1);
			}
		}
		System.out.println("RangeSetTests: testRangeSizes done.");
	}
	
	 public static void testPolling(RangeSet<Integer> set)
	 {
		 set.clear();
		 
		 set.add(1);
		 set.add(4);
		 set.add(6);
		 set.add(100);
		 
		 
		 assertTrue(set.poll(2) == 6);
		 assertTrue(!set.contains(6));
		 assertTrue(set.poll(1) == 4);
		 assertTrue(!set.contains(4));
		 assertTrue(set.poll(1) == 100);
		 assertTrue(!set.contains(100));
		 assertTrue(set.poll(0) == 1);
		 assertTrue(!set.contains(1));
		 System.out.println("RangeSetTests: testPolling done.");
	 }
	 
	 
	static public void testRangeSet(RangeSet<Integer> set)
	{
		set.clear();
		int testRange = 200;
		for (int i=0; i<testRange; i++) {
			set.add(i);
			assertIsInOrder(set);
		}
		set.clear();
		Random rand = new Random(1);
		for (int i=0; i<testRange; i++) {
			set.add(rand.nextInt());
			assertIsInOrder(set);
		}
		System.out.println("RangeSetTests: testRangeSet done.");
	}
	
	
	//helpers
	
	static void assertIsInOrder(RangeSet<Integer>set)
	{
		int i = 0;
		for (Integer value : set) {
			assert set.get(i) == value;
			assert set.indexOf(value) == i;
			i++;
		}
	}
	

	
}
