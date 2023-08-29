package no.auke.p2p.m2.systemtests;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import no.auke.p2p.m2.InitVar;
import no.auke.p2p.m2.NameSpace;
import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.Socket.Packet;
import no.auke.p2p.m2.SocketListener;
import no.auke.p2p.m2.SocketRetStatus;
import static org.junit.Assert.*;

public class Socket_Integration_Test  {

    
	TestServiceHelper helper = new TestServiceHelper();
	boolean isReadyToTest= false;
	
	
	@Before 
	public void setUp() {

		isReadyToTest = helper.initNewTestClients();

	}

	@After
	public void tearDown() {
		helper.closeTestClients();
	}
    
    @Test
    public void test_send() {
    	if(!isReadyToTest) return;
    	final AtomicBoolean dosend = new AtomicBoolean();

    	final AtomicInteger cnt_in = new AtomicInteger();
    	final AtomicInteger cnt_out = new AtomicInteger();

    	final AtomicInteger size_in = new AtomicInteger();
    	final AtomicInteger size_out = new AtomicInteger();
    	
    	Socket A = helper.clientA.openSocket(2000,new SocketListener(){

			@Override
			public void onIncomming(byte[] buffer) {
	        	
				cnt_in.incrementAndGet();
	        	size_in.addAndGet(buffer.length);

	        	System.out.println("A recieve message " + String.valueOf(buffer.length) + " num " + String.valueOf(cnt_in.get()));
				
				
			}

			@Override
			public boolean onIncommingPacket(Packet packet) {
				// TODO Auto-generated method stub
				return false;
			}

    	});

    	assertTrue(A.isOpen());
    	
    	final Socket B = helper.clientB.openSocket(2000);

    	assertTrue(B.isOpen());
    	
    	final Random rnd = new Random();
    	if(B.isOpen()) {
    		
    		helper.clientB.getMainServ().getExecutor().execute(new Runnable(){

    			@Override
    			public void run() {
    				
    				dosend.set(true);
    				
    		    	while(dosend.get()) {

    		    		byte[] data = new byte[rnd.nextInt(512)];
    		    		rnd.nextBytes(data);
    		    		
    		    		SocketRetStatus ret = B.send(helper.clientA.getClientid(),data);
    		    		if(!ret.isOk()) {

    		    			System.out.println("B sent message error " + String.valueOf(ret.getLastRetcode()));
    		    		} else {
    		    		
    		    			cnt_out.incrementAndGet();
        		        	size_out.addAndGet(data.length);
    		    		}
    		    		try {
    		    			Thread.sleep(100);
    		    		} catch (InterruptedException e) {
						}
    		    		
    		    	}
    				
    			}});
        	
        	try {
        		Thread.sleep(10000);
        		dosend.set(false);
        		Thread.sleep(10000);
        	} catch (InterruptedException e) {
    		}

        	B.close();
        	A.close();
    		
    	}
    	
    	System.out.println("test_stream_send--------------------");
    	System.out.println("Sent num messages " + String.valueOf(cnt_out.get()));
    	System.out.println("Sent size messages " + String.valueOf(size_out.get()));

    	System.out.println("Recieved num messages " + String.valueOf(cnt_in.get()));
    	System.out.println("Recieved size messages " + String.valueOf(size_in.get()));
    	
    	assertEquals(cnt_out.get(),cnt_in.get());
    	assertEquals(size_out.get(),size_in.get());
    	
    	
    } 

