/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.workers.keepalive;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import no.auke.p2p.m2.message.attribute.ErrorCode;
import no.auke.p2p.m2.message.attribute.MessageAttribute;
import no.auke.p2p.m2.message.attribute.PublicAddress;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.InitVar;

public class KeepAliveAgent {
	
	// private static final Logger logger =
	// LoggerFactory.getLogger(KeepAliveAgent.class);
	
	private KeepAlivePool agentpool;
	private PublicAddress publicaddress;

	public NetAddress getPublicaddress() {
		return publicaddress != null ? new NetAddress(publicaddress) : new NetAddress();
	}
	
	private AtomicInteger ping_frequence = new AtomicInteger();
	public int getPingFrequence() {
		return ping_frequence.get();
	}
	private Random random = new Random();
	AtomicBoolean agent_is_running = new AtomicBoolean();
	private AtomicBoolean agent_got_first_ping = new AtomicBoolean();
	AtomicBoolean agent_is_connected = new AtomicBoolean();
	AtomicBoolean agent_is_connecting = new AtomicBoolean();
	private AtomicInteger ping_time = new AtomicInteger();
	public int getPingTime() {
		return ping_time.get();
	}
	public boolean isConnecting() {
		return agent_is_connecting.get();
	}
	public boolean isConnected() {
		return agent_is_running.get() && agent_is_connected.get();
	}
	public boolean isRunning() {
		return agent_is_running.get();
	}
	private NetAddress pingaddress;
	public NetAddress getPingaddress() {
		return pingaddress;
	}
	public void setConnected(boolean b) {
		agent_is_connected.set(b);
		agent_is_connecting.set(false);
	}
	public void rePing() {
		// reset ping period
		agent_got_first_ping.set(false);
		ping_frequence.set(0);
		pingTask.reStart();
	}
	
	KeepAlivePingTask pingTask;
	public KeepAliveAgent(KeepAlivePool agentPool, NetAddress pingAddress) {
		agent_is_running.set(false);
		agent_got_first_ping.set(false);
		agent_is_connected.set(false);
		agent_is_connecting.set(true);
		this.agentpool = agentPool;
		this.pingaddress = pingAddress;
		// initiate so the keys is sent to KA until KA answer
		agentpool.getNameSpace().getSessionEncrypt().initKA(pingAddress);
	}
	
	//
	// send ID first time
	// and also ask for new mapping
	//
	
	public void gotPingResponse(MessageHeader receiveMH) {
		
		// Ping response
		// started
		
		if (isRunning() && pingTask != null) {
			
			try {
				
				if (isRunning() && getPingaddress().getAddressPort().equals(receiveMH.getAddress().getAddressPort())) {
					
					if (pingTask.getPingSendt().containsKey(receiveMH.getTransactionID())) {
						// ping is returned, remove from list
						// Everything OK
						pingTask.getPingSendt().clear();
						// reset the KA public key sending
						agentpool.getNameSpace().getSessionEncrypt().confirmKA(receiveMH);
						ping_time.set((int) (System.currentTimeMillis() - pingTask.getLastPing()));

						// first time ping received
						// send ping with user name to register
						if (!agent_got_first_ping.getAndSet(true)) {
							pingTask.sendPingHeader();
						}
						
						if (agent_got_first_ping.get()) {
							// err code 701 tell new user name on server
							ErrorCode err = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
							if (err != null) {
								
								if (err.getResponseCode() == 701) {
									pingTask.sendPingHeader();
								} else if (err.getResponseCode() == 704) {
									// wrong device id, user are connected with
									// some
									// other device
									agentpool.getNameSpace().getListen().message("Can not connect, rejected from KA " + getPingaddress().getAddressPort());
									agentpool.getNameSpace().getListen().connectionRejected(getPingaddress(), err.getReason());
								}
							} else {
								
								// received second response from KA with user id
								PublicAddress pubaddr = (PublicAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.PublicAddress);
								if (pubaddr != null) {

									// TODO: removed possibility for changing
									// public
									// address by NAT
									// if not equal possibly new address from
									// NAT

									if (publicaddress != null && !pubaddr.getAddress().toString().equals(publicaddress.getAddress().toString())) {
										agentpool.getNameSpace().getListen().debug(getPingaddress().getAddressPort() + " new address " + pubaddr.toString());
										pingTask.sendPingHeader();
									}
									publicaddress = pubaddr;
									
									if (!agent_is_connected.getAndSet(true) && agentpool.setAgentConnected(this, publicaddress)) {
									
										// Check if there is a symmetric NAT
										// involved by analyzing the IP address
										agentpool.checkSymmetricNAT(publicaddress, receiveMH.getAddress());
										agent_is_connecting.set(false);
										agentpool.getNameSpace().getListen().onServiceConnected(getPublicaddress(), getPingaddress());
									}
								}
								
								if (agent_is_connected.get()) {
									ping_frequence.addAndGet(InitVar.ADD_PING_PERIOD); // Increase
																						// time
									if (ping_frequence.get() > InitVar.MAX_PING_PERIOD) {
										ping_frequence.set(InitVar.MAX_PING_PERIOD);
									}
									// set random ping period to make sure all
									// ping is not sent same time
									int range = ping_frequence.get() / 5;
									ping_frequence.set((range * 4) + random.nextInt(range));
								}
							}
							
						}
						
					} else if (receiveMH.getTransactionID() == 0) {
						// got a hart beat from KA, check wakeup
						agentpool.getNameSpace().getListen().trace("got hartbeat from " + getPingaddress().getAddressPort());
						agentpool.getNameSpace().wakeUp();
					} else {
						agentpool.getNameSpace().getListen()
								.trace("ping response, no ping sent " + String.valueOf(receiveMH.getTransactionID() + " " + getPingaddress().getAddressPort()));
					}
				}
			} catch (Exception ex) {
				agentpool.getNameSpace().getListen().error("error when getting ping alive " + ex.getMessage());
				ex.printStackTrace();
			}
		}
	}
	public void startAgent() {
		
		if (!agent_is_running.getAndSet(true)) {
			// LHA: just in case, make sure previous task is closed
			if (pingTask != null) {
				pingTask.forceStop();
			}
			pingTask = new KeepAlivePingTask(agentpool, this);
			agentpool.getNameSpace().getMonitors().getPingMonitor().execute(pingTask);
		}
	}
	
	// close from agent
	public void stopAgent() {
		pingTask.forceStop();
	}
}
