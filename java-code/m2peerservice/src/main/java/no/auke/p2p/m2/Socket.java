/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2;

import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import no.auke.m2.task.TaskExecute;
import no.auke.p2p.m2.general.ReturMessageTypes;
import no.auke.p2p.m2.message.attribute.DataPacket;
import no.auke.p2p.m2.message.attribute.DataReplyPacket;
import no.auke.p2p.m2.message.attribute.ErrorCode;
import no.auke.p2p.m2.message.attribute.MessageAttribute;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.sockets.udp.SocketBuffer;
import no.auke.p2p.m2.sockets.udp.SocketBufferIn;
import no.auke.p2p.m2.sockets.udp.SocketBufferOut;
import no.auke.p2p.m2.workers.PeerSession;
import no.auke.p2p.m2.workers.keepalive.Peerid;
import no.auke.util.Lock;

public class Socket {
	
	public class Packet {
		
		public String getRemoteClientId() {return session.getPeerid().getUserid();}
		public Peerid getRemotePeerId() {return session.getPeerid();}
		
		int transactionId = 0;
		public int getTransactionId() {return transactionId;}
		byte[] data;
		public byte[] getData() {return data;}

		public Packet() {};
		public Packet(PeerSession session, int transactionId, byte[] data) {
			this.transactionId = transactionId;
			this.data = data;
			this.session = session;
		}
		private PeerSession session;
		public PeerSession getPeerSession() {return session;}
	}
	
	// for inspection
	public String getSocketStatus() {
		StringBuilder sb = new StringBuilder();
		sb.append("--outgoing_messages.size()=" + outgoing_messages.size());
		sb.append("--incoming_messages.size()=" + incoming_messages.size());
		sb.append("--inbuff.size()=" + inbuff.size());
		sb.append("--isBusy()=" + isBusy());
		sb.append("--isOpen()=" + isOpen());
		return sb.toString();
	}
	
	TaskExecute cleanup = null;
	class CleanUp extends TaskExecute {
		
		long lastClean = System.currentTimeMillis();
		public CleanUp(int serverId) {super(serverId, 10000);}

		// LHA:
		// loop thru finish messages and delete
		// Message can't be deleted right away because re send packets might be
		// under way
		//
		
		@Override
		public void onExecute() {
			
			// delete incoming closed older than 10 seconds
			if (Socket.this.incoming_messages.size() > 0) {
				
				lastClean = System.currentTimeMillis();
				ArrayList<Integer> delete = new ArrayList<Integer>();
				for (SocketBuffer buffer : Socket.this.incoming_messages.values()) {
					if (buffer.canRemove()) {
						delete.add(buffer.getMsgId());
						buffer.clear(); // important to stop
					}
				}
				for (int msgId : delete) {incoming_messages.remove(msgId);}
			}
			
			// delete outgoing older than 10 seconds
			if (Socket.this.outgoing_messages.size() > 0) {

				lastClean = System.currentTimeMillis();
				ArrayList<Integer> delete = new ArrayList<Integer>();
				
				for (SocketBuffer buffer : Socket.this.outgoing_messages.values()) {
					if (buffer.canRemove()) {
						delete.add(buffer.getMsgId());
						buffer.clear(); // important to stop
					}
				}
				for (int msgId : delete) {outgoing_messages.remove(msgId);}
				
			}
		}
	};
	
	private Random random = new java.util.Random(System.currentTimeMillis());
	private AtomicInteger last_msgid = new AtomicInteger(-1);
	public int nextMsgId() {
		int new_id = random.nextInt(Integer.MAX_VALUE);
		while(last_msgid.get()==new_id) {
			new_id = random.nextInt(Integer.MAX_VALUE);
		}
		last_msgid.set(new_id);
		return new_id;
	}
	
	// max waiting in buffers before socket is set busy
	private AtomicBoolean isopen = new AtomicBoolean();
	protected int port = 0;
	// busy socket
	private AtomicBoolean busy = new AtomicBoolean();
	private Queue<Packet> inbuff = new ConcurrentLinkedQueue<Packet>();
	public Queue<Packet> getInbuff() {
		return inbuff;
	}
	// output buffer holds socket buffers
	// on its way out
	private Map<Integer, SocketBufferOut> outgoing_messages = new ConcurrentHashMap<Integer, SocketBufferOut>();
	public void addOutBuffer(SocketBufferOut buffer) {
		outgoing_messages.put(buffer.getMsgId(), buffer);
		// lha: just for check not growing
		if (outgoing_messages.size() > 20) {
			this.getNameSpace().getListen().error("number of open output messages to high > 5 " + String.valueOf(outgoing_messages.size()));
		}
	}	
	public void deleteOutgoingBuffer(SocketBufferOut buffer) {
		outgoing_messages.remove(buffer.getMsgId());
	}

