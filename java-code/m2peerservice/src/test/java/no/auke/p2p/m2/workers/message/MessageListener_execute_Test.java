package no.auke.p2p.m2.workers.message;

//import static org.mockito.Matchers.*;
//import static org.mockito.Mockito.atLeast;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.spy;
//import static org.mockito.Mockito.stub;


import static org.mockito.Mockito.*;

import java.util.Random;

import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.workers.keepalive.NetAddress;

public class MessageListener_execute_Test extends MessageListenerBasetest {
	
	
	public void test_Execute_PingAlive() throws InterruptedException {
		
		MessageHeader ping = new MessageHeader(MessageHeader.MessageHeaderType.PingAlive);
		listner.executeMessage(ping);
		verify(pingAlive,times(1)).gotPingResponse(ping);
	
	}
	public void test_Execute_MapResponse() throws InterruptedException {
		
		MessageHeader map = new MessageHeader(MessageHeader.MessageHeaderType.MapResponse);
		listner.executeMessage(map);
		verify(pingAlive,times(1)).gotMapResponse(map);
	}
	public void test_Execute_PeerResponse() throws InterruptedException {

		MessageHeader reponse = new MessageHeader(MessageHeader.MessageHeaderType.PeerResponse);

		listner.executeMessage(reponse);
		verify(listner,times(1)).gotPeerConnectResponse(reponse);

		verify(listner,never()).gotPingPeerResponse(reponse);
		verify(listner,never()).gotData(reponse);
		verify(listner,never()).gotDataReply(reponse);
		verify(listner,never()).gotPingPeer(reponse);
		verify(listner,never()).gotStreamData(reponse);
		
		
	}
	public void test_Execute_PeerRequest() throws InterruptedException {
		
		MessageHeader request = new MessageHeader(MessageHeader.MessageHeaderType.PeerRequest);

		listner.executeMessage(request);
		verify(listner,times(1)).gotPeerConnectRequest(request);
		
		verify(listner,never()).gotPingPeerResponse(request);
		verify(listner,never()).gotPeerConnectResponse(request);
		verify(listner,never()).gotPingPeer(request);
		verify(listner,never()).gotData(request);
		verify(listner,never()).gotDataReply(request);
		verify(listner,never()).gotStreamData(request);
		
	
	}
	public void test_Execute_PingPeer() throws InterruptedException {
		
		MessageHeader pingpeer = spy(new MessageHeader(MessageHeader.MessageHeaderType.PingPeer));
		listner.executeMessage(pingpeer);
		verify(listner, times(1)).gotPingPeer(pingpeer);
		verify(listner,never()).gotPingPeerResponse(pingpeer);
		verify(listner,never()).gotPeerConnectResponse(pingpeer);
		verify(listner,never()).gotPeerConnectRequest(pingpeer);
		verify(listner,never()).gotData(pingpeer);
		verify(listner,never()).gotDataReply(pingpeer);
		verify(listner,never()).gotStreamData(pingpeer);
		

	}
	public void test_Execute_PingPeerResponse() {

		MessageHeader pingpeerresponse = spy(new MessageHeader(MessageHeader.MessageHeaderType.PingPeerResponse));
		when(pingpeerresponse.getAddress()).thenReturn(mock(NetAddress.class));
		listner.executeMessage(pingpeerresponse);
		verify(listner, times(1)).gotPingPeerResponse(pingpeerresponse);
		
		verify(listner,never()).gotPingPeer(pingpeerresponse);
		verify(listner,never()).gotPeerConnectResponse(pingpeerresponse);
		verify(listner,never()).gotPeerConnectRequest(pingpeerresponse);		
		verify(listner,never()).gotData(pingpeerresponse);
		verify(listner,never()).gotDataReply(pingpeerresponse);
		verify(listner,never()).gotStreamData(pingpeerresponse);
		
		
		
	}
	public void test_Execute_PingClose() {
		
		MessageHeader pingclose = new MessageHeader(MessageHeader.MessageHeaderType.PingClose);

		listner.executeMessage(pingclose);
		verify(listner,times(1)).gotPingClose(pingclose);
		
		verify(listner,never()).gotPingPeerResponse(pingclose);
		verify(listner,never()).gotPingPeer(pingclose);
		verify(listner,never()).gotPeerConnectResponse(pingclose);
		verify(listner,never()).gotPeerConnectRequest(pingclose);	
		verify(listner,never()).gotData(pingclose);
		verify(listner,never()).gotDataReply(pingclose);
		verify(listner,never()).gotStreamData(pingclose);
		

	}
	public void test_Execute_Data() {
		
		MessageHeader data = spy(new MessageHeader(MessageHeader.MessageHeaderType.Data));
		when(data.getAddress()).thenReturn(mock(NetAddress.class));

		listner.executeMessage(data);
		verify(listner,never()).gotPingPeerResponse(data);
		verify(listner,times(1)).gotData(data);
		
		verify(listner,never()).gotPingPeerResponse(data);
		verify(listner,never()).gotPingPeer(data);
		verify(listner,never()).gotPeerConnectResponse(data);
		verify(listner,never()).gotPeerConnectRequest(data);			
		verify(listner,never()).gotDataReply(data);
		verify(listner,never()).gotStreamData(data);
		
	}
	public void test_Execute_DataReply() {
		
		MessageHeader datareply = spy(new MessageHeader(MessageHeader.MessageHeaderType.DataReply));
		when(datareply.getAddress()).thenReturn(mock(NetAddress.class));
		
		listner.executeMessage(datareply);
		verify(listner,times(1)).gotDataReply(datareply);
		
		verify(listner,never()).gotPingPeerResponse(datareply);
		verify(listner,never()).gotPingPeer(datareply);
		verify(listner,never()).gotPeerConnectResponse(datareply);
		verify(listner,never()).gotPeerConnectRequest(datareply);			
		verify(listner,never()).gotData(datareply);		
		verify(listner,never()).gotStreamData(datareply);

	}
	public void test_Execute_StreamData() {
		
		MessageHeader streamdata = spy(new MessageHeader(MessageHeader.MessageHeaderType.StreamData));
		when(streamdata.getAddress()).thenReturn(mock(NetAddress.class));
		listner.executeMessage(streamdata);
		
		verify(listner,times(1)).gotStreamData(streamdata);
		verify(listner,never()).gotPingPeerResponse(streamdata);
		verify(listner,never()).gotPingPeer(streamdata);
		verify(listner,never()).gotPeerConnectResponse(streamdata);
		verify(listner,never()).gotPeerConnectRequest(streamdata);			
		verify(listner,never()).gotData(streamdata);			
		
	}
	
    
	public void test_messageListener_incomming_streammessage() {
		
    	final byte[] data = new byte[100];
    	(new Random()).nextBytes(data);
    	
		MessageHeader message1 = new MessageHeader(MessageHeader.MessageHeaderType.StreamData);
		message1.setAddress(mock(NetAddress.class));
		
    	listner.executeMessage(message1);
		verify(listner, atLeast(1)).gotStreamData(message1);
		
	} 	
	
}
