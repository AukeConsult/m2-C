//package no.auke.p2p.m2.sockets.udp;
//
//import static org.mockito.Matchers.*;
//import static org.powermock.api.mockito.PowerMockito.spy;
//import static org.powermock.api.mockito.PowerMockito.when;
//import static org.powermock.api.mockito.PowerMockito.mock;
//import static org.powermock.api.mockito.PowerMockito.doReturn;
//
//
////import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.atLeast;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.List;
//import java.util.Random;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import org.junit.Ignore;
//import org.junit.runner.RunWith;
////import org.mockito.invocation.InvocationOnMock;
////import org.mockito.stubbing.Answer;
////import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PowerMockIgnore;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//import org.powermock.reflect.Whitebox;
//
//import no.auke.m2.task.ServiceMonitors;
//import no.auke.p2p.m2.InitVar;
//import no.auke.p2p.m2.NameSpace;
//import no.auke.p2p.m2.PeerServer;
//import no.auke.p2p.m2.Socket;
//import no.auke.p2p.m2.SocketRetStatus;
//import no.auke.p2p.m2.general.IListener;
//import no.auke.p2p.m2.message.attribute.DataPacket;
//import no.auke.p2p.m2.message.attribute.DataReplyPacket;
//import no.auke.p2p.m2.message.header.MessageHeader;
//import no.auke.p2p.m2.sockets.udp.ChunkOut;
//import no.auke.p2p.m2.sockets.udp.SocketBuffer;
//import no.auke.p2p.m2.sockets.udp.SocketBufferOut;
//import no.auke.p2p.m2.sockets.udp.SocketBufferOut;
//import no.auke.p2p.m2.workers.ComChannel;
//import no.auke.p2p.m2.workers.PeerSession;
//import no.auke.p2p.m2.workers.connect.PeerSessionEncrypt;
//import no.auke.p2p.m2.workers.connect.SessionEncrypt;
//import no.auke.p2p.m2.workers.keepalive.NetAddress;
//import no.auke.p2p.m2.workers.message.MessageSender;
//import no.auke.util.ByteUtil;
//import junit.framework.TestCase;
//
//@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*",
//    "com.sun.org.apache.xerces.*", "ch.qos.logback.*",
//    "org.slf4j.*" })
//
//@RunWith(PowerMockRunner.class)
//@PrepareForTest( {System.class, ChunkOut.class, SocketBufferOut.class, SocketBufferOut.class, SocketBuffer.class })
//
//public class SocketBufferOutgoing2Test extends TestCase {
//
//    NameSpace netspace = mock(NameSpace.class);
//    PeerSession peeragent = mock(PeerSession.class);
//    SendStatistics stat = new SendStatistics(mock(NetAddress.class));
//
//    MessageSender sender = mock(MessageSender.class);
//    IListener listen = mock(IListener.class);
//
//	Random rnd = new Random();
//	int port=10;
//
//	ExecutorService executor = Executors.newCachedThreadPool();
//
//	//Socket socket=null;
//	int OLD_SEND_ABORT_TIMEOUT=InitVar.SEND_ABORT_TIMEOUT;
//    int OLD_SEND_RESEND_TIMEOUT=InitVar.SEND_RESEND_TIMEOUT;
// 	int OLD_MAX_SPEED = InitVar.MAX_SPEED;
//
//	private SocketBufferOut initBuffer(Socket socket, int speed, int abort_timeout, int resend_timeout) throws Exception {
//
//		InitVar.MAX_SPEED=speed;
//		InitVar.SEND_ABORT_TIMEOUT=abort_timeout;
//		InitVar.SEND_RESEND_TIMEOUT=resend_timeout;
//
//    	byte[] data = new byte[100000 + rnd.nextInt(100000)];
//    	rnd.nextBytes(data);
//
//    	SocketBufferOut buffer = new SocketBufferOut(peeragent, new SocketRetStatus(), socket, port, data);
//
//    	assertTrue("buffer not empty",buffer.getChunks_outgoing().size()>0);
//    	return buffer;
//
//	}
//
//	public void setUp(){
//
//        when(netspace.getMainServ()).thenReturn(mock(PeerServer.class));
//
//        when(netspace.getMainServ().getStat((NetAddress)anyObject())).thenReturn(stat);
//
//		when(netspace.getListen()).thenReturn(listen);
//        when(netspace.getMainServ().getExecutor()).thenReturn(executor);
//
//        when(netspace.getMessageSender()).thenReturn(sender);
//
//        when(sender.UDPSendEncrypt_Data((MessageHeader)anyObject())).thenReturn(true);
//        when(sender.UDPSendEncrypt((MessageHeader)anyObject())).thenReturn(true);
//
//        NetAddress address = new NetAddress("127.0.0.1",200);
//        when(peeragent.getPeerAddress()).thenReturn(address);
//        when(peeragent.getNameSpace()).thenReturn(netspace);
//        when(peeragent.getMainServ()).thenReturn(mock(PeerServer.class));
//        when(peeragent.getMainServ().getServerId()).thenReturn(1);
//
//
//        //socket = spy(new Socket(port,peerservice, 2));
//        when(netspace.getMonitors()).thenReturn(new ServiceMonitors("dummy"));
//
//		when(peeragent.isConnected()).thenReturn(true);
//		when(peeragent.isRunning()).thenReturn(true);
//		//when(peeragent.tryLockFind()).thenReturn(true);
//
//		SessionEncrypt sessionEncrypt = new SessionEncrypt(netspace);
//		when(netspace.getSessionEncrypt()).thenReturn(sessionEncrypt);
//
//		PeerSessionEncrypt session = new PeerSessionEncrypt(peeragent);
//		when(peeragent.getSessionEncrypt()).thenReturn(session);
//
//	}
//
//	public void tearDown(){
//		InitVar.SEND_ABORT_TIMEOUT=OLD_SEND_ABORT_TIMEOUT;
//        InitVar.SEND_RESEND_TIMEOUT=OLD_SEND_RESEND_TIMEOUT;
//    	InitVar.MAX_SPEED = OLD_MAX_SPEED;
//		executor.shutdownNow();
//
//	}
//    public void test_chunk_order() throws Exception {
//    	SocketBufferOut buffer = new SocketBufferOut(peeragent, new SocketRetStatus(), spy(new Socket(port,netspace)), port, new byte[0]);
//    	assertFalse(buffer.send());
//    }
//    public void test_send_nothing() throws Exception {
//    	SocketBufferOut buffer = new SocketBufferOut(peeragent, new SocketRetStatus(), spy(new Socket(port,netspace)), port, new byte[0]);
//    	assertFalse(buffer.send());
//    }
//    public void test_data_packets_in_order() throws Exception {
//    	for(int i=0;i<100;i++){
//    		SocketBufferOut buffer = initBuffer(spy(new Socket(port,netspace)),0,0,0);
//        	int cnt=1;
//        	for(DataPacket packet:buffer.getDatapackets()){
//        		assertEquals("data packets created in order",cnt,packet.getNumber());
//        		cnt++;
//        	}
//    	}
//    }
//    public void test_data_correct() throws Exception {
//    	for(int i=0;i<100;i++){
//    		byte[] data = new byte[1 + rnd.nextInt(100000)];
//        	rnd.nextBytes(data);
//        	SocketBufferOut buffer = new SocketBufferOut(peeragent, new SocketRetStatus(), spy(new Socket(port,netspace)), port, data);
//        	List<byte[]> listedata = new ArrayList<byte[]>();
//        	for(DataPacket packet:buffer.getDatapackets()){
//        		listedata.add(packet.getData());
//        	}
//        	byte[] databytes =  ByteUtil.mergeBytes(listedata);
//    		assertTrue("data is correct ",Arrays.equals(data, databytes));
//    	}
//    }
//
//    public void test_init_chunknumber() throws Exception {
//    	int port=10;
//    	for(int i=1;i<=100;i++){
//    		byte[] data = new byte[rnd.nextInt(100000)+1];
//        	rnd.nextBytes(data);
//        	int chunknum=1;
//        	final SocketBufferOut buffer = new SocketBufferOut(peeragent, new SocketRetStatus(), spy(new Socket(port,netspace)), port, data);
//        	for(ChunkOut chunk:buffer.getChunks_outgoing()) {
//        		assertEquals("chunk number",chunknum, chunk.getChunkNumber());
//        		chunknum++;
//        	}
//    	}
//    }
//
//    public void test_gotDataReply() throws Exception {
//
//    	int port=10;
//
//    	Socket socket = new Socket(port,netspace);
//    	NetAddress address = mock(NetAddress.class);
//
//    	for(int i=0;i<3;i++){
//
//        	byte[] data = new byte[rnd.nextInt(100000)];
//        	rnd.nextBytes(data);
//
//        	SocketBufferOut buffer = spy(new SocketBufferOut(peeragent, new SocketRetStatus(), socket, port, data));
//
//        	socket.addOutBuffer(buffer);
//
//        	assertTrue(buffer.getChunks_outgoing().size()>0);
//
//        	for(DataPacket packet:buffer.getDatapackets()){
//
//    			MessageHeader datareply = new MessageHeader(MessageHeader.MessageHeaderType.DataReply);
//    			datareply.setTransactionID(buffer.getTranactionId());
//                datareply.setAddress(address);
//
//                DataReplyPacket reply2 = new DataReplyPacket();
//    			reply2.setPort(socket.getPort());
//    			reply2.setChunkNumber(packet.getChunkNumber());
//
//    	        datareply.addMessageAttribute(reply2);
//    	        buffer.gotDataReply(datareply, reply2);
//
//        	}
//        	assertTrue(buffer.getChunks_outgoing().size()==0);
//
//
//    	}
//
//    }
//
//    public void test_send_bigdata() throws Exception {
//
//    	int port=10;
//
//    	final NetAddress address = mock(NetAddress.class);
//
//    	for(int i=1;i<=2;i++){
//
//     		int size=2000*i;
//
//    		byte[] data = new byte[size];
//        	rnd.nextBytes(data);
//
//        	final Socket socket = spy(new Socket(port,netspace));
//        	final SocketBufferOut buffer = new SocketBufferOut(peeragent, new SocketRetStatus(), socket, port, data);
//
//        	final AtomicBoolean prosessing=new AtomicBoolean();
//        	prosessing.set(true);
//
//        	// send in a thread
//        	netspace.getMainServ().getExecutor().execute(new Runnable(){
//
//				@Override
//				public void run() {
//
//		        	while(prosessing.get()){
//
//		             	final Collection<ChunkOut> chunks_for_reply = buffer.getChuncksNotReplied();
//
//			        	// send reply for each chunk
//			        	for(ChunkOut chunk:chunks_for_reply) {
//
//			        		assertNotNull(chunk);
//
//			        		assertTrue("chunk number > 0",chunk.getChunkNumber()>0);
//
//			    			MessageHeader datareply = new MessageHeader(MessageHeader.MessageHeaderType.DataReply);
//			    			datareply.setTransactionID(buffer.getTranactionId());
//			                datareply.setAddress(address);
//
//			                DataReplyPacket reply2 = new DataReplyPacket();
//			                reply2.setMsgId(buffer.getMsgId());
//			    			reply2.setPort(socket.getPort());
//			    			reply2.setComplete(true);
//			    			reply2.setChunkNumber(chunk.getChunkNumber());
//			    	        datareply.addMessageAttribute(reply2);
//
//			    	        buffer.gotDataReply(datareply, reply2);
//
//
//			        	}
//
//			        	try {
//							Thread.sleep(100);
//						} catch (InterruptedException e) {}
//
//
//		        	}
//				}
//        	});
//
//        	//System.out.println("send " + String.valueOf(i) + " size " + String.valueOf(size));
//        	if(!buffer.send()){
//            	fail(buffer.getSocketRetStatus().getLastMessage());
//        	}
//        	prosessing.set(false);
//
//    	}
//
//
//
//    }
//
////    public void test_socket_speed() throws InterruptedException {
////
////    	int port=10;
////
////    	final NetAddress address = mock(NetAddress.class);
////
////    	for(int speed=50;speed<=100;speed+=10){
////
////     		int size=10000;
////
////    		System.out.println(speed);
////
////    		byte[] data = new byte[size];
////        	rnd.nextBytes(data);
////
////        	int abort_timeout=1000;
////        	int retry_timeout=1000;
////
////
////        	socket.resetStatistics(); // NB start calculation fresh with new statistics
////
////        	final SocketBufferOutgoing2 buffer = new SocketBufferOutgoing2(peeragent, socket, port, data,speed,abort_timeout,retry_timeout);
////
////        	final AtomicBoolean prosessing=new AtomicBoolean();
////        	prosessing.set(true);
////
////        	// send in a thread
////        	executor.execute(new Runnable(){
////
////				@Override
////				public void run() {
////
////		        	while(prosessing.get()){
////
////		             	final ArrayList<ChunkOutgoing2> chunks_for_reply = buffer.getChunkNotReplied();
////
////		             	// send reply for each chunk
////			        	for(ChunkOutgoing2 chunk:chunks_for_reply){
////
////			    			MessageHeader datareply = new MessageHeader(MessageHeader.MessageHeaderType.DataReply);
////			    			datareply.setTransactionID(buffer.getMessageId());
////			                datareply.setAddress(address);
////
////			                DataReplyPacket reply2 = new DataReplyPacket();
////			    			reply2.setPort(socket.getPort());
////			    			reply2.setComplete(true);
////			    			reply2.setChunkNumber(chunk.getChunkNumber());
////
////			    	        datareply.addMessageAttribute(reply2);
////
////			    	        assertTrue("find packet " + String.valueOf(chunk.getChunkNumber()) + " of " + String.valueOf(chunks_for_reply.size()), buffer.gotDataReply(datareply, reply2));
////
////			        	}
////
////
////		        	}
////
////
////				}
////
////        	});
////
////        	if(!buffer.send()){
////
////            	fail("send failed ");
////
////        	}
////        	prosessing.set(false);
////
////        	// assertTrue("correct speed limits for speed " + String.valueOf(speed), buffer.getStat().getRealSpeed()<=speed && buffer.getStat().getRealSpeed()>speed*0.4);
////
////    	}
////
////
////    }
//
//	@Ignore
//	public void test_checkTimouts_all_timeout() {
//
//
//    	try {
//
//    		final Socket socket = spy(new Socket(port,netspace));
//
//    		byte[] data = new byte[rnd.nextInt(1000000)];
//        	rnd.nextBytes(data);
//
//        	SocketBufferOut buffer = spy(new SocketBufferOut(peeragent, new SocketRetStatus(), socket, port, data));
//        	socket.addOutBuffer(buffer);
//
//        	assertTrue(buffer.getChunks_outgoing().size()>0);
//    		doReturn(true).when(buffer).isSending();
//			doReturn(10000).when(socket).getCheckWait();
//	    	Whitebox.invokeMethod(buffer,"runCheck");
//
//        	assertEquals("num packets correct ", buffer.getChunks_outgoing().size(), buffer.getNumpackets());
//
//        	// spy the packets
//    		buffer.send();
////			ArrayList<ChunkOutgoing2> chunks = new ArrayList<ChunkOutgoing2>();
////	    	for(ChunkOutgoing2 chunk:buffer.getChunks_outgoing()){
////
////	    		ChunkOutgoing2 spychunk = spy(chunk);
////	    		doReturn(false).when(spychunk).isComplete();
////	    		doReturn(false).when(spychunk).doResend(anyInt());
////	    		doReturn(true).when(spychunk).doAbort(anyInt(),anyInt());
////	    		chunks.add(spychunk);
////
////	    		// simulate send
////	    		//Whitebox.invokeMethod(buffer,"setChunkSent",spychunk);
////
////	    	}
////
////	    	doReturn(chunks).when(buffer).getChuncksNotReplied();
//	    	//assertEquals("all chunks ready to check", chunks.size(), buffer.getNumChunkNotReplied());
//
//	    	int wait=0;
//	    	while(wait<=InitVar.SEND_ABORT_TIMEOUT +100 && buffer.getNumChunkNotReplied()>0){
//		        //Whitebox.invokeMethod(buffer,"checkTimouts");
//	    		Thread.sleep(100);
//	    		wait+=100;
//	    	}
//
//	    	//verify(buffer, atLeast(1)).doCheckTimeout();
//	    	assertTrue("is fail", buffer.isFailed());
//	    	assertEquals("all chunks are checked", 0, buffer.getNumChunkNotReplied());
//
//    	} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//
//	}
//
//	//Huy, ignore because this is not clear
//	@Ignore
//	public void test_checkTimouts_resend() {
//
//		try {
//
//    		final Socket socket = spy(new Socket(port,netspace));
//			// complete and remove buffer when all complete
//			SocketBufferOut buffer = this.initBuffer(socket, 500,0, 0);
//	    	assertTrue("chunks to send", buffer.getChunks_outgoing().size()>0);
//	    	doReturn(true).when(buffer).isSending();
//	    	doReturn(10000).when(socket).getCheckWait();
//
//	        Whitebox.invokeMethod(buffer,"checkTimouts");
//
//			ArrayList<ChunkOut> chunks = new ArrayList<ChunkOut>();
//			for(ChunkOut chunk:buffer.getChunks_outgoing()){
//
//				if(chunk!=null) {
//
//					ChunkOut spychunk = spy(chunk);
//		    		doReturn(false).when(spychunk).isComplete();
//		    		doReturn(false).when(spychunk).doAbort(anyInt(),anyInt());
//		    		doReturn(true).when(spychunk).doResend(anyInt());
//		    		chunks.add(spychunk);
//
//		    		// simulate send
//		    		Whitebox.invokeMethod(buffer,"setChunkSent",spychunk);
//
//				}
//
//	    	}
//
//	    	doReturn(chunks).when(buffer).getChuncksNotReplied();
//	    	assertEquals("chunks correct", chunks.size(), buffer.getChunks_outgoing().size());
//
//	    	assertEquals("all chunks ready to check", chunks.size(), buffer.getNumChunkNotReplied());
//	    	assertEquals("all chunks ready to check sorted", chunks.size(), buffer.getChuncksNotReplied().size());
//
//	    	assertEquals("all packets in send buffer", chunks.size(), buffer.getSendQueue().size());
//	    	assertEquals("no packets in resend buffer", 0, buffer.getResendQueue().size());
//
//
//	    	int wait=0;
//	    	while(wait<10000 && buffer.getNumChunkNotReplied()>0){
//		        Whitebox.invokeMethod(buffer,"checkTimouts");
//	    		Thread.sleep(100);
//	    		wait+=100;
//	    	}
//
//			verify(buffer, atLeast(1)).doCheckTimeout();
//	    	assertEquals("all chunks are checked", 0, buffer.getNumChunkNotReplied());
//	    	assertEquals("packets are in resend buffer", chunks.size(), buffer.getResendQueue().size());
//
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//
//	}
//
//
//
//	// -----------------------
//	// send sending when UDP error
//
//    public void test_send_udp_error() throws Exception {
//
//
//		final Socket socket = spy(new Socket(port,netspace));
//    	final SocketBufferOut buffer = initBuffer(socket,500,0,0);
//
//    	// -----------------
//    	// set UDP error
//    	when(sender.UDPSendEncrypt_Data((MessageHeader)anyObject())).thenReturn(false);
//
//
//        assertFalse(buffer.send());
//
//
//    }
//
//    //the test is not clear!
//    //@Ignore
//    public void test_send_getreply() {
//
//
//    	final AtomicInteger cnt= new AtomicInteger();
//
//    	for(int timeout_packet=100;timeout_packet>10;timeout_packet-=10){
//
//    		final int t_packet = timeout_packet;
//        	final AtomicBoolean prosessing=new AtomicBoolean();
//        	prosessing.set(true);
//
//    		executor.execute(new Runnable(){
//
//				@Override
//				public void run() {
//
//					final Socket socket = spy(new Socket(port,netspace));
//
//	            	final SocketBufferOut buffer;
//					try {
//
//						buffer = initBuffer(socket,500,10000,t_packet);
//		            	assertEquals(buffer.getChunks_outgoing().size(),buffer.getSendQueue().size());
//
//		            	// send in a thread
//		            	executor.execute(new Runnable(){
//
//							@Override
//							public void run() {
//								buffer.send();
//				            	assertTrue("buffer closed " + String.valueOf(cnt.get()), buffer.isClosed());
//				               	prosessing.set(false);
//
//							}
//
//		            	});
//
//		            	// reply thread
//		            	executor.execute(new Runnable(){
//
//							@Override
//							public void run() {
//
//								while(prosessing.get()){
//
//									try {
//										Thread.sleep(100);
//									} catch (InterruptedException e) {
//									}
//
//									for(ChunkOut chunk:buffer.getChuncksNotReplied()) {
//
//								        if(!chunk.isComplete()) { // only reply once
//
//											MessageHeader datareply = new MessageHeader(MessageHeader.MessageHeaderType.DataReply);
//											datareply.setTransactionID(buffer.getTranactionId());
//
//											// TODO: LHA: implement data reply packet
//											// if empty data reply = complete
//
//								            DataReplyPacket reply2 = new DataReplyPacket();
//								            reply2.setMsgId(buffer.getMsgId());
//											reply2.setPort(socket.getPort());
//											reply2.setChunkNumber(chunk.getChunkNumber());
//
//								        	datareply.addMessageAttribute(reply2);
//								        	buffer.gotDataReply(datareply, reply2);
//
//								        	System.out.println(buffer.getMsgId() + " reply chunk " + String.valueOf(chunk.getChunkNumber()) + " rest " + buffer.getChuncksNotReplied().size());
//
//											//Thread.yield();
//
//								        }
//									}
//								}
//
//								assertTrue(buffer.isClosed());
//
//
//							}
//
//		            	});
//
//
//					} catch (Exception e1) {
//						fail(e1.getMessage());
//					}
//
//
//				}
//
//    		});
//
//        	// make sure test dont run to long
//        	int wait=0;
//        	while(wait<30000 && prosessing.get()){
//        		try {
//					Thread.sleep(100);
//					wait+=100;
//				} catch (InterruptedException e) {
//				}
//
//        	}
//        	assertFalse("still running send "  + String.valueOf(cnt.get()), prosessing.get());
//        	cnt.incrementAndGet();
//
//        }
//
//
//    }
//
//
//}
