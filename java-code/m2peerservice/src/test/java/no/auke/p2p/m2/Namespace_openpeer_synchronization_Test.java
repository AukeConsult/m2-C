package no.auke.p2p.m2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import no.auke.util.Lock;
import junit.framework.TestCase;

//import static org.mockito.Matchers.*;
//import static org.mockito.Mockito.mock;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.spy;

import org.junit.runner.RunWith;

@PrepareForTest({NameSpace.class})
@RunWith(PowerMockRunner.class)

public class Namespace_openpeer_synchronization_Test extends TestCase {

	NameSpace nameSpace;
	Random rnd = new Random();
	
	public void setUp() throws Exception {

		nameSpace = spy(new PeerServer("","","",0).openNameSpace("testspace"));
		doReturn(true).when(nameSpace).isConnected();
		doReturn(true).when(nameSpace).isRunning();
		
		nameSpace.setClientid("leif_test");
		when(nameSpace.getClientid()).thenReturn("leif_test");
	}

	public void test_openpeer() {
		nameSpace.openPeer(nameSpace.makePeerid("leif"));
	}
	
	
	class Task implements Runnable {
		String id;
		public Task(){id = UUID.randomUUID().toString();}
		public int cnt=0;
		@Override
		public void run() {
			for(cnt=0;cnt<100;cnt++) {
				String newPeerId = "leif"+ id + "_" + String.valueOf(cnt);
				nameSpace.openPeer(nameSpace.makePeerid(newPeerId));
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
				}
				
			}
		}
		
	};
	
	public void test_openpeer_sync() {
		
		// check open peer synchronization
		
		List<Task> tasks = new ArrayList<Task>();
		for(int i=0;i<10;i++) {
			tasks.add(new Task());
		}
		for(int i=0;i<tasks.size();i++) {
			new Thread(tasks.get(i)).start();
		}
		
		int wait=0;
		int tot=0;
		while(wait<120000) {
			tot=0;
			for(Task t:tasks) {
				tot+=t.cnt;
			}
			if(tot==1000) {break;}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
			wait+=5000;
			
		}
		for(Task t:tasks) {
			assertEquals(100,t.cnt);
		}
		assertEquals(1000,nameSpace.getOpenPeerSessions().size());
		
	}
	
	
	
	

}
