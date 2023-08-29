//package no.auke.p2p.m2.workers;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//import static org.junit.Assert.*;
//import static org.mockito.Matchers.*;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.powermock.api.mockito.PowerMockito.when;
//import static org.powermock.api.mockito.PowerMockito.mock;
//import static org.powermock.api.mockito.PowerMockito.spy;
////import static org.powermock.api.mockito.PowerMockito.doReturn;
//
//import no.auke.p2p.m2.InitVar;
//import no.auke.p2p.m2.PeerServer;
//import no.auke.p2p.m2.encryption.EncryptFactory;
//import no.auke.p2p.m2.general.IListener;
//import no.auke.p2p.m2.workers.keepalive.IKeepAlivePool;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.powermock.core.classloader.annotations.PowerMockIgnore;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ReConnector2.class})
//
//@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*",
//    "com.sun.org.apache.xerces.internal.jaxp.*", "ch.qos.logback.*",
//    "org.slf4j.*" })
//
//public class ReConnectorTest  {
//
//	ReConnector2 connector;
//	PeerServer server;
//	EncryptFactory encrypt;
//	ExecutorService executor = Executors.newCachedThreadPool();
//	
//	@Before
//	public void setUp() throws Exception {
//
//		server = mock(PeerServer.class);
//		encrypt = mock(EncryptFactory.class);
//		connector = spy(new ReConnector2(server));
//		
//		when(server.getClientid()).thenReturn("sss");
//		when(server.getListen()).thenReturn(mock(IListener.class));
//		when(server.getReConnector()).thenReturn(connector);
//		when(server.getKeepAlivePool()).thenReturn(mock(IKeepAlivePool.class));
//		
//		
//		when(server.getChannel()).thenReturn(mock(ComChannel4.class));
//		when(server.getEncrypt()).thenReturn(encrypt);
//		
//		when(server.getExecutor(anyString())).thenReturn(executor);
//		
//		when(server.isRunning()).thenReturn(true);
//
//		Map<String,PeerPeerAgent> sessionList = new HashMap<String,PeerPeerAgent>();
//		when(server.getOpenPeerSessions()).thenReturn(sessionList);
//		
//		//assertNotNull(server.getLocaladdress());
//		//assertNotNull(server.getLocaladdress().getAddressPort());
//		assertNotNull(server.getListen());
//		assertNotNull(server.getClientid());
//		
//		InitVar.START_WAIT = 100;
//
//		
//	}
//
//	@Test
//	public void test_canCommunicate_not_hold() {
//
//		assertTrue(connector.waitConnect());	
//		
//	}
//	
////	public void test_canCommunicate_hold() {
////		
////		final ReentrantLock holdtread = new ReentrantLock();
////		
////		Thread holder = new Thread(new Runnable() {
////
////			@Override
////			public void run() {
////
////				try {
////					
////					connector.getSuspendLock().lock();
////					
////				} finally {
////					
////					holdtread.lock();
////					holdtread.unlock();	
////
////					connector.getSuspendLock().unlock();
////					
////				}
////				
////				
////				
////			}});
////		
////		holdtread.lock();
////		holder.start();	
////		
////		// check
////		
////		try {
////			Thread.sleep(100);
////		} catch (InterruptedException e) {
////		}
////		
////		assertFalse(connector.canCommunicate());	
////		
////		holdtread.unlock();
////		
////	}
//	
//	@Test
//	public void test_disConnectService() {
//
//		Map<String, PeerPeerAgent> sessions = new HashMap<String, PeerPeerAgent>();
//		for(int i=0;i<10;i++) {
//			PeerPeerAgent agent = mock(PeerPeerAgent.class);
//			server.getOpenPeerSessions().put(String.valueOf(i), agent);
//			sessions.put(String.valueOf(i), agent);
//		}
//		assertEquals(10,server.getOpenPeerSessions().size());
//		connector.disConnectService();
//		verify(server.getKeepAlivePool(),times(1)).stopKeepAlivePool();
//		
//		for(int i=0;i<10;i++) {
//			verify(sessions.get(String.valueOf(i)),times(1)).closeSession(anyString());
//		}
//		
//	}
//	
//	@Test
//	public void test_disConnectService_not_running() {
//
//		when(server.isRunning()).thenReturn(false);
//		
//		connector.disConnectService();
//
//		verify(connector,never()).lockConnect();
//		verify(connector,never()).unlockConnect();
//		verify(server.getKeepAlivePool(),never()).stopKeepAlivePool();
//		
//	}	
//	
//	@Test
//	public void test_Connect_getConnected() {
//
//		connector.connectService();
//		
//		verify(server.getChannel(),times(1)).startChannel();
//		verify(server,times(1)).setUseSymmetricNAT(false);
//		verify(server.getKeepAlivePool(),times(1)).startKeepAlivePool();
//		
//		verify(connector,times(1)).lockConnect();
//		verify(connector,times(1)).unlockConnect();
//		
//	
//	}
//	
//	@Test
//	public void test_Connect_not_running() {
//		
//		when(server.isRunning()).thenReturn(false);
//		
//		connector.connectService();
//		
//		verify(server.getChannel(),never()).startChannel();
//		verify(server,never()).setUseSymmetricNAT(false);
//		verify(server.getKeepAlivePool(),never()).startKeepAlivePool();
//		
//		verify(connector,never()).lockConnect();
//		verify(connector,never()).unlockConnect();
//		
//
//	
//	}	
//	
//	@Test
//	public void test_reConnect() {
//
//		when(server.isConnected()).thenReturn(true);
//		
//		connector.reConnectService();
//
//		assertTrue(connector.waitConnect());	
//
//		verify(connector,times(1)).disConnectService();
//		verify(connector,times(1)).connectService();
//
//		verify(connector,times(3)).lockConnect();
//		verify(connector,times(3)).unlockConnect();
//		
//	}
//
//	
//
//}
