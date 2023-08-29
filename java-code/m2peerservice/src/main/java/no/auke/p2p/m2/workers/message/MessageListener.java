/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011-2021 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */

package no.auke.p2p.m2.workers.message;


import no.auke.p2p.m2.message.attribute.ErrorCode;
import no.auke.p2p.m2.message.attribute.MessageAttributeException;
import no.auke.p2p.m2.message.attribute.PeerRemoteId;
import no.auke.p2p.m2.message.attribute.PublicAddress;
import no.auke.p2p.m2.message.attribute.MessageAttributeInterface.MessageAttributeType;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.message.header.MessageHeaderInterface.MessageHeaderType;
import no.auke.p2p.m2.workers.PeerSession;
import no.auke.p2p.m2.workers.keepalive.KeepAlivePool;
import no.auke.p2p.m2.workers.keepalive.Peerid;
import no.auke.p2p.m2.NameSpace;

//public class MessageListener implements Runnable {

public class MessageListener {
		
	final private NameSpace namespace;
	public NameSpace getNameSpace() {return namespace;}
	public KeepAlivePool getPingAlive() {return namespace.getKeepAlivePool();}
	
	public MessageListener(NameSpace namespace) {this.namespace = namespace;}
	
	//
	// major entry for reading messages and distribute to peer connections
	//
	
	public void executeMessage(MessageHeader receiveMH) {
		
		//
		// here is where to move channel to peerserver to use on port for peerserver
		//
		
		try {

			if (receiveMH.getType() == MessageHeaderType.PingAlive) {
				getPingAlive().gotPingResponse(receiveMH);
			
			} else if (receiveMH.getType() == MessageHeaderType.MapResponse) {
				getPingAlive().gotMapResponse(receiveMH);
			
			} else if (receiveMH.getType() == MessageHeaderType.PeerRequest) {
				gotPeerConnectRequest(receiveMH);

			} else if (receiveMH.getType() == MessageHeaderType.PeerResponse) {
				gotPeerConnectResponse(receiveMH);
			
			} else if (receiveMH.getType() == MessageHeaderType.PingPeerResponse) {
				gotPingPeerResponse(receiveMH);
				
			} else if (receiveMH.getType() == MessageHeaderType.PingPeer) {
				gotPingPeer(receiveMH);
			
			} else if (receiveMH.getType() == MessageHeaderType.PingClose) {
				gotPingClose(receiveMH);
			
			} else if (receiveMH.getType() == MessageHeaderType.Data) {
				// also call this to notify connection
				gotData(receiveMH);
			
			} else if (receiveMH.getType() == MessageHeaderType.DataReply) {
				gotDataReply(receiveMH);
			
			} else if (receiveMH.getType() == MessageHeaderType.StreamData) {
				gotStreamData(receiveMH);
			} else {
				getNameSpace().getListen().debug(
						"MessLOOP: unknown msgtype " + String.valueOf(receiveMH.getType()) + " from " + receiveMH.getAddress().getAddressPort());
			}
			
		} catch (Exception ex) {
			getNameSpace().getListen().fatalError("Message LOOP: General exception -> " + ex.getMessage());
		}
	}
	
	void gotPeerConnectResponse(MessageHeader receiveMH) {
		
		// get response from KA server for connection to another client
		// error attribute set
		PeerRemoteId peerid = (PeerRemoteId) receiveMH.getMessageAttribute(MessageAttributeType.Peer_Remote_Id);
		//getNameSpace().getListen().debug("gotPeerResponse " + receiveMH.getAddress().getAddressPort() + " peerid:" + peerid.getId());

		if (peerid != null) {
			
			// got a current active peer object
			PeerSession session = getNameSpace().getOpenPeerSessions().get(peerid.getId());
			if (session == null) {
				getNameSpace().getListen().error(
						"session unknown response arrived: " + receiveMH.getAddress().getAddressPort() + " peerid: " + peerid.getId());
			} else if (!session.hasGotResponce() && session.getRequestId() != receiveMH.getTransactionID()) {
				sendClose(receiveMH);
			} else {

				ErrorCode err = (ErrorCode) receiveMH.getMessageAttribute(MessageAttributeType.ErrorCode);
				if (err == null) {
					// only handle if not handled before
					if (!session.hasGotResponce()) {
						session.gotResponseRequest(receiveMH);
					}
				} else {
					// got no known id response from KA
					if (err.getResponseCode() == 702) {
						session.gotNoPeerid(receiveMH);
					} else if (err.getResponseCode() == 707) {
						session.gotNoMiddleman(receiveMH);
					} else if (err.getResponseCode() == 100) {
						session.gotFoundPeer(receiveMH);
					}
				}
			}
		}
	}
	
