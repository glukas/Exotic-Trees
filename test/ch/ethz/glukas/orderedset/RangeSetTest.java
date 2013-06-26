package ch.ethz.glukas.orderedset;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.*;

public class RangeSetTest {

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
	}
	
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
