package no.auke.p2p.m2.sockets;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.m2.task.Task;
import no.auke.p2p.m2.SocketRetStatus;

public abstract class SocketService {
	private static final Logger logger = LoggerFactory.getLogger(SocketService.class);
	private static int CHECK_STOP_FRQUENCY = 1000;
	private static long WAIT_TO_CLOSE = 60000;
	private String serviceId = "";
	private TransactionSocket socket;
	
	// start on client
	public SocketService(String serviceId) {this.serviceId = serviceId;}
	
	// start on server
	public SocketService() {}
	private SyncSession session = null;
	public SyncSession getSession() {
		if (session == null || !socket.IsValidSession(session)) {
			session = (SyncSession) socket.openSession(serviceId, socket.getPort());
		}
		return session;
	}
	Task doStop = null;
	private ReentrantLock openAndClose = new ReentrantLock();
	private AtomicLong lastOpen = new AtomicLong();
	public boolean openService() {
		lastOpen.set(System.currentTimeMillis());
		openAndClose.lock();
		try {
			if (socket == null) {
				socket = newSocket();
				if (!socket.isClosed()) {
					socket.setRemoteId(serviceId);
					if (!socket.getNameSpace().getClientid().equals(serviceId)) {
						int trial = 0;
						while (true) {
							if (socket.getNameSpace().findUser(serviceId).isOk()) {
								return true;
							} else {
								if (trial > 2) {
									logger.warn(socket.getNameSpace().getClientid() + " service " + serviceId + " NOT found, ");
									return false;
								}
							}
							trial++;
						}
					} else {
						return true;
					}
				} else {
					logger.warn(socket.getNameSpace().getClientid() + " service " + serviceId + " socket not open ");
					return false;
				}
			} else {
				return true;
			}
		} finally {
			openAndClose.unlock();
		}
	}
	public abstract TransactionSocket newSocket();
	public void closeService() {
		lastOpen.set(System.currentTimeMillis());
		if (socket != null) {
			openAndClose.lock();
			try {
				if (doStop == null) {
					doStop = new Task(socket.getNameSpace().getMainServ().getServerId(), CHECK_STOP_FRQUENCY) {
						@Override
						public void onStart() {}
						@Override
						public void onExecute() {
							if (socket != null) {
								openAndClose.lock();
								try {
									if (System.currentTimeMillis() - lastOpen.get() > WAIT_TO_CLOSE) {
										// stop if no activities in 5 minutes
										stopService();
										forceStop();
									}
								} finally {
									openAndClose.unlock();
								}
							} else {
								forceStop();
							}
						}
						@Override
						public void onStop() {
							doStop = null;
						}
					};
					socket.getNameSpace().getMonitors().getPingMonitor().execute(doStop);
				}
			} finally {
				openAndClose.unlock();
			}
		}
	}
	public void stopService() {
		openAndClose.lock();
		try {
			if (socket != null) {
				socket.close();
				socket = null;
			}
		} finally {
			openAndClose.unlock();
		}
	}
	// client get
	SocketRetStatus get(String function, byte[] data) {
		openAndClose.lock();
		try {
			return getSession().get(function, data);
		} finally {
			openAndClose.unlock();
		}
	}
}
