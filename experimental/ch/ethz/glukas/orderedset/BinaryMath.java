package ch.ethz.glukas.orderedset;

public class BinaryMath {

	
	////
	//MATH HELPERS
	////
	
	//fast binary logarithm by x4u at http://stackoverflow.com/questions/3305059/how-do-you-calculate-log-base-2-in-java-for-integers
	public static int log( int bits ) // returns 0 for bits=0
	{
	    int log = 0;
	    if( ( bits & 0xffff0000 ) != 0 ) { bits >>>= 16; log = 16; }
	    if( bits >= 256 ) { bits >>>= 8; log += 8; }
	    if( bits >= 16  ) { bits >>>= 4; log += 4; }
	    if( bits >= 4   ) { bits >>>= 2; log += 2; }
	    return log + ( bits >>> 1 );
	}
	
	public static int powerOfTwo(int exponent)
	{
		assert exponent <= 30;
		return 1 << exponent;
	}
	
	public static int nextHighestPowerOfTwo(int num)
	{
		assert num >= 0;
		if (num <= 1) return 1;
		int result = 2;
		while (num > result) {
			result = 2*result;
		}
		return result;
		
		/*alternative
		int highestOneBit = Integer.highestOneBit(num);
		if (highestOneBit == num) return highestOneBit;
		return highestOneBit<<1;*/
	}
	
	public static boolean isPowerOfTwo(int num)
	{
		return nextHighestPowerOfTwo(num) == num;
	}
	
}
