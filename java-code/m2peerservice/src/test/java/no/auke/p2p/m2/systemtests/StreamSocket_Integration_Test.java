package no.auke.p2p.m2.systemtests;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import no.auke.p2p.m2.SocketRetStatus;
import no.auke.p2p.m2.StreamSocket;
import no.auke.p2p.m2.StreamSocket.StreamPacket;
import no.auke.p2p.m2.StreamSocketListener;
import static org.junit.Assert.*;

public class StreamSocket_Integration_Test  {


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
	public void test_stream_send() {
		if(!isReadyToTest) return;
		final AtomicBoolean dosend = new AtomicBoolean();

		final AtomicInteger cnt_in = new AtomicInteger();
		final AtomicInteger cnt_out = new AtomicInteger();

		final AtomicInteger size_in = new AtomicInteger();
		final AtomicInteger size_out = new AtomicInteger();

		StreamSocket A = helper.clientA.openStream(2000,new StreamSocketListener(){

			@Override
			public void onIncomming(StreamPacket buffer) {

				cnt_in.incrementAndGet();
				size_in.addAndGet(buffer.getData().length);

				System.out.println("A recieve message " + String.valueOf(buffer.getData().length) + " num " + String.valueOf(cnt_in.get()));

			}

			@Override
			public void onNoData() {

				// TODO Auto-generated method stub

			}});

		assertTrue(A.isOpen());

		final StreamSocket B = helper.clientB.openStream(2000);

		assertTrue(B.isOpen());

		final Random rnd = new Random();
		
		helper.clientB.getMainServ().getExecutor().execute(new Runnable(){


				@Override
				public void run() {

					dosend.set(true);
					
					SocketRetStatus ret = B.findUser(helper.clientA.getClientid());

					while(dosend.get()) {

						byte[] data = new byte[rnd.nextInt(512)];
						rnd.nextBytes(data);

						
						ret = B.send(ret,data);
						if(ret.isOk()) { 
							cnt_out.incrementAndGet();
							size_out.addAndGet(data.length);
							System.out.println("B sent message " + String.valueOf(data.length)+" num " + String.valueOf(cnt_out.get()));
							
						} else {
							System.out.println("Error : B: "  + ret.getLastRetcode() + "-" + ret.getLastMessage());
						}

						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}

					}

				}});
		try {
			Thread.sleep(10000); //wait long enough for some packets to be sent
			dosend.set(false);
			int wait = 0;
			while(wait<10000) {//wait long enough to close the sockets
				System.out.println("A " + A.getSocketStatus() +", B " +B.getSocketStatus());
				Thread.sleep(5000);
				wait +=5000;
			}
			
			
		} catch (InterruptedException e) {
		}

		B.close();
		A.close();


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

		StreamSocketListener listenA = new StreamSocketListener(){

			@Override
			public void onIncomming(StreamPacket buffer) {

				cnt_in.incrementAndGet();
				size_in.addAndGet(buffer.getData().length);

				System.out.println("A recieve message " + String.valueOf(buffer.getData().length) + " num " + String.valueOf(cnt_in.get()));

			}

			@Override
			public void onNoData() {

				// TODO Auto-generated method stub

			}

		};

		StreamSocket A = helper.clientA.openStream(2000,listenA);
		assertTrue(A.isOpen());
		final StreamSocket B = helper.clientB.openStream(2000);
		assertTrue(B.isOpen());


