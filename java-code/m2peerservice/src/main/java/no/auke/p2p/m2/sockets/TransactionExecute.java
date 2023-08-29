package no.auke.p2p.m2.sockets;

import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.SocketRetStatus;
import no.auke.p2p.m2.general.BlockingQueue;
import no.auke.p2p.m2.general.ReturMessageTypes;
import no.auke.p2p.m2.sockets.messages.MsgFunctionResult;
import no.auke.p2p.m2.sockets.messages.MsgSendFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO LHA: made for later
// to make execute multi thread
public class TransactionExecute {
	private static final Logger logger = LoggerFactory.getLogger(TransactionExecute.class);
	private static final int NUMSOCKETS = 5;
	final private BlockingQueue<Socket> sockets = new BlockingQueue<Socket>(NUMSOCKETS);
	final TransactionSocket transactionSocket;
	public TransactionExecute(TransactionSocket transactionSocket) {
		this.transactionSocket = transactionSocket;
		for (int x = 0; x < NUMSOCKETS; x++) {
			Socket socket = this.transactionSocket.getNameSpace().openSocket(0);
			if (logger.isDebugEnabled())
				logger.debug("open port nr " + String.valueOf(socket.getPort()));
			sockets.add(socket);
		}
	}
	public void ExecuteMessage(MsgSendFunction message) {
		Socket socket;
		try {
			while ((socket = sockets.take()) != null) {
				transactionSocket.getNameSpace().getMainServ().getExecutor().execute(new ExecuteMessage(message, socket));
			}
		} catch (InterruptedException e) {}
	}
	class ExecuteMessage implements Runnable {
		final MsgSendFunction message;
		final Socket socket;
		public ExecuteMessage(MsgSendFunction message, Socket socket) {
			this.message = message;
			this.socket = socket;
		}
		@Override
		public void run() {
			if (logger.isDebugEnabled())
				logger.debug("got message from " + message.getFromClientId() + " function " + message.getFunction());
			if (!transactionSocket.getClientId().equals(message.getToClientId())) {
				logger.warn(transactionSocket.getClientId() + " ERROR -> wrong recieving message getToClientId() = " + message.getToClientId()
						+ " incoming from " + message.getFromClientId());
			} else {
				// LHA: Have transaction object
				// commit and rollback handling is implemented into
				// sub class of
				// ISyncTransaction
				Transaction transaction = transactionSocket.onTransFunctionMessage(message.getFromClientId(), message.getFunction(), message.getData());;
				if (transaction == null) {
					transaction = transactionSocket.new Trans(transactionSocket.onFunctionMessage(message.getFromClientId(), message.getFunction(),
							message.getData()));
				}
				// HUYDO: this is async communication, we can fire
				// and forget...dont send reply back if transaction
				// data is null
				if (transaction != null && transaction.getRetbuffer() != null) {
					if (logger.isDebugEnabled())
						logger.debug("reply message to " + message.getFromClientId() + " function " + message.getFunction());
					int trial = 0;
					while (true) {

						MsgFunctionResult retmessage = new MsgFunctionResult(transactionSocket.getClientId(), message.getSessionId(), message.getMsgId(),
								message.getFunction(), transaction.getRetbuffer());
						
						// send result back
						SocketRetStatus ret = socket.send(message.getFromClientId(), retmessage.toBytes());
						if (!ret.isOk()) {
							// not ok
							if (ret.getLastRetcode() == ReturMessageTypes.peer_not_found) {
								logger.warn("client not found when sending result back, " + message.getFromClientId());
								transactionSocket.getNameSpace().getListen().error("client not found when send result back " + message.getFromClientId());
								transaction.rollback();
								break;
							} else if (ret.getLastRetcode() == ReturMessageTypes.send_timeout) {
								trial++;
								if (trial > 1) {
									if (logger.isDebugEnabled())
										logger.debug("retry send twise, if send fail with no timeout, " + message.getFromClientId());
								} else {
									if (logger.isDebugEnabled())
										logger.debug("retry send twise, send fail with timeout, " + message.getFromClientId());
									transaction.rollback();
									break;
								}
							} else {
								transactionSocket.getNameSpace().getListen()
										.error("can not send result back to " + message.getFromClientId() + " > " + ret.getLastMessage());
								logger.warn("can not send result back to " + message.getFromClientId() + " > " + ret.getLastMessage());
								// LHA: rollback transaction
								// if not possible send reply
								// the rollback handling is
								// implemented into the class
								// from operating socket
								// implementation
								transaction.rollback();
								break;
							}
						} else {
							if (logger.isDebugEnabled())
								logger.debug("result sent back to " + message.getFromClientId());
							// LHA: commit transaction
							// if not possible send reply
							// the commit handling is implemented
							// into the class
							// from operating socket implementation
							transaction.commit();
							break;
						}
					}
				} else {
					logger.warn("reply transaction is empty, " + message.getFromClientId() + " function " + message.getFunction());
				}
			}
			// add back to list
			sockets.add(socket);
		}
	}
	public void close() {
		// TODO Auto-generated method stub
	}
}
