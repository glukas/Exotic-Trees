package ch.ethz.glukas.orderedset;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;
import java.util.TreeSet;

import org.junit.Test;

public class FixedSizeCOSearchTreeTest {

	@Test
	public void testSetForVariousSizes()
	{
		testImmutableSet();
	}
	
	@Test
	public void testUpdates()
	{
		//TODO
	}
	
	public static void testImmutableSet()
	{
		Random random = new Random(1);
		//testImmutableSet(0, 0);
		int maxValue = BinaryMath.powerOfTwo(21);
		for (int i=1; i<maxValue; i=i<<1) {
			testImmutableSet(i, random.nextInt());
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
		FixedSizeCOSearchTree uut = new FixedSizeCOSearchTree(input);
		
		for (int i=1; i<=testSize; i++) {
			assertTrue(uut.contains(i));
		}
		
		//random test
		int testRange = testSize*5;
		Random random = new Random(seed);
		input = new int[testSize];
		
		for (int i=0; i<testSize; i++) {
			int next = random.nextInt(testRange);
			input[i] = next;
			control.add(next);
		}
		Arrays.sort(input);
		uut = new FixedSizeCOSearchTree(input);
		for (int i=-10; i<testRange; i++) {
			assertEquals(uut.contains(i), control.contains(i));
		}
	}
}
