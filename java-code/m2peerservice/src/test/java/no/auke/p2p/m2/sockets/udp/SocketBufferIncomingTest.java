package no.auke.p2p.m2.sockets.udp;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyObject;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import no.auke.p2p.m2.NameSpace;
import no.auke.p2p.m2.PeerServer;
import no.auke.p2p.m2.general.IListener;
import no.auke.p2p.m2.workers.ComChannel;
import no.auke.p2p.m2.workers.PeerSession;
import junit.framework.TestCase;


//TODO: LHA: make tests 

public class SocketBufferIncomingTest extends TestCase {
    
    NameSpace namespace = mock(NameSpace.class);
    PeerSession peeragent = mock(PeerSession.class);
    
    ComChannel channel = mock(ComChannel.class);
    IListener listen = mock(IListener.class);
    
	ExecutorService executor = Executors.newCachedThreadPool();

	Random rnd = new Random();
	int port=10;
	
	public void setUp(){
		
		when(namespace.getMainServ()).thenReturn(mock(PeerServer.class));
        when(namespace.getMainServ().getChannel()).thenReturn(channel);
        when(namespace.getListen()).thenReturn(listen);
        when(namespace.getMainServ().getExecutor()).thenReturn(executor);
                
	}
	
	public void test_1(){
		assertTrue(true);
	}
	
//	public void test_outgoing_incomming_packets()
//	{
//
//        MessageHeader message = mock(MessageHeader.class);
//        when(message.getTransactionIDByte()).thenReturn(new byte[4]);
//        when(message.getAddress()).thenReturn(new NetAddress());
//        
//        
//        when(channel.UDPSendEncrypt((byte[])any(), (NetAddress)anyObject())).thenReturn(true);
//	    
//		for(int i=1;i<50;i++){
//			
//	    	byte[] data = new byte[rnd.nextInt(10000)];
//	    	rnd.nextBytes(data);
//			
//			// System.out.println("runing test no " + String.valueOf(i) +  " data length " + String.valueOf(dataout.length));
//			
//			Socket socket_in = new Socket(10,peerservice);
//
//			SocketBufferOutgoing1 buffer_out = new SocketBufferOutgoing1(new PeerPeerAgent(),new Socket(10,peerservice), 0,data,0,0);
//			SocketBufferIncoming buffer_in = new SocketBufferIncoming(new PeerPeerAgent(),socket_in, buffer_out.getMessageId(), 0);
//
//			// testing order of data packets
//			int packetnum=1; 
//			for(ChunkOutgoing chunk:buffer_out.getChunks_outgoing()){
//	
//				for(DataPacket packet:chunk.getDatapackets()){	
//				
//				    // simulate incoming packets
//					buffer_in.gotDataPacket(message, packet);					
//					assertEquals("incomming packets not in order", packetnum,packet.getNumber());
//					assertEquals("correct number of packets",packetnum,buffer_in.getNumpackets());					
//	                packetnum++;
//				}	
//				
//			}
//
//            assertTrue("correct number of packets in, packets sent",buffer_out.getNumpackets()==buffer_in.getNumpackets());
//            
//			// message complete mark on chunk
//			int cnt=1;
//			for(ChunkIncoming chunk:buffer_in.getChuncks_incoming()){				
//				
//				assertTrue("chunk not compete", chunk.isCompleteChunkRecieved());
//				cnt++;				
//			
//			}
//			
//			// get / clear message
//			
//			assertTrue("inbuffer contain data", buffer_in.getChuncks_incoming().size()>0);
//			assertNotNull("socket referenced",buffer_in.getSocket());
//			
//			byte[] datain=buffer_in.getBuffer();
//			
//			assertFalse("inbuffer cleared", buffer_in.getChuncks_incoming().size()>0);
//			assertTrue("data not equal to sent",Arrays.equals(data,datain));
//			
//			// polling the buffer and check data
//			
//			SocketBufferIncoming buffer_offer = socket_in.getInbuff().poll();
//			
//			assertNotNull("no buffer avail", buffer_offer);	
//			assertTrue("data not equal to sent, and still in socketbuffer", Arrays.equals(data,buffer_offer.getBuffer()));	
//			
//			
//		}
//	}

    
}
