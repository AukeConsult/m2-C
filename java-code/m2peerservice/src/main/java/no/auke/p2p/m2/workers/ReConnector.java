package no.auke.p2p.m2.workers;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import no.auke.p2p.m2.InitVar;
import no.auke.p2p.m2.NameSpace;

//
// doing restart without closing down service
// 

public class ReConnector {
	// private static final Logger logger =
	// LoggerFactory.getLogger(ReConnector2.class);
	private NameSpace nameSpace;
	public NameSpace getNameSpace() {return nameSpace;}

	private ReentrantLock connect_lock = new ReentrantLock();
		
	public ReConnector(NameSpace nameSpace) {this.nameSpace = nameSpace;}

	public boolean waitConnect() {
		try {
			if (connect_lock.tryLock(3000,TimeUnit.MILLISECONDS)) {
				try {
					if (!getNameSpace().isConnected()) {
						_connect();
					}					
				} finally {connect_lock.unlock();}
			}
		} catch (InterruptedException e) {}		
		return getNameSpace().isConnected();
	}
		
	public void reConnect() {
		if(!connect_lock.isLocked()) {
			try {
				connect_lock.lock();
				_disConnect();	
				_connect();
			} finally {
				connect_lock.unlock();
			}
		}
	}
	public void connect() {
		if(!connect_lock.isLocked()) {
			try {
				connect_lock.lock();
				_connect();
			} finally {
				connect_lock.unlock();
			}
		}
	}

	public void disConnect() {
		if(!connect_lock.isLocked()) {
			try {
				connect_lock.lock();
				_disConnect();
			} finally {
				connect_lock.unlock();
			}
		}
	}
	
	private void _connect() {
		
		if (getNameSpace().isRunning() && !getNameSpace().isConnected()) {
		
			try {
			
				getNameSpace().getListen().trace("Start connect");

				getNameSpace().getMainServ().getChannel().startChannel(getNameSpace());
				if (getNameSpace().getMainServ().getChannel().isRunning()) {
					
					// TODO: make possible to run without connected to KA server
					// wait for startup of KA
					// this is necessary to get the peers public address
					
					if (!getNameSpace().isPassive()) {

						getNameSpace().getKeepAlivePool().startKeepAlivePool();
						int wait = 0;
						while (wait <= InitVar.START_WAIT && !getNameSpace().getKeepAlivePool().isConnected()) {
							try {
								Thread.sleep(50);
								wait += 50;
							} catch (InterruptedException e) {}
						}

						if (!getNameSpace().getKeepAlivePool().isConnected()) {
							if (getNameSpace().getKeepAlivePool().isDisConnected()) {
								getNameSpace().getListen().message("Connect rejected");
								_disConnect();
							} else if (wait > InitVar.START_WAIT) {
								getNameSpace().getListen().message("Long wait for connect");
							} else {
								getNameSpace().getListen().message("can not connect");
							}
						} else {
							getNameSpace().getListen().onServiceStarted("");
							getNameSpace().getListen().message("Connected");
						}
					} else {
						getNameSpace().getListen().message("Connected passive");
					}
			
				} else {
					getNameSpace().getListen().error("No network when connect");
				}
			
			} catch (Exception ex) {
				getNameSpace().getListen().error("connectService exception: " + ex.getMessage());
			}
		}
	}
	private void _disConnect() {
		
		if (getNameSpace().isRunning() && getNameSpace().isConnected()) {
		
			try {

				// first close all open connections
				// stop all peer sessions
				// close open peer thread

				// stop all KA ping sessions
				if (getNameSpace().getKeepAlivePool() != null) {getNameSpace().getKeepAlivePool().stopKeepAlivePool();}
								
				if (getNameSpace().getOpenPeerSessions().size() > 0) {
					ArrayList<PeerSession> closelist = new ArrayList<PeerSession>(getNameSpace().getOpenPeerSessions().values());
					for (PeerSession session : closelist) {
						session.closeSession("server stopped");
					}
					int wait = 0;
					while (wait < 3000 && getNameSpace().getOpenPeerSessions().size() > 0) {
						try {
							Thread.sleep(50);
							wait += 50;
						} catch (InterruptedException e) {}
					}
				}
				
								
				getNameSpace().getListen().message("Disconnected");
				getNameSpace().getListen().onServiceStopped("");
				
			} catch (Exception ex) {
				getNameSpace().getListen().error("DisConnectService exception: " + ex.getMessage());
			}
		}
	}

}