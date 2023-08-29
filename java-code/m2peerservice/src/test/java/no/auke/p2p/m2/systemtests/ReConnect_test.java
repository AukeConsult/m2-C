package no.auke.p2p.m2.systemtests;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

//import static org.junit.Assert.*;

import no.auke.p2p.m2.InitVar;

public class ReConnect_test {
	
	TestServiceHelper helper = new TestServiceHelper();
	boolean isReadyToTest= false;
	
	
	@Before 
	public void setUp() {
		isReadyToTest = helper.initNewTestClients();
	}
	
	private void sendtest() {

		List<ITestSend> list = new ArrayList<ITestSend>();
		for(int i=0; i<100; i++) {
			list.add(helper.startBuiltInSendTest("socket " + String.valueOf(2000 +i), 2000 + i, 10, 100000));
		}
		final AtomicBoolean isrunning = new AtomicBoolean();
		
		isrunning.set(true);
		
		final AtomicInteger reconnects = new AtomicInteger();
		
		new Thread(new Runnable(){

			@Override
			public void run() {
				
				while(isrunning.get()) {

					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
					}
					
					System.out.println("DO RECONNECT " + reconnects.get());					
					helper.clientA.getReConnector().reConnect();

					reconnects.incrementAndGet();
				

				}
				
			}}).start();
		
		helper.waitUntilFinishAndCheck(list);
		isrunning.set(false);
		
	}

	@After
	public void tearDown() {helper.closeTestClients();}
	
	@Test
	public void test_socket_sending_with_forces_reconnect_no_direct() {

		InitVar.PEER_DO_DIRECT_CONNECT=false;
		
		if(!isReadyToTest) return;
		System.out.println("test_socket_sending_with_forces_reconnect_no_direct ->>>>>>>>>>>>>>>>>>>>>> ");
		
		sendtest();
		
		
	}

	@Test
	public void test_socket_sending_with_forces_reconnect() {

		InitVar.PEER_DO_DIRECT_CONNECT=true;
		InitVar.DEBUG_LEVEL=1;

		if(!isReadyToTest) return;
		System.out.println("test_socket_sending_with_forces_reconnect ->>>>>>>>>>>>>>>>>>>>>> ");
		
		sendtest();
		
	}	

	
	
	
}