    @Test
    public void test_fixed_remote_send() {
    	if(!isReadyToTest) return;
    	final AtomicBoolean dosend = new AtomicBoolean();

    	final AtomicInteger cnt_in = new AtomicInteger();
    	final AtomicInteger cnt_out = new AtomicInteger();

    	final AtomicInteger size_in = new AtomicInteger();
    	final AtomicInteger size_out = new AtomicInteger();
    	
    	Socket A = helper.clientA.openSocket(2000,new SocketListener(){

			@Override
			public void onIncomming(byte[] buffer) {
	        	
				cnt_in.incrementAndGet();
	        	size_in.addAndGet(buffer.length);

	        	System.out.println("A recieve message " + String.valueOf(buffer.length) + " num " + String.valueOf(cnt_in.get()));
				
				
			}

			@Override
			public boolean onIncommingPacket(Packet packet) {
				// TODO Auto-generated method stub
				return false;
			}

    	});

    	assertTrue(A.isOpen());
    	
    	final Socket B = helper.clientB.openSocket(2000);

    	assertTrue(B.isOpen());
    	
    	final Random rnd = new Random();
    	if(B.isOpen()) {
    		
    		helper.clientB.getMainServ().getExecutor().execute(new Runnable(){

    			@Override
    			public void run() {
    				
    				dosend.set(true);
    				
    		    	while(dosend.get()) {

    		    		byte[] data = new byte[rnd.nextInt(512)];
    		    		rnd.nextBytes(data);
    		    		
    		    		SocketRetStatus ret = B.send(helper.clientA.getClientid(),data);
    		    		if(!ret.isOk()) {

    		    			System.out.println("B sent message error " + String.valueOf(ret.getLastRetcode()));
            		    	
    		    		
    		    		}

    		    		else {
        		    		
    		    			cnt_out.incrementAndGet();
        		        	size_out.addAndGet(data.length);
    		    		}
    		    			
    		    		try {
    		    			Thread.sleep(100);
    		    		} catch (InterruptedException e) {
						}
    		    		
    		    	}
    				
    			}});
        	
        	try {
        		Thread.sleep(10000);
        		dosend.set(false);
        		Thread.sleep(10000);
        	} catch (InterruptedException e) {
    		}

        	B.close();
        	A.close();
    		
    	}
    	
    	System.out.println("test_stream_send--------------------");
    	System.out.println("Sent num messages " + String.valueOf(cnt_out.get()));
    	System.out.println("Sent size messages " + String.valueOf(size_out.get()));

    	System.out.println("Recieved num messages " + String.valueOf(cnt_in.get()));
    	System.out.println("Recieved size messages " + String.valueOf(size_in.get()));
    	
    	assertEquals(cnt_out.get(),cnt_in.get());
    	assertEquals(size_out.get(),size_in.get());
    	
    	
    } 
    
    @Test
    public void test_stream_reuse_listnener_send() {
    	if(!isReadyToTest) return;
    	final AtomicBoolean dosend = new AtomicBoolean();

    	final AtomicInteger cnt_in = new AtomicInteger();
    	final AtomicInteger cnt_out = new AtomicInteger();

    	final AtomicInteger size_in = new AtomicInteger();
    	final AtomicInteger size_out = new AtomicInteger();

		final Random rnd = new Random();
    	
    	SocketListener listenA = new SocketListener(){

			@Override
			public void onIncomming(byte[] buffer) {
	        	cnt_in.incrementAndGet();
	        	size_in.addAndGet(buffer.length);

	        	System.out.println("A recieve message " + String.valueOf(buffer.length) + " num " + String.valueOf(cnt_in.get()));

				
			}

			@Override
			public boolean onIncommingPacket(Packet packet) {
				// TODO Auto-generated method stub
				return false;
			}
			
    	};
    	
		Socket A = helper.clientA.openSocket(2000,listenA);
    	assertTrue(A.isOpen());
    	final Socket B = helper.clientB.openSocket(2000);
    	assertTrue(B.isOpen());
    	
    	if(B.isOpen()) {

    		helper.clientB.getMainServ().getExecutor().execute(new Runnable(){

        		SocketRetStatus ret=new SocketRetStatus();
    			@Override
    			public void run() {
    				
    				dosend.set(true);
    				
    		    	while(dosend.get()) {

    		    		byte[] data = new byte[rnd.nextInt(512)];
    		    		rnd.nextBytes(data);
    		    		
    		        	assertTrue("can not send B",B.send(helper.clientA.getClientid(),data).isOk()); 
    		        	System.out.println("B sent message " + String.valueOf(data.length));
        		    	cnt_out.incrementAndGet();
        		        size_out.addAndGet(data.length);
    		    			
    		    		try {
    		    			Thread.sleep(100);
    		    		} catch (InterruptedException e) {
						}
    		    		
    		    	}
    				
    			}});
        	
        	
        	
        	try {
        		Thread.sleep(10000);
        		dosend.set(false);
        		Thread.sleep(10000);
        	} catch (InterruptedException e) {
    		}
        	

        	B.close();
        	A.close();
        	
    		Socket A2 = helper.clientA.openSocket(2000,listenA);
        	assertTrue(A2.isOpen());
        	final Socket B2 = helper.clientB.openSocket(2000);
        	assertTrue(B2.isOpen());
        	
        	if(B2.isOpen()) {

            	helper.clientB.getMainServ().getExecutor().execute(new Runnable(){

            		SocketRetStatus ret=new SocketRetStatus();
        			@Override
        			public void run() {
        				
        				dosend.set(true);
        				
        		    	while(dosend.get()) {

        		    		byte[] data = new byte[rnd.nextInt(512)];
        		    		rnd.nextBytes(data);
        		    		
        		        	assertTrue("can not send B2",B2.send(helper.clientA.getClientid(),data).isOk()); 
        		        	System.out.println("B2 sent message " + String.valueOf(data.length));

            		    	cnt_out.incrementAndGet();
            		        size_out.addAndGet(data.length);
        		    			
        		    		try {
        		    			Thread.sleep(100);
        		    		} catch (InterruptedException e) {
    						}
        		    		
        		    	}
        				
        			}});
            	
            	try {
            		Thread.sleep(10000);
            		dosend.set(false);
            		Thread.sleep(10000);
            	} catch (InterruptedException e) {
        		}
            	

            	B2.close();
            	A2.close();
        	
        	}
    		
    	}
    	
    	System.out.println("test_stream_send--------------------");
    	System.out.println("Sent num messages " + String.valueOf(cnt_out.get()));
    	System.out.println("Sent size messages " + String.valueOf(size_out.get()));

    	System.out.println("Recieved num messages " + String.valueOf(cnt_in.get()));
    	System.out.println("Recieved size messages " + String.valueOf(size_in.get()));
    	
    	assertEquals(cnt_out.get(),cnt_in.get());
    	assertEquals(size_out.get(),size_in.get());
    	
    }     
    
