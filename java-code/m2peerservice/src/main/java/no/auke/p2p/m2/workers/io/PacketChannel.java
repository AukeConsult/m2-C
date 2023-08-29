package no.auke.p2p.m2.workers.io;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import no.auke.p2p.m2.workers.ComChannel;
import no.auke.p2p.m2.workers.keepalive.NetAddress;

public abstract class PacketChannel implements IPacketChannel {
	
	public enum Error {
		NONE, BindException, SocketException, UnknownHostException, IOException, Exception, ClosedByInterruptException, AsynchronousCloseException, ClosedChannelException
	};
	
	NetAddress localAddress = null;
	public void setLocalAddress(NetAddress localAddress) {this.localAddress = localAddress;}
	public NetAddress getLocalAddress() {return localAddress;}
	private Map<String, SocketAddress> remoteaddresslist = new HashMap<String, SocketAddress>();
	public void setRemoteaddresslist(Map<String, SocketAddress> remoteaddresslist) {this.remoteaddresslist = remoteaddresslist;}
	public Map<String, SocketAddress> getRemoteaddresslist() {return remoteaddresslist;}
	protected SocketAddress getSocketAddress(NetAddress address) {
		if (!remoteaddresslist.containsKey(address.getAddressPort())) {
			remoteaddresslist.put(address.getAddressPort(), new InetSocketAddress(address.getAddress(), address.getPort()));
		}
		return remoteaddresslist.get(address.getAddressPort());
	}
	Error error = Error.NONE;
	public Error getError() {return error;}
	ComChannel comchannel;
	public ComChannel getComchannel() {return comchannel;}
	public PacketChannel(ComChannel comchannel) {this.comchannel = comchannel;}
}
