package ch.ethz.glukas.orderedset;

import static org.junit.Assert.*;

import org.junit.Test;

public class SplayTreeTest {

	@Test
	public void testSet()
	{
		SetTests.testSet(new SplayTree<Integer>());
		SetTests.randomizedTestSet(new SplayTree<Integer>(), 2000);
	}
	
}
