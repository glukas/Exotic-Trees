package ch.ethz.glukas.orderedmap;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.ethz.glukas.orderedmap.Trie;

public class TrieTest {

	@Test
	public void testTrie()
	{
		basicTestTrie();
	}
	
	public static void basicTestTrie()
	{
		Trie<Integer> trie = new Trie<Integer>();
		int testSize = 100000;
		for (int i=0; i<testSize; i++) {
			trie.put(new Integer(i).toString(), i);
		}
		for (int i=0; i<testSize; i++) {
			assertTrue(trie.contains(new Integer(i).toString()));
			assertEquals(trie.get(new Integer(i).toString()), new Integer(i));
		}
		for (int i=-100; i<0; i++) {
			assertFalse(trie.contains(new Integer(i).toString()));
			assertEquals(trie.get(new Integer(i).toString()), null);
		}
		for (int i=testSize; i<testSize+100; i++) {
			assertFalse(trie.contains(new Integer(i).toString()));
			assertEquals(trie.get(new Integer(i).toString()), null);
		}
	}
	
}
