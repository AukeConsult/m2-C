package no.auke.p2p.m2.workers.keepalive;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import no.auke.m2.task.Task;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import no.auke.p2p.m2.InitVar;
import no.auke.p2p.m2.message.attribute.Deviceid;
import no.auke.p2p.m2.message.attribute.LocalAddress;
import no.auke.p2p.m2.message.attribute.MiddleManRequire;
import no.auke.p2p.m2.message.attribute.PeerLocalId;
import no.auke.p2p.m2.message.header.MessageHeader;

class KeepAlivePingTask extends Task {
	
	//private static final Logger logger = LoggerFactory.getLogger(PingTask.class);
	private static final int CHECK_PING_FREQUENCE = 1500;
	
	AtomicBoolean sendping = new AtomicBoolean(true);
	AtomicLong sentping = new AtomicLong(0);
	AtomicInteger failping = new AtomicInteger(0);
	
	private AtomicLong lastping = new AtomicLong();
	final private KeepAlivePool pool;
	final private KeepAliveAgent agent;
		
	public long getLastPing() {return lastping.get();}
	
	private Map<Integer, String> pingsendt = new ConcurrentHashMap<Integer, String>();	
	public Map<Integer, String> getPingSendt() {return pingsendt;}
	
	public KeepAlivePingTask(KeepAlivePool pool, KeepAliveAgent agent) {
		super(pool.getNameSpace().getMainServ().getServerId(), CHECK_PING_FREQUENCE);
		this.pool = pool;
		this.agent = agent;
	}
	
	public MessageHeader sendPing() {
		
		MessageHeader msg = new MessageHeader(MessageHeader.MessageHeaderType.PingAlive);
		msg.addMessageAttribute(new PeerLocalId(pool.getNameSpace().getThisPeerId().getPeerhexid()));
		msg.addMessageAttribute(new Deviceid(pool.getNameSpace().getMainServ().getThisdevice()));
		msg.addMessageAttribute(new LocalAddress(pool.getNameSpace().getMainServ().getLocaladdress().getAddress(), pool.getNameSpace().getMainServ().getLocaladdress().getPort()));
		// send MM request attribute to tell KA that this user demand for MM
		if (pool.getNameSpace().isMiddleman()) {
			msg.addMessageAttribute(new MiddleManRequire(InitVar.NUMBER_OF_MIDDLEMAN_REQURIED));
		}
		return sendPing(msg);
	}
	
	
	MessageHeader sendPingHeader() {
		MessageHeader msg = new MessageHeader(MessageHeader.MessageHeaderType.PingAlive);
		msg.addMessageAttribute(new PeerLocalId(pool.getNameSpace().getThisPeerId().getPeerhexid()));
		msg.addMessageAttribute(new Deviceid(pool.getNameSpace().getMainServ().getThisdevice()));
		msg.addMessageAttribute(new LocalAddress(pool.getNameSpace().getMainServ().getLocaladdress().getAddress(), pool.getNameSpace().getMainServ().getLocaladdress().getPort()));
		msg.addMessageAttribute(pool.getMapfileCurrent());
		// send MM request attribute to tell KA that this user demand for MM
		if (pool.getNameSpace().isMiddleman()) {
			msg.addMessageAttribute(new MiddleManRequire(InitVar.NUMBER_OF_MIDDLEMAN_REQURIED));
		}
		// LHA june 2017, add a license attribute to send to KA
		// if (pool.getNameSpace().getPeerLicense()!=null) {
		// msg.addMessageAttribute(pool.getNameSpace().getPeerLicense());
		// }
		// send header twice
		return sendPing(msg);
	}
	public MessageHeader sendPing(MessageHeader msg) {
		lastping.set(System.currentTimeMillis()); // set last time ping sent
		msg.setTransactionID(pool.getRequestId());
		msg.setAddress(agent.getPingaddress());
		msg = pool.getNameSpace().getSessionEncrypt().pingKA(msg); 
		pool.getNameSpace().getMessageSender().UDPSendEncrypt(msg);
		return msg;
	}
	public void reStart() {
		sendping.set(true);
		failping.set(0);
		execute();
	}
	
	@Override
	public void onStart() {
		getPingSendt().clear();
		sendping.set(true);
	}
	
	@Override
	public void onExecute() {
		lock();
		try {
			if (agent.agent_is_running.get()) {
				if (sendping.getAndSet(false)) {
					getPingSendt().clear();
					getPingSendt().put(sendPing().getTransactionID(), "");
					sentping.set(System.currentTimeMillis());
					// wait before checking response (default 3 seconds)
					waitFor(InitVar.KEEPALIVE_WAIT);
				} else if (!sendping.getAndSet(true)) {
					if (getPingSendt().size() > 0) {
						// set not connected after some fail ping
						if (failping.incrementAndGet() >= InitVar.MAX_PING_TRIALS) {
							pool.getNameSpace().getListen()
									.debug("KA NOT AVAIL " + agent.getPingaddress().getAddressPort() + " fails " + String.valueOf(failping));
							// speed down when not connected
							waitFor(InitVar.MAX_PING_PERIOD);
						} else {
							if (agent.isConnected()) {
								waitFor(InitVar.KEEPALIVE_WAIT);
							}
						}
					} else {
						failping.set(0);
						// Wait for next ping circuit
						if (!pool.getNameSpace().getSessionEncrypt().kaHasKey(agent.getPingaddress())) {
							// if KA have no key, keep high ping frequency (send
							// again as soon as possible)
							waitFor(InitVar.KEEPALIVE_WAIT * 2);
						} else {
							// everything OK
							agent.setConnected(true);
							// set time for last response = connected
							// used in wakeup
							pool.setLastConnected();
							long waittime = agent.getPingFrequence() - (System.currentTimeMillis() - lastping.get());
							waitFor(waittime);
						}
					}
				}
			} else {
				forceStop();
			}
		} catch (Exception ex) {
			pool.getNameSpace().getListen().error("error execute ping task " + ex.getMessage());
		} finally {
			unLock();
		}
	}
	@Override
	public void onStop() {
		lock();
		try {
			if (agent.agent_is_running.getAndSet(false)) {
				pool.agentRemove(agent);
				
				// send a close message to keep alive service to clean up peer
				// from active list
				LocalAddress localaddress = new LocalAddress();
				localaddress.setAddress(pool.getNameSpace().getMainServ().getLocaladdress().getAddress(), pool.getNameSpace().getMainServ().getLocaladdress().getPort());
				
				MessageHeader msg = new MessageHeader(MessageHeader.MessageHeaderType.PingClose);
				msg.addMessageAttribute(new PeerLocalId(pool.getNameSpace().getThisPeerId().getPeerhexid()));
				msg.addMessageAttribute(new Deviceid(pool.getNameSpace().getMainServ().getThisdevice()));
				msg.addMessageAttribute(localaddress);
				
				sendPing(msg);
				sendPing(msg);
				
				pool.getNameSpace().getListen().onServiceDisconnected(agent.getPingaddress());
		
			}
		
		} catch (Exception ex) {
			pool.getNameSpace().getListen().error("Error when stop ping task " + ex.getMessage());
		} finally {
			unLock();
		}
	}
	private ReentrantLock tasklock = new ReentrantLock();
	public void lock() {
		tasklock.lock();
	}
	public void unLock() {
		tasklock.unlock();
	}
}
