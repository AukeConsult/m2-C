/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.workers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.general.ReturMessageTypes;
import no.auke.p2p.m2.message.attribute.DataPacket;
import no.auke.p2p.m2.message.attribute.DataReplyPacket;
import no.auke.p2p.m2.message.attribute.ErrorCode;
import no.auke.p2p.m2.message.attribute.LocalAddress;
import no.auke.p2p.m2.message.attribute.MessageAttribute;
import no.auke.p2p.m2.message.attribute.MessageAttributeException;
import no.auke.p2p.m2.message.attribute.MiddleManRequire;
import no.auke.p2p.m2.message.attribute.PeerAddInfo;
import no.auke.p2p.m2.message.attribute.PeerRemoteId;
import no.auke.p2p.m2.message.attribute.PublicAddress;
import no.auke.p2p.m2.message.attribute.PublicKey;
import no.auke.p2p.m2.message.attribute.RawDataPacket;
import no.auke.p2p.m2.message.attribute.SessionKey;
import no.auke.p2p.m2.message.attribute.PeerLocalId;
import no.auke.p2p.m2.message.attribute.MessageAttributeInterface.MessageAttributeType;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.workers.connect.ConnectPingTask;
import no.auke.p2p.m2.workers.connect.ConnectionNegotiate;
import no.auke.p2p.m2.workers.connect.PeerSessionEncrypt;
import no.auke.p2p.m2.workers.keepalive.NetAddress;
import no.auke.p2p.m2.workers.keepalive.Peerid;
import no.auke.util.Lock;
import no.auke.m2.task.Task;
import no.auke.p2p.m2.InitVar;
import no.auke.p2p.m2.NameSpace;
import no.auke.p2p.m2.PeerServer;
import no.auke.p2p.m2.StreamSocket;

public class PeerSession {
	
	// private static final Logger logger =
	// LoggerFactory.getLogger(PeerPeerAgent.class);
	// loop wait frequency when wait for connect (only internal. low is faster,
	// but use more resources)
	
	private static final long WAIT_FOR_SLEEP = 50;
	
	private PeerPeerInfo peerinfo;
	public PeerPeerInfo getPeerInfo() {return peerinfo;}
		
	PeerSessionEncrypt sessionEncrypt = null;
	public synchronized PeerSessionEncrypt getSessionEncrypt() {
		if (sessionEncrypt == null) {sessionEncrypt = new PeerSessionEncrypt(this);}
		return sessionEncrypt;
	}
	
	private Peerid peerid;
	public Peerid getPeerid() {return peerid;}
	public synchronized void setPeerId(Peerid peerid) {this.peerid = peerid;}
	private NetAddress peerAddress;
	public NetAddress getPeerAddress() {return peerAddress;}
	
	public synchronized void setPeerAddress(NetAddress address) {
		getPeerInfo().setAddress(address);
		peerAddress = address;
	}
	
	private AtomicInteger requestId = new AtomicInteger();
	public int getRequestId() {return requestId.get();}
	public void setRequestId(int requestid) {requestId.set(requestid);}
	public void setNewRequestId() {this.requestId.set(PeerServer.newRequestId());}
	
	// arrive
	private AtomicBoolean peer_is_running = new AtomicBoolean(true);
	private AtomicBoolean peer_is_connected = new AtomicBoolean(false);
	private AtomicBoolean peer_is_not_found = new AtomicBoolean(true);
	
	// private AtomicInteger connect_requests_waiting = new AtomicInteger();
	private AtomicBoolean peer_is_pinged = new AtomicBoolean();
	// flag to tell that a connect request message have arrived and negotiations
	// are started
	private AtomicBoolean peer_wait_response = new AtomicBoolean(true);
	// used to measure if got either
	// request or response from KA
	// used to avoid mutual connects
	public boolean hasGotResponce() {return !peer_wait_response.get();}
	
	private AtomicInteger peer_KA_requested = new AtomicInteger(0);
	private Map<String, MessageHeader> connect_requested_addresses = new ConcurrentHashMap<String, MessageHeader>();
	
	private NameSpace nameSpace = null;
	public NameSpace getNameSpace() {return nameSpace;}
	
	public PeerServer getMainServ() {return getNameSpace().getMainServ();}
	
	// time size last ping
	private AtomicLong last_time_peer_alive = new AtomicLong(0);
	// time since last data
	private AtomicLong last_time_peer_data = new AtomicLong(0);
	// time size last ping reply
	private AtomicLong last_time_peer_reponse = new AtomicLong(0);
	// register what address sent connection request
	private ConcurrentHashMap<String, ConnectionNegotiate> connection_requests = new ConcurrentHashMap<String, ConnectionNegotiate>(); // temporary
	public ConcurrentHashMap<String, ConnectionNegotiate> getConnectionRequests() {
		return connection_requests;
	}
	
	// Running when thread is active
	public boolean isRunning() {return peer_is_running.get();}

	public boolean isConnected() {
		// peer_is_running -> set to true when object create, set to false when
		// session killed or disconnected
		// peer_is_connected -> set to true when receiving gotPingPeerResponse,
		// this means after ping is sent to the other side, a ping response
		// returned and session are connected
		// peer_is_ready -> set in isPeerNew() when response from a negotiation
		// process get back from the other side, > the all set to communicate
		return peer_is_running.get() & peer_is_connected.get();
	}

