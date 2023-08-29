package no.auke.p2p.m2.workers.io;

import no.auke.p2p.m2.workers.io.PacketChannel.Error;
import no.auke.p2p.m2.workers.keepalive.NetAddress;

public interface IPacketChannel {
	public NetAddress getLocalAddress();
	public void setLocalAddress(NetAddress localAddress);
	// boolean connect();
	public boolean send(byte[] buffer, NetAddress address);
	public void close();
	public byte[] receive();
	public String getHostAddress();
	public int getPort();
	public String getAddressPort();
	public Error getError();
	public void wakeUp();
}