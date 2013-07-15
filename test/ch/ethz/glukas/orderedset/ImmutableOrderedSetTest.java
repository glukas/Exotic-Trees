package ch.ethz.glukas.orderedset;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;
import java.util.TreeSet;

import org.junit.Test;

public class ImmutableOrderedSetTest {

	
	@Test
	public void testImmutableSetForVariousSizes()
	{
		testImmutableSet();
	}
	
	public static void testImmutableSet()
	{
		int testSize = 1;
		Random random = new Random(1);
		while (testSize < 2048) {
			testImmutableSet(testSize, random.nextInt());
			testSize = testSize << 1;
		}
	}
	
	public static void testImmutableSet(int testSize, int seed)
	{
		TreeSet<Integer> control = new TreeSet<Integer>();
		int[] input = new int[testSize];
		//ordered test
		for (int i=0; i<testSize; i++) {
			input[i] = i+1;
		}
		ImmutableOrderedSet uut = new ImmutableOrderedSet(input);
		for (int i=1; i<=testSize; i++) {
			assertTrue(uut.contains(i));
		}
		
		//random test
		int testRange = testSize*5;
		Random random = new Random(seed);
		input = new int[testSize];
		
		for (int i=0; i<testSize; i++) {
			input[i] = random.nextInt(testRange);
			control.add(input[i]);
		}
		Arrays.sort(input);
		uut = new ImmutableOrderedSet(input);
		for (int i=-10; i<testRange; i++) {
			assertEquals(uut.contains(i), control.contains(i));
		}
	}
	
}
