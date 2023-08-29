package no.auke.p2p.m2.workers.keepalive;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import no.auke.p2p.m2.message.attribute.IpAddress;

public class NetAddress {
	private String address = "";
	private InetAddress inetaddress = null;
	public String getAddress() {
		return address;
	}
	private int port = 0;
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		inetaddress = null;
		addressPort = "";
		this.port = port;
	}
	private String addressPort = "";
	public String getAddressPort() {
		// verry verry smal tuning :)
		if (addressPort == null || addressPort.length() == 0) {
			addressPort = address != null && port != 0 ? address + ":" + String.valueOf(port) : "";
		}
		return addressPort;
	}
	public NetAddress(String address, int port) {
		this.address = address;
		this.port = port;
	}
	public NetAddress(NetAddress address) throws UnknownHostException {
		this.address = InetAddress.getByName(address.getAddress()).getHostAddress();
		this.port = address.getPort();
	}
	public NetAddress(IpAddress address) {
		this.address = address.getAddress().toString();
		this.port = address.getPort();
	}
	public InetAddress getInetAddress() throws UnknownHostException {
		if (inetaddress == null)
			inetaddress = InetAddress.getByName(InetAddress.getByName(address).getHostAddress());
		return inetaddress;
	}
	public NetAddress() {}
	/**
	 * Checks to see if a specific port is available.
	 *
	 * @param port
	 *            the port to check for availability
	 */
	public static boolean available(int port) {
		// if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
		// throw new IllegalArgumentException("Invalid start port: " + port);
		// }
		ServerSocket ss = null;
		DatagramSocket ds = null;
		try {
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(port);
			ds.setReuseAddress(true);
			return true;
		} catch (IOException e) {} finally {
			if (ds != null) {
				ds.close();
			}
			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {}
			}
		}
		return false;
	}
}