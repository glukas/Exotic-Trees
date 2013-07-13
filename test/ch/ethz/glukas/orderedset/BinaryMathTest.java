package ch.ethz.glukas.orderedset;

import static org.junit.Assert.*;

import org.junit.Test;


public class BinaryMathTest {

	@Test
	public void testNextHighestPowerOfTwo()
	{
		assertEquals(1, BinaryMath.nextHighestPowerOfTwo(1));
		assertEquals(2, BinaryMath.nextHighestPowerOfTwo(2));
		assertEquals(4, BinaryMath.nextHighestPowerOfTwo(3));
		assertEquals(4, BinaryMath.nextHighestPowerOfTwo(4));
		assertEquals(8, BinaryMath.nextHighestPowerOfTwo(5));
		assertEquals(8, BinaryMath.nextHighestPowerOfTwo(6));
		assertEquals(8, BinaryMath.nextHighestPowerOfTwo(7));
		assertEquals(8, BinaryMath.nextHighestPowerOfTwo(8));
		assertEquals(16, BinaryMath.nextHighestPowerOfTwo(9));
		assertEquals(16, BinaryMath.nextHighestPowerOfTwo(10));
		assertEquals(16, BinaryMath.nextHighestPowerOfTwo(11));
		assertEquals(16, BinaryMath.nextHighestPowerOfTwo(12));
		assertEquals(16, BinaryMath.nextHighestPowerOfTwo(13));
		assertEquals(16, BinaryMath.nextHighestPowerOfTwo(14));
		assertEquals(16, BinaryMath.nextHighestPowerOfTwo(15));
		assertEquals(16, BinaryMath.nextHighestPowerOfTwo(16));
	}
	
}
