package ch.ethz.glukas.orderedset;

import static org.junit.Assert.*;

import org.junit.Test;

public class ScapegoatTreeTest {

	@Test
	public void testSetAddAndContains() {
		SetTests.testAddAndContains(new ScapegoatTree<Integer>());
	}

	
	@Test
	public void testSet() {
		SetTests.testSet(new ScapegoatTree<Integer>());
	}

}
