package no.auke.p2p.m2.workers;

import no.auke.p2p.m2.message.attribute.ErrorCode;
import no.auke.p2p.m2.message.attribute.MessageAttributeException;
import no.auke.p2p.m2.message.header.MessageHeader;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.powermock.core.classloader.annotations.PowerMockIgnore;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;

// import static org.mockito.Mockito.*;




import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({PeerServer.class, PeerPeerAgent.class, AgentInterface.class})
//
//@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*",
//    "com.sun.org.apache.xerces.*", "ch.qos.logback.*",
//    "org.slf4j.*" })

public class PeerSessionTest extends PeerSessionTestBase {
	
    // TODO: LHA
    // upgrade tests later
    
	@Before
	public void setUp() {
		super.init();
	}

    @Test
    public void testRun_open() throws Exception {
    	assertEquals(peer_clientid,session.getPeerid().getUserid());
    	assertEquals(peerid,session.getPeerid());
    }	
    
    @Test
    public void testRun_findConnect_no_reply() throws Exception {
    	assertFalse(peeragent_spy.isConnected());
    	assertTrue(peeragent_spy.isRunning());
    	assertFalse(peeragent_spy.findAndConnect());  
    	assertEquals(0,openPeerSessions.size());
    	verify(namespace.getMessageSender(),times(peer_keepalive_list.size()*2)).UDPSendEncrypt((MessageHeader)any());
    }

    @Test
    public void testRun_findConnect_check() throws Exception {

    	assertFalse(peeragent_spy.isConnected());
    	assertTrue(peeragent_spy.isRunning());
    	
    	assertFalse(peeragent_spy.findAndConnect());  
    	assertEquals(0,openPeerSessions.size());

    	//verify(peerServer.getReConnector(),times(1)).checkNetworkFailure();

    	verify(namespace.getMessageSender(),times(peer_keepalive_list.size()*2)).UDPSendEncrypt((MessageHeader)any());
    	

    }
    
    @Test
    public void testRun_findConnect_with_reply() throws Exception {

    	assertFalse(peeragent_spy.isConnected());
    	assertTrue(peeragent_spy.isRunning());
    	
    	assertFalse(peeragent_spy.findAndConnect());  
    	assertEquals(0,openPeerSessions.size());
    	
    	verify(namespace.getMessageSender(),times(peer_keepalive_list.size()*2)).UDPSendEncrypt((MessageHeader)any());
    	
    } 
    
//    @Test
//    @Ignore
//    public void testRun_gotData() throws Exception {
//    	System.out.println("NOT IMPLEMENTED testRun_gotData");
//    }     
//
//    @Test
//    @Ignore
//    public void testRun_gotDataRepy() throws Exception {
//    	System.out.println("NOT IMPLEMENTED testRun_gotDataRepy");
//    }
//    
//    @Test
//    @Ignore
//    public void testRun_isPeerReady() throws Exception {
//    	System.out.println("NOT IMPLEMENTED testRun_isPeerReady");
//    }
//    
//    @Test
//    @Ignore
//    public void testRun_isPeerNew() throws Exception {
//    	System.out.println("NOT IMPLEMENTED testRun_isPeerNew");
//    } 
//    
//    @Test
//    @Ignore
//    public void testRun_gotPingPeerResponse() throws Exception {
//    	System.out.println("NOT IMPLEMENTED testRun_gotPingPeerResponse");
//    }     
//
//    @Test
//    @Ignore
//    public void testRun_gotPingPeer() throws Exception {
//    	System.out.println("NOT IMPLEMENTED testRun_gotPingPeer");
//    }   

    @Test
    public void test_isErrorInMessage_703() throws MessageAttributeException {
    	
		MessageHeader pingMHerr = new MessageHeader(MessageHeader.MessageHeaderType.PingPeerResponse);
		//pingMHerr.setTransactionID(receiveMH.getTransactionID());
		//pingMHerr.setAddress(receiveMH.getAddress());

		ErrorCode err = new ErrorCode();
		err.setResponseCode(703); // socket port not open
		pingMHerr.addMessageAttribute(err);
		//pingMHerr.addMessageAttribute(packet);
    	
    	assertTrue(session.isErrorInMessage(pingMHerr));
    	//assertEquals(ReturMessageTypes.peer_is_closed,peeragent.getLastretcode());
    	
    }   

    @Test
    public void test_isErrorInMessage_705() throws MessageAttributeException {
    	
		MessageHeader pingMHerr = new MessageHeader(MessageHeader.MessageHeaderType.PingPeerResponse);
		//pingMHerr.setTransactionID(receiveMH.getTransactionID());
		//pingMHerr.setAddress(receiveMH.getAddress());

		ErrorCode err = new ErrorCode();
		err.setResponseCode(705); // socket port not open
		pingMHerr.addMessageAttribute(err);
		//pingMHerr.addMessageAttribute(packet);
    	
    	assertTrue(session.isErrorInMessage(pingMHerr));
    	//assertEquals(ReturMessageTypes.peer_unknown_port,peeragent.getLastretcode());
    	
    }
    
    @Test
    public void test_isErrorInMessage_715() throws MessageAttributeException {
    	
		MessageHeader pingMHerr = new MessageHeader(MessageHeader.MessageHeaderType.PingPeerResponse);
		//pingMHerr.setTransactionID(receiveMH.getTransactionID());
		//pingMHerr.setAddress(receiveMH.getAddress());

		ErrorCode err = new ErrorCode();
		err.setResponseCode(715); // socket port not open
		pingMHerr.addMessageAttribute(err);
		//pingMHerr.addMessageAttribute(packet);
    	
    	assertTrue(session.isErrorInMessage(pingMHerr));
    	//assertEquals(ReturMessageTypes.peer_unknown_port,peeragent.getLastretcode());
    	
    }    
    
    
}