	public boolean isFinish() {return !peer_is_running.get() && !peer_is_not_found.get();}
		
	public PeerSession() {
		
		peer_is_running.set(true);
		peer_is_connected.set(false);
		peer_is_pinged.set(false);
		peer_is_not_found.set(true);
		
		peer_wait_response.set(true);  // when
		
		updateAliveTime();
		updateDataTime();
	
	}
	
	// constructed on find
	public PeerSession(NameSpace nameSpace, Peerid peerid) {
		this();
		
		this.nameSpace = nameSpace;
		this.peerid = peerid;		
		
		setNewRequestId();
		
		if (!getNameSpace().getKnownPeers().containsKey(getPeerid().getPeerhexid())) {
			getNameSpace().getKnownPeers().put(getPeerid().getPeerhexid(), new PeerPeerInfo(getPeerid().getPeerhexid()));
		}
		peerinfo = getNameSpace().getKnownPeers().get(this.peerid.getPeerhexid());
		
	}
	
	// constructed from incoming request in message listener
	public PeerSession(NameSpace namespace, Peerid peerid, int requestId) {
		this(namespace, peerid);
		this.requestId.set(requestId);
	}
	
	// find a peer at KA server
	//
	// sending request to know peers
	// i.e peer previously connected
	//
	public boolean sendKnownRequest() {
		
		try {

			if (InitVar.PEER_DO_DIRECT_CONNECT 
					&& isRunning() 
					&& !getNameSpace().isMiddleman() 
					&& !isConnected() 
					&& getPeerInfo().doDirectConnect()
					&& getPeerInfo().getAddress() != null
					) 
			{

				getNameSpace().getListen().message("DIRECT CONNECT for " + getPeerid().getUserid() 
						+ " from address " + this.getMainServ().getLocaladdress().getAddressPort()
						+ " to address " + getPeerInfo().getAddress().getAddressPort()
						);
			
				connection_requests.put(
						getPeerInfo().getAddress().getAddressPort(), 
						new ConnectionNegotiate(this, getPeerInfo().getAddress(),InitVar.CONNECT_PING_DELAY / 2)
						);
				
				MessageHeader msg = new MessageHeader(MessageHeader.MessageHeaderType.PeerRequest);
				
				msg.setTransactionID(getRequestId());
				
				msg.addMessageAttribute(new PeerRemoteId(getNameSpace().getThisPeerId().getPeerhexid()));
				msg.addMessageAttribute(new PeerLocalId(getPeerid().getPeerhexid()));
				msg.addMessageAttribute(new LocalAddress(getMainServ().getLocaladdress()));	
				msg.addMessageAttribute(new PublicAddress(getMainServ().getPublicaddress())); 
				msg.addMessageAttribute(getNameSpace().getSessionEncrypt().getPublickey());
				msg.addMessageAttribute(new PeerAddInfo((short)1, 3,"",null));
				msg.setAddress(getPeerInfo().getAddress());

				sendMessage(msg);
				sendMessage(msg);
								
				return true;
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	public boolean sendPeerRequest() {return false;}
	
	//
	// sending request to KA
	//
	public boolean sendKARequest(boolean useMM) {
		
		if (isRunning() 
				&& peer_wait_response.get() 
				&& !isConnected()
				) 
		{
		
			if (getNameSpace().isMMBlocked() && (useMM || getNameSpace().isMiddleman())) {
				return false;
			}
			
			//peer_wait_response.set(true);
			boolean connectMM = useMM || getNameSpace().isMiddleman();
			
			getPeerInfo().setUsedMM(connectMM);
			if (connectMM) {getNameSpace().getListen().message("KA CONNECT via MM for " + getPeerid().getUserid());} 
			else {getNameSpace().getListen().message("KA CONNECT for " + getPeerid().getUserid());}
			
			//
			// tell if MM is used, is MM is used, direct connect not possible
			// when next connect
			//
			
			getPeerInfo().setUsedMM(connectMM);
			
			List<NetAddress> addresses = getNameSpace().getMainServ().locateKeepAlive(getPeerid());
			for (NetAddress address : addresses) {
				
				MessageHeader msg = new MessageHeader(MessageHeader.MessageHeaderType.PeerRequest);
				msg.setTransactionID(getRequestId());
				msg.addMessageAttribute(new PeerLocalId(getNameSpace().getThisPeerId().getPeerhexid()));
				msg.addMessageAttribute(new PeerRemoteId(getPeerid().getPeerhexid()));
				msg.addMessageAttribute(new LocalAddress(getMainServ().getLocaladdress()));				
				
				if (connectMM) {
					msg.addMessageAttribute(new MiddleManRequire(InitVar.NUMBER_OF_MIDDLEMAN_REQURIED));
				}
				msg.setAddress(address);
				connect_requested_addresses.put(address.getAddressPort(), msg);
			}
			
			getNameSpace().getListen().debug("KA lookup for user " + getPeerid().getUserid() + " peerid " + getPeerid().getPeerhexid() + " requestid " + getRequestId());
			
			peer_KA_requested.set(connect_requested_addresses.size());
			if (isRunning()) {
				for (MessageHeader msg : connect_requested_addresses.values()) {
					sendMessage(msg);
				}
				if (isRunning()) {
					// give it a little time and send again
					try {
						Thread.sleep(WAIT_FOR_SLEEP);
					} catch (InterruptedException e) {}
					for (MessageHeader msg : connect_requested_addresses.values()) {
						sendMessage(msg);
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}
	//
	// waiting for first ping from peer
	//
	public boolean waitForPing(long startwait, long timeout) {
		// connectAddress is list of KA that is included in the find request
		// normally 4 different KA server
		// when / if KA answer no found, KA address is removed from list
		// if remaining not KA is bigger than numWaitConnectAdresses
		// for ex. numWaitConnectAdresses = 1, all KA except one must say no
		// peer found to leave
		// the wait loop an peer is regarded not found
		if (!isPinged() && startwait < timeout && !peer_wait_response.get() && isRunning()) {
			return true;
		} else {
			return false;
		}
	}
	//
	// waiting for connect from peer
	//
	public boolean waitForConnect(long startwait, long timeout) {
		// connectAddress is list of KA that is included in the find request
		// normally 4 different KA server
		// when / if KA answer no found, KA address is removed from list
		// if remaining not KA is bigger than numWaitConnectAdresses
		// for ex. numWaitConnectAdresses = 1, all KA except one must say no
		// peer found to leave
		// the wait loop an peer is regarded not found
		if (isPinged() && !isConnected() && startwait < timeout && !peer_wait_response.get() && isRunning()) {
			return true;
		} else {
			return false;
		}
	}
	//
	// waiting for answers from KA after find request
	//
	public boolean waitForRequest(long startwait, long timeout) {
		// wait until request from KA and start negotiate
		if (!isConnected() 
				&& startwait < timeout 
				&& peer_wait_response.get() 
				&& peer_KA_requested.get() - connect_requested_addresses.size() < 2
				&& isRunning()) {
			return true;
		} else {
			return false;
		}
	}
	public boolean waitForDirectRequest(long startwait, long timeout) {
		// wait until request from KA and start negotiate
		if (!isConnected() 
				&& startwait < timeout 
				&& peer_wait_response.get() 
				&& isRunning()) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean findAndConnect() {
		
		if (isRunning() 
			&& !isConnected() 
			&& !hasGotResponce()
			&& peer_is_not_found.getAndSet(false)
			) 
		{
		
			try {
			
				// find a peer and open the session
				// only one request at a time
				// only try to connect once
				
				if (!isConnected()) {
					
					getNameSpace().getListen().debug(getNameSpace().getClientid() 
							+ " > try to connect to peer " 
							+ getPeerid().getUserid() 
							+ getRequestId());
					connection_requests.clear();
					
					long connecttime = System.currentTimeMillis();
					
					// IP address exists from previous
					// connection, try this address first
					// TODO: do_direct is always false
					
					if (sendKnownRequest()) {
						int wait = 0;
						while (waitForDirectRequest(wait, InitVar.PEER_DIRECT_CONNECT_TIMEOUT)) {
							try {
								Thread.sleep(WAIT_FOR_SLEEP);
							} catch (InterruptedException ignored) {}
							wait += WAIT_FOR_SLEEP;
						}
					}
					
					// // TODO: not implemented
					// // ask other friends for look up
					// if (sendPeerRequest()) {
					//
					// int wait = 0;
					// while
					// (waitForConnect(wait,InitVar.PEER_DIRECT_CONNECT_TIMEOUT))
					// {
					// try {
					// Thread.sleep(WAIT_FOR_SLEEP);
					// } catch (InterruptedException ignored) {}
					// wait += WAIT_FOR_SLEEP;
					// }
					// }
				
					if (sendKARequest(false)) {
											
						if (isRunning() && !isConnected()) {
						
							int wait = 0;
							int waitfor = InitVar.REQUEST_RESPONSE_KA_TIMEOUT + (getNameSpace().isMiddleman() ? InitVar.REQUEST_RESPONSE_MIDDLEMAN_TIMEOUT : 0);
							
							// wait till KA have responded with some addresses
							while (waitForRequest(wait, waitfor)) {
								try {
									Thread.sleep(WAIT_FOR_SLEEP);
								} catch (InterruptedException e) {}
								wait += WAIT_FOR_SLEEP;
							}
							
							if (isRunning() && !isConnected()) {
								
								if (!peer_wait_response.get()) {
								
									getNameSpace().getListen().debug(
											getNameSpace().getClientid() + " " + getPeerid().getUserid() + " response from KA after "
													+ String.valueOf(System.currentTimeMillis() - connecttime));
									
									// got answer from KA
									// WAIT for first ping from peer
									
									wait = 0;
									while (waitForPing(wait, InitVar.PEER_CONNECT_TIMEOUT)) {
										try {
											Thread.sleep(WAIT_FOR_SLEEP);
										} catch (InterruptedException e) {}
										wait += WAIT_FOR_SLEEP;
									}
									
									// WAIT to connect to peer
									wait = 0;
									while (waitForConnect(wait, InitVar.PEER_CONNECT_TIMEOUT)) {
										try {
											Thread.sleep(WAIT_FOR_SLEEP);
										} catch (InterruptedException e) {}
										wait += WAIT_FOR_SLEEP;
									}
									
									if (isRunning() && !isPinged() && !isConnected() && !getNameSpace().isMMBlocked()) {
										
										// LHA: this is incase other side use MM
										// (is symetric nat)
										
										getNameSpace().getListen()
												.debug(getNameSpace().getClientid() + " " + getPeerid().getUserid() + " TRY CONNECT WITH MM ");
										
										connection_requests.clear();
										
										setNewRequestId();
										
										// try again and use MM										
										if (sendKARequest(true)) {
											
											wait = 0;
											waitfor = InitVar.REQUEST_RESPONSE_KA_TIMEOUT + InitVar.REQUEST_RESPONSE_MIDDLEMAN_TIMEOUT;
											// wait til KA have responded with
											// some addresses
											while (waitForRequest(wait, waitfor)) {
												try {
													Thread.sleep(WAIT_FOR_SLEEP);
												} catch (InterruptedException e) {}
												wait += WAIT_FOR_SLEEP;
											}
											
											if (isRunning() && !isConnected()) {
											
												if (!peer_wait_response.get()) {
												
													getNameSpace().getListen().debug(
															getNameSpace().getClientid() + " > " + getPeerid().getUserid() + "response from KA after "
																	+ String.valueOf(System.currentTimeMillis() - connecttime));
													// got answer from KA
													// WAIT for first ping from
													// peer
													wait = 0;
													while (waitForPing(wait, InitVar.PEER_CONNECT_TIMEOUT)) {
														try {
															Thread.sleep(WAIT_FOR_SLEEP);
														} catch (InterruptedException e) {}
														wait += WAIT_FOR_SLEEP;
													}
												
													// WAIT to connect to peer
													wait = 0;
													while (waitForConnect(wait, InitVar.PEER_CONNECT_TIMEOUT)) {
														try {
															Thread.sleep(WAIT_FOR_SLEEP);
														} catch (InterruptedException e) {}
														wait += WAIT_FOR_SLEEP;
													}
												
												}
												
											}
										}
									}
									
								} else {
									
									getNameSpace().getListen().debug(
											getNameSpace().getClientid() + " > " + getPeerid().getUserid() + "NO Reponse from KA after "
													+ String.valueOf(System.currentTimeMillis() - connecttime));
								
								}
							}
						}
					}
					
					
					if (!isConnected()) {
						getPeerInfo().setLastNotFound();
						killSession("can not connect");
					}	
						
					if (!isRunning()) {
						// close session if not connected
						getNameSpace().getListen().debug(
								getNameSpace().getClientid() + " > " + getPeerid().getUserid() + " STOPPED, waiting MS " + String.valueOf(System.currentTimeMillis() - connecttime));
						
						getPeerInfo().setLastNotFound();
					
					} else if (!isConnected()) {
						// close session if not connected
						getNameSpace().getListen().debug(
								getNameSpace().getClientid() + " > " + getPeerid().getUserid() + " could not connect to after waiting MS "
										+ String.valueOf(System.currentTimeMillis() - connecttime));
					} else {
						getNameSpace().getListen().debug(
								getNameSpace().getClientid() + " > " + getPeerid().getUserid() + " Connected, after MS " + String.valueOf(System.currentTimeMillis() - connecttime));
					}
				}
				
			} catch (Exception ex) {
				getNameSpace().getListen().fatalError("connect exception " + ex.getMessage());
			}
		}
		return isConnected();
	}
	// LHA: set connected
	public void setConnected(MessageHeader msg) {
		
		// Here is where session is regarded connected
		// Got a response when set to opening, other peer is alive
		
		if (!peer_is_pinged.getAndSet(true)) {
		
			//peer_wait_response.set(false); // no need to wait for response if party already have pinged
			
			updateAliveTime();
			setPeerAddress(msg.getAddress());
			
			// removed all connects that use another addresses
			for (ConnectionNegotiate conn : new ArrayList<ConnectionNegotiate>(connection_requests.values())) {
				
				if (!conn.getAddress().getAddressPort().equals(this.getPeerAddress().getAddressPort())) {
					conn.stop();
					connection_requests.remove(conn.getAddress().getAddressPort());
				
				} else {
					//
					// extend the connect_request task where the ping com from
					// and
					//
					conn.reStart();
				}
			}
			
			
		}
		
		if (!peer_is_connected.get()) {
			
			if ((SessionKey) msg.getMessageAttribute(MessageAttributeType.SessionKey) != null) {
			
				// clear of outstanding requests
				connection_requests.clear();
				peer_is_connected.set(true);
		
				getNameSpace().getListen().debug(
						getNameSpace().getClientid() + " CONNECTED " + getPeerid().getUserid() + " "
								+ (getPeerAddress() != null ? getPeerAddress().getAddressPort() : ""));
				
				// start ping task
				pingtask = new PingTask(getNameSpace().getMainServ().getServerId());
				getNameSpace().getMonitors().getPingMonitor().execute(pingtask);
				getNameSpace().getListen().onPeerConnected(msg.getAddress());
			
			}
		}
	}
	public void wakeUp() {
		if (isConnected() && System.currentTimeMillis() - last_time_peer_alive.get() > (InitVar.PEER_TIMEOUT / 2)) {
			if (pingtask != null) {
				pingtask.execute();
			}
		}
	}
	//
	// get a response or a request message from KA
	// start connect process by starting a negotiate process to punch hole in
	// NAT
	//
	public boolean gotResponseRequest(MessageHeader receiveMH) {
		
		if (!isRunning()) {
		
			getNameSpace().getListen().error("got gotResponseRequest NOT running " + receiveMH.getAddress().getAddressPort());
			return false;
		
		} else if (peer_wait_response.getAndSet(false)) {

			peer_is_not_found.set(false);

			PublicAddress publicaddress = (PublicAddress) receiveMH.getMessageAttribute(MessageAttributeType.PublicAddress);
			LocalAddress localaddress = (LocalAddress) receiveMH.getMessageAttribute(MessageAttributeType.LocalAddress);

			getNameSpace().getListen().debug("responserequest from " + localaddress.getAddress().toString() +  ":" + localaddress.getPort());

			if (publicaddress != null && !publicaddress.isNull()) {
				
				// check if direct connect
				// peer info must be present
				PeerAddInfo peer_info = (PeerAddInfo) receiveMH.getMessageAttribute(MessageAttributeType.PeerInfo);
				if(peer_info!=null) {
					
					// send response back
					
					MessageHeader msg = new MessageHeader(MessageHeader.MessageHeaderType.PeerResponse);		
					msg.setTransactionID(receiveMH.getTransactionID());
					msg.addMessageAttribute(new PeerRemoteId(getNameSpace().getThisPeerId().getPeerhexid()));
					msg.addMessageAttribute(new PeerLocalId(getPeerid().getPeerhexid()));
					msg.addMessageAttribute(new LocalAddress(getMainServ().getLocaladdress()));
					msg.addMessageAttribute(new PublicAddress(getMainServ().getPublicaddress())); 
					msg.addMessageAttribute(getNameSpace().getSessionEncrypt().getPublickey());
					msg.setAddress(receiveMH.getAddress());
					
					sendMessage(msg);
					sendMessage(msg);
					
				}
				
				getSessionEncrypt().gotPublicKey(receiveMH, false);
				
				// try local address if address in same local network
				if (localaddress != null && !localaddress.isNull()) {
					
					// open a session to on local address
					NetAddress connect_local_address = new NetAddress(localaddress.getAddress().toString().trim(), localaddress.getPort());
					if (!connection_requests.containsKey(connect_local_address.getAddressPort())) {
						//getNameSpace().getListen().debug("add local address " + connect_local_address.getAddressPort() + " to negotiate");
						connection_requests.put(connect_local_address.getAddressPort(), new ConnectionNegotiate(this, connect_local_address,
								InitVar.CONNECT_PING_DELAY));
					}
					connection_requests.get(connect_local_address.getAddressPort()).run();
				}
				
				if (!isPinged()) {
					
					// open a session to bang hole on public address
					NetAddress connect_public_address = new NetAddress(publicaddress.getAddress().toString().trim(), publicaddress.getPort());
					if (!connection_requests.containsKey(connect_public_address.getAddressPort())) {
						//getNameSpace().getListen().debug("add public address " + connect_public_address.getAddressPort() + " to negotiate ");
						connection_requests.put(connect_public_address.getAddressPort(), new ConnectionNegotiate(this, connect_public_address,
								InitVar.CONNECT_PING_DELAY));
					}
					connection_requests.get(connect_public_address.getAddressPort()).run();
				}
			}
			return true;
		}
		return false;
	}
	// got a ping message from other peer
	// response message is exchanged before session is alive
	public void gotPingPeer(MessageHeader receiveMH) {
				
		//getNameSpace().getListen().debug("gotPingPeer " + receiveMH.getAddress().getAddressPort() );
		
		if (!isErrorInMessage(receiveMH)) {

			updateAliveTime();
			
			// check if public key is sent direct from other peer
			getSessionEncrypt().gotPublicKey(receiveMH, true);
			setConnected(receiveMH);
			// wait for key exchange
			MessageHeader outMsg = new MessageHeader(MessageHeader.MessageHeaderType.PingPeerResponse);
			outMsg.setTransactionID(getRequestId());
			outMsg.setAddress(receiveMH.getAddress());
			// if session key is received, return it to sender as a confirmation
			getSessionEncrypt().gotSessionKey(receiveMH, outMsg);
			// send confirmation and ping response
			sendMessage(outMsg);
			runConnectPingTask();
			
		} else {
			getNameSpace().getListen().debug(getNameSpace().getClientid() + " err attribute in message");
		}
		
	}
	// handle error messages from other side
	public void gotPingPeerError(MessageHeader receiveMH) {
		// error handling from other side
		ErrorCode err = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
		if (err != null) {
			// error from data sending
			DataReplyPacket packetreply = (DataReplyPacket) receiveMH.getMessageAttribute(MessageAttributeType.DataReplyPacket);
			if (packetreply != null) {
				Socket socket = getNameSpace().getSocket(packetreply.getPort());
				if (socket != null) {
					socket.gotError(receiveMH,packetreply);
				}
			} else {
				// TODO: error from stream sending,
				RawDataPacket rawpacketreply = (RawDataPacket) receiveMH.getMessageAttribute(MessageAttributeType.RawData);
				if (rawpacketreply != null) {}
			}
		}
	}
	// response message is exchanged when session is alive
	public void gotPingPeerResponse(MessageHeader receiveMH) {
		
		// Is received after session is OK
		if (!isErrorInMessage(receiveMH)) {
			updateAliveTime();
			last_time_peer_reponse.set(System.currentTimeMillis());
			// check if session key is confirmed from other peer
			getSessionEncrypt().gotSessionKeyConfirmed(receiveMH);
			// check if public key is sent direct from other peer
			// very important (do not clean out)
			getSessionEncrypt().gotPublicKey(receiveMH, true);
			setConnected(receiveMH);
			runConnectPingTask();
		} else {
			gotPingPeerError(receiveMH);
		}
	}
	
	// action to take if peer not found on KA
	public void gotNoPeerid(MessageHeader receiveMH) {
		// got response for no peer found
		if (!isConnected()) {
			// remove KA address from waiting list
			// when list is empty, waiting for request stops
			connect_requested_addresses.remove(receiveMH.getAddress().getAddressPort());
			getPeerInfo().setLastNotFound();
		}
	}
	// LHA: MIDDLMAN: action to take if no MM found on KA
	public void gotNoMiddleman(MessageHeader receiveMH) {
		// got response for no middle man when request connect with middle man

		if (!isConnected()) {
			// remove KA address from waiting list
			connect_requested_addresses.remove(receiveMH.getAddress().getAddressPort());
			getPeerInfo().setLastNotFound();
		}
	}
	public boolean isErrorInMessage(MessageHeader receiveMH) {
		
		ErrorCode err = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
		if (err != null) {
		
			getNameSpace().getListen().message(
					getNameSpace().getClientid() + " error from " + getPeerid().getUserid() + " "
							+ (getPeerAddress() != null ? getPeerAddress().getAddressPort() : "") + " message: " + err.getReason() + ", code: "
							+ String.valueOf(err.getResponseCode()));
			if (err.getResponseCode() == 703) {
				getNameSpace().getListen().peerErrors(this, ReturMessageTypes.peer_is_closed, err.getReason());
				killSession("error from other peer");
			} else if (err.getResponseCode() == 705) {
				// socket is closed
				getNameSpace().getListen().peerErrors(this, ReturMessageTypes.peer_unknown_port, err.getReason());
			} else if (err.getResponseCode() == 711) {
				// socket is closed
				getNameSpace().getListen().peerErrors(this, ReturMessageTypes.peer_is_stopped, err.getReason());
				killSession("error from other peer");
			} else if (err.getResponseCode() == 712) {
				// TODO: LHA: do nothing if not connected
				// session on other side might not be ready
				// check if ping has arrived
				if (isConnected()) {
					killSession("closed other side");
				}
			} else if (err.getResponseCode() == 715) {
				// stream socket is closed
				getNameSpace().getListen().peerErrors(this, ReturMessageTypes.peer_unknown_port, err.getReason());
			} else {
				getNameSpace().getListen().peerErrors(this, ReturMessageTypes.socket_error, err.getReason());
			}
			return true;
		} else {
			return false;
		}
	}
	// check message from other peer
	// validate ok to receive
	public boolean isOkRequest(MessageHeader message) {
		return getRequestId() == message.getTransactionID();
	}
	
	//
	// main check for active when incoming
	//
	public boolean isPeerReady(MessageHeader message) {
		
		if (peer_is_running.get()) {
		
			if (isPinged()) {
			
				// is ready, check address and port
				// implicates that once a session is ready
				// only first ping address and port are valid
				if (getPeerAddress().getAddressPort().equals(message.getAddress().getAddressPort())) {
					return true;
				} else {
					return false;
				}
			
			} else {
				
				if (connection_requests.containsKey(message.getAddress().getAddressPort())) {
					return true;
				} else {
					for (ConnectionNegotiate negotiate : connection_requests.values()) {
						if (negotiate.getAddress().getAddress().equals(message.getAddress().getAddress())) {
							return true;
						}
					}
					return false;
				}
			}
			
		} else {
			return false;
		}
	}
	
	void updateAliveTime() {
		last_time_peer_alive.set(System.currentTimeMillis());
	}
	public void updateDataTime() {
		last_time_peer_alive.set(System.currentTimeMillis());
		last_time_peer_data.set(System.currentTimeMillis());
	}
	public void updateSendTime() {
		last_time_peer_data.set(System.currentTimeMillis());
	}
	// got a data reply from peer
	public void gotDataReply(MessageHeader receiveMH) {
		
		if (peer_is_running.get() && peer_is_connected.get()) {
			
			updateDataTime();
			// LHA: use data reply packet
			DataReplyPacket packetreply = (DataReplyPacket) receiveMH.getMessageAttribute(MessageAttributeType.DataReplyPacket);
			if (packetreply != null) {
				Socket socket = getNameSpace().getSocket(packetreply.getPort());
				if (socket != null) {
					socket.gotDataReply(receiveMH, packetreply);
				}
			}
		}
	}
	// got data message from peer
	public void gotData(MessageHeader receiveMH) {
		
		if (peer_is_running.get()) {
		
			updateDataTime();
			setConnected(receiveMH);
			DataPacket packet = (DataPacket) receiveMH.getMessageAttribute(MessageAttributeType.DataPacket);
			
			if (getNameSpace().isSocketOpen(packet.getPort())) {
			
				Socket socket = getNameSpace().getSocket(packet.getPort());
				
				if (socket != null) {
					// check busy
					// busy is set if to many buffers not read by
					// Socket.readBuffer()
					if (!socket.isBusy()) {
						socket.gotData(this, receiveMH, packet);
					} else {
						// TODO: LHA: handle socket busy situation 1.5.2014
						// socket is busy, buffer is full, don't read
						// send user name
						// add, because we don't handle the busy situation yet
						socket.gotData(this, receiveMH, packet);
					}
				}
			} else {

				// send info to peer the socket is closed
				// error inn adding data to socket
				try {
					// LHA: changed 30.6.2016, use dataReply instead of ping
					// reply message to signal port is closed
					MessageHeader dataReply = new MessageHeader(MessageHeader.MessageHeaderType.PingPeerResponse);
					dataReply.setTransactionID(receiveMH.getTransactionID());
					dataReply.setAddress(receiveMH.getAddress());
					ErrorCode err = new ErrorCode();
					err.setResponseCode(705); // socket port not open
					dataReply.addMessageAttribute(err);
					DataReplyPacket reply2 = new DataReplyPacket();
					reply2.setPort(packet.getPort());
					reply2.setChunkNumber(packet.getChunkNumber());
					reply2.setChunkVersion(packet.getChunkVersion());
					dataReply.addMessageAttribute(reply2);
					sendMessage(dataReply);
				} catch (MessageAttributeException e) {
					getNameSpace().getListen().fatalError(getNameSpace().getClientid() + " error in sending response " + e.getMessage());
				}
			}
		}
	}
	// reset all for peer and lose
	public void resetSession(String reason) {
		getPeerInfo().setSessionKey(null);
		getPeerInfo().setRemoteAesKey(null);
		closeSession(reason);
	}
	// close this channel
	public void closeSession(String reason) {
		if (peer_is_connected.get() && peer_is_running.get()) {
			// send close ping
			MessageHeader msg = new MessageHeader(MessageHeader.MessageHeaderType.PingClose);
			msg.setTransactionID(getRequestId());
			msg.setAddress(getPeerAddress());
			sendMessage(msg);
			sendMessage(msg);
			getNameSpace().getListen().onPeerDisconnected(getPeerAddress());
		}
		killSession(reason);
	}
	// kill this channel
	// without sending response
	public void killSession(String reason) {
		// LHA: first remove from list
		// then stop / kill session
		//System.out.println("killing " + reason);
		if (peer_is_running.getAndSet(false)) {
			// make sure ping task close
			if (pingtask != null) {
				pingtask.forceStop();
			}
			getNameSpace().getListen().onPeerDisconnected(getPeerAddress() != null ? getPeerAddress() : new NetAddress("0:0:0:0", 0));
		}
		peer_is_connected.set(true);
	}
	public boolean isPingTimeout() {
		return System.currentTimeMillis() - last_time_peer_alive.get() > InitVar.PEER_TIMEOUT;
	}
	public boolean isDataTimeout() {
		return System.currentTimeMillis() - last_time_peer_data.get() > InitVar.PEER_DATA_TIMEOUT;
	}
	public boolean isTimeout() {
		return isPingTimeout() | isDataTimeout();
	}
	public boolean sendMessage(MessageHeader msg) {
		try {
			if (msg.getAddress() == null || msg.getAddress().getAddressPort().equals("0.0.0.0:0")) {
				getNameSpace().getListen().error("can't send message with empty address");
				return false;
			} else if (msg.getTransactionID() == 0) {
				getNameSpace().getListen().error("can't send message messageid = 0");
				return false;
			} else {
				// add encrypted AES key to other party if not already sent
				getNameSpace().getMessageSender().UDPSendEncrypt(msg);
				return true;
			}
		} catch (Exception e) {
			getNameSpace().getListen().error("exeption when send message, error: " + e.getMessage());
		}
		return false;
	}
	public boolean sendPing() {
		MessageHeader msg = new MessageHeader(MessageHeader.MessageHeaderType.PingPeer);
		msg.setTransactionID(getRequestId());
		msg.setAddress(getPeerAddress());
		msg = getSessionEncrypt().sendSessionKey(msg, "ping");
		return sendMessage(msg);
	}
	// LHA:
	// evaluate speed and adjust timeout
	// used in socket buffer
	private AtomicLong actualTimeout = new AtomicLong();
	public long getActualTimeout() {
		return actualTimeout.get();
	}
	public void setActualTimeout(long actualTimeout) {
		this.actualTimeout.set(actualTimeout);
	}

	public void gotStreamData(MessageHeader receiveMH) {
		
		if (isRunning()) {
			
			updateDataTime();
			RawDataPacket packet = (RawDataPacket) receiveMH.getMessageAttribute(MessageAttributeType.RawData);
			
			if (packet != null) {
			
				StreamSocket stream_socket = getNameSpace().getStreamSockets().get(packet.getPort());
				if (stream_socket != null && stream_socket.isOpen()) {
					// offer the data
					stream_socket.gotData(this, receiveMH.getTransactionID(), packet);
				} else {

					// send info to peer the socket is closed
					// error inn adding data to socket
					try {
						// LHA: changed 30.6.2016, use dataReply instead of ping
						// reply message to signal port is closed
						MessageHeader dataReply = new MessageHeader(MessageHeader.MessageHeaderType.PingPeerResponse);
						dataReply.setTransactionID(receiveMH.getTransactionID());
						dataReply.setAddress(receiveMH.getAddress());
						
						ErrorCode err = new ErrorCode();
						err.setResponseCode(715); // socket port not open
						dataReply.addMessageAttribute(err);
						
						RawDataPacket reply2 = new RawDataPacket();
						reply2.setPort(packet.getPort());
						dataReply.addMessageAttribute(reply2);
						
						sendMessage(dataReply);
						
					} catch (MessageAttributeException e) {
						getNameSpace().getListen().fatalError("error in sending response " + e.getMessage());
					}
				}
			} else {
				getNameSpace().getListen().error("got NON stream type packet from peer " + (getPeerAddress() != null ? getPeerAddress().getAddressPort() : ""));
			}
		} else {
			getNameSpace().getListen().error("got stream data when not running");
		}
	}
	public void gotFoundPeer(MessageHeader receiveMH) {}
	// session thread
	private PingTask pingtask;
	class PingTask extends Task {
		public PingTask(int serverId) {
			super(serverId, InitVar.PEER_PING_REPLY_TIME);
		}
		@Override
		public void onStart() {
			// start run when either response or
			// request message is arrived
			// is then set for running running
			getNameSpace().getListen().debug(
					getNameSpace().getClientid() + " session started for " + getPeerAddress().getAddressPort() + " client id " + getPeerid().getUserid());
			// set timeout (both ping and data)
			updateDataTime();
		}
		@Override
		public void onExecute() {
			if (peer_is_running.get()) {
				if (isPingTimeout()) { // Since last ping
					// ping not returned peer is disconnected
					getNameSpace().getListen().debug(
							getNameSpace().getClientid() + " ping timeout " + String.valueOf(System.currentTimeMillis() - last_time_peer_alive.get()) + " "
									+ getPeerAddress().getAddressPort() + " client id " + getPeerid().getUserid());
					killSession("ping timeout");
					forceStop();
				
				} else if (isDataTimeout()) { // since last data
					// ping not returned peer is disconnected
					getNameSpace().getListen().debug(
							getNameSpace().getClientid() + " data timeout " + String.valueOf(System.currentTimeMillis() - last_time_peer_data.get()) + " "
									+ getPeerAddress().getAddressPort() + " client id " + getPeerid().getUserid());
					closeSession("data timeout");
					forceStop();
				}
				if (peer_is_running.get()) {
					if (System.currentTimeMillis() - last_time_peer_reponse.get() > InitVar.PEER_PING_REPLY_TIME) {
						getNameSpace().getListen().debug(
								getNameSpace().getClientid() + " ping send to " + (getPeerAddress() != null ? getPeerAddress().getAddressPort() : "")
										+ " client id " + getPeerid().getUserid());
						sendPing();
						waitFor(InitVar.PEER_PING_REPLY_TIME);
					} else {
						waitFor(InitVar.PEER_PING_TIME);
					}
				} else {
					forceStop();
				}
			} else {
				forceStop();
			}
		}
		@Override
		public void onStop() {}
	}
	// run the connectpingtask to initiate encryption
	public void runConnectPingTask() {
		// start negotiate key if not confirmed
		if (isConnected() && !getSessionEncrypt().isKeyConfirmed()) {
			// keep sending key negotiation ping until
			// key is confirmed
			if (getConnectPingTask() == null) {
				getNameSpace().getMonitors().getConnectMonitor().execute(new ConnectPingTask(this));
			} else {
				getConnectPingTask().initStopTime();
			}
		}
	}
	private ConnectPingTask connectPingTask=null;
	public void setConnectPingTask(ConnectPingTask connectPingTask) {
		if(this.connectPingTask!=null) {
			this.connectPingTask.forceStop();
		}
		this.connectPingTask = connectPingTask;
	}
	public ConnectPingTask getConnectPingTask() {
		return connectPingTask;
	}
	public void disConnect() {
		closeSession("disconnect");
	}
	public boolean isPinged() {
		return peer_is_pinged.get();
	}
}
