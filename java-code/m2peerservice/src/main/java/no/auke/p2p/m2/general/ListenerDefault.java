package no.auke.p2p.m2.general;

import no.auke.p2p.m2.workers.keepalive.NetAddress;

public class ListenerDefault extends IListener {
	public ListenerDefault(int level) {}
	@Override
	public void printLog(String message) {
		System.out.println(message);
	}
	@Override
	public void onServiceStarted(String message) {
		System.out.println(message);
	}
	@Override
	public void onServiceStopped(String message) {
		System.out.println(message);
	}
	@Override
	public void onServiceConnected(NetAddress publicAddress, NetAddress kaServerAddress) {}
	@Override
	public void onServiceDisconnected(NetAddress kaServerAddress) {}
	@Override
	public void connectionRejected(NetAddress kaServerAddress, String msg) {}
	@Override
	public void onPeerConnected(NetAddress peerAddress) {}
	@Override
	public void onPeerDisconnected(NetAddress peerAddress) {}
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
	@Override
	public void onLicenseError(LicenseReasons reason, String licenseKey) {}
}