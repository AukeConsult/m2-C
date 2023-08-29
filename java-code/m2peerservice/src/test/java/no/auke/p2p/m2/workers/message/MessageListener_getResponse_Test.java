package no.auke.p2p.m2.workers.message;

import static org.mockito.Mockito.*;

import org.junit.Ignore;

import no.auke.m2.task.Task;
import no.auke.p2p.m2.message.attribute.LocalAddress;
import no.auke.p2p.m2.message.attribute.PeerRemoteId;
import no.auke.p2p.m2.message.attribute.PublicAddress;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.workers.keepalive.NetAddress;


//Huy: these test cases are incorrect. There is no place where getSessionWaiting() is used in the code base
//Thus, we can assess this assertNotNull(listner.getSessionWaiting().get(transid));
@Ignore
public class MessageListener_getResponse_Test extends MessageListenerBasetest {
	
	int transid=1000;
	int port=100;
	
	private MessageHeader makeRequest(String username, String publicaddress, String localaddress) {
		
		MessageHeader request = spy(new MessageHeader(MessageHeader.MessageHeaderType.PeerRequest));
		request.setTransactionID(transid);
		request.setAddress(mock(NetAddress.class));
		request.addMessageAttribute(new PeerRemoteId(username));		
		request.addMessageAttribute(new PublicAddress(publicaddress,port));
		request.addMessageAttribute(new LocalAddress(localaddress,port));
	
		return request;
	}
	
	private MessageHeader makePing(String address) {
		
		MessageHeader request = spy(new MessageHeader(MessageHeader.MessageHeaderType.PeerRequest));
		request.setTransactionID(transid);
		request.setAddress(new NetAddress(address,port));
	
		return request;
	}	
	
	public void setUp() {
		
		super.setUp();
		
		
				
	}

	
//	public void test_getPeerRequest_have_session() {
//
//		MessageHeader request = makeRequest("leif","10.10.10.10","20.10.10.10"); 
//		listner.gotPeerRequest(request);
//		assertTrue(listner.getSessionWaiting().get(transid).existsSession(((PublicAddress) request.getMessageAttribute(MessageAttributeType.PublicAddress)).toString()));
//		
//	}	
	
	public void test_getPeerRequest_same_twise() {

		listner.gotPeerConnectRequest(makeRequest("leif","10.10.10.10","20.10.10.10"));
		listner.gotPeerConnectRequest(makeRequest("leif","10.10.10.10","20.10.10.10"));


	}	
	
	public void test_getPeerRequest_first_session_same_localaddress() {

		listner.gotPeerConnectRequest(makeRequest("leif","10.10.10.10","10.10.10.10"));

	}	
	
	public void test_getPeerRequest_3_sessions() {

		listner.gotPeerConnectRequest(makeRequest("leif","10.10.10.10","20.10.10.10"));
		listner.gotPeerConnectRequest(makeRequest("leif","10.10.10.11","20.10.10.12"));
		listner.gotPeerConnectRequest(makeRequest("leif","10.10.10.12","20.10.10.13"));
		
		verify(monitors.getConnectMonitor(),times(6)).execute((Task) any());
		
	}
	
	public void test_getPeerRequest_3_sessions_first_pinged() {

		listner.gotPeerConnectRequest(makeRequest("leif","10.10.10.10","20.10.10.10"));
		listner.gotPeerConnectRequest(makeRequest("leif","10.10.10.11","20.10.10.12"));
		listner.gotPeerConnectRequest(makeRequest("leif","10.10.10.12","20.10.10.13"));

	}	

	public void test_getPeerRequest_not_setaddress() {

		MessageHeader request = makeRequest("leif","10.10.10.10","20.10.10.10"); 
		listner.gotPeerConnectRequest(request);
		
	}
	
	public void test_checkWaitingRequest() {

		MessageHeader request = makeRequest("leif","10.10.10.10","20.10.10.10"); 
		listner.gotPeerConnectRequest(request);
		assertEquals(1,namespace.getOpenPeerSessions().size());
		
	}	
	
	public void test_checkWaitingRequest_unknown_address() {

		MessageHeader request = makeRequest("leif","10.10.10.10","20.10.10.10"); 
		listner.gotPeerConnectRequest(request);
		assertEquals(0,namespace.getOpenPeerSessions().size());
		
	}	
	
		
}
