package ch.ethz.glukas.orderedset;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

public class COBTreeTest {

	@Test
	public void testPMA()
	{
		testPackedMemoryStructure();
	}
	
	public static void testPackedMemoryStructure()
	{
		for (int i=0; i<3000; i*=2) {
			testPackedMemoryStructure(i, i);
			i++;
		}
	}
	
	
	static void testPackedMemoryStructure(int testSize, int seed)
	{
		TreeSet<Integer> control = new TreeSet<Integer>();
		COBTree pma = new COBTree();
		Random rand = new Random(seed);
		assertFalse(pma.contains(1));
		//sequence insert
		for (int i=1; i<testSize/2; i++) {
			pma.insert(i);
			control.add(i);
			assertEquals(pma.contains(i), control.contains(i));
			assertTrue(pma.size() == control.size());
		}
		
		assertEqualSets(pma, control);
		
		//random insert
		for (int i=0; i<testSize-(testSize/2); i++) {
			int next = rand.nextInt();
			if (next == 0) continue;
			pma.insert(next);
			control.add(next);
			assertTrue(pma.contains(next));
		}

		assertEqualSets(pma, control);
		
	}
	
	
	static void assertEqualSets(COBTree pma, Set<Integer> control)
	{
		assertEquals(pma.size(), control.size());
		//check result
		for (Integer i : control)
		{
			assertTrue(pma.contains(i));
		}
	}
	
	
}
