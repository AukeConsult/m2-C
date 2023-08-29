package no.auke.p2p.m2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import no.auke.m2.task.ServiceMonitors;
import no.auke.p2p.m2.general.IListener;
import no.auke.p2p.m2.general.IPeerServerListener;
import no.auke.p2p.m2.general.ListenerDefault;
import no.auke.p2p.m2.general.ParamReader;
import no.auke.p2p.m2.message.attribute.PeerLicense;
import no.auke.p2p.m2.message.attribute.PublicAddress;
import no.auke.p2p.m2.sockets.udp.SendStatistics;
import no.auke.p2p.m2.workers.ComChannel;
import no.auke.p2p.m2.workers.keepalive.KeepAliveMap;
import no.auke.p2p.m2.workers.keepalive.NetAddress;
import no.auke.p2p.m2.workers.keepalive.Peerid;
import no.auke.p2p.m2.workers.keepalive.Util;
import no.auke.util.ListNetworks;

public class PeerServer {
	
	//private String VERSION = "3.0.0";
	public static Random rnd = new java.util.Random(System.nanoTime());
	private int serverId = rnd.nextInt(Integer.MAX_VALUE);
	
	// changed to static to make more secure for request ID
	public static synchronized int newRequestId() {return rnd.nextInt(Integer.MAX_VALUE);}
	
	public int getServerId() {return serverId;}
	
	private String thisdevice;
	private String configdir;
	private String bootaddress;
	
	private String address = "";
	private int port = 0;
	
	public String getConfigdir() {return configdir;}
	public String getBootaddress() {return bootaddress;}
	public String getAddress() {return address;}
	public int getPort() {return port;}
	private String application;
	public String getApplication() {return application;}
	private PeerLicense peerLicense;
	public PeerLicense getPeerLicense() {return peerLicense;}
	public String getBootAddress() {return bootaddress;}
	
	// Used for pwd og device check. Not in use
	public void setThisdevice(String thisdevice) {this.thisdevice = thisdevice;}
	public String getThisdevice() {return thisdevice;}
			
	private Map<String, NameSpace> nameSpaceList = new ConcurrentHashMap<String, NameSpace>();
	
	private Map<String, SendStatistics> send_stats = new ConcurrentHashMap<String, SendStatistics>();
	public Map<String, SendStatistics> getSendstats() {return send_stats;}
	
	public SendStatistics getStat(NetAddress address) {
		if (!send_stats.containsKey(address.getAddress())) {
			send_stats.put(address.getAddress(), new SendStatistics(address));
		}
		return send_stats.get(address.getAddress());
	}
	
	private IListener listen;
	public IListener getListen() {return listen==null?new PeerListnerDefault():listen;}
	public void setListen(IListener listen) {this.listen = listen;}
	
	// global comchannel
	private ComChannel channel;
	public ComChannel getChannel() {return channel;}
	
	public NetAddress getLocaladdress() {
		return getChannel() != null ? getChannel().getLocaladdress() : new NetAddress();
	}
	
	private NetAddress publicaddress = new NetAddress();
	public NetAddress getPublicaddress() {
		synchronized(publicaddress) {
			return publicaddress != null ? publicaddress : new NetAddress();
		}
	}
	
	public void updatePublicAddress(PublicAddress address) {
		synchronized(publicaddress) {
			publicaddress = new NetAddress(address.getAddress().toString().trim(),address.getPort());
		}
	}	
	
	// a potential global monitor
	ServiceMonitors globalMonitors;
	public ServiceMonitors getGlobalMonitors() {return globalMonitors;}
	public void setGlobalMonitors(ServiceMonitors globalMonitors) {this.globalMonitors = globalMonitors;}
	
	private ExecutorService executor = Executors.newCachedThreadPool();	
	public ExecutorService getExecutor() {
		if (executor == null) {executor = Executors.newCachedThreadPool();}
		return executor;
	}
	
	public PeerServer(String application, String configdir, String bootaddress, int port, String address) {
		
		this.bootaddress = bootaddress;
		this.port=port;
		this.configdir = configdir;
		this.application = application;
		this.address = address;
		this.port = port;
		
		// not in use (for future use on check for devices legal)
		this.thisdevice = Util.convertToHex(Util.getHash(ListNetworks.getMacAddress()));
		
		ParamReader.readIfExisting();
		listen = new ListenerDefault(9);
		
		channel = new ComChannel(this, address, port, InitVar.IN_QUEUE_SIZE, InitVar.OUT_QUEUE_SIZE, InitVar.PACKET_LOSS_SIMULATE);
		
		if (!bootaddress.equals("")) {
			keepaliveMap = new KeepAliveMap(configdir, bootaddress);
		} else {
			keepaliveMap = new KeepAliveMap(configdir, InitVar.USE_TEST_KA ? InitVar.TEST_BOOT_ADDRESS : InitVar.BOOT_ADDRESS);
		}
	}

	// Constructor boot address
	public PeerServer(String application, String configdir, String bootaddress, int port, IListener listen) {
		this(application, configdir, bootaddress, port,"");
		this.listen = listen;
	}

	public PeerServer(String application, String configdir, String bootaddress, int port) {
		this(application, configdir, bootaddress, port,"");
	}

	// Constructor boot address
	public PeerServer(String application, String configdir, int port, IListener listen) {
		this(application, configdir, "", port,"");
		this.listen = listen;
	}
	public PeerServer(String application, String configdir, int port) {
		this(application, configdir, "", port,"");
	}	
	
	
	
	
	//
	// Open namespace
	// id is used for identify NS temporarily
	private int nsId=0;
	public NameSpace openNameSpace(String namespaceId) {
		return openNameSpace(namespaceId,null);
	}
	
	//boolean startNameSpace(NameSpace nameSpace) {
	//	return getChannel().startChannel(nameSpace);		
	//}
	void stopNameSpace(NameSpace nameSpace) {
		nameSpaceList.remove(nameSpace.getNameSpaceId());
		if(nameSpaceList.size()==0) {
			stop();
		}
	}

	public void stop() {
		getChannel().closeChannel();
		getExecutor().shutdown();
	}

	public NameSpace openNameSpace(String namespaceId, IListener listen) {
		if (!nameSpaceList.containsKey(namespaceId)) {
			nsId++;
			NameSpace namespace = new NameSpace(this, namespaceId, nsId, listen==null?getListen():listen);
			nameSpaceList.put(namespaceId, namespace);
		}		
		return nameSpaceList.get(namespaceId);		
	}
	
	// contains version of server mapping
	private KeepAliveMap keepaliveMap;
	public void setKeepaliveMap(KeepAliveMap keepaliveMap) {this.keepaliveMap = keepaliveMap;}
	public KeepAliveMap getKeepaliveMap() {return keepaliveMap;}
	
	public int maxKAServers() {return (keepaliveMap != null ? keepaliveMap.getBootEntry().size() : 0) + InitVar.POOL_MAXIMUM_AGENTS;}
	public int minKAServers() {return (keepaliveMap != null ? keepaliveMap.getBootEntry().size() : 0) + InitVar.POOL_MINIMUM_AGENTS;}
	
	public List<NetAddress> locateKeepAlive(Peerid peerid) {
		
		// core locate
		// get all possible keep alive servers for a user id
		// make priority
		// return a list of address
		
		if (getKeepaliveMap() != null) {
			if (getKeepaliveMap().getVersion() <= 1000) {
				return getKeepaliveMap().getAddresses();
			} else {
				return getKeepaliveMap().getUserAddresses(peerid.peerid);
			}
		} else {
			return new ArrayList<NetAddress>();
		}
	}



}