	private Map<Integer, SocketBufferIn> incoming_messages = new ConcurrentHashMap<Integer, SocketBufferIn>();
	public void deleteIncomingBuffer(SocketBufferIn buffer) {
		incoming_messages.remove(buffer.getMsgId());
	}

	private NameSpace nameSpace;
	public void setNameSpace(NameSpace service) {this.nameSpace = service;}
	public NameSpace getNameSpace() {return nameSpace;}
	public int getPort() {return port;}
	
//	private String remoteclientid = "";
//	public String getRemoteClientid() {return remoteclientid;}
//	
//	private String remotepeerid = "";
//	public String getRemotePeerid() {return remotepeerid;}

	// for testing purpose
	public Socket(int port) {
		this.port = port;
		busy.set(false);
		// must set before starting threads (idiot)
		isopen.set(true);
	}
	public Socket(int port, NameSpace nameSpace) {
		if (nameSpace != null) {
			this.port = port;
			this.nameSpace = nameSpace;
			busy.set(false);
			// must set before starting threads (idiot)
			isopen.set(true);
			cleanup = new CleanUp(nameSpace.getMainServ().getServerId());
			if (getNameSpace().getMonitors() != null) {getNameSpace().getMonitors().getPingMonitor().execute(cleanup);}
			
		}
	}
	private SocketBufferOut testbuffer = null;
	public Socket(int port, NameSpace service, SocketBufferOut testbuffer) {
		this(port, service);
		this.testbuffer = testbuffer;
	}
//	public Socket(String remoteclientid, int port, NameSpace service) {
//		this(port, service);
//		this.remoteclientid = remoteclientid;
//		this.remotepeerid = service.makePeerid(remoteclientid).getPeerhexid();
//	}
	
	// for testing purpose to able to mock
	public SocketBufferOut getOutputBuffer(SocketRetStatus ret, PeerSession peer, int port, byte[] data) throws Exception {
		if (testbuffer != null) {
			return testbuffer;
		} else {
			return new SocketBufferOut(peer, ret, this, port == 0 ? getPort() : port, data);
		}
	}
	
	public SocketRetStatus findUser(String peerClientId) {return getNameSpace().findUser(peerClientId);}
	
	public SocketRetStatus send(String remoteClientId, final byte[] data) {return send(null, getNameSpace().makePeerid(remoteClientId), getPort(), data, null);}	
	public SocketRetStatus sendPeer(Peerid peerId, byte[] data) {return send(null, peerId, getPort(), data, null);}

	public SocketRetStatus send(SocketRetStatus ret, String remoteClientId, final byte[] data) {return send(ret, getNameSpace().makePeerid(remoteClientId), getPort(), data, null);}	
	public SocketRetStatus sendPeer(SocketRetStatus ret, Peerid peerId, byte[] data) {return send(ret, peerId, getPort(), data, null);}

	public SocketRetStatus send(String remoteClientId, final byte[] data, SendTask sendTask) {return send(null, getNameSpace().makePeerid(remoteClientId), getPort(), data, sendTask);}	
	public SocketRetStatus sendPeer(Peerid peerId, byte[] data, SendTask sendTask) {return send(null, peerId, getPort(), data, sendTask);}

	public SocketRetStatus send(SocketRetStatus ret, String remoteClientId, final byte[] data, SendTask sendTask) {return send(ret, getNameSpace().makePeerid(remoteClientId), getPort(), data, sendTask);}	
	public SocketRetStatus sendPeer(SocketRetStatus ret, Peerid peerId, byte[] data, SendTask sendTask) {return send(ret, peerId, getPort(), data, sendTask);}
		
