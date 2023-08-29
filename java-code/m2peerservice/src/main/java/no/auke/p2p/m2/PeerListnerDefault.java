package no.auke.p2p.m2;

import no.auke.p2p.m2.general.IListener;
import no.auke.p2p.m2.general.LicenseReasons;
import no.auke.p2p.m2.workers.keepalive.NetAddress;

public class PeerListnerDefault extends IListener {
	public PeerListnerDefault() {super(5);}
	@Override
	public final void onServiceDisconnected(NetAddress kaServerAddress) {}
	@Override
	public void connectionRejected(NetAddress kaServerAddress, String msg) {}
	@Override
	public final void onPeerDisconnected(NetAddress peerAddress) {}
	@Override
	public void printLog(String message) {}
	@Override
	public void onServiceStarted(String message) {}
	@Override
	public void onServiceStopped(String message) {}
	@Override
	public void onServiceConnected(NetAddress publicAddress, NetAddress kaServerAddress) {}
	@Override
	public void onLicenseError(LicenseReasons reason, String licenseKey) {}
	@Override
	public void onPeerConnected(NetAddress peerAddress) {}
	@Override
	public void onMessageSend(NetAddress peerAddress, int socketPort, int messageId, int size) {}
	@Override
	public void onMessageRecieved(NetAddress peerAddress, int socketPort, int messageId, int size) {}
	@Override
	public void onMessageDisplay(String message) {}
	@Override
	public void onMessageConfirmed(NetAddress peerAddress, int messageId) {}
	@Override
	public void onTraffic(float bytes_in_sec, float bytes_out_sec, long bytes_total_in, long bytes_total_out) {}

}
