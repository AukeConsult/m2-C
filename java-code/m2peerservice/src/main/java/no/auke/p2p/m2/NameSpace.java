/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import no.auke.m2.task.ServiceMonitors;
import no.auke.p2p.m2.general.IListener;
import no.auke.p2p.m2.general.ReturMessageTypes;
import no.auke.p2p.m2.workers.PeerSession;
import no.auke.p2p.m2.workers.PeerPeerInfo;
import no.auke.p2p.m2.workers.ReConnector;
import no.auke.p2p.m2.workers.connect.SessionEncrypt;
import no.auke.p2p.m2.workers.keepalive.KeepAlivePool;
import no.auke.p2p.m2.workers.keepalive.Peerid;
import no.auke.p2p.m2.workers.message.MessageListener;
import no.auke.p2p.m2.workers.message.MessageSender;

public class NameSpace {
	
	// private static final Logger logger = LoggerFactory.getLogger(PeerServer.class);

	private int id;
	public int getId() {return id;}

	private String namespaceid;
	public String getNameSpaceId() {return namespaceid;}
		
	private PeerServer server;
	public PeerServer getMainServ() {return server;}
	public void setMainServ(PeerServer server) {this.server = server;}
	
	private Peerid peerid;
	public Peerid getThisPeerId() {return makePeerid(getClientid());}
	public String getClientid() {return peerid.getUserid();}
	public void setClientid(String clientid) {peerid = makePeerid(clientid);}
	
	
	private AtomicBoolean passive = new AtomicBoolean(false);
	public boolean isPassive() {return passive.get();}
	
	public void setPassive(boolean passive) {
		if (!this.passive.get() && passive) {
			this.getKeepAlivePool().startKeepAlivePool();
		}
		if (this.passive.get() && !passive) {
			this.getKeepAlivePool().startKeepAlivePool();
		}
		this.passive.set(passive);
	}
			
	// local monitors
	private ServiceMonitors monitors;
	public ServiceMonitors getMonitors() {return monitors;}
	
	// restart helper
	private ReConnector reconnector;
	public ReConnector getReConnector() {return reconnector;}
	
	private KeepAlivePool pingAlive;
	public KeepAlivePool getKeepAlivePool() {return pingAlive;}
	
	private MessageListener messagelistener = null;
	public MessageListener getMessageListener() {return messagelistener;}

	private MessageSender messageSender = null;
	public MessageSender getMessageSender() {return messageSender;}	

	private SessionEncrypt sessionencrypt;
	public SessionEncrypt getSessionEncrypt() {return sessionencrypt;}
	
	// TODO: document turn on/off session encryption
	private boolean do_session_encryption = InitVar.DO_SESSION_ENCYPTION;
	public boolean doSessionEncryption() {return do_session_encryption;}
	public void setSessionEncryption(boolean b) {do_session_encryption = b;}
	private boolean useMiddleman = false;
	public void setUseMiddleman(boolean useMiddleman) {this.useMiddleman = useMiddleman;}
	private boolean useSymmetricNAT = false;
	public boolean isUseSymmetricNAT() {return useSymmetricNAT;}
	public void setUseSymmetricNAT(boolean useSymmetricNAT) {this.useSymmetricNAT = useSymmetricNAT;}
	public boolean isMiddleman() {return useMiddleman || useSymmetricNAT;}
	private IListener listen;
	public IListener getListen() {return listen;}
	public void setListen(IListener listen) {this.listen = listen;}
	
	// peer list for reconnect, identified with userid
	private Map<String, PeerPeerInfo> knownPeers = new ConcurrentHashMap<String, PeerPeerInfo>();
	public Map<String, PeerPeerInfo> getKnownPeers() {return knownPeers;}
	
	// list of thread for each active peer connection
	private Map<String, PeerSession> openPeerSessions = new ConcurrentHashMap<String, PeerSession>();
	public boolean isPeerClosed(String peerClientId) {
		String peerhexid = makePeerid(peerClientId).getPeerhexid();
		if (getKnownPeers().containsKey(peerhexid)) {
			return getKnownPeers().get(peerhexid).isClosed();
		}
		return true;
	}
	// open sockets
	private Map<Integer, Socket> openSockets = new ConcurrentHashMap<Integer, Socket>();
	public Map<Integer, Socket> getSockets() {return openSockets;}
	// open stream sockets
	Map<Integer, StreamSocket> openStreamSockets = new ConcurrentHashMap<Integer, StreamSocket>();
	public Map<Integer, StreamSocket> getStreamSockets() {return openStreamSockets;}
	private AtomicBoolean service_is_running = new AtomicBoolean();
	