	private Lock sendlock = new Lock();
	public SocketRetStatus send(SocketRetStatus ret, final Peerid peerid, final int remotePort, byte[] data, SendTask sentTask) {
		
		if(ret==null) ret = new SocketRetStatus();
		
		if (!isopen.get()) {
			ret.setLastRetcode(ReturMessageTypes.send_socket_closed);
			ret.setLastMessage("socket is closed");
			return ret;
		} else if (!getNameSpace().isRunning()) {
			ret.setLastRetcode(ReturMessageTypes.service_not_running);
			ret.setLastMessage("service not running");
			return ret;			
		} else if (data == null || data.length == 0) {
			ret.setLastRetcode(ReturMessageTypes.empty_data);
			ret.setLastMessage("no data to send");
			return ret;
		}
		
		if (getNameSpace().getReConnector().waitConnect()) {
			
			// peer = service.openPeer(peerClientId, true); // TODO: HUY: Check
			// the getPeerAgent this is critical

			if (ret.getPeerSession() == null) {
				ret = getNameSpace().openPeer(peerid);
				if (!ret.isOk()) {
					return ret;
				}
			}			
			
			if (ret.getPeerSession().isFinish()) {
				// get a new session with same peerid
				ret = getNameSpace().openPeer(ret.getPeerSession().getPeerid());
				if (!ret.isOk()) {
					return ret;
				}
			}
			
			if (sentTask == null) {
			
				if (!ret.getPeerSession().findAndConnect()) {
					
					ret.setLastRetcode(ReturMessageTypes.peer_not_found);
					ret.setLastMessage("can not find peer " + ret.getPeerSession().getPeerid().getUserid());
					
					return ret;
				}
				
				try {
				
					sendlock.lock();
					// wait for encryption to enable
					ret = ret.getPeerSession().getSessionEncrypt().waitForEncryption(ret);
					// make sure encryption is in place before starting to send
					// data
					if (ret.getPeerSession().isRunning() && ret.isOk()) {
						try {
							
							this.getNameSpace().getListen().onMessageSend(ret.getPeerSession().getPeerAddress(),getPort(),ret.getPeerSession().getRequestId(),data.length);

							SocketBufferOut buffer = getOutputBuffer(ret, ret.getPeerSession(), remotePort, data);
							data = null;
							if (buffer.send()) {
								ret.setLastRetcode(ReturMessageTypes.ok);
							} else if (ret.getLastRetcode() == ReturMessageTypes.send_no_session) {
								this.getNameSpace().getListen().debug(String.valueOf(ret.getLastRetcode()) + " : " + ret.getLastMessage());
							} else if (ret.getLastRetcode() == ReturMessageTypes.send_timeout) {
								this.getNameSpace().getListen().debug(String.valueOf(ret.getLastRetcode()) + " : " + ret.getLastMessage());
							} else {
								this.getNameSpace().getListen().error(String.valueOf(ret.getLastRetcode()) + " : " + ret.getLastMessage());
								if (InitVar.PEER_SESSION_CLOSE_ON_FAIL) {
									ret.getPeerSession().closeSession("can not send buffer");
								}
							}
						} catch (Exception e) {
							ret.setLastRetcode(ReturMessageTypes.sending_error);
							ret.setLastMessage(e.getMessage());
						}
					} else {
						if (ret.getLastRetcode() == ReturMessageTypes.no_session_encryption) {
							this.getNameSpace().getListen().fatalError("Session encryption not enabled");
							ret.getPeerSession().resetSession("no sesson encryption");
						}
						return ret;
					}
				} catch (Exception ex) {
					this.getNameSpace().getListen().fatalError("Socket exception " +  ex.getMessage());
				} finally {
					sendlock.unlock();
				}
			} else {
				// TODO: make send async
				// send async
				sentTask.send(this, ret, remotePort, data);
			}
		} else {
			if (!getNameSpace().isConnected()) {
				ret.setLastRetcode(ReturMessageTypes.service_not_connected);
				ret.setLastMessage("service not connected");
			} else {
				ret.setLastRetcode(ReturMessageTypes.service_is_restarting);
				ret.setLastMessage("server is restarting");
			}
		}
		return ret;
	}
	public void close(String peerClientId) {
		// LHA, was error
		PeerSession session = getNameSpace().getOpenPeerSessions().get(getNameSpace().makePeerid(peerClientId).getPeerhexid());
		if (session != null) {
			session.closeSession("socket closed");
		}
	}
	public void close() {
		if (isopen.getAndSet(false)) {
			inbuff.clear();
			synchronized (readsync) {
				readsync.notify();
			}
			// cleans up all buffers etc when stopping
			if (cleanup != null) {
				cleanup.forceStop();
			}
			outgoing_messages.clear();
			if (getNameSpace() != null) {
				getNameSpace().removeSocket(this);
			}
		}
	}
	// called from SocketBufferIncoming to offer itself to
	// socket
	private Object readsync = new Object();
	public void offer(SocketBufferIn buffer) {
		synchronized (readsync) {
			inbuff.offer(new Packet(buffer.getPeerAgent(), buffer.getTranactionId(), buffer.getBuffer()));
			readsync.notify();
			if (inbuff.size() > InitVar.MAX_INCOMING_SOCKET_BUFFERS) {
				busy.set(true);
			}
		}
	}
	// reading buffer
	public Packet readBuffer(int timeout) {
		synchronized (readsync) {
			try {
				if (inbuff.isEmpty()) {
					readsync.wait(timeout);
				}
			} catch (InterruptedException e) {}
			//
			if (busy.get() && inbuff.size() < InitVar.MAX_INCOMING_SOCKET_BUFFERS) {
				busy.set(false);
			}
			return inbuff.poll();
		}
	}
	
