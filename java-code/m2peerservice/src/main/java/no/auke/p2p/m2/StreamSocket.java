package no.auke.p2p.m2;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import no.auke.p2p.m2.general.ReturMessageTypes;
import no.auke.p2p.m2.message.attribute.RawDataPacket;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.workers.PeerSession;
import no.auke.p2p.m2.workers.keepalive.Peerid;
import no.auke.util.Lock;

public class StreamSocket {
		
	public class StreamPacket {

		private Peerid remotepeerid;
		public Peerid getRemotePeerId() {return remotepeerid;}
		public int getTransactionId() {return transactionId;}
		public long getSeqNumber() {return seqnumber;}
		public byte[] getData() {return data;}
		String remoteclient = "";
		private long seqnumber = 0;
		private long frequency = 0;
		public long getFrequency() {return frequency;}
		int transactionId = 0;
		byte[] data;
		private long received = System.currentTimeMillis();
		public long getReceived() {
			return received;
		}
		public StreamPacket(Peerid remotepeerid, int transactionId, long seqnumber, long frequency, byte[] data) {
			this.remotepeerid = remotepeerid;
			this.transactionId = transactionId;
			this.seqnumber = seqnumber;
			this.frequency = frequency;
			this.data = data;
		}
	}
	
	//private static Random random = new java.util.Random(System.currentTimeMillis());
	//public static int nextId() {return random.nextInt(Integer.MAX_VALUE);}
	
	private NameSpace namespace;
	public NameSpace getNameSpace() {return namespace;}
	private int id = 0;
	public int getId() {return id;}
	private long seqnumber = 0;
	private int port = 0;
	public int getPort() {return port;}
	private AtomicBoolean isopen = new AtomicBoolean();
	AtomicBoolean isListening = new AtomicBoolean();
	
	public void setListener(final StreamSocketListener l) {
		
		if (!isListening.getAndSet(true)) {
			
			namespace.getMainServ().getExecutor().execute(new Runnable() {
				final StreamSocketListener listener = l;
				@Override
				public void run() {

					while (isOpen() && getNameSpace().isRunning()) {
						try {
							StreamPacket buff = ReadBuffer(1000);
							if (buff != null) {
								listener.onIncomming(buff);
							} else {
								listener.onNoData();
							}
						} catch (Exception ex) {
							getNameSpace().getListen().error("Stream listener error got data " + ex.getMessage());
						}
					}
					isListening.set(false);
				}
			});
		}
	}
	
	public StreamSocket(NameSpace namespace, int port) {
		//this.id = nextId();
		this.port = port;
		this.namespace = namespace;
		isopen.set(true); // set default to open
	}
	
	public String getSocketStatus() {
		StringBuilder sb = new StringBuilder();
		sb.append("--inbuff.size()=" + inbuff.size());
		sb.append("--isOpen()=" + isOpen());
		return sb.toString();
	}
	public boolean isOpen() {return isopen.get();}

	public SocketRetStatus findUser(String remoteClientid) {return findUser(getNameSpace().makePeerid(remoteClientid));}
	
	public SocketRetStatus findUser(Peerid peerid) {
		SocketRetStatus ret = getNameSpace().openPeer(peerid);
		if (ret.isOk()) {
			if (!ret.getPeerSession().findAndConnect()) {
				ret.setLastRetcode(ReturMessageTypes.peer_not_found);
				ret.setLastMessage("can not find peer");
			}
		}
		return ret;
	}

	public SocketRetStatus send(SocketRetStatus ret, byte[] data) {return send(ret, getPort(), 0, data);}
	
	//
	// Sending with frequency
	// frequency is in nanoseconds for the expected time to next package
	// if next package arrive later, it will be skipped
	// See: stream socket listener
	//
	
