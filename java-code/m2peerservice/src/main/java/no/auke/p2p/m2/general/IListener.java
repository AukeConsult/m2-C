/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.general;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import no.auke.p2p.m2.workers.PeerSession;
import no.auke.p2p.m2.workers.keepalive.NetAddress;

public abstract class IListener {
	protected Logger getLogger() {
		return null;
	}
	protected AtomicInteger lockcont = new AtomicInteger();
	private int loglevel = 0;
	public void setLockcont(int lockcont) {
		this.lockcont.set(lockcont);
	}
	public int getLockcont() {
		return lockcont.get();
	}
	public IListener(int Loglevel) {
		setLoglevel(Loglevel);
	}
	public IListener() {}
	public void Exception(Object obj, ReturMessageTypes megtype, String msg, Exception Exception) {
		if (Exception != null)
			error(obj.getClass().getName() + ":" + megtype.toString() + ":" + msg + ":" + Exception.getClass().getName() + ":" + Exception.getMessage());
		else
			error(obj.getClass().getName() + ":" + megtype.toString() + ":" + msg);
	}
	public void onTrace(String message) {}
	public final synchronized void trace(String message) {
		if (getLoglevel() <= 0) {
			if (getLogger() != null) {
				getLogger().trace("T:" + message);
			} else {
				printLog("T:" + message);
			}
		}
	}
	public void onDebug(String message) {}
	public final synchronized void debug(String message) {
		if (getLoglevel() <= 1) {
			if (getLogger() != null) {
				getLogger().debug("D:" + message);
			} else {
				printLog("D:" + message);
			}
		}
	}
	public void onMessage(String message) {}
	public final synchronized void message(String message) {
		if (getLoglevel() <= 2) {
			if (getLogger() != null) {
				getLogger().info("M:" + message);
			} else {
				printLog("M:" + message);
			}
		}
	}
	public void onError(String message) {}
	public final synchronized void error(String message) {
		onError(message);
		if (getLoglevel() <= 3) {
			if (getLogger() != null) {
				getLogger().warn("ERROR:" + message);
			} else {
				printLog("ERROR:" + message);
			}
		}
	}
	public void onFatalError(String message) {}
	public final synchronized void fatalError(String message) {
		onFatalError(message);
		if (getLoglevel() <= 4) {
			if (getLogger() != null) {
				getLogger().error("FATALERROR:" + message);
			} else {
				printLog("FATALERROR:" + message);
			}
		}
	}
	public abstract void printLog(String message);
	public synchronized void setLoglevel(int loglevel) {
		this.loglevel = loglevel;
	}
	public int getLoglevel() {
		return loglevel;
	}
	public void peerErrors(PeerSession session, ReturMessageTypes megtype, String msg) {
		error(session.getPeerid().getUserid() + " err: " + megtype.toString() + ", " + msg);
	}
	public void lock() {
		lockcont.getAndIncrement();
	}
	public void unLock() {
		lockcont.getAndDecrement();
	}
	// events for service
	// Service is started
	public abstract void onServiceStarted(String message);
	// Service is stopped
	public abstract void onServiceStopped(String message);
	// Service is connected
	public abstract void onServiceConnected(NetAddress publicAddress, NetAddress kaServerAddress);
	// Service is disconnected
	public abstract void onServiceDisconnected(NetAddress kaServerAddress);
	// Service is disconnected with wrong user id
	public abstract void connectionRejected(NetAddress kaServerAddress, String msg);
	// Peer Service is connected
	public abstract void onPeerConnected(NetAddress peerAddress);
	// Peer Service is disconnected
	public abstract void onPeerDisconnected(NetAddress peerAddress);
	// message sent
	public abstract void onMessageSend(NetAddress peerAddress, int socketPort, int messageId, int size);
	// message sent
	public abstract void onMessageRecieved(NetAddress peerAddress, int socketPort, int messageId, int size);
	// message sent
	public abstract void onMessageDisplay(String message);
	// message sent
	public abstract void onMessageConfirmed(NetAddress peerAddress, int messageId);
	public abstract void onTraffic(float bytes_in_sec, float bytes_out_sec, long bytes_total_in, long bytes_total_out);
	public abstract void onLicenseError(LicenseReasons reason, String licenseKey);
}