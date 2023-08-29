package no.auke.p2p.m2.workers.connect;

import java.util.concurrent.atomic.AtomicLong;

import no.auke.m2.task.Task;
import no.auke.p2p.m2.InitVar;
import no.auke.p2p.m2.workers.PeerSession;

// running a separate task to ping in keys
public class ConnectPingTask extends Task {
	final private PeerSession peeragent;
	AtomicLong stopTime = new AtomicLong(0);
	public PeerSession getPeerAgent() {return peeragent;}
	public void initStopTime() {
		stopTime.set(System.currentTimeMillis() + InitVar.INIT_PEER_ENCRYPTION_TIMEOUT);
	}
	public ConnectPingTask(PeerSession peeragent) {
		super(peeragent.getNameSpace().getMainServ().getServerId(), InitVar.PING_FREQUENCY_NOT_ENCRYPTING);
		this.peeragent = peeragent;
		getPeerAgent().setConnectPingTask(this);
	}
	@Override
	public void onStart() {initStopTime();}
	@Override
	public void onExecute() {
		if (stopTime.get() > System.currentTimeMillis() && getPeerAgent().isConnected() && !getPeerAgent().getSessionEncrypt().isKeyConfirmed()) {
			getPeerAgent().sendPing();
		} else {
			forceStop();
		}
	}
	@Override
	public void onStop() {getPeerAgent().setConnectPingTask(null);}
};