package ch.ethz.glukas.orderedset;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;
import java.util.TreeSet;

import org.junit.Test;

public class CocoTreeTest {

	@Test
	public void testSetForVariousSizes()
	{
		testImmutableSet();
	}
	
	@Test
	public void testUpdatesForVariousSizes()
	{
		testUpdates();
	}

	public static void testUpdates()
	{
		Random random = new Random(423);
		for (int i=1; i<BinaryMath.powerOfTwo(18); i=i<<1) {
			testUpdates(i, random.nextInt());
		}
	}
	
	public static void testUpdates(int testSize, int seed)
	{
		Random random = new Random(seed);
		int[] keys = inOrderArray(testSize, 100);
		CocoTree uut = new CocoTree(keys);
		
		//test single key updates
		for (int i=0; i<testSize; i++) {
			int updateIndex = random.nextInt(testSize);
			int oldValue = keys[updateIndex];
			int newValue = oldValue+1;
			
			if ((updateIndex == 0 || keys[updateIndex-1] < newValue) && (updateIndex == testSize-1 || keys[updateIndex+1] > newValue) && !uut.contains(newValue)) {
				keys[updateIndex] = newValue;
				uut.update(oldValue, oldValue);
				assertTrue(uut.contains(newValue));
				assertFalse(uut.contains(oldValue));
			}
			
		}
		
	}
	
	public static void testImmutableSet()
	{
		Random random = new Random(1);
		//testImmutableSet(0, 0);
		int maxValue = BinaryMath.powerOfTwo(20);
		for (int i=1; i<maxValue; i=i<<1) {
			testImmutableSet(i, random.nextInt());
		}
	}
	
	//returns an array of length testSize where the value at index k is k*spacing
	protected static int[]inOrderArray(int testSize, int spacing)
	{
		int[] input = new int[testSize];
		//ordered test
		for (int i=0; i<testSize; i++) {
			input[i] = i*spacing;
		}
		return input;
	}
	
	protected static CocoTree inOrderTree(int testSize)
	{
		CocoTree uut = new CocoTree(inOrderArray(testSize, 1));
		return uut;
	}
	
	
	public static void testImmutableSet(int testSize, int seed)
	{
		CocoTree uut = inOrderTree(testSize);
		
		for (int i=0; i<testSize; i++) {
			assertTrue(uut.contains(i));
		}
		
		//random test
		int testRange = testSize*5;
		Random random = new Random(seed);
		int[] input = new int[testSize];
		TreeSet<Integer> control = new TreeSet<Integer>();
		
		for (int i=0; i<testSize; i++) {
			int next = random.nextInt(testRange);
			input[i] = next;
			control.add(next);
		}
		Arrays.sort(input);
		uut = new CocoTree(input);
		for (int i=-10; i<testRange; i++) {
			assertEquals(uut.contains(i), control.contains(i));
		}
	}
}