	// LHA: final constructor for all
	public NameSpace(PeerServer server, String namespaceid, int id, IListener listen) {
	
		this.server = server;
		this.namespaceid = namespaceid;
		this.id = id;
		
		if (listen == null) {
			this.listen = server.getListen();
		} else {
			this.listen = listen;
		}
		
		sessionencrypt = new SessionEncrypt(this);
		reconnector = new ReConnector(this);
		pingAlive = new KeepAlivePool(this);
		messageSender = new MessageSender(this);
		service_is_running.set(false);
		
	}
	
	public void onConnected() {
		listen.message("Connected:" + getMainServ().getLocaladdress().getAddressPort() + ":" + getNameSpaceId() + ":" + getClientid());
	}

	// service connected, have got keep alive connection
	public boolean isRunning() {return service_is_running.get();}
	
	// service connected, have got keep alive connection
	public boolean isConnected() {
		if (service_is_running.get() && getKeepAlivePool() != null) {
			return (isPassive() || getKeepAlivePool().isConnected());
		}
		return false;
	}
	// Android implementation because phone fell asleep
	AtomicBoolean doing_wakeup = new AtomicBoolean(false);
	public void wakeUp() {
		if (!isConnected() && !doing_wakeup.getAndSet(true)) {
			try {
				getMainServ().getChannel().getPacketChannel().wakeUp();
				if (getReConnector().waitConnect() && !isConnected()) {
					getKeepAlivePool().wakeUp();
					// LHA: make a session ping as well
					for (PeerSession session : getOpenPeerSessions().values()) {
						if (session.isConnected()) {
							session.wakeUp();
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				doing_wakeup.set(false);
			}
		}
	}
	// find peer
	public SocketRetStatus findUser(String remoteclientid) {
		SocketRetStatus ret = openPeer(makePeerid(remoteclientid));
		if (ret.getLastRetcode() != ReturMessageTypes.ok) {
			return ret;
		} else if (ret.getPeerSession().findAndConnect()) {
			ret.setLastRetcode(ReturMessageTypes.ok);
			return ret;
		}
		ret.setLastRetcode(ReturMessageTypes.peer_not_found);
		if (ret.getPeerSession() != null) {
			ret.getPeerSession().killSession("not found in find user");
		}
		return ret;
	}
	
	
	public synchronized SocketRetStatus openPeer(Peerid peerid) {
		
		SocketRetStatus ret = new SocketRetStatus();
		
		if (peerid.getUserid().length() == 0) {
			ret.setLastRetcode(ReturMessageTypes.no_peerid);
			ret.setLastMessage("no peerid provided");
			return ret;
		} else if (!isRunning()) {
			ret.setLastRetcode(ReturMessageTypes.peer_session_stopped);
			ret.setLastMessage("server is stopped");
			return ret;
		}

		if (!peerid.getUserid().equals(this.getThisPeerId().getUserid())) { // not me to open
			
			if (!getOpenPeerSessions().containsKey(peerid.getPeerhexid())) {
				
				System.out.println("XX open new peer:" + peerid.getPeerhexid());

				// add a new the session
				PeerSession session_new = new PeerSession(this, peerid);
				ret.setPeerAgent(session_new);
				ret.setOk();
				
				getOpenPeerSessions().put(session_new.getPeerid().getPeerhexid(), session_new);
			}
			
			PeerSession session = getOpenPeerSessions().get(peerid.getPeerhexid());
			if (session.isFinish()) {

				PeerSession session_new = new PeerSession(this, session.getPeerid());
				ret.setPeerAgent(session_new);
				ret.setOk();
				getOpenPeerSessions().remove(peerid.getPeerhexid());
				getOpenPeerSessions().put(peerid.getPeerhexid(), ret.getPeerSession());
			
			} else {
			
				ret.setPeerAgent(session);
				ret.setOk();					
			}
							
		} else {
			ret.setLastRetcode(ReturMessageTypes.peer_is_me);
			ret.setLastMessage("can't connect to self " + peerid.getUserid());
			getOpenPeerSessions().remove(peerid.getUserid());
		}
		return ret;
	}
	
	public boolean isSocketOpen(int port) {
		if (openSockets.containsKey(port)) {
			return openSockets.get(port).isOpen();
		}
		return false;
	}
	
	// socket
	public Socket getSocket(int port) {return openSockets.get(port);}
	public Socket openSocket(int port) {
		// TODO: this must be tested
		// LHA: get random port number for outgoing
		if (port == 0) {
			for (int newport = 64000; newport > 50000; newport--) {
				if (!openSockets.containsKey(newport)) {
					port = newport;
					break;
				}
			}
		}
		if (!openSockets.containsKey(port)) {
			openSockets.put(port, new Socket(port, this));
		}
		return openSockets.get(port);
	}
	
	public Socket openSocket(int port, SocketListener listen) {return listen.start(openSocket(port));}
	public void removeSocket(Socket socket) {openSockets.remove(socket.getPort());}
	public void removeStreamSocket(StreamSocket socket) {openStreamSockets.remove(socket.getPort());}
	
	// open multiple remote
	public StreamSocket openStream(int localport) {
		if (!getStreamSockets().containsKey(localport)) {
			StreamSocket socket = new StreamSocket(this, localport);
			getStreamSockets().put(localport, socket);
		}
		return getStreamSockets().get(localport);
	}

	// open multiple remote with listener
	public StreamSocket openStream(int localport, StreamSocketListener listener) {
		if (!getStreamSockets().containsKey(localport)) {
			StreamSocket socket = new StreamSocket(this, localport);
			getStreamSockets().put(localport, socket);
		}
		getStreamSockets().get(localport).setListener(listener);
		return getStreamSockets().get(localport);
	}
		
	//
	// major point for starting monitor and com channel
	// is to be moved up to peerserver
	// 
	
	public void start(String clientid) {
		
		if (!service_is_running.getAndSet(true)) {
		
			if (!clientid.equals("")) {
				
				setClientid(clientid);
				listen.message("Namespace start");
				listen.debug("Namespace   : " + namespaceid);
				listen.debug("Client id   : " + clientid);
				
				// Global monitor is set, us this
				if (getMainServ().getGlobalMonitors() != null) {
					monitors = getMainServ().getGlobalMonitors();
				} else {
					monitors = new ServiceMonitors(getNameSpaceId());
				}
				monitors.start();
				
				// message listener
				messagelistener = new MessageListener(this);
				getReConnector().connect();
								
			} else {
				
				listen.error("Can't start service, clientid is empty");
			}
			
		} else {
			listen.debug("No startup, service is running");
		}
	}	
	
	//
	// major point for stop monitor and com channel
	// is to be moved up to peerserver
	// 
	
	public void stop() {
		
		if (service_is_running.getAndSet(false)) {

			// closing the soft sockets								
			// close all sockets
			for (Socket s : new ArrayList<Socket>(this.getSockets().values())) {
				s.close();
			}
			getSockets().clear();
			
			// close all stream sockets
			for (StreamSocket s : new ArrayList<StreamSocket>(this.getStreamSockets().values())) {
				s.close();
			}
			getStreamSockets().clear();

			reconnector.disConnect();

			// stop monitors if not global
			if (monitors != null) {
				monitors.stop();
				monitors = null;
			}
			getMainServ().stopNameSpace(this);				

			listen.message("stopped namespace");
			service_is_running.set(false);
		}	
	}
	
	public int getNumKAServers() {
		if (service_is_running.get()) {
			KeepAlivePool keepAlivePool = getKeepAlivePool();
			return keepAlivePool.getAgentsSize();
		}
		return -1;
	}
	public int getNumberOfKnownPeers() {
		if (service_is_running.get()) {
			int num = 0;
			for (PeerPeerInfo info : getKnownPeers().values()) {
				if (!info.isClosed()) {
					num++;
				}
			}
			return num;
		}
		return -1;
	}
	public Map<String, PeerSession> getOpenPeerSessions() {
		return openPeerSessions;
	}
	public void closePeerAgent(String reason, String remoteClientId) {
		// LHA: fixed
		PeerSession session = getOpenPeerSessions().get(makePeerid(remoteClientId).getPeerhexid());
		if (session != null) {
			session.closeSession(reason);
			listen.debug(reason + " " + remoteClientId);
		}
	}
	public void disConnect(String remoteClientId) {
		SocketRetStatus ret = openPeer(makePeerid(remoteClientId));
		if (ret.isOk()) {
			ret.getPeerSession().disConnect();
		}
	}
	// LHA: for use with no MM
	private boolean ismmBlocked = false;
	public void setMMBlocked() {
		this.ismmBlocked = true;
	}
	public boolean isMMBlocked() {
		return ismmBlocked;
	}
	private Map<String, Peerid> idlist = new ConcurrentHashMap<String, Peerid>();
	public Peerid makePeerid(String userid) {
		if (userid == null || userid.length() == 0) {
			return new Peerid();
		} else {
			if (!idlist.containsKey(namespaceid + userid)) {
				idlist.put(namespaceid + userid, new Peerid(namespaceid, userid));
			}
			return idlist.get(namespaceid + userid);
		}
	}
}
