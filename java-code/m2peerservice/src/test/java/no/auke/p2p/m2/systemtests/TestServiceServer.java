package no.auke.p2p.m2.systemtests;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import no.auke.p2p.m2.NameSpace;
import no.auke.p2p.m2.PeerServer;

public class TestServiceServer {
	
	public TestServiceServer(String id) {
		clientId = id + "-" + UUID.randomUUID().toString().substring(1,3);
	}
	public PeerServer peerserver=null;
	public NameSpace client;
	public String clientId;
	public List<NameSpace> nsList = new LinkedList<NameSpace>();

}