		helper.clientB.getMainServ().getExecutor().execute(new Runnable(){


			@Override
			public void run() {

				dosend.set(true);

				SocketRetStatus ret = B.findUser(helper.clientA.getClientid());
				while(dosend.get()) {

					byte[] data = new byte[rnd.nextInt(512)];
					rnd.nextBytes(data);
					ret = B.send(ret,data);
					if(ret.isOk()) { 
						cnt_out.incrementAndGet();
						size_out.addAndGet(data.length);
						System.out.println("B sent message " + String.valueOf(data.length)+" num " + String.valueOf(cnt_out.get()));
						
					} else {
						System.out.println("Error : B: "  + ret.getLastRetcode() + "-" + ret.getLastMessage());
					}

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}

				}

			}});



		try {
			Thread.sleep(10000); //wait long enough for some packets to be sent
			dosend.set(false);
			int wait = 0;
			while(wait<10000) {//wait long enough to close the sockets
				System.out.println("A " + A.getSocketStatus() +", B " +B.getSocketStatus());
				Thread.sleep(5000);
				wait +=5000;
			}
			
			
		} catch (InterruptedException e) {
		}


		B.close();
		A.close();


		assertFalse(B.isOpen());
		assertFalse(A.isOpen());


		//open again
		System.out.println("Open 2 streaming clients again");

		A = helper.clientA.openStream(2000,listenA);
		assertTrue(A.isOpen());
		final StreamSocket B2 = helper.clientB.openStream(2000);
		assertTrue(B2.isOpen());



		helper.clientB.getMainServ().getExecutor().execute(new Runnable(){


			@Override
			public void run() {

				dosend.set(true);
				
				SocketRetStatus ret = B2.findUser(helper.clientA.getClientid());

				while(dosend.get()) {

					byte[] data = new byte[rnd.nextInt(512)];
					rnd.nextBytes(data);

					ret = B2.send(ret,data);
					if(ret.isOk()) { 
						cnt_out.incrementAndGet();
						size_out.addAndGet(data.length);
						System.out.println("B2 sent message " + String.valueOf(data.length)+" num " + String.valueOf(cnt_out.get()));
						
					} else {
						System.out.println("Error : B2: "  + ret.getLastRetcode() + "-" + ret.getLastMessage());
					}


					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}

				}

			}});

		try {
			Thread.sleep(10000); //wait long enough for some packets to be sent
			dosend.set(false);
			int wait = 0;
			while(wait<10000) {//wait long enough to close the sockets
				System.out.println("A " + A.getSocketStatus() +", B2 " +B2.getSocketStatus());
				Thread.sleep(5000);
				wait +=5000;
			}
			
			
		} catch (InterruptedException e) {
		}




		B2.close();
		A.close();


		assertFalse(B2.isOpen());
		assertFalse(A.isOpen());



		System.out.println("test_stream_reuse_listnener_send--------------------");
		System.out.println("Sent num messages " + String.valueOf(cnt_out.get()));
		System.out.println("Sent size messages " + String.valueOf(size_out.get()));

		System.out.println("Recieved num messages " + String.valueOf(cnt_in.get()));
		System.out.println("Recieved size messages " + String.valueOf(size_in.get()));

		assertEquals(cnt_out.get(),cnt_in.get());
		assertEquals(size_out.get(),size_in.get());


	}     

	@Test
	public void test_stream_noclientid_on_open_send() {
		if(!isReadyToTest) return;
		final AtomicBoolean dosend = new AtomicBoolean();

		final AtomicInteger cnt_in = new AtomicInteger();
		final AtomicInteger cnt_out = new AtomicInteger();

		final AtomicInteger size_in = new AtomicInteger();
		final AtomicInteger size_out = new AtomicInteger();

		StreamSocket A = helper.clientA.openStream(2000,new StreamSocketListener(){

			@Override
			public void onIncomming(StreamPacket buffer) {

				cnt_in.incrementAndGet();
				size_in.addAndGet(buffer.getData().length);

				System.out.println("A recieve message " + String.valueOf(buffer.getData().length) + " num " + String.valueOf(cnt_in.get()));

			}

			@Override
			public void onNoData() {

				// TODO Auto-generated method stub

			}});

		assertTrue(A.isOpen());

		final StreamSocket B = helper.clientB.openStream(2000);

		assertTrue(B.isOpen());

		final Random rnd = new Random();
		if(B.isOpen()) {

			helper.clientB.getMainServ().getExecutor().execute(new Runnable(){

				SocketRetStatus ret=new SocketRetStatus();
				@Override
				public void run() {

					dosend.set(true);
					
					SocketRetStatus ret = B.findUser(helper.clientA.getClientid());

					while(dosend.get()) {

						byte[] data = new byte[rnd.nextInt(512)];
						rnd.nextBytes(data);

						ret = B.send(ret,data);
						if(ret.isOk()) { 
							cnt_out.incrementAndGet();
							size_out.addAndGet(data.length);
							System.out.println("B sent message " + String.valueOf(data.length)+" num " + String.valueOf(cnt_out.get()));
							
						} else {
							System.out.println("Error : B: "  + ret.getLastRetcode() + "-" + ret.getLastMessage());
						}


						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}

					}

				}});
			try {
				Thread.sleep(10000); //wait long enough for some packets to be sent
				dosend.set(false);
				int wait = 0;
				while(wait<10000) {//wait long enough to close the sockets
					System.out.println("A " + A.getSocketStatus() +", B " +B.getSocketStatus());
					Thread.sleep(5000);
					wait +=5000;
				}
				
				
			} catch (InterruptedException e) {
			}



			B.close();
			A.close();

		}

		System.out.println("test_stream_noclientid_on_open_send--------------------");
		System.out.println("Sent num messages " + String.valueOf(cnt_out.get()));
		System.out.println("Sent size messages " + String.valueOf(size_out.get()));

		System.out.println("Recieved num messages " + String.valueOf(cnt_in.get()));
		System.out.println("Recieved size messages " + String.valueOf(size_in.get()));

		assertEquals(cnt_out.get(),cnt_in.get());
		assertEquals(size_out.get(),size_in.get());


	}     

	@Test
	public void test_stream_not_encrypted_send() {
		if(!isReadyToTest) return;
		final AtomicBoolean dosend = new AtomicBoolean();

		final AtomicInteger cnt_in = new AtomicInteger();
		final AtomicInteger cnt_out = new AtomicInteger();

		final AtomicInteger size_in = new AtomicInteger();
		final AtomicInteger size_out = new AtomicInteger();

		helper.clientA.setSessionEncryption(false);
		helper.clientB.setSessionEncryption(false);

		final String testmessage="sdfsdf sdfsdv sdfetryngbfn sdfx sdfsdretew  sdfsdfwe5wf sdfrwer3453  werwer345g werwe532 wer343523";

		StreamSocket A = helper.clientA.openStream(2000,new StreamSocketListener(){

			@Override
			public void onIncomming(StreamPacket buffer) {

				String in = new String(buffer.getData());
				if (in.equals(testmessage)) {

					cnt_in.incrementAndGet();
					size_in.addAndGet(buffer.getData().length);

					System.out.println("A recieve message " + String.valueOf(buffer.getData().length) + " num " + String.valueOf(cnt_in.get()));

				}

			}

			@Override
			public void onNoData() {

				// TODO Auto-generated method stub

			}});

		assertTrue(A.isOpen());

		final StreamSocket B = helper.clientB.openStream(2000);

		assertTrue(B.isOpen());

		final Random rnd = new Random();
		if(B.isOpen()) {

			helper.clientB.getMainServ().getExecutor().execute(new Runnable(){

				SocketRetStatus ret=new SocketRetStatus();
				@Override
				public void run() {

					dosend.set(true);
					
					SocketRetStatus ret = B.findUser(helper.clientA.getClientid());

					while(dosend.get()) {

						byte[] data = testmessage.getBytes();

					
						ret = B.send(ret,data);
						if(ret.isOk()) { 
							cnt_out.incrementAndGet();
							size_out.addAndGet(data.length);
							System.out.println("B sent message " + String.valueOf(data.length)+" num " + String.valueOf(cnt_out.get()));
							
						} else {
							System.out.println("Error : B: "  + ret.getLastRetcode() + "-" + ret.getLastMessage());
						}


						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}

					}

				}});

			try {
				Thread.sleep(10000); //wait long enough for some packets to be sent
				dosend.set(false);
				int wait = 0;
				while(wait<10000) {//wait long enough to close the sockets
					System.out.println("A " + A.getSocketStatus() +", B " +B.getSocketStatus());
					Thread.sleep(5000);
					wait +=5000;
				}
				
				
			} catch (InterruptedException e) {
			}


			B.close();
			A.close();

		}

		System.out.println("test_stream_not_encrypted_send--------------------");
		System.out.println("Sent num messages " + String.valueOf(cnt_out.get()));
		System.out.println("Sent size messages " + String.valueOf(size_out.get()));

		System.out.println("Recieved num messages " + String.valueOf(cnt_in.get()));
		System.out.println("Recieved size messages " + String.valueOf(size_in.get()));

		assertEquals(cnt_out.get(),cnt_in.get());
		assertEquals(size_out.get(),size_in.get());


	} 

	@Test
	public void test_stream_send_both_direction() throws InterruptedException {
		if(!isReadyToTest) return;
		final AtomicBoolean dosend = new AtomicBoolean();

		final AtomicInteger cnt_in = new AtomicInteger();
		final AtomicInteger cnt_out = new AtomicInteger();

		final AtomicInteger size_in = new AtomicInteger();
		final AtomicInteger size_out = new AtomicInteger();

		final StreamSocket A = helper.clientA.openStream(2000, new StreamSocketListener(){

			@Override
			public void onIncomming(StreamPacket buffer) {

				cnt_in.incrementAndGet();
				size_in.addAndGet(buffer.getData().length);
				System.out.println("A recieve message " + String.valueOf(buffer.getData().length) + " num " + String.valueOf(cnt_in.get()));

			}

			@Override
			public void onNoData() {

				// TODO Auto-generated method stub

			}});

		assertTrue(A.isOpen());

		final StreamSocket B = helper.clientB.openStream(2000,new StreamSocketListener(){

			@Override
			public void onIncomming(StreamPacket buffer) {

				cnt_in.incrementAndGet();
				size_in.addAndGet(buffer.getData().length);
				System.out.println("B recieve message " + String.valueOf(buffer.getData().length) + " num " + String.valueOf(cnt_in.get()));

			}

			@Override
			public void onNoData() {

				// TODO Auto-generated method stub

			}});

		assertTrue(B.isOpen());

		final Random rnd = new Random();
		if(B.isOpen() && A.isOpen()) {

			dosend.set(true);

			helper.clientB.getMainServ().getExecutor().execute(new Runnable(){

				@Override
				public void run() {

					SocketRetStatus ret = B.findUser(helper.clientA.getClientid());
					
					while(dosend.get()) {

						byte[] data = new byte[rnd.nextInt(512)];

						rnd.nextBytes(data);

						ret = B.send(ret,data);
						if(ret.isOk()) { 
							cnt_out.incrementAndGet();
							size_out.addAndGet(data.length);
							System.out.println("B sent message " + String.valueOf(data.length)+" num " + String.valueOf(cnt_out.get()));
							
						} else {
							System.out.println("Error : B: "  + ret.getLastRetcode() + "-" + ret.getLastMessage());
						}
						
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}

					}

				}});

			
			helper.clientA.getMainServ().getExecutor().execute(new Runnable(){

				@Override
				public void run() {

					SocketRetStatus ret = B.findUser(helper.clientB.getClientid());
					while(dosend.get()) {

						byte[] data = new byte[rnd.nextInt(512)];

						rnd.nextBytes(data);

						ret = A.send(ret,data);
						if(ret.isOk()) { 
							cnt_out.incrementAndGet();
							size_out.addAndGet(data.length);
							System.out.println("A sent message " + String.valueOf(data.length)+" num " + String.valueOf(cnt_out.get()));
							
						} else {
							System.out.println("Error : A: "  + ret.getLastRetcode() + "-" + ret.getLastMessage());
						}


						
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}

					}

				}});
			

		}

		try {
			Thread.sleep(10000); //wait long enough for some packets to be sent
			dosend.set(false);
			int wait = 0;
			while(wait<10000) {//wait long enough to close the sockets
				System.out.println("A " + A.getSocketStatus() +", B " +B.getSocketStatus());
				Thread.sleep(5000);
				wait +=5000;
			}
			
			
		} catch (InterruptedException e) {
		}


		B.close();
		A.close();
		
		System.out.println("test_stream_send_both_direction--------------------");
		System.out.println("Sent num messages " + String.valueOf(cnt_out.get()));
		System.out.println("Sent size messages " + String.valueOf(size_out.get()));

		System.out.println("Recieved num messages " + String.valueOf(cnt_in.get()));
		System.out.println("Recieved size messages " + String.valueOf(size_in.get()));

		assertEquals(cnt_out.get(),cnt_in.get());
		assertEquals(size_out.get(),size_in.get());

	}    

}

