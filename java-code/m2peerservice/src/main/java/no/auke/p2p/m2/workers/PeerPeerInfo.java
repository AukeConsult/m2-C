/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.workers;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import no.auke.p2p.m2.InitVar;
import no.auke.p2p.m2.message.attribute.SessionKey;
import no.auke.p2p.m2.workers.keepalive.NetAddress;

public class PeerPeerInfo {
	
	private String peerClientId;
	private String peerid = "";
	private NetAddress address;
	private boolean localaddress;
	private Date lastactive;
	private boolean closed = true;
	private AtomicLong lastNotFound = new AtomicLong();
	
	public String getPeerid() {return peerid;}
	public void setPeerid(String peerid) {this.peerid = peerid;}
	public NetAddress getAddress() {return address;}
	public void setAddress(NetAddress address) {this.address = address;}
	public void setLocaladdress(boolean localaddress) {this.localaddress = localaddress;}
	public boolean isLocaladdress() {return localaddress;}
	public Date getLastactive() {return lastactive;}
	public void setLastactive(Date lastactive) {this.lastactive = lastactive;}
	public String getPeerClientId() {return peerClientId;}
	public boolean isClosed() {return closed;}
	public void setClosed() {this.closed = true;}
	
	
	public PeerPeerInfo(String peerId, String peerClientId) {
		this.peerid = peerId;
		this.peerClientId = peerClientId;
	}
	public PeerPeerInfo(String peerId) {this.peerid = peerId;}
	void lastPingPeer(NetAddress address, String peerclientid) {
		this.address = address;
		this.lastactive = new Date();
		this.peerClientId = peerclientid;
		this.closed = false;
	}
	public long getLastNotFound() {return lastNotFound.get();}
	// at least a minute between each attempt to find in KA
	public boolean readyForLookup() {return System.currentTimeMillis() - lastNotFound.get() > InitVar.WAIT_FOR_LOOKUP_PERIOD;}
	public int waitForLookup() {return (int) (InitVar.WAIT_FOR_LOOKUP_PERIOD - (System.currentTimeMillis() - lastNotFound.get()));}
	public void setLastNotFound() {
		remoteAesKey = null;
		localAesKey = null;
		sessionkey = null;
		address = null;
		lastNotFound.set(System.currentTimeMillis());
	}
	public void resetLastNotFound() {lastNotFound.set(0);}
	
	// LHA: direct use of remove key
	SessionKey sessionkey = null;
	public SessionKey getSessionKey() {return sessionkey;}
	public void setSessionKey(SessionKey sessionkey) {this.sessionkey = sessionkey;}
	byte[] remoteAesKey = null;
	public byte[] getRemoteAesKey() {return remoteAesKey;}
	public void setRemoteAesKey(byte[] remoteAesKey) {this.remoteAesKey = remoteAesKey;}
	private boolean dodirectconnect = false;
	public boolean doDirectConnect() {return dodirectconnect && !usedMM;}
	public void setDirectConnect(boolean dodirectconnect) {this.dodirectconnect = dodirectconnect;}
	private byte[] localAesKey = null;
	public byte[] getLocalAesKey() {return localAesKey;}
	public void setLocalAesKey(byte[] localAesKey) {this.localAesKey = localAesKey;}
	private boolean usedMM = false;
	public void setUsedMM(boolean usedMM) {this.usedMM = usedMM;}
}