package no.auke.p2p.m2.systemtests;


import org.junit.After;
import org.junit.Before;
//import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.UUID;

import no.auke.p2p.m2.InitVar;
import no.auke.p2p.m2.NameSpace;

//@RunWith(PowerMockRunner.class)


public class ConnectionTest {

	
	TestServiceHelper helper = new TestServiceHelper();
	
	public TestServiceServer clientA = new TestServiceServer("A");
		
	@Before 
	public void setUp() {}

	@After
	public void tearDown() {helper.closeTestClients();}
	
	@Test
	public void test_start_service() throws InterruptedException {
		
		//Huy: rewrite test		
		startPeerAndWait(
				"xx" + UUID.randomUUID().toString().substring(0, 6), 
				UUID.randomUUID().toString().substring(0, 6)
				);

		assertTrue(clientA.nsList.get(0).isConnected());
	
	}	
	
	private void startPeerAndWait(String ns, String clientId) throws InterruptedException {		
		
		for(int i=0;i<3;i++) {
			helper.CreateAndStartNewPeer(clientA,ns+i, 57005);
		}
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {				
				while(!clientA.nsList.get(clientA.nsList.size()-1).isConnected()) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		});		
		
		t.start();
		t.join(InitVar.START_WAIT);
	}
	
	
	@Test
	public void test_stop_service() throws InterruptedException {
		
		startPeerAndWait(
				"xx" + UUID.randomUUID().toString().substring(0, 6), 
				UUID.randomUUID().toString().substring(0, 6)
				);
		
		for(NameSpace peer:clientA.nsList) {
			assertTrue(peer.isRunning());
			assertTrue(peer.isConnected());
			peer.stop();
			assertTrue(!peer.isRunning());
			assertTrue(!peer.isConnected());
			
		}

	}
	

	
}
