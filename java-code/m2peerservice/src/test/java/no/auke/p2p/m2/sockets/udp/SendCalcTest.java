package no.auke.p2p.m2.sockets.udp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Test;

public class SendCalcTest {
	
	ExecutorService executor = Executors.newCachedThreadPool();

	@Test
	public void test_send_speed() {
		
//		for(InitVar.FIXED_SPEED=50;InitVar.FIXED_SPEED<=1000;InitVar.FIXED_SPEED+=100){
//			
//			InitVar.MAX_SPEED=100;
//			InitVar.SEND_RESEND_TIMEOUT=500;
//			InitVar.SEND_ABORT_TIMEOUT=3000;
//			
//			ISpeedCalc calc = new SpeedCalc2();
//		
//			long starttime = System.currentTimeMillis();
//
//			while(System.currentTimeMillis() - starttime < 2000){
//				
//				// calc.doDelayPacketSend();
//				
//			}
//			assertEquals("fail at speed " + String.valueOf(InitVar.FIXED_SPEED),(double)calc.getSpeed(),0,(double)calc.getSpeed()*0.5);
//			
//		}
		
	}
	
	@Test
	public void test_reply_speed() {

//		for(InitVar.FIXED_SPEED=50;InitVar.FIXED_SPEED<=1000;InitVar.FIXED_SPEED+=100){
//			
//			InitVar.MAX_SPEED=100;
//			InitVar.SEND_RESEND_TIMEOUT=500;
//			InitVar.SEND_ABORT_TIMEOUT=3000;
//			
//			ISpeedCalc calc = new SpeedCalc2();
//		
//			long starttime = System.currentTimeMillis();
//
//			while(System.currentTimeMillis() - starttime<2000){
//				
//				for(int i=0;i<8;i++){
//
//					calc.doDelayPacketSend();
//					
//				}
//				calc.replyChunk(0, 0, 512 * 8, false);
//				
//			}
//			//assertEquals("fail at speed " + String.valueOf(InitVar.FIXED_SPEED),(double)calc.getSpeed(),calc.getReplySpeedOverall(-1),(double)calc.getSpeed()*0.5);
//			
//		} 

		
	}	
	
	
	@Test
	public void test_send_reply_speed_concurrent() {


//    	final AtomicInteger prosessing=new AtomicInteger();
//    	prosessing.set(2);
//    	
//		InitVar.FIXED_SPEED=50;
//		
//		InitVar.MAX_SPEED=100;
//		InitVar.SEND_RESEND_TIMEOUT=500;
//		InitVar.SEND_ABORT_TIMEOUT=3000;
//		
//		final ISpeedCalc calc = new SpeedCalc2();
//		
//		executor.execute(new Runnable(){
//
//			@Override
//			public void run() {
//				
//				long starttime = System.currentTimeMillis();
//
//				while(System.currentTimeMillis() - starttime < 5000){
//					calc.doDelayPacketSend();
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
//				long starttime = System.currentTimeMillis();
//				while(System.currentTimeMillis() - starttime<5000){
//					
//					for(int i=0;i<8;i++){
//						calc.doDelayPacketSend();
//					}
//					calc.replyChunk(0, 0, 512 * 8, false);
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
//		assertEquals("fail at speed " + String.valueOf(InitVar.FIXED_SPEED),(double)calc.getSpeed(),0,(double)calc.getSpeed()*0.5);
//		//assertEquals("fail at reply speed " + String.valueOf(InitVar.FIXED_SPEED),(double)calc.getSpeed(),calc.getReplySpeedOverall(-1),(double)calc.getSpeed()*0.5);
//		
//		executor.shutdownNow();
		

	}	
	
	
}
