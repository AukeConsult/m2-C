package no.auke.p2p.m2.sockets.udp;

import no.auke.m2.task.ServiceMonitors;
import no.auke.p2p.m2.InitVar;
import no.auke.p2p.m2.NameSpace;
import no.auke.p2p.m2.PeerServer;
import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.SocketRetStatus;
import no.auke.p2p.m2.message.attribute.DataPacket;
import no.auke.p2p.m2.sockets.udp.ChunkOut;
import no.auke.p2p.m2.sockets.udp.SendStatistics;
import no.auke.p2p.m2.sockets.udp.SocketBuffer;
import no.auke.p2p.m2.sockets.udp.SocketBufferOut;
import no.auke.p2p.m2.sockets.udp.SocketBufferOut;
import no.auke.p2p.m2.workers.PeerSession;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.junit.Assert.*;
//import org.mockito.ArgumentMatcher;
//import org.mockito.invocation.InvocationOnMock;
//import org.mockito.stubbing.Answer;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import no.auke.p2p.m2.workers.connect.PeerSessionEncrypt;
import no.auke.p2p.m2.workers.keepalive.NetAddress;

//@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*",
//    "com.sun.org.apache.xerces.*", "ch.qos.logback.*",
//    "org.slf4j.*" })
//
//@RunWith(PowerMockRunner.class)
//@PrepareForTest( {ChunkOutgoing2.class, SocketBufferOutgoing2.class, SocketBufferOutgoing.class, SocketBuffer.class })

public class SocketBufferTest  {
	
	Socket socket = mock(Socket.class);
	PeerSession peeragent = mock(PeerSession.class);	
    NameSpace namespace = mock(NameSpace.class);
    PeerServer server = mock(PeerServer.class);
     
    @Before
    public void setUp(){
    	
    	when(server.getServerId()).thenReturn(1);
    	
        when(namespace.getMainServ()).thenReturn(server);
        when(namespace.getMonitors()).thenReturn(new ServiceMonitors("dummy"));
        
        when(socket.getNameSpace()).thenReturn(namespace);
        
        when(peeragent.getMainServ()).thenReturn(server);
        when(peeragent.getNameSpace()).thenReturn(namespace);
        
        PeerSessionEncrypt session = new PeerSessionEncrypt(peeragent);
		when(peeragent.getSessionEncrypt()).thenReturn(session);
    	
    }
    
	public byte[] data(int length) {
		byte[] data = new byte[length];
		int x=0;
		for(int i=0;i<length;i++){			
			data[i] = (byte)x;
			x++;
			if(x>255){
				x=0;
			}			
		}
		return data;
	}
	
	@Test
	public void test_Buffer_Split() throws Exception {
		
		for(int i=1;i<10;i++){
			byte[] msg=data(i*3233);
			SocketBufferOut buffer = new SocketBufferOut(peeragent, new SocketRetStatus(), socket, 0,msg);
			int size = (int) Math.ceil((msg.length + 0.0) / (InitVar.PACKET_SIZE-52));
	        assertEquals("check total lenght " + String.valueOf(msg.length), size,buffer.getDatapackets().size());
		}
	
	}
	
	@Test
	public void test_PacketNumber_order() throws Exception {
		
		for(int i=1;i<100;i++){
		
			SocketBufferOut buffer = new SocketBufferOut(peeragent, new SocketRetStatus(), socket, 0, data(i*1223));
			int total=buffer.getDatapackets().size();
			int packetnum=1;
			for(ChunkOut chunk:buffer.getChunks_outgoing()){
				for(DataPacket packet:chunk.getDatapackets()){
					assertEquals(packetnum,packet.getNumber());
					assertEquals(total,packet.getTotal());
					packetnum++;
				}
			}
			assertEquals(total,packetnum-1);
		}
	}
}

