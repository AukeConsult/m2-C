package no.auke.p2p.m2.sockets;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.p2p.m2.SocketRetStatus;
import no.auke.p2p.m2.general.ReturMessageTypes;
import no.auke.p2p.m2.sockets.messages.MsgFunctionResult;
import no.auke.p2p.m2.sockets.messages.MsgSendFunction;
import no.auke.util.Lock;

public class SyncSession implements ITransacationSession {
	
	private static final Logger logger = LoggerFactory.getLogger(SyncSession.class);
	private String sessionID;
	private Queue<MsgSendFunction> sendmsg = new ConcurrentLinkedQueue<MsgSendFunction>();
	private ConcurrentLinkedQueue<byte[]> result = new ConcurrentLinkedQueue<byte[]>();

	private String toClientId;	
	public String getToClientId() {return toClientId;}
	
	private int toPort;
	public int getToPort() {return toPort;}

	public String getSessionID() {return sessionID;}
	
	private ISocket sync_socket;
	public ISocket getSocket() {return sync_socket;}
	
	private AtomicBoolean isopen = new AtomicBoolean();
	public boolean isOpen() {return isopen.get();}
	
	public SyncSession(ISocket sync_socket, String remoteclientid, int port) {
		sessionID = UUID.randomUUID().toString().substring(1, 6);
		this.toClientId = remoteclientid;
		this.toPort = port;
		this.sync_socket = sync_socket;
		this.isopen.set(true);
	}
	private byte[] getResult(byte[] buffer) {
		byte[] ret_result = new byte[buffer.length];
		System.arraycopy(buffer, 0, ret_result, 0, buffer.length);
		return ret_result;
	}
	public void stopSession() {
		isopen.set(false);
		synchronized (syncget) {
			syncget.notify();
		}
		// LHA: close underlying connection. Possibly Ok most of the time
		sync_socket.getNameSpace().closePeerAgent("session close", getToClientId());
	}
	private Object syncget = new Object();
	public void gotReply(MsgFunctionResult retmsg) {
		synchronized (syncget) {
			MsgSendFunction msg = sendmsg.poll();
			if (msg != null) {
				// LHA:
				if (!msg.getToClientId().equals(retmsg.getFrom())) {
					// LHA; just ignore if not correct
					// can happend if timeout
					// just ignore
					if (logger.isDebugEnabled())
						logger.warn(getSessionID() + " got from wrong clientid " + retmsg.getFrom());
				} else if (!msg.getMsgId().equals(retmsg.getMsgId())) {
					// LHA; just ignore if not correct
					// can happend if timeout
					// just ignore
					if (logger.isDebugEnabled())
						logger.warn(getSessionID() + " got from wrong messageid " + retmsg.getMsgId() + " from " + retmsg.getFrom());
				} else if (!getSessionID().equals(retmsg.getSessionId())) {
					// LHA; just ignore if not correct
					// can happend if timeout
					// just ignore
					if (logger.isDebugEnabled())
						logger.warn(getSessionID() + " got from wrong sessionid " + retmsg.getSessionId() + " from " + retmsg.getFrom());
				} else {
					// correct reply
					// return to get
					if (logger.isTraceEnabled())
						logger.trace("got reply message from " + retmsg.getFrom());
					result.clear();
					result.add(getResult(retmsg.getData()));
					syncget.notify();
				}
			} else {
				if (logger.isDebugEnabled())
					logger.warn("got reply from " + retmsg.getFrom() + " but no waiting session");
			}
		}
	}
	
	public boolean isWaiting() {return sendmsg.size() > 0;}
	
	//
	// sending a message
	// gotReply get the result when coming in
	
	Lock lock = new Lock();
	public SocketRetStatus get(String function, byte[] data) {
		
		SocketRetStatus ret = new SocketRetStatus();
		
		try {
		
			lock.lock();
			synchronized (syncget) {
				
				if (!isopen.get() || sync_socket.isClosed()) {
					ret.setLastRetcode(ReturMessageTypes.send_socket_closed);
					ret.setLastMessage("socket closed when try to send");
					return ret;
				}

				long timestart = System.currentTimeMillis();
				byte[] ret_result = null;
				// add message
				MsgSendFunction msg = new MsgSendFunction(sync_socket.getClientId(), sync_socket.getPort(), getToClientId(), function, getSessionID(), false,
						data);
				sendmsg.clear();
				sendmsg.add(msg);
				try {
					int trials = 0;
					while (trials < sync_socket.getNumTrial()) {
						// ret =
						// sync_socket.getSocket().send(msg.getToClientId(),
						// getToPort(), getSessionID(), msg.toBytes());
						ret = sync_socket.getSocket().send(msg.getToClientId(), msg.toBytes());
						if (ret.isOk()) {
							// waiting for result
							if (result.size() == 0) {
								if (logger.isTraceEnabled())
									logger.trace(sync_socket.getClientId() + " wait for incoming, timeout " + String.valueOf(sync_socket.getTimeout()));
								syncget.wait(sync_socket.getTimeout());
								if (result.size() == 0) {
									// lha: check if incoming
									while (sync_socket.checkIncomming(msg.getToClientId())) {
										syncget.wait(50);
										if (result.size() > 0) {
											break;
										}
									}
								}
							}
							if (result.size() == 0) {
								ret.setLastRetcode(ReturMessageTypes.session_timeout);
								ret.setLastMessage("timeout after " + String.valueOf(System.currentTimeMillis() - timestart) + " send to "
										+ msg.getToClientId() + " trial " + String.valueOf(trials));
								trials++;
							} else if (isopen.get() && !getSocket().isClosed()) {
								ret_result = getResult(result.poll());
								break;
							}
						} else if (ret.getLastRetcode() == ReturMessageTypes.send_timeout || ret.getLastRetcode() == ReturMessageTypes.no_session_encryption) {
							// retry
							trials++;
						} else {
							break;
						}
					}
				} catch (Exception e) {
					ret.setLastRetcode(ReturMessageTypes.general);
					ret.setLastMessage("Fatal: " + e.getMessage());
					e.printStackTrace();
				} finally {
					sendmsg.clear();
				}
				ret.setData(ret_result);
			}
		} catch (InterruptedException ex) {} finally {
			lock.unlock();
		}
		return ret;
	}
	@Override
	public SocketRetStatus fire(String function, byte[] data) {
		return new SocketRetStatus();
	}
	@Override
	public void close() {}
	@Override
	public SocketRetStatus find() {
		return sync_socket.getSocket().findUser(getToClientId());
	}
}