    @Test
    public void test_no_encryption_send() {
    	if(!isReadyToTest) return;
    	final AtomicBoolean dosend = new AtomicBoolean();

    	final AtomicInteger cnt_in = new AtomicInteger();
    	final AtomicInteger cnt_out = new AtomicInteger();

    	final AtomicInteger size_in = new AtomicInteger();
    	final AtomicInteger size_out = new AtomicInteger();
    	
    	InitVar.DO_SESSION_ENCYPTION=false;
    	
    	final String message = "sadfasfsdsfsfsdf  sdf s df sdf sdf sdfsdfsdf s df sdf sdf sdf";
    	
    	Socket A = helper.clientA.openSocket(2000,new SocketListener(){

			@Override
			public void onIncomming(byte[] buffer) {
				
				if(new String(buffer).equals(message)) {

					cnt_in.incrementAndGet();
		        	size_in.addAndGet(buffer.length);

		        	System.out.println("A recieve message " + String.valueOf(buffer.length) + " num " + String.valueOf(cnt_in.get()));					
				}
				
			}

			@Override
			public boolean onIncommingPacket(Packet packet) {
				// TODO Auto-generated method stub
				return false;
			}

    	});

    	assertTrue(A.isOpen());
    	
    	final Socket B = helper.clientB.openSocket(2000);

    	assertTrue(B.isOpen());
    	
    	//final Random rnd = new Random();
    	if(B.isOpen()) {
    		
        	helper.clientB.getMainServ().getExecutor().execute(new Runnable(){

    			@Override
    			public void run() {
    				
    				dosend.set(true);
    				
    		    	while(dosend.get()) {

    		    		byte[] data = message.getBytes();
    		    		
    		    		SocketRetStatus ret = B.send(helper.clientA.getClientid(),data);
    		    		if(!ret.isOk()) {

    		    			System.out.println("B sent message error " + String.valueOf(ret.getLastRetcode()));
            		    	
    		    		
    		    		} else {
        		    		
    		    			cnt_out.incrementAndGet();
        		        	size_out.addAndGet(data.length);
    		    		}
    		    			
    		    		try {
    		    			Thread.sleep(100);
    		    		} catch (InterruptedException e) {
						}
    		    		
    		    	}
    				
    			}});
        	
        	try {
        		Thread.sleep(10000);
        		dosend.set(false);
        		Thread.sleep(10000);
        	} catch (InterruptedException e) {
    		}

        	B.close();
        	A.close();
    		
    	}
    	
    	System.out.println("test_stream_send--------------------");
    	System.out.println("Sent num messages " + String.valueOf(cnt_out.get()));
    	System.out.println("Sent size messages " + String.valueOf(size_out.get()));

    	System.out.println("Recieved num messages " + String.valueOf(cnt_in.get()));
    	System.out.println("Recieved size messages " + String.valueOf(size_in.get()));
    	
    	assertEquals(cnt_out.get(),cnt_in.get());
    	assertEquals(size_out.get(),size_in.get());
    	
    	
    }     
 
    
}

