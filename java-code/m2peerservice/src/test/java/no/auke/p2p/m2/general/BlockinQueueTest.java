package no.auke.p2p.m2.general;

import static org.junit.Assert.*;
import no.auke.p2p.m2.general.BlockingQueue;

import org.junit.Test;

public class BlockinQueueTest {
	
	BlockingQueue<String> test = new BlockingQueue<String>(10);
	@Test
	public void test() throws InterruptedException {
		test.put("sdasd");
		assertEquals(1,test.size());
	}

	@Test
	public void testFill() throws InterruptedException {
		for(int i=0;i<11;i++) {
			test.put("sdasd");
			assertEquals(i+1,test.size());
		}
		assertFalse(test.offer("sdasd"));
	}
	
}
