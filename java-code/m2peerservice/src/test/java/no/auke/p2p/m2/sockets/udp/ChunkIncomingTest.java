package no.auke.p2p.m2.sockets.udp;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import no.auke.p2p.m2.NameSpace;
import no.auke.p2p.m2.PeerServer;
import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.general.IListener;
import no.auke.p2p.m2.message.attribute.DataPacket;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.InitVar;
import no.auke.p2p.m2.sockets.udp.ChunkIn;
import no.auke.p2p.m2.sockets.udp.SocketBufferIn;
import no.auke.p2p.m2.workers.PeerSession;
import no.auke.p2p.m2.workers.keepalive.NetAddress;
import no.auke.p2p.m2.workers.message.MessageSender;
import junit.framework.TestCase;

import static org.mockito.Matchers.anyObject;
//import static org.mockito.Matchers.any;
//import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

// TODO: LHA: make tests 

public class ChunkIncomingTest extends TestCase {
    
    PeerServer server = mock(PeerServer.class);
    NameSpace namespace = mock(NameSpace.class);
    PeerSession peeragent = mock(PeerSession.class);

    
    IListener listen = mock(IListener.class);
	Socket socket = mock(Socket.class);
	SocketBufferIn socketbuffer = mock(SocketBufferIn.class);

	Random rnd = new Random();
	int port=10;
	
	public void setUp(){

		socketbuffer = mock(SocketBufferIn.class);
        when(server.getServerId()).thenReturn(1);
        when(peeragent.getMainServ()).thenReturn(server);
        when(namespace.getListen()).thenReturn(listen);
        when(socket.getNameSpace()).thenReturn(namespace);
        when(socketbuffer.getMsgId()).thenReturn(100);
        
	}
	
	
	public void test_open() {
    	ChunkIn chunk = new ChunkIn(socket,socketbuffer, new ConcurrentHashMap<Integer, DataPacket>(),10, 0,0, new NetAddress(), 10000);
    	assertEquals(chunk.getDatapackets().size(),0);
    }
	
	public void test_addpackets() {

    	int numChunks=8;
		for(int chunknum=1;chunknum<=numChunks;chunknum++){

		    MessageSender sender = mock(MessageSender.class);
	        when(sender.UDPSendEncrypt((MessageHeader)anyObject())).thenReturn(true);
	        when(namespace.getMessageSender()).thenReturn(sender);

	    	ChunkIn chunk = spy(new ChunkIn(socket,socketbuffer,new ConcurrentHashMap<Integer, DataPacket>(),chunknum,0,InitVar.CHUNK_SIZE, new NetAddress(), 10000));
	    	
			for (int index = 1; index <= InitVar.CHUNK_SIZE; index++) {
				
				int packetnum = index+((chunknum-1)*InitVar.CHUNK_SIZE);
				
				DataPacket packet = new DataPacket();
				packet.setPort(1);				
				packet.setMsgId(socketbuffer.getMsgId());
				packet.setNumber(packetnum);
				packet.setTotal(numChunks * InitVar.CHUNK_SIZE);
				packet.setData(new byte[0]);
				packet.setChunkSize(InitVar.CHUNK_SIZE);
				
				assertTrue("fail add " + String.valueOf(chunknum) + " " + String.valueOf(packet.getNumber()), chunk.addIncomingPacket(packet));
		    	assertEquals("wrong size " + String.valueOf(chunknum) + " "  + String.valueOf(packet.getNumber()),index,chunk.getDatapackets().size());
		    	assertEquals("wrong chunk "  + String.valueOf(chunknum) + " " + String.valueOf(packet.getNumber()),chunknum,chunk.getChunkNumber());
		    	// add again
		    	assertFalse("added twise "  + String.valueOf(chunknum) + " " + String.valueOf(packet.getNumber()), chunk.addIncomingPacket(packet));
		    	assertEquals("wrong size "  + String.valueOf(chunknum) + " " + String.valueOf(packet.getNumber()),index,chunk.getDatapackets().size());
		    	assertEquals("wrong chunk "  + String.valueOf(chunknum) + " " + String.valueOf(packet.getNumber()),chunknum,chunk.getChunkNumber());
				
			}
			assertTrue("not complete " +  String.valueOf(chunknum) + " " + String.valueOf(chunknum), chunk.isComplete());
			
			chunk.replyComplete();
			assertTrue(chunk.isReplied());
			
			verify(sender, times(1)).UDPSendEncrypt((MessageHeader)anyObject());

		}
    }
	
	public void test_addpackets_missing() {

    	int numChunks=8;
    	for(int chunknum=1;chunknum<=numChunks;chunknum++){
    		
		    MessageSender sender = mock(MessageSender.class);
	        when(sender.UDPSendEncrypt((MessageHeader)anyObject())).thenReturn(true);
	        when(namespace.getMessageSender()).thenReturn(sender);
	    	
    		ChunkIn chunk = spy(new ChunkIn(socket,socketbuffer,new ConcurrentHashMap<Integer, DataPacket>(),chunknum, 0, InitVar.CHUNK_SIZE, new NetAddress(), 10000));
			
	    	for (int index = 1; index <= InitVar.CHUNK_SIZE; index++) {

				if(index==1 | index == 3) {
					// two packet missing
				} else {
				
					int packetnum = index+((chunknum-1)*InitVar.CHUNK_SIZE);
					DataPacket packet = new DataPacket();
					packet.setMsgId(socketbuffer.getMsgId());
					packet.setPort(1);
					packet.setNumber(packetnum);
					packet.setTotal(numChunks * InitVar.CHUNK_SIZE);
					packet.setData(new byte[0]);
					packet.setChunkSize(InitVar.CHUNK_SIZE);
					assertTrue("fail add " + String.valueOf(chunknum) + " " + String.valueOf(packet.getNumber()), chunk.addIncomingPacket(packet));
				}
			
	    	}
	    	
			assertFalse("complete " +  String.valueOf(chunknum) + " " + String.valueOf(chunknum), chunk.isComplete());

			chunk.replyMissing();
			verify(sender, times(1)).UDPSendEncrypt((MessageHeader)anyObject());
		
    	}
    }
}
