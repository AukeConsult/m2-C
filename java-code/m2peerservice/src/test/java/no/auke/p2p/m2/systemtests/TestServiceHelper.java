package no.auke.p2p.m2.systemtests;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import no.auke.p2p.m2.InitVar;
import no.auke.p2p.m2.NameSpace;
import no.auke.p2p.m2.PeerServer;
import no.auke.p2p.m2.general.IPeerServerListener;
import no.auke.p2p.m2.general.LicenseReasons;
import no.auke.p2p.m2.workers.keepalive.NetAddress;

public class TestServiceHelper {
		
	public static String APPID="m2test";
	public static String NAMESPACE="m2test";
		
	public static String LOCAL_BOOTADDRESS = "";
	public static String USERDIR=System.getProperty("user.dir") + "/run_test";
	public static int DEBUG=InitVar.DEBUG_LEVEL;
	public static int ENCRYPTION=0;
	
	String localaddress = "";	
	
	public NameSpace clientA;
	public NameSpace clientB;

	public TestServiceServer serverA = new TestServiceServer("A");
	public TestServiceServer serverB = new TestServiceServer("B");	
	
	public ExecutorService executor = Executors.newCachedThreadPool();
		
	public TestServiceHelper() {
		InitVar.USE_TEST_KA=true;
		executor = Executors.newCachedThreadPool();
	}
	
	public void rest(int i) {
		try {
			Thread.sleep(i *  10);
		} catch (InterruptedException e) {
		}
	}
		
	public NameSpace CreateAndStartNewPeer(TestServiceServer server, String namespaceId, int port) {
		
		if(server.peerserver==null) {
						
			server.peerserver = new PeerServer( 
					APPID, 
					USERDIR+"/"+server.clientId,
					port,
						new IPeerServerListener(DEBUG){
							public void printLog(String message) {
								System.out.println(message);
							}
							public void onServiceConnected(NetAddress publicAddress, NetAddress kaServerAddress) {}
							public void connectionRejected(NetAddress kaServerAddress, String msg) {assertTrue(msg,false);}
				            @Override
				            public void onLicenseError(LicenseReasons reason, String licenseKey) {}
							@Override
							public void onServiceStarted(String message) {}
							@Override
							public void onServiceStopped(String message) {}
						}
					);
			
		}				
		
		NameSpace namespace = server.peerserver.openNameSpace(namespaceId);				
		//server.setGlobalMonitors(globalMonitors);
		server.nsList.add(namespace);
		
		namespace.start(server.clientId);
		assertTrue("not running from start ", namespace.isRunning());
		
		return namespace;
		
	}
	

	public TestBuiltIn_SendMechanism startBuiltInSendTest(String testmessage, int port, int nummessages, int messagesize) {
		
		TestBuiltIn_SendMechanism instance = new TestBuiltIn_SendMechanism(clientA, clientB, testmessage, port, nummessages, messagesize);
		executor.execute(instance);
		return instance;
		
	}
	
	public TestSendWith_ISendTask startSendTestWithISendTask(String testmessage, int port, int nummessages, int messagesize) {
		
		TestSendWith_ISendTask instance = new TestSendWith_ISendTask(clientA, clientB, testmessage, port, nummessages, messagesize);
		executor.execute(instance);
		return instance;
		
	}
	
	public void waitUntilFinishAndCheck(List<ITestSend> list) {
		
		boolean stillrunning=true;
		while(stillrunning) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
			stillrunning=false;
			int cnt=0;
			for (ITestSend test : list) {				
				if(!test.getFinish()){
					System.out.println(test.getTestMessage() + " send queue=" + ((TestBuiltIn_SendMechanism)test).queue.size() + " Not finish, num err: " + test.getTotErrors() + " rest: " + test.getTotMissing() +  " : " + test.getTotMessages());
					stillrunning=true;
					cnt++;					
				}

			}
			System.out.println("still running tests " + String.valueOf(cnt));
		}
		try {
			Thread.sleep(10000); //wait for everything settled before closing sockets
		} catch (InterruptedException e) {
		} 
		//close all sockets
		for (ITestSend test : list) {test.closeSockets();}				
		for (ITestSend test : list) {
			for (String s : test.getErrors()) {log(s);}
		}
		for (ITestSend test : list) {
			assertTrue(test.getTotMissing()==0);
			assertTrue(test.getTotMessages()==test.getTotRecieved());
		}
		
	}
		
	public void closeTestClients() {
		for(NameSpace s : serverA.nsList) {
			s.stop();
		}
		for(NameSpace s : serverB.nsList) {
			s.stop();
		}
		
		executor.shutdownNow();
	}
	
	
	public boolean initNewTestClients() {
		
		String ns = UUID.randomUUID().toString().substring(1,7);
		
		InitVar.NO_LICENSE_CHECK=true;
		
		// create 2 peers
		clientA = CreateAndStartNewPeer(serverA,ns,53000);
        clientB = CreateAndStartNewPeer(serverB,ns,53001);
        
        int wait = 0;
        while (wait <= InitVar.START_WAIT && !clientA.isConnected()) {
            try {
                Thread.sleep(100);
                wait += 100;                
            } catch (InterruptedException e) {}          
        }
        if(!clientA.isConnected()) {return false;}
        wait = 0;
        while (wait <= InitVar.START_WAIT && !clientB.isConnected()) {
            try {
                Thread.sleep(100);
                wait += 100;
            } catch (InterruptedException e) {}          
        }
        
        if(!clientB.isConnected()) {return false;}
        
		int trial=0;
		while(true) {								    
			if(!clientA.findUser(clientB.getClientid()).isOk()) {	
				if(trial>2) {
					log(clientB.getClientid() + " NOT found");					
					return false;
					
				}
			} else {
				break;
			}
			trial++;			
		}
		trial=0;
		while(true) {		    
			if(!clientB.findUser(clientA.getClientid()).isOk()) {	
				if(trial>2) {
					log(clientA.getClientid() + " NOT found");					
					return false;
					
				}
			} else {
				break;
			}
			trial++;			
		}
		return true;        
	}
	private void log(String string) {System.out.println(string);}

}
