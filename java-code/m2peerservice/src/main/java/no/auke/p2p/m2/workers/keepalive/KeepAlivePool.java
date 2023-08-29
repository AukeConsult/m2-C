/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.workers.keepalive;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import no.auke.p2p.m2.message.attribute.MapFile;
import no.auke.p2p.m2.message.attribute.MessageAttributeInterface.MessageAttributeType;
import no.auke.p2p.m2.message.attribute.PublicAddress;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.m2.task.Task;
import no.auke.p2p.m2.InitVar;
import no.auke.p2p.m2.NameSpace;
import no.auke.p2p.m2.PeerServer;

public class KeepAlivePool extends Task {
	
	private MapFile mapfile;
	
	private NameSpace nameSpace;
	public NameSpace getNameSpace() {return nameSpace;}
	public void setNameSpace(NameSpace nameSpace) {this.nameSpace = nameSpace;}
	public MapFile getMapfileCurrent() {
		mapfile.setVersion(getNameSpace().getMainServ().getKeepaliveMap().getVersion());
		return mapfile;
	}
	private AtomicBoolean pool_is_stopped = new AtomicBoolean(true);
	private AtomicBoolean pool_is_connecting = new AtomicBoolean(false);
	// Symmetric checking
	private Map<String, Integer> checked_addresses = new ConcurrentHashMap<String, Integer>();
	public int numConnected() {
		int num_connected = 0;
		for (KeepAliveAgent agent : active_keepalive_agents.values()) {
			if (agent.isConnected() || agent.isConnecting()) {
				num_connected++;
			}
		}
		return num_connected;
	}
	public boolean isConnected() {
		return !pool_is_connecting.get() 
				&& !pool_is_stopped.get() 
				&& System.currentTimeMillis() - lastConnected.get() < InitVar.MAX_PING_PERIOD
				&& (numConnected() >= nameSpace.getMainServ().minKAServers()
				);
	}
	public boolean isDisConnected() {return pool_is_stopped.get();}
	
	private AtomicInteger requestId = new AtomicInteger(PeerServer.newRequestId());
	public int getRequestId() {return requestId.get();}
	
	// used when rejected
	// can not connect again
	// public void setDisConnect() {stopKeepAlivePool();}
	// list of thread for each active peer connection
	private Map<String, KeepAliveAgent> active_keepalive_agents = new ConcurrentHashMap<String, KeepAliveAgent>();
	// HUYDO: we need to get all active keep alive server
	private KeepAliveAgent connectedKeepAliveServer;
	public KeepAliveAgent getCurrentConnectKeepAliveAgent() {
		return connectedKeepAliveServer;
	}
	public KeepAlivePool(NameSpace nameSpace) {
		super(nameSpace.getMainServ().getServerId(), InitVar.POOL_WAIT);
		this.nameSpace = nameSpace;
	}
	public void wakeUp() {
		if (isRunning()) {
			//
			// to long since any answer
			// do reping
			//
			if (System.currentTimeMillis() - lastConnected.get() > InitVar.MAX_PING_PERIOD && !pool_is_connecting.get() && !pool_is_stopped.get()) {
				getNameSpace().getListen().debug(
						getNameSpace().getNameSpaceId() + " WakeUp do REPING after " + (System.currentTimeMillis() - lastConnected.get()));
				lastConnected.set(0);
				rePing();
			}
		}
	}
	// LHA: set time for last successful check pin OK
	private AtomicLong lastConnected = new AtomicLong();
	public void setLastConnected() {lastConnected.set(System.currentTimeMillis());}

	// running check every 15 seconds (POOL_WAIT) to see how many agents are
	// running
	@Override
	public void onStart() {
		
		if (pool_is_stopped.get()) {
			forceStop();
		} else {
			pool_is_connecting.set(true);
			checked_addresses.clear();
			getNameSpace().getListen().debug("Start new KA agents");
			
			for (NetAddress address : getNameSpace().getMainServ().locateKeepAlive(getNameSpace().getThisPeerId())) {
				
				try {
					
					NetAddress addressIP = new NetAddress(address);
					if (!isDisConnected() && !active_keepalive_agents.containsKey(addressIP.getAddressPort())) {
						if (active_keepalive_agents.size() < getNameSpace().getMainServ().maxKAServers()) {
							// create a new agent
							KeepAliveAgent agent = new KeepAliveAgent(KeepAlivePool.this, addressIP);
							active_keepalive_agents.put(addressIP.getAddressPort(), agent);
							// start (if not already started)
							agent.startAgent();
						}
					}
				} catch (UnknownHostException e) {
					getNameSpace().getListen().error("Can not resolve KA host " + e.getMessage());
				}
			}
		}
	}
	
	@Override
	public void onExecute() {
		if (pool_is_stopped.get()) {
			forceStop();
		} else if (!pool_is_connecting.get()) {
			// to long since any answer
			if (System.currentTimeMillis() - lastConnected.get() > InitVar.MAX_PING_PERIOD) {
				rePing();
			}
		}
	}
	
	@Override
	public void onStop() {
		// disconnect all KA, send close to KA server
		// disconnecting all KA agents
		// LHA: stop all
		for (KeepAliveAgent agent : new ArrayList<KeepAliveAgent>(active_keepalive_agents.values())) {
			agent.stopAgent();
		}
		active_keepalive_agents.clear();

	}
	
