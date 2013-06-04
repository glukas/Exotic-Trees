import java.io.IOException;
import java.util.*;

import ch.ethz.glukas.orderedset.*;

class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

		SetTests.testSet(new RandomizedBST<Integer>());
		OrderedSetTests.randomTestSet(new RandomizedBST<Integer>());
		OrderedSetTests.testNavigation(new RandomizedBST<Integer>());
		OrderedSetTests.testSortedSet(new RandomizedBST<Integer>());
		
		//int performanceTestSize = 2000000;
		//performanceTestSet(new RandomizedBST<Integer>(), performanceTestSize);
		//performanceTestSet(treap, performanceTestSize);
		//performanceTestSet(new HashSet<Integer>(), performanceTestSize);
		//performanceTestSet(new TreeSet<Integer>(), performanceTestSize);
		
	}
	
	
	static void performanceTestSet(Set<Integer> set, int testSize)
	{
		int testRange = testSize/5;
		Random random = new Random();
		Date start = new Date();
		
		
		for (int i=0; i<testSize; i++) {
			set.add(i);
			set.contains(i);
		}
		for (int i=0; i<testSize; i++) {
			set.remove(i);
			set.contains(i);
		}
		
		
		for (int i=0; i< testSize; i++) {
			int nextOperation = random.nextInt(10);
			int nextNumber = random.nextInt(testRange);
			if (nextOperation < 5) {
				set.add(nextNumber);
			} else if (nextOperation < 7) {
				set.remove(nextNumber);
			} else {
				set.contains(nextNumber);
			}
			
		}
		Date end = new Date();
		System.out.println("randomized performance test set done!" + " took " + (end.getTime()-start.getTime())/ 1000.0 + " s");
	}

}
