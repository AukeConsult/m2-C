package no.auke.p2p.m2;

import no.auke.p2p.m2.Socket.Packet;

public abstract class SocketHandler {
	private final long WAITTIME = 5000;
	public abstract SocketHandlerMsg onFunctionReceive(SocketHandlerMsg msg);
	private Socket socket;
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
		listener.start(socket);
	}
	private SocketListener listener = new SocketListener() {
		
		@Override
		public void onIncomming(byte[] buffer) {}
		
		@Override
		public boolean onIncommingPacket(Packet packet) {
			
			SocketHandlerMsg msg = new SocketHandlerMsg(packet.getData());
			msg.setPeerId(packet.getRemotePeerId());
			msg.setTransactionId(packet.getTransactionId());
			
			if (msg.isRetMessage()) {
				
				if (inMessage == null 
						&& msg.getTransactionId() == getInMessage().getTransactionId() 
						&& msg.getPeerId().equals(getInMessage().getPeerId())
						) 
				{
					inMessage = msg;
					synchronized (waitReply) {
						waitReply.notifyAll();
					}
				}
			
			} else {
			
				// it is function
				SocketHandlerMsg outMsg = onFunctionReceive(msg);
				outMsg.setRetur();
				SocketRetStatus ret = socket.sendPeer(packet.getPeerSession().getPeerid(), outMsg.toBytes());
				if (ret.isOk()) {
					// return ok
				} else {
					// retur fails
				}
			}
			return true;
		}
	};
	Object waitReply = new Object();
	public SocketHandler() {}
	public SocketHandler(Socket socket) {
		this.socket = socket;
		listener.start(socket);
	}
	public synchronized SocketHandlerMsg getInMessage() {
		return inMessage;
	}
	private SocketHandlerMsg outMessage;
	private SocketHandlerMsg inMessage;
	public SocketHandlerMsg send(String clientid, String function, byte[] data) {
		synchronized (waitReply) {
			
			SocketRetStatus ret = null;
			
			inMessage = null;
			outMessage = new SocketHandlerMsg(function, 0, data);
			ret = socket.send(clientid, outMessage.toBytes());
			//outMessage.setTransactionId(ret.getTransactionId());
			outMessage.setPeerId(ret.getPeerSession().getPeerid());
			if (ret.isOk()) {
				
				int wait = 0;
				while (getInMessage() == null && wait < WAITTIME) {
					try {
						waitReply.wait(50);
					} catch (InterruptedException e) {}
					wait += 50;
				}
				if (getInMessage() != null) {
				
					getInMessage().setRetStatus(ret);
					return getInMessage();
				
				} else {
					outMessage.setRetStatus(ret);
					outMessage.setStatus((short) -2);
					return outMessage;
				}
			} else {
				outMessage.setRetStatus(ret);
				outMessage.setStatus((short) -1);
				return outMessage;
			}
		}
	}
}
