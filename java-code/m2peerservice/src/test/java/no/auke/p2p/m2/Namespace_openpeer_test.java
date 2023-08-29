package no.auke.p2p.m2;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Test;

import no.auke.p2p.m2.StreamSocket.StreamPacket;
import no.auke.p2p.m2.general.ReturMessageTypes;
import no.auke.p2p.m2.workers.keepalive.Peerid;
import junit.framework.TestCase;

public class Namespace_openpeer_test extends TestCase {

	NameSpace nameSpace;
	
	public void setUp() throws Exception {
		
		nameSpace = spy(new PeerServer("","","",0).openNameSpace("testspace"));
		nameSpace.setClientid("leif");
		when(nameSpace.isRunning()).thenReturn(true);
		
		assertEquals(nameSpace.getClientid(),"leif");
		assertEquals(nameSpace.getThisPeerId().getUserid(),"leif");
		assertNotNull(nameSpace.getThisPeerId().getPeerhexid());
		assertEquals(nameSpace.getNameSpaceId(),"testspace");
		
	}
	
	public void test_emptyClientid() {
		SocketRetStatus ret = nameSpace.openPeer(new Peerid());
		assertNull(ret.getPeerSession());
		assertEquals(ReturMessageTypes.no_peerid,ret.getLastRetcode());
	}
	
	public void test_Clientid() {
		assertEquals(ReturMessageTypes.ok,nameSpace.openPeer(nameSpace.makePeerid("peer")).getLastRetcode());
		assertNotNull(nameSpace.openPeer(nameSpace.makePeerid("peer")).getPeerSession());
	}	
	
	public void test_not_running() {
		SocketRetStatus ret = nameSpace.openPeer(new Peerid());
		assertNull(ret.getPeerSession());
		assertEquals(ReturMessageTypes.no_peerid,ret.getLastRetcode());
		
	}
	
	public void test_not_connected() {
		
		when(nameSpace.isRunning()).thenReturn(false);
		assertFalse(nameSpace.isRunning());

		SocketRetStatus ret = nameSpace.openPeer(nameSpace.makePeerid("peer"));
		assertEquals(ReturMessageTypes.peer_session_stopped,ret.getLastRetcode());
		assertNull(ret.getPeerSession());
	}	

	public void test_not_open_myself() {
		
		Peerid peerme = nameSpace.makePeerid("leif");
		when(nameSpace.getThisPeerId()).thenReturn(peerme);
		assertEquals("leif",nameSpace.getThisPeerId().getUserid());
		when(nameSpace.isRunning()).thenReturn(true);

		SocketRetStatus ret = nameSpace.openPeer(nameSpace.makePeerid("leif"));
		assertNull(ret.getPeerSession());
		assertEquals(ReturMessageTypes.peer_is_me,ret.getLastRetcode());
		
	}	
	
	public void test_open_new_ok() {

		assertEquals(0,nameSpace.getOpenPeerSessions().size());
		SocketRetStatus ret = nameSpace.openPeer(nameSpace.makePeerid("peer"));
		assertNotNull(ret.getPeerSession());

		assertNotNull(nameSpace.openPeer(nameSpace.makePeerid("peer")));
		assertEquals(ReturMessageTypes.ok,ret.getLastRetcode());
		assertEquals(1,nameSpace.getOpenPeerSessions().size());
		
	}	

	public void test_open_twise_ok() {

		assertEquals(0,nameSpace.getOpenPeerSessions().size());
		SocketRetStatus ret = nameSpace.openPeer(nameSpace.makePeerid("peer"));
		assertNotNull(ret.getPeerSession());
		
		ret = nameSpace.openPeer(nameSpace.makePeerid("peer"));
		assertNotNull(ret.getPeerSession());
		
		assertEquals(ReturMessageTypes.ok,ret.getLastRetcode());
		assertEquals(1,nameSpace.getOpenPeerSessions().size());
		
	}
	
	@Test
	public void test_openstream_multiple() {

		StreamSocket socket1 = nameSpace.openStream(1);
		assertNotNull(socket1);
		
		assertEquals(1, socket1.getPort());
		
		StreamSocket socket2 = nameSpace.openStream(1, new StreamSocketListener() {
			@Override
			public void onIncomming(StreamPacket buffer) {}
			@Override
			public void onNoData() {}
			
		});
	    	
		assertNotNull(socket2);
    	assertEquals(1,socket2.getPort());
	    	
	}
	
	@Test
	public void test_openStream_single() {
				
		NameSpace nameSpace = new PeerServer("", "", "", 0).openNameSpace("", null);
		StreamSocket socket = nameSpace.openStream(10, new StreamSocketListener(){
			@Override
			public void onIncomming(StreamPacket buffer) {}
			@Override
			public void onNoData() {}
		});
		
		assertNotNull(socket);
		assertEquals(1,nameSpace.getStreamSockets().size());
		assertNotNull(nameSpace.getStreamSockets().get(10));
		assertEquals(10,socket.getPort());
		
	}	


}
