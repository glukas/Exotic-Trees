package ch.ethz.glukas.orderedmap;

import static org.junit.Assert.*;

import org.junit.Test;

public class CharMapTest {

	
	@Test
	public void testCharMap()
	{
		CharMap<Integer> map = new CharMap<Integer>();
		int testSize = 300;
		for (int i=1; i<=testSize; i++) {
			assertNull(map.put((char) i, i));
			assertTrue(map.size() == i);
		}
		for (int i=1; i<=testSize; i++) {
			assertTrue(map.get((char) i) == i);
		}
		
		map = new CharMap<Integer>();
	}
	
}
