package priorityQueue;

import static org.junit.Assert.*;
import java.util.PriorityQueue;
import java.util.Random;
import org.junit.Test;

public class COPriorityQueueTest {

	
	@Test
	public void testInsertAndMinimum()
	{
		testInsertionAndMinimum();
	}
	
	@Test
	public void testDequeue()
	{
		testPolling();
	}
	
	
	@Test
	public void testRandomUsage()
	{
		randomUsage();
	}
	
	public static void randomUsage()
	{
		COPriorityQueue uut = new COPriorityQueue();
		PriorityQueue<Integer> control = new PriorityQueue<Integer>();
		Random rand = new Random(3);
		int testSize = 1020;
		int testRange = 10*testSize;
		for (int i=0; i<testSize; i++) {
			int next = rand.nextInt(testRange);
			uut.add(next);
			control.add(next);
			assertTrue(uut.size() == control.size());
			assertTrue(uut.peek() == control.peek());
			
			next = rand.nextInt(testRange);
			uut.add(next);
			control.add(next);
			
			int result = uut.poll();
			int shouldBe = control.poll();
			assertTrue(result == shouldBe);
			assertTrue(uut.size() == control.size());
			
			if (rand.nextBoolean()) {
				next = rand.nextInt(testRange);
				uut.add(next);
				control.add(next);
				assertTrue(uut.size() == control.size());
				assertTrue(uut.peek() == control.peek());
			}
		}
		
		//test contains calls
		for (int i=0; i<testRange; i++) {
			assertTrue(uut.contains(i) == control.contains(i));
		}
		
		while (!control.isEmpty()) {
			int result = uut.poll();
			int shouldBe = control.poll();
			assertTrue(uut.size() == control.size());
			assertTrue(result == shouldBe);
			
		}
		assertTrue(uut.size() == control.size());
		assertTrue(uut.isEmpty());
	}

	
	public static void testPolling()
	{
		COPriorityQueue uut = new COPriorityQueue();
		PriorityQueue<Integer> control = new PriorityQueue<Integer>();
		Random rand = new Random(2);
		int testSize = 1000;

		for (int i=0; i<testSize; i++) {
			int next = rand.nextInt(testSize);
			uut.add(next);
			control.add(next);
		}
		
		for (int i=0; i<testSize; i++) {
			int result = uut.poll();
			int shouldBe = control.poll();
			assertTrue(uut.size() == control.size());
			assertTrue(result == shouldBe);
			//test contains calls
			for (int j=0; j<testSize; j++) {
				assertTrue(uut.contains(j) == control.contains(j));
			}
		}
	}
	
	
	
	public static void testInsertionAndMinimum()
	{
		COPriorityQueue uut = new COPriorityQueue();
		PriorityQueue<Integer> control = new PriorityQueue<Integer>();
		Random rand = new Random(1);
		int testSize = 300;
		for (int i=0; i<testSize; i++) {
			int next = rand.nextInt();
			uut.add(next);
			control.add(next);
			assertTrue(uut.peek() == control.peek());
			assertTrue(uut.size() == control.size());
			assertTrue(uut.contains(next));
		}
	}
	
}
