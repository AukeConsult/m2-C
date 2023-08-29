package no.auke.p2p.m2;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.Socket.Packet;
import no.auke.p2p.m2.NameSpace;

public abstract class SocketListener {
	private static final Logger logger = LoggerFactory.getLogger(SocketListener.class);
	public abstract boolean onIncommingPacket(Packet packet);
	public abstract void onIncomming(byte[] buffer);
	private Socket socket = null;
	public Socket getSocket() {
		return socket;
	}
	private AtomicBoolean isrunning = new AtomicBoolean();
	public boolean isRunning() {
		return isrunning.get();
	}
	public Socket start(Socket socket) {
		if (isrunning.getAndSet(false) && this.socket != null) {
			this.socket.close();
			int wait = 0;
			while (wait < 1000 && isrunning.get()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {}
				wait += 10;
			}
		}
		if (!isrunning.getAndSet(true)) {
			this.socket = socket;
			socket.getNameSpace().getMainServ().getExecutor().execute(new InputThread());
		}
		return this.socket;
	}
	private class InputThread implements Runnable {
		@Override
		public void run() {
			while (socket.isOpen() && socket.getNameSpace().isRunning()) {
				try {
					Packet buffer = socket.readBuffer(1000);
					if (buffer != null) {

						if (logger.isTraceEnabled())
							logger.trace("got data from socket " + String.valueOf(socket.getPort()));
						if (!onIncommingPacket(buffer)) {
							// LHA: encryption might end up null value
							byte[] data = buffer.getData();
							if (data != null) {
								socket.getNameSpace().getListen().onMessageRecieved(buffer.getPeerSession().getPeerAddress(),socket.getPort(),buffer.getTransactionId(),data.length);
								onIncomming(data);
							}
						}
					}
				} catch (Exception ex) {
					logger.warn("error socket listener " + ex.getMessage());
					ex.printStackTrace();
				}
			}
			isrunning.set(false);
		}
	}
}
