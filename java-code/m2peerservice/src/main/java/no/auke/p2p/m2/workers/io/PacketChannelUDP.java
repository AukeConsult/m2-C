package no.auke.p2p.m2.workers.io;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import no.auke.p2p.m2.workers.ComChannel;
import no.auke.p2p.m2.workers.keepalive.NetAddress;

//HUY made
public class PacketChannelUDP extends PacketChannel {
	
	private InetSocketAddress sockAddress;
	
	DatagramSocket socket;
	public PacketChannelUDP(ComChannel comchannel) {
		super(comchannel);
	}
	@Override
	public boolean send(byte[] data, NetAddress remoteAddress) {
		try {
			
			if (socket == null) {
				socket = new DatagramSocket(new InetSocketAddress(getLocalAddress().getAddress(), getLocalAddress().getPort()));
			}
			
			if(comchannel.isRunning()) {
				DatagramPacket packet = new DatagramPacket(data, data.length, getSocketAddress(remoteAddress));
				socket.send(packet);
				return true;
			} else {
				socket.close();
				socket=null;
			}
		
		} catch (SocketException e) {
			error = Error.SocketException;
		} catch (IOException e) {
			error = Error.IOException;
			System.out.println(e.getMessage());
		} catch (Exception e) {
			error = Error.Exception;
		}
		return false;
	}
	@Override
	public byte[] receive() {
		try {
			if (socket == null || socket.isClosed()) {
				socket = new DatagramSocket(new InetSocketAddress(getLocalAddress().getAddress(), getLocalAddress().getPort()));
			}
			byte[] buff = new byte[1024];
			DatagramPacket datagram = new DatagramPacket(buff, 1024);
			socket.receive(datagram);
			byte[] data = new byte[datagram.getLength()];
			System.arraycopy(buff, 0, data, 0, datagram.getLength());
			sockAddress = new InetSocketAddress(datagram.getAddress(), datagram.getPort());
			return data;
		
		} catch (IOException e) {
			error = Error.IOException;
		} catch (Exception e) {
			error = Error.Exception;
		}
		return new byte[0];
	}
	@Override
	public void close() {
		if(socket!=null)socket.close();
		socket=null;
	}
	@Override
	public String getHostAddress() {return sockAddress.getAddress().getHostAddress();}
	@Override
	public int getPort() {return sockAddress.getPort();}
	@Override
	public String getAddressPort() {return getHostAddress() + ":" + getPort();}
	@Override
	public void wakeUp() {}
	
}
