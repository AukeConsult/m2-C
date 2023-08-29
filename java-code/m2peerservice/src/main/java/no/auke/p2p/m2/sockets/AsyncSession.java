package no.auke.p2p.m2.sockets;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.p2p.m2.SocketRetStatus;
import no.auke.p2p.m2.general.ReturMessageTypes;
import no.auke.p2p.m2.sockets.TransactionSocket.IGotReply;
import no.auke.p2p.m2.sockets.messages.MsgFunctionResult;
import no.auke.p2p.m2.sockets.messages.MsgSendFunction;

public class AsyncSession implements ITransacationSession {
	
	private static final Logger logger = LoggerFactory.getLogger(AsyncSession.class);

	private ISocket async_socket;
	public ISocket getSocket() {return async_socket;}
	
	private String sessionID;
	private String toClientId;
	public TransactionSocket.IGotReply replyEvent;
	
	@Override
	public String getToClientId() {return toClientId;}
	
	private int port;
	public int getPort() {return port;}
	public String getSessionID() {return sessionID;}
	
	@Override
	public boolean isOpen() {
		return !async_socket.isClosed();
	}
	public AsyncSession(ISocket iTransactionSocket, String remoteclientId, int port, IGotReply onGotReply) {
		sessionID = UUID.randomUUID().toString().substring(1, 5);
		this.toClientId = remoteclientId;
		this.port = port;
		this.replyEvent = onGotReply;
		this.async_socket = iTransactionSocket;
	}
	private AtomicBoolean isWaiting = new AtomicBoolean();
	public void gotReply(MsgFunctionResult retmsg) {
		// remove from queue before we have received message
		// ackWaitingQueue.remove(retmsg.getMsgId());
		if (!getToClientId().equals(retmsg.getFrom())) {
			// LHA; just ignore if not correct
			// can happend if timeout
			// just ignore
			logger.error("got result message from unexpected user : " + retmsg.getFrom() + "(expected user" + getToClientId() + ")");
		} else if (!sessionID.equals(retmsg.getSessionId())) {
			// LHA; just ignore if not correct
			// can happend if timeout
			logger.error("faltal error: wrong sessionid: " + retmsg.getSessionId());
		} else {
			
			if (replyEvent == null) {
				new Throwable("onGotReply must be implemented");
			} else {
				try {
					if (logger.isTraceEnabled())
						logger.trace("got result message from " + retmsg.getFrom());
					replyEvent.onGetResult(retmsg.getSessionId(), retmsg.getFrom(), retmsg.getFunction(), retmsg.getData());
				} catch (Exception ex) {
					ex.printStackTrace();
					logger.error("Error when process reply :" + ex.getMessage());
				}
			}
		}
	}
	private ReentrantLock sendlock = new ReentrantLock();
	public SocketRetStatus fire(String function, byte[] data) {
		try {
			
			sendlock.lock();
			SocketRetStatus ret = new SocketRetStatus();

			if (async_socket.isClosed()) {
				ret.setLastRetcode(ReturMessageTypes.socket_error);
				ret.setLastMessage("socket closed when try to send");
				isWaiting.set(false);
				return ret;
			}
			
			MsgSendFunction outmsg = new MsgSendFunction(async_socket.getClientId(), async_socket.getPort(), getToClientId(), function, getSessionID(), true,data);
			int trial = 0;
			while (trial < async_socket.getNumTrial()) {
				ret = async_socket.getSocket().send(outmsg.getToClientId(), outmsg.toBytes());
				if (ret.isOk()) {
					return ret;
				} else {
					if (ret.getLastRetcode() == ReturMessageTypes.peer_not_found) {
						break;
					} else if (ret.getLastRetcode() == ReturMessageTypes.send_timeout || ret.getLastRetcode() == ReturMessageTypes.no_session_encryption) {
						trial++;
					} else {
						break;
					}
				}
			}
			return ret;
		} finally {
			sendlock.unlock();
		}
	}
	
	public void close() {}
	public void stopSession() {}
	
	@Override
	public SocketRetStatus get(String function, byte[] data) {
		return new SocketRetStatus();
	}
	
	@Override
	public SocketRetStatus find() {return async_socket.getSocket().findUser(getToClientId());}
	
}
