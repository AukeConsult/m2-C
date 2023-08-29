package no.auke.p2p.m2.systemtests;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import no.auke.p2p.m2.SocketRetStatus;
import no.auke.p2p.m2.sockets.*;
import no.auke.util.StopWatch;
import no.auke.util.StringConv;
import static java.lang.System.out;
import static org.junit.Assert.*;

public class AsyncSocket_Integration_Test {
	
	TestServiceHelper helper = new TestServiceHelper();
	
	public class TransTest extends ISyncDefaultTrans {

		public TransTest(byte[] buffer) {
			super(buffer);
		}

		public TransTest(String retur){
			super(retur);
		}		
		@Override
		public void commit() {
			
		}

		@Override
		public void rollback() {
			
		}
	}
	
	
	
	ITransacationSession sessionA;
	ITransacationSession sessionB;
	
	Random rnd = new Random();
	
	TransactionSocket socketA;
	TransactionSocket socketB;
	
	List<String> sendQueueA = new LinkedList<String>();
	List<String> receiveQueueA = new LinkedList<String>();
	int incomingQueueA = 0;
	
	List<String> sendQueueB = new LinkedList<String>();
	List<String> receiveQueueB = new LinkedList<String>();
	int incomingQueueB = 0;
	
	boolean isReadyToTest= false;
	@Before
	public void setUp() {
		
		
		isReadyToTest=helper.initNewTestClients();
		
		if(!isReadyToTest) return;
		
		while(!helper.clientA.isConnected() && !helper.clientB.isConnected()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		//open socket from peerA
		socketA = new TransactionSocket() {
			
			@Override
			public Transaction onTransFunctionMessage(String fromClientId,
					String function, final byte[] data) {

				if(function.equals("simulate_lost")) {
					
					if(new Random().nextBoolean()) {
						return null;
					
					} else {
						
						incomingQueueA++;
						return new Trans(data); 
					}
				
				} else {
					
					incomingQueueA++;
					if(function.equals("echo")) {

						return new TransTest(data);

					} else {

						return new TransTest("ok");
					}
				}
				
			}
			
			@Override
			public byte[] onFunctionMessage(String fromClientId, String function,
					byte[] data) {
				return null;
			}

		};
		
		//open port
		socketA.open(helper.clientA, 5000);
		sessionA = socketA.openSession(helper.clientB.getClientid(), 5000, new TransactionSocket.IGotReply() {
			
			@Override
			public void onGetResult(String sessionId, String from, String function, byte[] data) {
				String echo = StringConv.UTF8(data);
				
				//System.out.println(peerB.getClientid() + " :echo: " +  echo);
				receiveQueueA.add(echo);
				//System.out.println(peerB.getClientid() + " :echo: " + String.valueOf(receiveQueueA.size()));
                if(receiveQueueA.size()>sendQueueB.size()){
                    
                }
				
			}
		});
		
		//open socket from peerB
		socketB = new TransactionSocket() {
			
			@Override
			public Transaction onTransFunctionMessage(String fromClientId,
					String function, final byte[] data) {

				if(function.equals("simulate_lost")) {
					
					if(new Random().nextBoolean()) {
						
						return null;
						
					} else {
						
						incomingQueueB++;
						return new TransTest(data); 
					}
					
				} else {
					
					incomingQueueB++;

					if(function.equals("echo")) {

						return new TransTest(data);

					} else {

						return new TransTest("ok");
					}
				}
				
			}
			
			@Override
			public byte[] onFunctionMessage(String fromClientId, String function,
					byte[] data) {
				return null;
			}
			
		};
		
		socketB.open(helper.clientB, 5000);
		sessionB = socketB.openSession(helper.clientA.getClientid(), 5000, new TransactionSocket.IGotReply() {
			
			@Override
			public void onGetResult(String sessionId, String from, String function, byte[] data) {
				String echo = StringConv.UTF8(data);
				receiveQueueB.add(echo);
				// System.out.println(peerA.getClientid() + " :echo: " + String.valueOf(receiveQueueB.size()));

			}
			
		});
		
	}
	
	@After
	public void tearDown() {
		
		helper.closeTestClients();
		if(!isReadyToTest) return;
		
		sendQueueA.clear();
		sendQueueB.clear();
		incomingQueueA=0;
		
		receiveQueueB.clear();
		receiveQueueB.clear();
		incomingQueueB=0;
		
		socketA.close();
		socketB.close();
		
		
		
	}
	
	@Test
	public void test_FireAndGetEcho() throws InterruptedException {
		
		if(!isReadyToTest) return;
		int count = 100;
		StopWatch watch = new StopWatch();
		//see how long all messages are fired
		
		watch.start();
		for(int i=1; i <= count; i++) {
			
			String msg = getMsg(100);	
			
			SocketRetStatus ret  = sessionA.fire("echo", StringConv.getBytes(msg));
			if(ret.isOk()) {
				sendQueueA.add(msg);
			}
//			while(true) {
//				SocketRetStatus ret = sessionA.fire("echo", StringConv.getBytes(msg));
//				if(ret.isOk()) {
//					out.println(peerA.getClientid() + " :fire: " + msg.length());
//					break;
//				} else {
//					out.println(peerA.getClientid() + " :fire_error: " + ret.getLastMessage() + ", resending...");
//					Thread.sleep(100);
//				}
//			}
		}
		
		//some wait due to async
		watch.stop();
		System.out.println("test_FireAndGetEcho - messages " + String.valueOf(count)+ " Fire all after " + watch.getElapsedTime() + " ms");
		//check result
		checkReplyOk(sendQueueA, receiveQueueA);
		
		Thread.sleep(1000);
		
	}
	
	@Test
	public void test_FireAndGetEcho_2PeersFire_AtSameTime() throws InterruptedException {
		if(!isReadyToTest) return;
		
		System.out.println("test_FireAndGetEcho_2PeersFire_AtSameTime---------");
		
		int count = 20;
		StopWatch watch = new StopWatch();
		
		//see how long all messages are fired
		watch.start();
		try {
		
			for(int i=1; i <= count; i++) {
				
				String msg = getMsg(10);
				
	            if(receiveQueueA.size()<=sendQueueA.size()){
	            	
	            	
	    			
	                //out.println(peerA.getClientid() + " :fire: " + String.valueOf(sendQueueB.size()) + " :echo: " + String.valueOf(receiveQueueA.size()));
	            	sendQueueA.add(msg);

	    			
	    			while(true) {
	    				SocketRetStatus ret = sessionA.fire("echo", StringConv.getBytes(msg));
	    				if(ret.isOk()) {
	    					out.println(helper.clientA.getClientid() + " :fire: " + msg.length());
	    					break;
	    				} else {
	    					out.println(helper.clientA.getClientid() + " :fire_error: " + ret.getLastMessage() + ", resending...");
	    					Thread.sleep(100);
	    				}
	    			}  
	                
	            } 			
				
	            if(receiveQueueB.size()<=sendQueueB.size()) {
	
	            	sendQueueB.add(msg);

	                out.println(helper.clientB.getClientid() + " :fire: " + String.valueOf(sendQueueB.size()) + " :echo: " + String.valueOf(receiveQueueB.size()));
	                
	    			
	    			while(true) {
	    				SocketRetStatus ret = sessionB.fire("echo", StringConv.getBytes(msg));
	    				if(ret.isOk()) {
	    					out.println(helper.clientB.getClientid() + " :fire: " + msg.length());
	    					break;
	    				} else {
	    					out.println(helper.clientB.getClientid() + " :fire_error: " + ret.getLastMessage() + ", resending...");
	    					Thread.sleep(100);
	    				}
	    			}
	    		
	                
	            } 
				
			}
			
			//some wait due to async
			watch.stop();
			System.out.println("test_FireAndGetEcho_2PeersFire_AtSameTime---------");
			
			out.println(helper.clientA.getClientid() + " fires all " + String.valueOf(sendQueueA.size()) + " msg(s) after " + watch.getElapsedTime() + " ms");
			out.println(helper.clientB.getClientid() + " fires all " + String.valueOf(sendQueueB.size()) + " msg(s) after " + watch.getElapsedTime() + " ms");
			
			//see how long wait for all echos
			checkReplyOk(sendQueueA, receiveQueueA);
			checkReplyOk(sendQueueB, receiveQueueB);
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		Thread.sleep(1000);
	}
	
	public void checkReplyOk(List<String> queueSend, List<String> queueReceive) throws InterruptedException {
		
		StopWatch watch = new StopWatch();
		//see how long wait for all echos
		watch.start();
		boolean ok = true;
		while(queueSend.size() > queueReceive.size()) {
			
			if(watch.getElapsedTimeSecs() == 60) {
				
				ok = queueReceive.size() >= queueSend.size();
				break;
			}
			Thread.sleep(100);
		}
		
		watch.stop();
		if(ok) {
			
			System.out.println("Got all " + queueReceive.size() + " echoes after " + watch.getElapsedTime() + " ms");
		
		} else {
			
			System.out.println("Sent : " + queueSend.size() + " but received: " + queueReceive.size());
		}
		//must get back same count for both sending / echoing messages
		assertTrue("Number of sending and receiving messages must be equal", ok);
		
	}
	
	@Test
	public void test_FireAndGetEcho_100_BIG_messages() throws InterruptedException {

		if(!isReadyToTest) return;
		System.out.println("test_FireAndGetEcho_100_BIG_messages " );

		int count = 100;
		
		StopWatch watch = new StopWatch();
		//see how long all messages are fired
		watch.start();
		for(int i=1; i <= count; i++) {
			
			String msg = getMsg(100);	
			sendQueueA.add(msg);
			
			
			while(true) {
				SocketRetStatus ret = sessionA.fire("echo", StringConv.getBytes(msg));
				if(ret.isOk()) {
					out.println(helper.clientA.getClientid() + " :fire: " + msg.length());
					break;
				} else {
					out.println(helper.clientA.getClientid() + " :fire_error: " + ret.getLastMessage() + ", resending...");
					Thread.sleep(100);
				}
			}
		}
		
		//some wait due to async
		watch.stop();
		System.out.println("test_FireAndGetEcho_100_BIG_messages - messages " + String.valueOf(count)+ " Fire all after " + watch.getElapsedTime() + " ms");
		
		//check result
		checkReplyOk(sendQueueA, receiveQueueA);
		
	}

	
	private String getMsg(int cntsize){
		
		StringBuilder msgbuild = new StringBuilder();
		
		String msg = "message as is s  ate sd asdf fdsd sdfdsf sdfsdf  sdf sdf dsf sdf sdf  sf" +
				"dfsdf sd fs df sd fs df sd fsdf sd fs df sdf sdf sdfsdf sdf s dfs fd sdf" +
				"2134234234" +
				"32 342344444444444444444444444444444444444444444444444444444444444444444444444444" +
				"sdffffffffffwersdfsdfsdfwerwfsdfwerwetyghjftyudfgvsfsdrewtfgqwewqeasasdsadqweqweqweqew" +
				"qweqweqwefsadsfdsafdsfsdf ";
		
		while(rnd.nextInt(cntsize)>0){				
			msgbuild.append(msg);			
		}
		
		return msgbuild.toString();
		
	}

}