	private Lock sendlock = new Lock();
	public SocketRetStatus send(SocketRetStatus ret, int port, int frequency, byte[] data) {
				
		if (!isopen.get()) {
			ret.setLastRetcode(ReturMessageTypes.send_socket_closed);
			ret.setLastMessage("socket is closed");
			return ret;
		} else if (!getNameSpace().isRunning()) {
			ret.setLastRetcode(ReturMessageTypes.service_not_running);
			ret.setLastMessage("service not running");
			return ret;			
		} else if (data == null) {
			ret.setLastRetcode(ReturMessageTypes.empty_data);
			ret.setLastMessage("no data to send");
			return ret;
		} else if (data.length > InitVar.STREAM_MAX_DATA_LENGTH) {
			ret.setLastRetcode(ReturMessageTypes.data_exceed_size);
			ret.setLastMessage("data buffer is over max size of " + String.valueOf(InitVar.STREAM_MAX_DATA_LENGTH));
			return ret;
		}
				
		if (getNameSpace().getReConnector().waitConnect()) {
								
			if (!ret.getPeerSession().isRunning()) {
				// get a new session with same peerid
				ret = getNameSpace().openPeer(ret.getPeerSession().getPeerid());
				if (!ret.isOk()) {
					return ret;
				}
			}
			
			if (!ret.getPeerSession().findAndConnect()) {
				ret.setLastRetcode(ReturMessageTypes.peer_not_found);
				ret.setLastMessage("can not find peer");
				return ret;
			}
			
			ret = ret.getPeerSession().getSessionEncrypt().waitForEncryption(ret);

			try {

				sendlock.lock();
				// make sure encryption is in place before starting to send
				// data
				if (ret.getPeerSession().isRunning() && ret.isOk()) {
					
					ret.getPeerSession().updateSendTime();
					data = ret.getPeerSession().getSessionEncrypt().enCrypt(data);
					MessageHeader dataMH = new MessageHeader(MessageHeader.MessageHeaderType.StreamData);
					dataMH.setTransactionID(getId());
					dataMH.setAddress(ret.getPeerSession().getPeerAddress());
					dataMH.addMessageAttribute(new RawDataPacket(port, seqnumber++, frequency, data, ret.getPeerSession().getSessionEncrypt().getKeyId()));
					
					getNameSpace().getMessageSender().UDPSendEncrypt_Stream(dataMH);
				
				} else {
					
					if (ret.getLastRetcode() == ReturMessageTypes.no_session_encryption) {
						ret.getPeerSession().resetSession("no sesson encryption");
					}
					return ret;	
				}				
				
			} catch (Exception ex) {
				getNameSpace().getListen().error(ex.getMessage());
				ex.printStackTrace();
			} finally {
				sendlock.unlock();
			}

		} else {
			
			if (!getNameSpace().isConnected()) {
				ret.setLastRetcode(ReturMessageTypes.service_not_connected);
				ret.setLastMessage("service not connected");
			} else {
				ret.setLastRetcode(ReturMessageTypes.service_is_restarting);
				ret.setLastMessage("server is restarting");
			}
		}
		return ret;
		
	}
	private ConcurrentLinkedQueue<StreamPacket> inbuff = new ConcurrentLinkedQueue<StreamPacket>();
	public ConcurrentLinkedQueue<StreamPacket> getInbuff() {
		return inbuff;
	}
	private Object readsync = new Object();
	public StreamPacket ReadBuffer(long readwait) {
		if (inbuff.size() == 0 && readwait > 0) {
			synchronized (readsync) {
				try {
					readsync.wait(readwait);
				} catch (InterruptedException e) {}
			}
		}
		return inbuff.poll();
	}
	public void gotData(PeerSession session, int transactionId, RawDataPacket packet) {
		try {
			// LHA: increased size in input buffer, but
			// added a received time to stream packet
			// to eventually throw away too old packets
			//
			if (isOpen() && inbuff.size() < 100) {
				byte[] data = session.getSessionEncrypt().deCrypt(packet.getData());
				inbuff.add(new StreamPacket(session.getPeerid(), transactionId, packet.getSeqNumber(), packet.getFrequency(), data));
				synchronized (readsync) {
					readsync.notify();
				}
			}
		} catch (Exception ex) {
			session.getSessionEncrypt().resetEncryption();
			getNameSpace().getListen().error(this.getNameSpace().getClientid() + " error got data " + String.valueOf(getPort()) + " " + ex.getMessage());
		}
		// Thread.yield();
	}
	public void close() {
		if (isopen.getAndSet(false)) {
			inbuff.clear();
			synchronized (readsync) {
				readsync.notify();
			}
			if (getNameSpace() != null) {
				getNameSpace().removeStreamSocket(this);
			}
		}
	}
}