	//PublicAddress publicaddress;
	//public PublicAddress getPublicaddress() {
	//	return publicaddress;
	//}
	
	// AgentTask agenttask;
	// only once, when peer server starts
	public void startKeepAlivePool() {
		if (getNameSpace().isRunning() && pool_is_stopped.getAndSet(false)) {
			forceStop(); // to be sure it is stopped before start again
			mapfile = new MapFile();
			getNameSpace().getMonitors().getPingMonitor().execute(this);
			getNameSpace().getListen().trace("keep alive pool started");
		}
	}
	// only once, when peer server stops
	public void stopKeepAlivePool() {
		if (!pool_is_stopped.getAndSet(true)) {
			forceStop();
			int wait = 0;
			while (wait < 200 && numConnected() > 0) {
				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {}
				wait += 25;
			}
			//publicaddress = null;
			getNameSpace().getListen().debug("keep alive pool stopped");
		}
	}
	// agent is got connection, what to do with rest of agents
	public boolean setAgentConnected(KeepAliveAgent agent, PublicAddress publicaddress) {
		if (pool_is_connecting.getAndSet(false)) {
			getNameSpace().getMainServ().updatePublicAddress(publicaddress);
			getNameSpace().onConnected();
			return true;
		} else {
			return false;
		}
	}
	public void agentRemove(KeepAliveAgent agent) {
		if (!agent.isRunning()) {
			if (active_keepalive_agents.containsKey(agent.getPingaddress().getAddressPort())) {
				active_keepalive_agents.remove(agent.getPingaddress().getAddressPort());
			}
		}
	}
	public void rePing() {
		notSymetric = false;
		for (KeepAliveAgent agent : new ArrayList<KeepAliveAgent>(active_keepalive_agents.values())) {
			agent.rePing();
		}
	}
	
	// handle ping from KA server
	public void gotPingResponse(MessageHeader receiveMH) {
		
		// Thread.yield();
		KeepAliveAgent agent = active_keepalive_agents.get(receiveMH.getAddress().getAddressPort());
		if (agent != null) {
			agent.gotPingResponse(receiveMH);
		} else {
			getNameSpace().getListen().error(
					"OBS: no agents found for this addresse: " + receiveMH.getAddress().getAddressPort() + " when get KA ping "
							+ " Agentlist not initiated or closed because to long response, size of agentlist is "
							+ String.valueOf(active_keepalive_agents.size()));
		}
	}
	
	public void gotMapResponse(MessageHeader receiveMH) {
		
		// got mapping from keep alive
		MapFile readmap = (MapFile) receiveMH.getMessageAttribute(MessageAttributeType.MapFile);
		
		if (readmap != null) {
			
			getNameSpace().getListen().debug("MapResponse: map response " + readmap.getVersion());
			if (getNameSpace().getMainServ().getKeepaliveMap() != null && readmap.getVersion() > getNameSpace().getMainServ().getKeepaliveMap().getVersion()) {
				
				try {
				
					getNameSpace().getListen().message("Got new map version: " + String.valueOf(readmap.getVersion()));
					stopKeepAlivePool();
					// LHA: compare map version and install new
					// when install new, stop current KA pool
					// and restart everything
					getNameSpace().getMainServ().getKeepaliveMap().fromString(readmap.getFiletext());
					getNameSpace().getMainServ().getKeepaliveMap().setVersion(readmap.getVersion());
					getNameSpace().getMainServ().getKeepaliveMap().save();
					startKeepAlivePool();
				
				} catch (IOException e) {
					getNameSpace().getListen().error(" mapResponse: Error saving KA map file " + e.getMessage());
				}
				
			}
		}
	}
	
	// for external use
	public int getAgentsSize() {
		return active_keepalive_agents.size();
	}
	public ArrayList<KeepAliveAgent> getActiveAgents() {
		return new ArrayList<KeepAliveAgent>(active_keepalive_agents.values());
	}
	public boolean isSocketOpen() {
		return true;
	}
	
	// LHA: for symmetric NAT inspection
	// called from Keepalive agents when public IP is returned from KA
	int count_equal = 0;
	boolean notSymetric = false;
	public void checkSymmetricNAT(PublicAddress publicaddress, NetAddress fromKA) {
		if (!notSymetric && !getNameSpace().isMiddleman()) {

			// checking if there is same public address for client reported from
			// KA bu different ports
			// if same but different port, NAT is symmetric
			if (!checked_addresses.containsKey(fromKA.getAddressPort())) {
				checked_addresses.put(fromKA.getAddressPort(), publicaddress.getPort());
			}
			int port = -1;
			for (int current_port : checked_addresses.values()) {
				if (port == -1) {
					port = current_port;
				}
				if (port != current_port) {
					// mark session for middle man communication
					getNameSpace().setUseSymmetricNAT(true);
					// ping to KA with information that this client demand
					// middle man communication
					// send new ping twice to make sure KA's get informed
					rePing();
					getNameSpace().getListen().message(nameSpace.getClientid() + " session is using SYMMETRIC NAT, switched to middle man communication");
					break;
				} else if (checked_addresses.size() > 2 && port == current_port && !getNameSpace().isMiddleman()) {
					// this is a not symmetric NAT
					notSymetric = true;
					getNameSpace().getListen().message(nameSpace.getNameSpaceId() + " session is using ASYMMETRIC NAT");
				}
			}
		}
	}
}