	public boolean checkIncomming(String clientid) {
		for (SocketBufferIn buffer : new ArrayList<SocketBufferIn>(incoming_messages.values())) {
			if (!buffer.isClosed() && buffer.getPeerAgent().getPeerid().getUserid().equals(clientid)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isOpen() {return getNameSpace() != null && isopen.get();}
	public boolean isEmpty() {return inbuff.isEmpty();}
	public boolean isBusy() {return busy.get();}
	public int getMaxIncomming() {return InitVar.MAX_INCOMING_SOCKET_BUFFERS;}
	
	public void gotError(MessageHeader receiveMH, DataReplyPacket packetreply) {
		
		try {
			
			// FIXIT: error handling
			
			if(packetreply!=null) {

				SocketBufferOut buffer = outgoing_messages.get(packetreply.getMsgId());
				if (buffer != null) {
					ErrorCode err = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
					if (err != null) {
						if (err.getResponseCode() == 705) {
							buffer.getSocketRetStatus().setLastRetcode(ReturMessageTypes.peer_unknown_port);
						} else {
							buffer.getSocketRetStatus().setLastRetcode(ReturMessageTypes.sending_error);
						}
						buffer.getSocketRetStatus().setLastMessage(err.getReason());
						buffer.failed();
					}
				}
				
			}
		
			
		} catch (Exception ex) {
			this.getNameSpace().getListen().fatalError("Socket exception " +  ex.getMessage());
		}
	}
	public void gotDataReply(MessageHeader receiveMH, DataReplyPacket packet) {
		
		try {
			SocketBufferOut buffer = outgoing_messages.get(packet.getMsgId());
			if (buffer != null) {
				buffer.gotDataReply(receiveMH, packet);
			}
		} catch (Exception ex) {
			this.getNameSpace().getListen().fatalError("Socket exception " +  ex.getMessage());
		}
	}
	public boolean gotData(PeerSession peeragent, MessageHeader receiveMH, DataPacket packet) {
		try {
			
			// LHA: find the buffer
			// check how many waiting, could be broken transmissions
			if (!incoming_messages.containsKey(packet.getMsgId())) {
				
				// new message arrive
				// add a new buffer
				
				incoming_messages.put(
						packet.getMsgId(),
						new SocketBufferIn(peeragent, new SocketRetStatus(), this, packet.getMsgId())
				);
				
				// lha: just for check not growing out of range
				// lha: 1.5.2014, also clean old messages
				
				if (incoming_messages.size() > InitVar.MAX_INCOMMING_MESSAGES) {
					this.getNameSpace().getListen().error("number of open input messages " + String.valueOf(incoming_messages.size()));
					getNameSpace().getMonitors().getPingMonitor().execute(cleanup);
				}
				
			}
			
			SocketBufferIn buffer = incoming_messages.get(packet.getMsgId());
			if (buffer != null) {
				// LHA: fix 1.5.2014
				if (buffer.gotDataPacket(receiveMH, packet)) {
					return true;
				}
			}
		} catch (Exception ex) {
			this.getNameSpace().getListen().fatalError("Socket exception " +  ex.getMessage());
		}
		return false;
	}
	public int getCheckWait() {
		return 50;
	}
	// LHA: added SocketHandler
	private SocketHandler socketHandler;
	public SocketHandler getSocketHandler() {
		return socketHandler;
	}
	public void setSocketHandler(SocketHandler socketHandler) {
		this.socketHandler = socketHandler;
		this.socketHandler.setSocket(this);
	}
}
