package no.auke.p2p.m2.workers;

import static org.mockito.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import no.auke.p2p.m2.NameSpace;
import no.auke.p2p.m2.PeerServer;
import no.auke.p2p.m2.workers.keepalive.NetAddress;
import no.auke.p2p.m2.workers.keepalive.Peerid;
import no.auke.p2p.m2.workers.keepalive.Util;
import no.auke.p2p.m2.workers.message.MessageSender;
import no.auke.p2p.m2.general.IListener;

public class PeerSessionTestBase {
	
	PeerServer server = mock(PeerServer.class);
	NameSpace namespace= mock(NameSpace.class);
	ComChannel channel;

	String namespaceid = "nameXX1";
	String peer_clientid = "peerClient123";
	Peerid peerid = new Peerid(namespaceid,peer_clientid);

    Peerid userid=new Peerid(namespaceid,"local");
	
    PeerSession session=null;
    PeerSession peeragent_spy=null;
    
    ReConnector reconnector;
    ArrayList<NetAddress> peer_keepalive_list =  new ArrayList<NetAddress>();
    
    ConcurrentHashMap<String, PeerPeerInfo> knownPeers = new ConcurrentHashMap<String, PeerPeerInfo>();
    ConcurrentHashMap<String, PeerSession> openPeerSessions = new ConcurrentHashMap<String, PeerSession>();
    
	ExecutorService executor = Executors.newCachedThreadPool();

    // TODO: LHA
    // upgrade tests later
    
	public void init() {
		
		peer_keepalive_list.clear();
		openPeerSessions.clear();
		knownPeers.clear();
		
        peer_keepalive_list.add(new NetAddress("127.0.0.1",100));
        peer_keepalive_list.add(new NetAddress("127.0.0.1",200));
        peer_keepalive_list.add(new NetAddress("127.0.0.1",300));
        peer_keepalive_list.add(new NetAddress("127.0.0.1",400));
		
        when(namespace.getNameSpaceId()).thenReturn(namespaceid);
        when(namespace.getMainServ()).thenReturn(server);
        
        
        when(namespace.getThisPeerId()).thenReturn(userid);
        when(namespace.getMainServ().getLocaladdress()).thenReturn(new NetAddress("127.0.0.1",1));
        when(namespace.getMainServ().getExecutor()).thenReturn(executor);
        when(namespace.getMessageSender()).thenReturn(mock(MessageSender.class));
         
        reconnector = mock(ReConnector.class);
        when(reconnector.waitConnect()).thenReturn(true);
        
        when(namespace.getReConnector()).thenReturn(reconnector);
        
        
        when(namespace.getListen()).thenReturn(mock(IListener.class));
        when(namespace.isConnected()).thenReturn(true);
        when(namespace.getMainServ().locateKeepAlive((Peerid)any())).thenReturn(peer_keepalive_list);
                
        when(namespace.getOpenPeerSessions()).thenReturn(openPeerSessions);
        when(namespace.getKnownPeers()).thenReturn(knownPeers);
        
        session = new PeerSession(namespace, peerid);        
        peeragent_spy = spy(new PeerSession(namespace, peerid));
        
        when(peeragent_spy.isConnected()).thenReturn(false);
        
        channel = mock(ComChannel.class);
        when(namespace.getMainServ().getChannel()).thenReturn(channel);
		
        //doReturn(true).when(channel).UDPSendEncrypt((MessageHeader)any());   

	}
	
   

}
