package no.auke.p2p.m2.workers.message;

import static org.mockito.Mockito.*;

import javax.xml.ws.Response;

//import org.junit.Ignore;

import no.auke.p2p.m2.message.attribute.ErrorCode;
import no.auke.p2p.m2.message.attribute.MessageAttributeException;
import no.auke.p2p.m2.message.attribute.PeerRemoteId;
import no.auke.p2p.m2.message.attribute.MessageAttributeInterface.MessageAttributeType;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.workers.PeerSession;
import no.auke.p2p.m2.workers.keepalive.NetAddress;

public class MessageListener_getRequest_Test extends MessageListenerBasetest {
	
	int transid=100;
	
	MessageHeader reponse;
	String username = "leif";
	PeerSession session;
	
	public void setUp() {
		
		super.setUp();
		
		session = mock(PeerSession.class);
		when(session.isConnected()).thenReturn(true);
		when(session.getRequestId()).thenReturn(transid);		
		
		namespace.getOpenPeerSessions().put(username, session);
		
		reponse = spy(new MessageHeader(MessageHeader.MessageHeaderType.PeerResponse));
		reponse.setTransactionID(transid);
		reponse.setAddress(mock(NetAddress.class));		
		reponse.addMessageAttribute(new PeerRemoteId(username));

	}
	
	public void test_getPeerResponse_correctUser() {

		listner.gotPeerConnectResponse(reponse);
		verify(session,times(1)).getRequestId();
		
	}
	
	public void test_getPeerResponse_wrongUser() {

		reponse = new MessageHeader(MessageHeader.MessageHeaderType.PeerResponse);
		reponse.addMessageAttribute(new PeerRemoteId("leif2"));
		listner.gotPeerConnectResponse(reponse);
		verify(session,never()).getRequestId();
		
	}
	
	public void test_getPeerResponse_correctRequestId() {

		listner.gotPeerConnectResponse(reponse);
		verify(reponse,times(1)).getMessageAttribute(MessageAttributeType.ErrorCode);
		verify(session,times(1)).gotResponseRequest(reponse);
		
	}
	
	public void test_getPeerResponse_wrongRequestId() {

		when(session.getRequestId()).thenReturn(112121);
		listner.gotPeerConnectResponse(reponse);
		verify(reponse,never()).getMessageAttribute(MessageAttributeType.ErrorCode);
		verify(session,never()).gotResponseRequest(reponse);
		
	}
	
	public void test_getPeerResponse_error702() {

		ErrorCode err = new ErrorCode();
		try {
			err.setResponseCode(702);
		} catch (MessageAttributeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		reponse.addMessageAttribute(err);
		listner.gotPeerConnectResponse(reponse);

		verify(reponse,times(1)).getMessageAttribute(MessageAttributeType.ErrorCode);
		verify(session,never()).isConnected();
		verify(session,times(1)).gotNoPeerid(reponse);
		
	}
	
	public void test_getPeerResponse_error707() {

		ErrorCode err = new ErrorCode();
		try {
			err.setResponseCode(707);
		} catch (MessageAttributeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		reponse.addMessageAttribute(err);
		listner.gotPeerConnectResponse(reponse);

		verify(reponse,times(1)).getMessageAttribute(MessageAttributeType.ErrorCode);
		verify(session,never()).isConnected();
		verify(session,times(1)).gotNoMiddleman(reponse);
		
	}	
		
	
}
