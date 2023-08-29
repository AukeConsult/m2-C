package no.auke.p2p.m2.workers;

import no.auke.p2p.m2.workers.io.PriorityQueue;
import no.auke.p2p.m2.workers.io.PriorityQueue.OutPacket;
import junit.framework.TestCase;

//import static org.junit.Assert.*;
//import static org.mockito.Matchers.*;
//import static org.mockito.Mockito.atLeast;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.spy;
//import static org.mockito.Mockito.stub;


//import static org.powermock.api.mockito.PowerMockito.when;
//import static org.powermock.api.mockito.PowerMockito.mock;
//import static org.powermock.api.mockito.PowerMockito.spy;
//import static org.powermock.api.mockito.PowerMockito.doReturn;









import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import no.auke.p2p.m2.message.header.MessageHeader;

import org.junit.runner.RunWith;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

//import org.powermock.core.classloader.annotations.PowerMockIgnore;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({PriorityQueue.class})
//
//@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*",
//    "com.sun.org.apache.*", "ch.qos.logback.*",
//    "org.slf4j.*" })


public class PriorityQueueTest extends TestCase {

	ExecutorService executor = Executors.newCachedThreadPool();
	
	@Before
	public void setUp() throws Exception {}
	
	@Test
	public void test_incoming_high() throws InterruptedException {
				
		final PriorityQueue queue = new PriorityQueue(100,100);
		
		final AtomicInteger num_packets = new AtomicInteger();
		final AtomicInteger num_packets_out = new AtomicInteger();
		
		executor.execute(new Runnable(){

			@Override
			public void run() {

				while(true) {

					try {
						if(queue.getInQueue()!=null){
							num_packets.incrementAndGet();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
			}});
		
		for(int x=1;x<10000;x++) {
			
			MessageHeader msg = new MessageHeader(MessageHeader.MessageHeaderType.PingClose);
			
			queue.addInQueue(msg, PriorityQueue.Priority.high);
			num_packets_out.incrementAndGet();
			
			
		}
		
		Thread.sleep(1000);
		
		assertEquals(0,queue.getInBuffSize());
		assertEquals(num_packets_out.get(),num_packets.get());
		
		
	}

	@Test
	public void test_incoming_medium() throws InterruptedException {
		
		final PriorityQueue queue = new PriorityQueue(100,100);
		final AtomicInteger num_packets = new AtomicInteger();
		final AtomicInteger num_packets_out = new AtomicInteger();
		
		executor.execute(new Runnable(){

			@Override
			public void run() {

				while(true) {
					
					try {
						if(queue.getInQueue()!=null){
							num_packets.incrementAndGet();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}});
		
		for(int x=1;x<10000;x++) {
			
			MessageHeader msg = new MessageHeader(MessageHeader.MessageHeaderType.PingClose);
			queue.addInQueue(msg, PriorityQueue.Priority.medium);
			num_packets_out.incrementAndGet();
			
		}
		
		Thread.sleep(1000);
		
		assertEquals(0,queue.getInBuffSize());
		assertTrue(num_packets_out.get()>=num_packets.get());
		
		
	}		
	
	@Test
	public void test_incoming_low() throws InterruptedException {
		
		final PriorityQueue queue = new PriorityQueue(100,100);
		final AtomicInteger num_packets = new AtomicInteger();
		final AtomicInteger num_packets_out = new AtomicInteger();

		executor.execute(new Runnable(){

			@Override
			public void run() {

				while(true) {

					try {
						if(queue.getInQueue()!=null){
							num_packets.incrementAndGet();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
			}});
		
		for(int x=1;x<10000;x++) {
			
			MessageHeader msg = new MessageHeader(MessageHeader.MessageHeaderType.PingClose);
			queue.addInQueue(msg, PriorityQueue.Priority.low);
			num_packets_out.incrementAndGet();
			
		}
		
		Thread.sleep(1000);
		assertEquals(0,queue.getInBuffSize());
		assertEquals(num_packets_out.get(),num_packets.get());
		
		
	}	

	@Test
	public void test_incoming_mixed() throws InterruptedException {
		
		Random rnd = new Random();
		
		final PriorityQueue queue = new PriorityQueue(100,100);
		
		final AtomicInteger num_packets = new AtomicInteger();
		final AtomicInteger num_packets_out = new AtomicInteger();		
		
		executor.execute(new Runnable(){

			@Override
			public void run() {

				while(true) {
					
					try {
						if(queue.getInQueue()!=null){
							num_packets.incrementAndGet();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
			}});
		
		for(int x=1;x<10000;x++) {
			
			MessageHeader msg = new MessageHeader(MessageHeader.MessageHeaderType.PingClose);
			
			int level = rnd.nextInt(10);
			if(level<2) {

				queue.addInQueue(msg, PriorityQueue.Priority.high);
				
			} else if (level<4) {
				
				queue.addInQueue(msg, PriorityQueue.Priority.medium);
				
			} else {
				
				queue.addInQueue(msg, PriorityQueue.Priority.low);
				
			}
			
			num_packets_out.incrementAndGet();
		}
		
		Thread.sleep(1000);
		assertEquals(0,queue.getInBuffSize());
		assertEquals(num_packets_out.get(),num_packets.get());		
		
	}	
	
	@Test
	public void test_outcoming_high() throws InterruptedException {
		
		final PriorityQueue queue = new PriorityQueue(100,100);
		final AtomicInteger num_packets = new AtomicInteger();
		final AtomicInteger num_packets_out = new AtomicInteger();		
		
		executor.execute(new Runnable(){

			@Override
			public void run() {

				while(true) {

					try {
						
						if(queue.getOutQueue()!=null){
							num_packets.incrementAndGet();
						}

					
					} catch (InterruptedException e) {
					}
					
				}
				
			}});

		Thread.sleep(10);
		
		for(int x=1;x<10000;x++) {
			OutPacket packet = queue.new OutPacket(null,new byte[]{1}, null);
			queue.addOutQueue(packet, PriorityQueue.Priority.high);
			num_packets_out.incrementAndGet();
		}
		
		Thread.sleep(1000);
		assertEquals(0,queue.getOutBuffSize());
		assertEquals(num_packets_out.get(),num_packets.get());
		
	}	
	
	@Test
	public void test_outcoming_medium() throws InterruptedException {
		
		final PriorityQueue queue = new PriorityQueue(100,100);
		final AtomicInteger num_packets = new AtomicInteger();
		final AtomicInteger num_packets_out = new AtomicInteger();		
		
		executor.execute(new Runnable(){

			@Override
			public void run() {

				while(true) {

					try {
						
						if(queue.getOutQueue()!=null ) {
							num_packets.incrementAndGet();
						}
						
					} catch (InterruptedException e) {
					}
				}
				
			}});
		
		Thread.sleep(10);

		for(int x=1;x<10000;x++) {
			
			OutPacket packet = queue.new OutPacket(null,new byte[]{1}, null);
			queue.addOutQueue(packet, PriorityQueue.Priority.medium);
			num_packets_out.incrementAndGet();
			
		}
		Thread.sleep(1000);
		assertEquals(0,queue.getOutBuffSize());
		assertEquals(num_packets_out.get(),num_packets.get());		
		
	}	
	
	@Test
	public void test_outcoming_low() throws InterruptedException {
		
		
		final PriorityQueue queue = new PriorityQueue(100,100);
		final AtomicInteger num_packets = new AtomicInteger();
		final AtomicInteger num_packets_out = new AtomicInteger();		
		
		executor.execute(new Runnable(){

			@Override
			public void run() {

				while(true) {

					try {
						
						if(queue.getOutQueue()!=null ) {
							num_packets.incrementAndGet();
						}
					
					} catch (InterruptedException e) {
					}
					
				}
				
			}});
		
		Thread.sleep(10);

		for(int x=1;x<10000;x++) {
			OutPacket packet = queue.new OutPacket(null,new byte[]{1}, null);
			queue.addOutQueue(packet, PriorityQueue.Priority.low);
			num_packets_out.incrementAndGet();
		}
		Thread.sleep(1000);
		assertEquals(0,queue.getOutBuffSize());
		assertEquals(num_packets_out.get(),num_packets.get());		
		
	}	
	
	@Test
	public void test_outcoming_mixed() throws InterruptedException {
		
		Random rnd = new Random();
		
		final PriorityQueue queue = new PriorityQueue(100,100);
		final AtomicInteger num_packets = new AtomicInteger();
		final AtomicInteger num_packets_out = new AtomicInteger();		
		final int total = 10000;
		
		executor.execute(new Runnable(){
			
			@Override
			public void run() {

				while(true) {

					try {
						
						if(queue.getOutQueue()!=null ) {
							num_packets.incrementAndGet();
						}
						
						if(num_packets.get()== total) {
							break;
						} else {
							Thread.yield();
						}
					
					} catch (InterruptedException e) {
					}
					
				}
				
			}});

		Thread.sleep(10);

		for(int x=1;x<=total;x++) {
			
			OutPacket packet = queue.new OutPacket(null,new byte[]{1}, null);
			
			int level = rnd.nextInt(10);
			if(level<2) {
				
				queue.addOutQueue(packet, PriorityQueue.Priority.high);
			
			} else if (level<4) {
			
				queue.addOutQueue(packet, PriorityQueue.Priority.medium);
			
			} else {
				
				queue.addOutQueue(packet, PriorityQueue.Priority.low);				
			}
			num_packets_out.incrementAndGet();			
			
		}
		Thread.sleep(2000);
		
		assertEquals(0,queue.getInBuffSize());
		assertEquals(num_packets_out.get(),num_packets.get());
		
	}	
	
	@Test
	public void test_outcoming_mixed_skip_old() throws InterruptedException {
		
		final PriorityQueue queue = new PriorityQueue(1000,1000);
		
		final AtomicInteger num_packets = new AtomicInteger();
		final int total = 100;
		
		executor.execute(new Runnable(){

			@Override
			public void run() {

				while(true) {

					try {
						
						Thread.sleep(PriorityQueue.getStreamPacketLifetime());
						if(queue.getOutQueue()!=null ) {
							num_packets.incrementAndGet();
						}
						if(num_packets.get()== total) {
							break;
						} 
					
					} catch (InterruptedException e) {
					}
					
				}
				
			}});
		
		Thread.sleep(10);

		for(int x=1;x<=total;x++) {
			OutPacket packet = queue.new OutPacket(null,new byte[]{1}, null);
			queue.addOutQueue(packet, PriorityQueue.Priority.low);
		}
		
		Thread.sleep(1000);
		
		assertTrue(num_packets.get()<=10);
		assertEquals(0,queue.getOutBuffSize());
		
	}	
	
	@Test
	public void test_incoming_mixed_skip_old() throws Exception {
		
		final PriorityQueue queue = new PriorityQueue(100,100);
		
		final AtomicInteger num_packets = new AtomicInteger();
		executor.execute(new Runnable(){

			@Override
			public void run() {

				while(true) {
					
					try {

						Thread.sleep(PriorityQueue.getStreamPacketLifetime());
						if(queue.getInQueue()!=null){
							num_packets.incrementAndGet();
						}
						
					} catch (InterruptedException e) {
					}
					
				}
				
			}});
		
		for(int x=1;x<100;x++) {
			
			MessageHeader msg = new MessageHeader(MessageHeader.MessageHeaderType.PingClose);
			queue.addInQueue(msg, PriorityQueue.Priority.low);
			
		}
		
		Thread.sleep(1000);
		
		assertEquals(0,queue.getInBuffSize());
		assertTrue(num_packets.get()<10);		
		
	}	

}
