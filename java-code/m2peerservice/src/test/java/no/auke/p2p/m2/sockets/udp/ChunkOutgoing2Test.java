//TODO fix test

//package no.auke.p2p.m2.sockets.udp;
//
//import static org.mockito.Matchers.*;
//import static org.powermock.api.mockito.PowerMockito.when;
//import static org.powermock.api.mockito.PowerMockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.times;
//
//import org.junit.Ignore;
//import org.junit.runner.RunWith;
//import org.powermock.core.classloader.annotations.PowerMockIgnore;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//
//import java.util.ArrayList;
//import java.util.Random;
//
//import no.auke.p2p.m2.NameSpace;
//import no.auke.p2p.m2.PeerServer;
//import no.auke.p2p.m2.Socket;
//import no.auke.p2p.m2.SocketRetStatus;
//import no.auke.p2p.m2.general.IListener;
//import no.auke.p2p.m2.message.attribute.DataPacket;
//import no.auke.p2p.m2.message.attribute.DataReplyPacket;
//import no.auke.p2p.m2.message.header.MessageHeader;
//import no.auke.m2.task.ServiceMonitors;
//import no.auke.p2p.m2.InitVar;
//import no.auke.p2p.m2.sockets.udp.ChunkOut;
//import no.auke.p2p.m2.sockets.udp.SendStatistics;
//import no.auke.p2p.m2.sockets.udp.SocketBufferIn;
//import no.auke.p2p.m2.sockets.udp.SocketBufferOut;
//import no.auke.p2p.m2.workers.ComChannel;
//import no.auke.p2p.m2.workers.PeerSession;
//import no.auke.p2p.m2.workers.connect.PeerSessionEncrypt;
//import no.auke.p2p.m2.workers.connect.SessionEncrypt;
//import no.auke.p2p.m2.workers.keepalive.NetAddress;
//import no.auke.p2p.m2.workers.message.MessageSender;
//import junit.framework.TestCase;
//
//@RunWith(PowerMockRunner.class)
//@PrepareForTest( { System.class, ChunkOut.class })
//
//@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*",
//    "com.sun.org.apache.xerces.*", "ch.qos.logback.*",
//    "org.slf4j.*" })
//public class ChunkOutgoing2Test extends TestCase {
//
//    PeerServer server = mock(PeerServer.class);
//	NameSpace namespace = mock(NameSpace.class);
//    PeerSession peeragent = mock(PeerSession.class);
//
//    MessageSender sender = mock(MessageSender.class);
//    IListener listen = mock(IListener.class);
//
//	Random rnd = new Random();
//	int port=10;
//
//	Socket socket = mock(Socket.class);
//	SocketBufferIn socketbuffer = mock(SocketBufferIn.class);
//	SocketBufferOut bufferout = mock(SocketBufferOut.class);
//	SendStatistics sendstat = mock(SendStatistics.class);
//
//	public void setUp(){
//
//		NetAddress address = new NetAddress("127.0.0.1",200);
//
//        SendStatistics stat = new SendStatistics(address);
//
//        when(server.getServerId()).thenReturn(1);
//        when(peeragent.getMainServ()).thenReturn(server);
//
//        when(peeragent.getPeerAddress()).thenReturn(address);
//
//        when(sender.UDPSendEncrypt_Data((MessageHeader)anyObject())).thenReturn(true);
//        when(sender.UDPSendEncrypt((MessageHeader)anyObject())).thenReturn(true);
//
//        when(namespace.getMessageSender()).thenReturn(sender);
//        when(namespace.getListen()).thenReturn(listen);
//        when(namespace.getMainServ()).thenReturn(server);
//        when(namespace.getMessageSender()).thenReturn(sender);
//
//        when(namespace.getMainServ().getStat((NetAddress)anyObject())).thenReturn(stat);
//
//        when(socket.getNameSpace()).thenReturn(namespace);
//        when(peeragent.isRunning()).thenReturn(true);
//
//        when(bufferout.getStat()).thenReturn(stat);
//
//        when(peeragent.getNameSpace()).thenReturn(namespace);
//        when(namespace.getMonitors()).thenReturn(new ServiceMonitors("dummy"));
//
//        SessionEncrypt sessionEncrypt = new SessionEncrypt(namespace);
//		when(namespace.getSessionEncrypt()).thenReturn(sessionEncrypt);
//
//		PeerSessionEncrypt session = new PeerSessionEncrypt(peeragent);
//		when(peeragent.getSessionEncrypt()).thenReturn(session);
//
//	}
//
//    public void test_open() {
//
//    	ChunkOut chunk = new ChunkOut(socket, bufferout);
//    	assertFalse(chunk.isComplete());
//    	assertEquals(0,chunk.getDatapackets().size());
//    	assertEquals(0,chunk.getChunkNumber());
//
//    }
//
//
//    private ArrayList<ChunkOut> fill_chunks(int numChunks) {
//    	ArrayList<ChunkOut> list = new ArrayList<ChunkOut>();
//		for(int chunknum=1;chunknum<=numChunks;chunknum++){
//
//			ChunkOut chunk = new ChunkOut(socket, bufferout);
//
//			for (int index = 1; index <= InitVar.CHUNK_SIZE; index++) {
//
//				int packetnum = index+((chunknum-1)*InitVar.CHUNK_SIZE);
//				DataPacket packet = new DataPacket();
//				packet.setPort(1);
//				packet.setNumber(packetnum);
//				packet.setTotal(numChunks * InitVar.CHUNK_SIZE);
//				packet.setData(new byte[0]);
//
//				assertTrue("fail add " + String.valueOf(chunknum) + " " + String.valueOf(packet.getNumber()), chunk.addOutgoingPacket(packet));
//
//		    	assertEquals("wrong size " + String.valueOf(chunknum) + " "  + String.valueOf(packet.getNumber()),index,chunk.getDatapackets().size());
//		    	assertEquals("wrong chunk "  + String.valueOf(chunknum) + " " + String.valueOf(packet.getNumber()),chunknum,chunk.getChunkNumber());
//
//		    	// add again
//		    	assertFalse("added twise "  + String.valueOf(chunknum) + " " + String.valueOf(packet.getNumber()), chunk.addOutgoingPacket(packet));
//
//		    	assertEquals("wrong size "  + String.valueOf(chunknum) + " " + String.valueOf(packet.getNumber()),index,chunk.getDatapackets().size());
//		    	assertEquals("wrong chunk "  + String.valueOf(chunknum) + " " + String.valueOf(packet.getNumber()),chunknum,chunk.getChunkNumber());
//
//			}
//
//	    	list.add(chunk);
//
//		}
//		return list;
//
//    }
//
//
//	public void test_addpackets() {
//		fill_chunks(50);
//    }
//
//	public void test_send() {
//
//		ArrayList<ChunkOut> chunklist = fill_chunks(3);
//		assertEquals(3,chunklist.size());
//		int times = 0;
//		for(ChunkOut chunk:chunklist){
//
//			// no timeout
//			assertTrue(chunk.send(11111, mock(NetAddress.class)));
//			times += chunk.getDatapackets().size();
//
//		}
//
//		// verify all packets are sent out
//		verify(sender, times(times)).UDPSendEncrypt_Data((MessageHeader)anyObject());
//
//
//    }
//
//	public void test_reply_complete() {
//
//		ArrayList<ChunkOut> chunklist = fill_chunks(10);
//		for(ChunkOut chunk:chunklist){
//
//			assertFalse(chunk.isComplete());
//
//			MessageHeader datareply = new MessageHeader(MessageHeader.MessageHeaderType.DataReply);
//			datareply.setTransactionID(1);
//            datareply.setAddress(null);
//
//            DataReplyPacket packet = new DataReplyPacket();
//            packet.setMsgId(bufferout.getMsgId());
//            packet.setPort(0);
//            packet.setChunkNumber(chunk.getChunkNumber());
//            packet.setComplete(true);
//
//			chunk.gotReply(datareply, packet);
//
//			assertTrue(chunk.isComplete());
//
//		}
//
//    }
//
//	//Huy: this test is wrong
//	//@Ignore
//	public void test_wrong_chunkid() {
//
//		ArrayList<ChunkOut> chunklist = fill_chunks(10);
//		for(ChunkOut chunk:chunklist){
//
//			assertFalse(chunk.isComplete());
//
//			MessageHeader datareply = new MessageHeader(MessageHeader.MessageHeaderType.DataReply);
//			datareply.setTransactionID(1);
//            datareply.setAddress(null);
//
//            DataReplyPacket packet = new DataReplyPacket();
//            packet.setMsgId(bufferout.getMsgId());
//            packet.setPort(0);
//            packet.setChunkNumber(chunk.getChunkNumber()+1);
//            packet.setComplete(true);
//
//            assertTrue(chunk.gotReply(datareply, packet));
//
//		}
//
//    }
//
//	public void test_wrong_because_complete() {
//
//		ArrayList<ChunkOut> chunklist = fill_chunks(10);
//		for(ChunkOut chunk:chunklist){
//
//			assertFalse(chunk.isComplete());
//
//			MessageHeader datareply = new MessageHeader(MessageHeader.MessageHeaderType.DataReply);
//			datareply.setTransactionID(1);
//            datareply.setAddress(null);
//
//            DataReplyPacket packet = new DataReplyPacket();
//            packet.setMsgId(bufferout.getMsgId());
//            packet.setPort(0);
//            packet.setChunkNumber(chunk.getChunkNumber());
//            packet.setComplete(true);
//
//            assertTrue(chunk.gotReply(datareply, packet));
//			assertTrue(chunk.isComplete());
//
//            assertFalse("is complete",chunk.gotReply(datareply, packet));
//
//		}
//
//    }
//
//
//	public void test_reply_missing() throws Exception {
//
//		byte[] data = new byte[10000];
//    	rnd.nextBytes(data);
//		SocketRetStatus ret = new SocketRetStatus();
//
//		bufferout = new SocketBufferOut(peeragent, ret, socket, port, data);
//
//		for(ChunkOut chunk:bufferout.getChunks_outgoing()){
//
//			int numpackets = chunk.getNumDataPackets();
//
//			if(numpackets>2) {
//
//				MessageHeader datareply = new MessageHeader(MessageHeader.MessageHeaderType.DataReply);
//				datareply.setTransactionID(1);
//	            datareply.setAddress(null);
//
//	            DataReplyPacket packet = new DataReplyPacket();
//	            packet.setMsgId(bufferout.getMsgId());
//	            packet.setPort(0);
//	            packet.setChunkNumber(chunk.getChunkNumber());
//
//	            // two packets are missing
//	            packet.addPacketnum(chunk.getFirstPacketNum());
//	            packet.addPacketnum(chunk.getFirstPacketNum()+1);
//
//	            bufferout.gotDataReply(datareply, packet);
//
//	            assertFalse(chunk.isComplete());
//				assertTrue(numpackets>chunk.getNumDataPackets());
//				assertEquals(2,chunk.getNumDataPackets());
//
//			}
//
//		}
//
//    }
//
//	public void test_new_Version() {
//
//		ArrayList<ChunkOut> chunklist = fill_chunks(10);
//		assertEquals(10,chunklist.size());
//
//		for(ChunkOut chunk:chunklist){
//
//			for(int i=0;i<10;i++){
//
//				ChunkOut chunk_new = chunk.cloneNewVersion();
//				assertEquals(chunk_new.getChunkNumber(),chunk.getChunkNumber());
//				assertEquals(chunk_new.getChunkVersion(),chunk.getChunkVersion()+1);
//
//				for(DataPacket packet:chunk.getDatapackets()){
//
//					boolean found = false;
//					for(DataPacket packet_new:chunk_new.getDatapackets()){
//
//						if(packet_new.getChunkNumber()==packet.getChunkNumber() &&
//							packet_new.getNumber()==packet.getNumber()) {
//							found = true;
//						}
//					}
//					assertTrue(found);
//				}
//				chunk = chunk_new;
//			}
//		}
//    }
//
//}
