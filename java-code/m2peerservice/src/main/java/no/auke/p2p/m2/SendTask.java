package no.auke.p2p.m2;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.m2.task.Task;
import no.auke.p2p.m2.general.ReturMessageTypes;
import no.auke.p2p.m2.sockets.udp.SocketBufferOut;

public abstract class SendTask extends Task {
	private static final Logger logger = LoggerFactory.getLogger(Task.class);
	private SocketRetStatus ret;
	private int port = 0;
	private long startfind = 0;
	private AtomicBoolean iscomplete = new AtomicBoolean();
	private int sequence = 0;
	private Socket socket;
	private byte[] data = null;
	public byte[] getData() {
		return data;
	}
	public boolean isSendingComplete() {
		return iscomplete.get();
	}
	public SendTask() {
		super(0, 50);
	}
	public void send(Socket socket, SocketRetStatus ret, int port, byte[] data) {
		
		this.serverId = ret.getPeerSession().getNameSpace().getMainServ().getServerId();
		ret.getPeerSession().getNameSpace().getMonitors().getConnectMonitor().execute(this);
		this.ret = ret;
		this.data = data;
		this.port = port;
		this.socket = socket;
	}
	final public void onStart() {
		startfind = System.currentTimeMillis();
	}
	// special handling of stop to force on stop
	@Override
	public void stop() {
		if (!stopped.getAndSet(true)) {
			onStop();
		}
	}
	final public void onExecute() {
		if (ret.getPeerSession().isRunning()) {
			if (sequence == 0) {
				//if (!ret.getPeerSession().isConnected() && !ret.getPeerSession().isFinding()) {
					sequence = 1;
				//}
			}
			// sending to known
			if (!ret.getPeerSession().isConnected() && sequence == 1) {
				if (ret.getPeerSession().sendKnownRequest()) {
					sequence = 2;
				} else {
					sequence = 3;
				}
			}
			// wait for known
			if (!ret.getPeerSession().isConnected() && sequence == 2) {
				if (!ret.getPeerSession().waitForConnect(System.currentTimeMillis() - startfind, InitVar.PEER_DIRECT_CONNECT_TIMEOUT)) {
					sequence = 3;
				}
			}
			// sending to other peers
			if (!ret.getPeerSession().isConnected() && sequence == 3) {
				if (ret.getPeerSession().sendPeerRequest()) {
					sequence = 4;
				} else {
					sequence = 5;
				}
			}
			// waiting for other peers
			if (!ret.getPeerSession().isConnected() && sequence == 4) {
				if (!ret.getPeerSession().waitForConnect(System.currentTimeMillis() - startfind, InitVar.PEER_DIRECT_CONNECT_TIMEOUT)) {
					sequence = 5;
				}
			}
			// sending to other peers
			if (!ret.getPeerSession().isConnected() && sequence == 5) {
				if (ret.getPeerSession().sendKARequest(false)) {
					sequence = 6;
				} else {
					stop();
				}
			}
			if (!ret.getPeerSession().isConnected() && sequence == 6) {
				if (!ret.getPeerSession().waitForConnect(System.currentTimeMillis() - startfind,
						(ret.getPeerSession().getNameSpace().isMiddleman() ? InitVar.PEER_CONNECT_TIMEOUT + 2000 : InitVar.PEER_CONNECT_TIMEOUT))) {
					stop();
				}
			} else if (ret.getPeerSession().isConnected()) {
				stop();
			}
		} else {
			stop();
		}
	}
	final public void onStop() {

		// wait for encryption to enable
		if (ret.getPeerSession().isRunning()) {
			
			ret = ret.getPeerSession().getSessionEncrypt().waitForEncryption(ret);
			
			if (ret.getPeerSession().isConnected()) {
				ret = ret.getPeerSession().getSessionEncrypt().waitForEncryption(ret);
				if (ret.getPeerSession().isRunning() && ret.isOk()) {
					
					if (data != null) {
					
						ret.getPeerSession().getMainServ().getExecutor().execute(new Runnable() {
						
							@Override
							public void run() {
								SocketBufferOut buffer;
								try {
									buffer = socket.getOutputBuffer(ret, ret.getPeerSession(), port, data);
									// send the message and let buffer
									// start sending packages
									if (buffer.send()) {
										ret.setLastRetcode(ReturMessageTypes.ok);
									} else {
										// LHA: try out, see if better
										if (InitVar.PEER_SESSION_CLOSE_ON_FAIL) {
											ret.getPeerSession().getNameSpace().closePeerAgent("can not send buffer", ret.getPeerSession().getPeerid().getUserid());
										}
									}
									buffer.clear();
								} catch (Exception e) {
									logger.warn(String.valueOf(port) + " Encryption error " + e.getMessage());
								}
								onSentComplete(ret);
								iscomplete.set(true);
							}
						});
						int wait = 0;
						while (wait <= 1000 && !iscomplete.get()) {
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {}
							wait += 10;
						}
					} else {
						onError(ret);
						iscomplete.set(true);
					}
				} else {
					if (ret.getLastRetcode() == ReturMessageTypes.no_session_encryption) {
						ret.getPeerSession().resetSession("no sesson encryption");
					}
					onError(ret);
					iscomplete.set(true);
				}
			} else {
				ret.setLastRetcode(ReturMessageTypes.peer_connect_timeout);
				onError(ret);
				iscomplete.set(true);
			}
		} else {
			ret.setLastRetcode(ReturMessageTypes.peer_is_closed);
			onError(ret);
			iscomplete.set(true);
		}
	}
	public abstract void onError(SocketRetStatus ret);
	public abstract void onSentComplete(SocketRetStatus ret);
}
