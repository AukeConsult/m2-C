/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.sockets.udp;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.m2.task.TaskWaitFirst;
import no.auke.p2p.m2.InitVar;
import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.SocketRetStatus;
import no.auke.p2p.m2.workers.PeerSession;
import no.auke.p2p.m2.workers.keepalive.Peerid;

// send and receive packet buffer
public abstract class SocketBuffer {
	
	static final Logger logger = LoggerFactory.getLogger(SocketBuffer.class);
	
	public abstract void clear();
	
	// TODO: LHA, finish up control on start and stop send
	
	private Socket socket;
	public Socket getSocket() {return socket;}
	protected PeerSession peeragent;
	public PeerSession getPeerAgent() {return peeragent;}

	protected int port = 0;
	protected int length = 0;
	// total number of data packets in buffer
	protected AtomicInteger numPackets = new AtomicInteger();
	public int getNumpackets() {return numPackets.get();}
	public int getPort() {return port;}
	
	protected int transactionId = 0;
	public int getTranactionId() {return transactionId;}
	
	protected int msgId = 0;
	public int getMsgId() {return msgId;}
	public int length() {return length;}
	protected short keyId = 0;
	public short getKeyId() {return keyId;}
	public void setKeyId(short keyId) {this.keyId=keyId;}

	protected AtomicBoolean closed = new AtomicBoolean();
	public boolean isClosed() {return closed.get();}
	
	// HUYDO: add who sent this buffer? / We can attach more specific info
	protected Peerid peerId;
	public Peerid getPeerId() {return peerId;}
	public void setPeerId(Peerid peerId) {this.peerId = peerId;}
	
	// for timing of buffer
	protected AtomicLong starttime = new AtomicLong();
	protected AtomicLong last_time_packet = new AtomicLong();
	public int getSendTime() {
		if (last_time_packet.get() > 0) {
			return (int) (last_time_packet.get() - starttime.get());
		} else {
			return 0;
		}
	}
	public boolean canRemove() {
		// LHA: 1.5.2014, delete if very old (more than 30 seconds since last
		// packet)
		if ((System.currentTimeMillis() - last_time_packet.get() > InitVar.MESSAGE_MAX_TIME_WAIT_TO_COMPLETE)) {
			return true;
		} else if (closed.get() && (System.currentTimeMillis() - last_time_packet.get()) > 5000) {
			return true;
		} else {
			return false;
		}
	}
	
	
	private String subclass = "";
	protected String getDebugId() {return subclass + " " + String.valueOf(port) + " " + String.valueOf(transactionId);}
	
	protected SocketRetStatus ret;
	public SocketRetStatus getSocketRetStatus() {return ret;}
	
	class CheckTask extends TaskWaitFirst {
		public CheckTask(int serverId, int frequency) {
			super(serverId, frequency);
		}
		@Override
		public void onStart() {}
		@Override
		public void onExecute() {
			if (!runCheck()) {
				forceStop();
			}
		}
		@Override
		public void onStop() {}
	};
	
	private CheckTask checktask;
	
	// general constructor
	public SocketBuffer(String subclass, final PeerSession peersession, final SocketRetStatus ret, final Socket socket) {
		
		this.msgId = socket.nextMsgId();
		this.keyId = peersession.getSessionEncrypt().getKeyId();
		this.transactionId = peersession.getRequestId();
		this.ret = ret;
		this.socket = socket;
		this.port = socket.getPort();
		this.peeragent = peersession;
		this.peerId = peersession.getPeerid();
		this.closed.set(false);
		this.subclass = subclass;	
		
		this.checktask = new CheckTask(peersession.getMainServ().getServerId(), 250);
		this.peeragent.getNameSpace().getMonitors().getSendMonitor().execute(checktask);
	
	}
	protected void startCheckTimouts() {
		this.peeragent.getNameSpace().getMonitors().getSendMonitor().execute(checktask);
	}
	public abstract boolean runCheck();
}