	public void gotPeerConnectRequest(MessageHeader receiveMH) {
		
		//
		// get request from KA server for connection (client will connect)
		//
				
		PeerRemoteId peer_remote_id = (PeerRemoteId) receiveMH.getMessageAttribute(MessageAttributeType.Peer_Remote_Id);
		PublicAddress from_public_address = (PublicAddress) receiveMH.getMessageAttribute(MessageAttributeType.PublicAddress);

		//getNameSpace().getListen().debug("gotPeerConnectRequest " + receiveMH.getAddress().getAddressPort());

		if (peer_remote_id != null && from_public_address != null) {

			PeerSession session = null;
			if (getNameSpace().getOpenPeerSessions().containsKey(peer_remote_id.getId())) {
				// use session if same trans id or
				// session already have received response or request
				if (getNameSpace().getOpenPeerSessions().get(peer_remote_id.getId()).getRequestId() == receiveMH.getTransactionID()
						&& getNameSpace().getOpenPeerSessions().get(peer_remote_id.getId()).hasGotResponce()) {
					session = getNameSpace().getOpenPeerSessions().get(peer_remote_id.getId());
				}
				
			}
			
			if (session == null) {
				session = new PeerSession(getNameSpace(), new Peerid(peer_remote_id.getId()), receiveMH.getTransactionID());
			}
			
			// session must not have got either response or request to handle this
			// request.
			// This avoid for double connect
			// because one side will get request before response (normally)
			
			if (!session.hasGotResponce()) {
				
				if (session.gotResponseRequest(receiveMH)) {
					
					PeerSession oldsession = getNameSpace().getOpenPeerSessions().get(peer_remote_id.getId());
					// kill possibly old session
					if (oldsession != null) {
						if (oldsession.getRequestId() != receiveMH.getTransactionID()) {
							oldsession.killSession("killed old session on new request");
						}
					}
					// important to add this, also on incoming requests
					getNameSpace().getOpenPeerSessions().put(session.getPeerid().getPeerhexid(), session);
					
				}

			}
		}
	}
	
	
	// for handling direct lookup between peers
	
	void gotPingPeer(final MessageHeader receiveMH) {
		
		boolean found = false;
		for (PeerSession session : getNameSpace().getOpenPeerSessions().values()) {
			if (session.isOkRequest(receiveMH) && session.isPeerReady(receiveMH)) {
				found = true;
				session.gotPingPeer(receiveMH);
				break;
			}
		}
		if (found == false)
			sendClose(receiveMH);
		
	}
	
	void gotPingPeerResponse(MessageHeader receiveMH) {
		
		boolean found = false;
		for (PeerSession session : getNameSpace().getOpenPeerSessions().values()) {
			if (session.isPeerReady(receiveMH)) {
				found = true;
				if (session.isOkRequest(receiveMH))
					session.gotPingPeerResponse(receiveMH);
				else
					session.gotPingPeerError(receiveMH);
				break;
			}
		}
		
		if (found == false)
			sendClose(receiveMH);
	}
	
	
	//
	// got something back, mean other peer is connected
	//
	void gotPingClose(MessageHeader receiveMH) {
		
		for (PeerSession session : getNameSpace().getOpenPeerSessions().values()) {
			
			if (session.isConnected() && session.isPeerReady(receiveMH)) {
				session.isErrorInMessage(receiveMH);
				session.killSession("Ping close recieved");
				break;
			}
		}
		
	}
	
	void gotDataReply(MessageHeader receiveMH) {
		
		boolean found = false;
		for (PeerSession session : getNameSpace().getOpenPeerSessions().values()) {
			if (session.isConnected() && session.isPeerReady(receiveMH)) {
				found = true;
				session.gotDataReply(receiveMH);
				break;
			}
		}
		if (found == false)
			sendClose(receiveMH);
		
	}
	
	void gotData(MessageHeader receiveMH) {
		
		boolean found = false;
		for (PeerSession session : getNameSpace().getOpenPeerSessions().values()) {
			// only data to connected sessions
			if (session.isConnected() && session.isPeerReady(receiveMH)) {
				found = true;
				session.gotData(receiveMH);
				break;
			}
		}
		if (found == false)
			sendClose(receiveMH);
	}
	
	void gotStreamData(MessageHeader receiveMH) {
		for (PeerSession session : getNameSpace().getOpenPeerSessions().values()) {
			if (session.isConnected() && session.isPeerReady(receiveMH)) {
				session.gotStreamData(receiveMH);
				return;
			}
		}
	}
	
	// incoming data, but session was closed
	// send error to other party
	private void sendClose(MessageHeader receiveMH) {
		
		MessageHeader msg = new MessageHeader(MessageHeader.MessageHeaderType.PingClose);
		msg.setTransactionID(receiveMH.getTransactionIDByte());
		msg.setAddress(receiveMH.getAddress());
		try {
			msg.addMessageAttribute(new ErrorCode(712));
		} catch (MessageAttributeException e) {}
		
		getNameSpace().getMessageSender().UDPSendEncrypt(msg);
		getNameSpace().getMessageSender().UDPSendEncrypt(msg);
	
	}
}
