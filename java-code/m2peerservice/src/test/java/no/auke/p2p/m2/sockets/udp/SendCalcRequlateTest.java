package no.auke.p2p.m2.sockets.udp;

//import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Test;


public class SendCalcRequlateTest {
		
	ExecutorService executor = Executors.newCachedThreadPool();

	class Measure {
	
		public boolean sendreply=false;
		public double speed=0;
		public double speed_measure=0;
		public long time=0;
		
		
		public Measure(boolean sendreply, double speed, double speed_measure){
			
			this.sendreply=sendreply;
			this.speed=speed;
			this.speed_measure=speed_measure;
			this.time= System.currentTimeMillis();
			
		}

		public String print(long fixedtime) {
			return (sendreply?"send":"reply") + " time " + String.valueOf(fixedtime - time) + " speed " + String.valueOf(speed) + " avg speed " + String.valueOf(speed_measure);
		}
		
	}
	
	
	public ArrayList<Measure> send_reply(final int MAX_SPEED, final int FIXED_SPEED, final int testlength, final double reply_delta) {


		final ConcurrentHashMap<Integer,Measure> measures = new ConcurrentHashMap<Integer,Measure>();
//
//    	final AtomicInteger counter=new AtomicInteger();
//    	final AtomicInteger prosessing=new AtomicInteger();
//    	prosessing.set(2);
//    	
//		final ISpeedCalc calc = new SpeedCalc2();
//		
//		// do at least on delay to make sure initiated
//		calc.doDelayPacketSend();
//
//		final long starttime = System.currentTimeMillis();
//
//		executor.execute(new Runnable(){
//
//			@Override
//			public void run() {
//				
//				while(System.currentTimeMillis() - starttime<testlength){
//					
//					calc.doDelayPacketSend();
//					measures.put(counter.incrementAndGet(),new Measure(true, calc.getSpeed(),0));
//					
//				}
//				prosessing.decrementAndGet();
//			}
//			
//			
//		});
//		
//		executor.execute(new Runnable(){
//			
//			@Override
//			public void run() {
//				
//				DelaySpeed delayspeed = new DelaySpeed((int) (FIXED_SPEED * reply_delta), FIXED_SPEED, MAX_SPEED, 512);
//			
//				while(System.currentTimeMillis() - starttime<testlength){
//					
//					delayspeed.doDelayPacketSend();
//					calc.replyChunk(0, 0, 512, false);
//					calc.calculate();
//					
//					measures.put(counter.incrementAndGet(),new Measure(false, delayspeed.getSpeed(), calc.getReplySpeedOverall(-1)));
//
//					
//				}
//				prosessing.decrementAndGet();
//					
//			}
//			
//			
//		});
//		
//		while(prosessing.get()>0){
//			
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//			}
//			
//		}
//		
//		executor.shutdownNow();
//		
		ArrayList<Measure> list = new ArrayList<Measure>();
		List<Integer> set = new LinkedList<Integer>(measures.keySet());
		Collections.sort(set);
		for (int i : set) {
			list.add(measures.get(i));
		}
		
		return list;
		

	}	
	
	@Test
	public void test_send_reply() {
		
		long fixedtime=System.currentTimeMillis();
		
		ArrayList<Measure> measures = send_reply(500,50,5000, 1.5);
		for(Measure measure:measures){
			System.out.println(measure.print(fixedtime));
		}
		
	}
	
	
}
