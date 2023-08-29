package no.auke.p2p.m2.workers.connect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.m2.task.Task;
import no.auke.p2p.m2.InitVar;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.workers.PeerSession;
import no.auke.p2p.m2.workers.keepalive.NetAddress;

// helper thread
// to bang hole in NAT
// or send initial ping to other peer
public class ConnectionNegotiate {
	private static final Logger logger = LoggerFactory.getLogger(ConnectionNegotiate.class);
	private static final int MAX_PING = 20;
	private int connectPingDelay;
	private NetAddress address = null;
	public NetAddress getAddress() {
		return address == null ? new NetAddress() : address;
	}
	private PeerSession peeragent = null;
	public PeerSession getSession() {
		return peeragent;
	}
	public String getAddressPort() {
		return getAddress().getAddressPort();
	}
	private Task task;
	public ConnectionNegotiate() {}
	public ConnectionNegotiate(PeerSession peeragent, NetAddress address, int connectPingDelay) {
		this.address = address;
		this.peeragent = peeragent;
		this.connectPingDelay = connectPingDelay;
	}
	public void reStart() {
		stop();
		run();
	}
	// @Override
	public void run() {
		if (!getSession().isConnected() && task == null) {
			if (!getAddress().getAddressPort().equals("0.0.0.0:0")) {
				task = new Task(getSession().getNameSpace().getMainServ().getServerId(), 50) {
					long startConnect = 0;
					long lastping = 0;
					int numPing = 0;
					@Override
					public void onStart() {
						try {
							if (getSession().isConnected()) {
								forceStop();
							} else if (getSession().isPinged() && getSession().getPeerAddress() != null
									&& !getSession().getPeerAddress().getAddressPort().equals(getAddress().getAddressPort())) {
								forceStop();
							} else {
								startConnect = System.currentTimeMillis();
							}
						} catch (Exception ex) {
							getSession().getNameSpace().getListen().fatalError("error task " +ex.getMessage());
						}
					}
					@Override
					public void onExecute() {

						if (getSession().isRunning() && !getSession().isConnected() && numPing < MAX_PING
								&& (System.currentTimeMillis() - startConnect) < InitVar.PEER_CONNECT_TIMEOUT) {
							if (getSession().isPinged() && getSession().getPeerAddress() != null
									&& !getSession().getPeerAddress().getAddressPort().equals(getAddress().getAddressPort())) {
								forceStop();
							} else if (System.currentTimeMillis() - lastping > ConnectionNegotiate.this.connectPingDelay) {
								MessageHeader pingMsg = new MessageHeader(MessageHeader.MessageHeaderType.PingPeer);
								pingMsg.setTransactionID(getSession().getRequestId());
								pingMsg.setAddress(getAddress());
								pingMsg = getSession().getSessionEncrypt().sendSessionKey(pingMsg, "ping");
								// send 3 messages and wait
								getSession().sendMessage(pingMsg);
								getSession().sendMessage(pingMsg);
								getSession().sendMessage(pingMsg);
								lastping = System.currentTimeMillis();
								// send only a few pings
								numPing++;
							}
						} else {
							forceStop();
						}
					}
					@Override
					public void onStop() {}
				};
				getSession().getNameSpace().getMonitors().getConnectMonitor().execute(task);
			}
		}
	}
	public void stop() {
		if (task != null) {
			task.forceStop();
			task = null;
		}
	}
}
