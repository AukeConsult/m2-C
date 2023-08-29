package no.auke.p2p.m2.sockets;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import no.auke.p2p.m2.general.ReturMessageTypes;
import no.auke.p2p.m2.sockets.messages.MsgFunctionResult;
import no.auke.p2p.m2.sockets.messages.MsgSendFunction;
import no.auke.p2p.m2.InitVar;
import no.auke.p2p.m2.NameSpace;
import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.Socket.Packet;
import no.auke.p2p.m2.SocketRetStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: make a test for this TransactionSocket class 
public abstract class TransactionSocket implements ISocket {
	
	private static final Logger logger = LoggerFactory.getLogger(TransactionSocket.class);
	public class Trans extends ISyncDefaultTrans {
		public Trans(byte[] buffer) {
			super(buffer);
		}
		@Override
		public void commit() {}
		@Override
		public void rollback() {}
	}
	private TransactionExecute asyncExcute = null;
	// AtomicBoolean running = new AtomicBoolean();
	AtomicBoolean closed = new AtomicBoolean();
	public boolean isConnected() {return nameSpace != null ? nameSpace.isConnected() : false;}
	public boolean isClosed() {return closed.get();}
	public String getClientId() {return nameSpace != null && nameSpace.getClientid() != null ? nameSpace.getClientid() : "";}
	@Override
	public int getPort() {return socket != null ? socket.getPort() : 0;}
	private InputThread inputThread;

	protected NameSpace nameSpace;
	public NameSpace getNameSpace() {return nameSpace;}
	public void setNameSpace(NameSpace nameSpace) {this.nameSpace = nameSpace;}

	private Socket socket;
	@Override
	public Socket getSocket() {return socket;}
	
	@Override
	public void setSocket(Socket socket) {this.socket = socket;}
	
	private AtomicInteger timeout = new AtomicInteger(InitVar.SYNC_SOCKET_TIMEOUT);
	@Override
	public int getTimeout() {return timeout.get();}
	
	@Override
	public void setTimeout(int timeout) {this.timeout.set(timeout);}
	private AtomicInteger numTrial = new AtomicInteger(2);
	
	@Override
	public int getNumTrial() {return numTrial.get();}
	@Override
	public void setNumTrial(int timeout) {this.numTrial.set(timeout);}
	
	public SocketRetStatus open(NameSpace server, int socketPort, int waitingTimeout) {
		this.setTimeout(waitingTimeout);
		return open(server, socketPort);
	}
	
	public TransactionSocket() {closed.set(true);}
	
	public SocketRetStatus open(NameSpace namespace, int socketPort) {
		if (!namespace.isSocketOpen(socketPort) && closed.getAndSet(false)) {
			this.nameSpace = namespace;
			socket = nameSpace.openSocket(socketPort);
			inputThread = new InputThread(socket);
			getNameSpace().getMainServ().getExecutor().execute(inputThread);
			if (logger.isDebugEnabled())
				logger.debug(getClientId() + " Open transaction socket " + String.valueOf(getPort()));
			return new SocketRetStatus();
		} else if (closed.get()) {
			logger.error(getClientId() + " transaction socket already open " + String.valueOf(socketPort));
			SocketRetStatus ret = new SocketRetStatus();
			ret.setLastMessage("Socket already open when closed, error in core");
			ret.setLastRetcode(ReturMessageTypes.socket_error);
			return ret;
		} else {
			return new SocketRetStatus();
		}
	}
	public void gotReply(MsgFunctionResult retmsg) {};
	public interface IGotReply {
		void onGetResult(String sessionId, String from, String function, byte[] result);
	}
	public void close() {
		if (!closed.getAndSet(true)) {
			for (ITransacationSession session : sessions.values()) {
				session.stopSession();
			}
			sessions.clear();
			sessions_ID.clear();
			if (getSocket() != null) {
				getSocket().close();
			}
			if (asyncExcute != null) {
				asyncExcute.close();
			}
		}
	}
	abstract public byte[] onFunctionMessage(String fromClientId, String function, byte[] data);
	abstract public Transaction onTransFunctionMessage(String fromClientId, String function, byte[] data);
	@Override
	public boolean checkIncomming(String fromClient) {
		return getSocket().checkIncomming(fromClient);
	}
	private class InputThread implements Runnable {
		final Socket socket;
		public InputThread(Socket socket) {
			this.socket = socket;
		}
		@Override
		public void run() {
			while (!closed.get()) {
				try {
					Packet buff = socket.readBuffer(60000);
					if (logger.isTraceEnabled())
						logger.trace("readbuffer return " + (buff == null ? " empty " : buff.getData().length));
					// TODO: LHA: check length = 0.. this is a bug in peer
					// server
					// when puffer is returned with length 0
					if (!closed.get() && buff != null && buff.getData() != null && buff.getData().length > 0) {
						if (logger.isDebugEnabled())
							logger.debug("incomming message type " + (int) buff.getData()[0]);
						if (buff.getData()[0] == MsgFunctionResult.getType()) {
							// return message
							MsgFunctionResult retmsg = new MsgFunctionResult(buff.getData());
							if (retmsg.getData() != null) {
								if (IsValidSession(retmsg.getSessionId())) {
									if (logger.isDebugEnabled())
										logger.debug("got result message from " + retmsg.getFrom());
									ITransacationSession session = sessions.get(retmsg.getSessionId());
									session.gotReply(retmsg);
								} else {
									String lastmessage = "got result for unknown session " + retmsg.getSessionId() + " from " + retmsg.getFrom() + " function "
											+ retmsg.getFunction();
									getNameSpace().getListen().error(lastmessage);
									logger.warn(lastmessage);
								}
							} else {
								getNameSpace().getListen().error("got empty result message SessionID " + retmsg.getSessionId() + " from " + retmsg.getFrom());
							}
						} else if (buff.getData()[0] == MsgSendFunction.getType()) {
							// TODO: LHA: add TransactionExecute here
							MsgSendFunction message = new MsgSendFunction(buff.getData());
							if (logger.isDebugEnabled())
								logger.debug("got message from " + message.getFromClientId() + " function " + message.getFunction());
							if (!getClientId().equals(message.getToClientId())) {
								logger.warn(getClientId() + " wrong recieving message getToClientId() = " + message.getToClientId() + " incoming from "
										+ message.getFromClientId());
							} else {
								// Using Transaction object
								// commit and roll-back handling is implemented
								// into implementation class of ISyncTransaction
								// Inside here the code is implemented
								if (asyncExcute != null) {
									// TODO: async handling in a thread
									// must be tested
									asyncExcute.ExecuteMessage(message);
								} else {
									// normal handling synchronized
									Transaction transaction = onTransFunctionMessage(message.getFromClientId(), message.getFunction(), message.getData());;
									if (transaction == null) {
										transaction = new Trans(onFunctionMessage(message.getFromClientId(), message.getFunction(), message.getData()));
									}
									// HUYDO: this is async communication, we
									// can fire
									// and forget...don't send reply back if
									// transaction
									// data is null
									if (transaction.getRetbuffer() != null | !message.isAsync()) {
										byte[] data = transaction.getRetbuffer();
										if (logger.isDebugEnabled())
											logger.debug("reply message to " + message.getFromClientId() + " function " + message.getFunction());
										MsgFunctionResult retmessage = new MsgFunctionResult(getClientId(), message.getSessionId(), message.getMsgId(),
												message.getFunction(), data);
										// LHA: skip retry, just fail if not
										// possible to send back
										// int trial = 0;
										// while (true) {
										if (transaction.reply) {
											// send result back
											SocketRetStatus ret = socket.send(message.getFromClientId(), retmessage.toBytes());
											transaction.setReStatus(ret);
											if (!ret.isOk()) {
												// // not ok
												// LHA: skip retry when sending
												// back
												getNameSpace().getListen().error(
														"Sending result back to + " + message.getFromClientId() + " > " + ret.getLastMessage());
												// LHA: roll-back transaction
												transaction.rollback();
												// break;
											} else {
												if (logger.isDebugEnabled())
													logger.debug("result sent back to " + message.getFromClientId() + " function " + message.getFunction()
															+ " trans " + transaction.getClass());
												// LHA: commit transaction
												transaction.commit();
												// break;
											}
										} else {
											transaction.rollback();
										}
									} else {
										logger.warn("reply trandsaction is empty, " + message.getFromClientId() + " function " + message.getFunction());
									}
								}
							}
						} else {
							logger.warn(getClientId() + " wrong message type incomming");
						}
					}
					// Thread.yield();
				} catch (Exception ex) {
					logger.warn("Excpetion in input thread " + ex.getMessage());
				}
			}
			sessions.clear();
			closed.set(true);
		}
	}
	final private Map<String, ITransacationSession> sessions = new ConcurrentHashMap<String, ITransacationSession>();
	final private Map<String, ITransacationSession> sessions_ID = new ConcurrentHashMap<String, ITransacationSession>();
	public boolean IsValidSession(ITransacationSession session) {
		return sessions.containsKey(session.getSessionID());
	}
	public ITransacationSession openSession(String toClientId, int port, IGotReply onGotReply) {
		ITransacationSession session = sessions_ID.get(toClientId + String.valueOf(port));
		if (session == null) {
			session = new AsyncSession(TransactionSocket.this, toClientId, port, onGotReply);
			sessions_ID.put(session.getToClientId() + String.valueOf(port), session);
			sessions.put(session.getSessionID(), session);
		}
		return sessions.get(session.getSessionID());
	}
	public ITransacationSession openSession(String toClientId, int port) {
		ITransacationSession session = sessions_ID.get(toClientId + String.valueOf(port));
		if (session == null) {
			session = new SyncSession(TransactionSocket.this, toClientId, port);
			sessions_ID.put(session.getToClientId() + String.valueOf(port), session);
			sessions.put(session.getSessionID(), session);
		}
		return sessions.get(session.getSessionID());
	}
	public ITransacationSession openSession(String toClientId) {
		ITransacationSession session = sessions_ID.get(toClientId + String.valueOf(getPort()));
		if (session == null) {
			session = new SyncSession(TransactionSocket.this, toClientId, getPort());
			sessions_ID.put(session.getToClientId() + String.valueOf(getPort()), session);
			sessions.put(session.getSessionID(), session);
		}
		return sessions.get(session.getSessionID());
	}
	public ITransacationSession openSession() {
		ITransacationSession session = sessions_ID.get(serviceId + String.valueOf(getPort()));
		if (session == null) {
			session = new SyncSession(TransactionSocket.this, serviceId, getPort());
			sessions_ID.put(session.getToClientId() + String.valueOf(getPort()), session);
			sessions.put(session.getSessionID(), session);
		}
		return sessions.get(session.getSessionID());
	}
	public boolean IsValidSession(String sessionID) {
		if (sessions.containsKey(sessionID)) {
			return true;
		} else {
			logger.warn("no session found " + sessionID + " object " + String.valueOf(getClass().hashCode()));
			return false;
		}
	}
	private String serviceId = "";
	public void setRemoteId(String serviceId) {this.serviceId = serviceId;}
	public String getRemoteId() {return serviceId;}
}
