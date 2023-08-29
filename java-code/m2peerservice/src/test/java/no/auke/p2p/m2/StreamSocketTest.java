package no.auke.p2p.m2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import no.auke.p2p.m2.StreamSocket.StreamPacket;
import no.auke.p2p.m2.general.IListener;
import no.auke.p2p.m2.general.ReturMessageTypes;
import no.auke.p2p.m2.message.attribute.MessageAttributeInterface.MessageAttributeType;
import no.auke.p2p.m2.message.attribute.MessageAttributeParsingException;
import no.auke.p2p.m2.message.attribute.RawDataPacket;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.message.header.MessageHeaderInterface.MessageHeaderType;
import no.auke.p2p.m2.message.header.MessageHeaderParsingException;
import no.auke.p2p.m2.workers.PeerSession;
import no.auke.p2p.m2.workers.PeerPeerInfo;
import no.auke.p2p.m2.workers.ReConnector;
import no.auke.p2p.m2.workers.keepalive.Peerid;
import no.auke.p2p.m2.workers.message.MessageSender;

//TODO: LHA: make tests 

@PrepareForTest({ StreamSocket.class })
@RunWith(PowerMockRunner.class)
public class StreamSocketTest  {

	Random rnd = new Random();

	int port=10;	
	int remote_port=20;
	
    public NameSpace nameSpace = mock(NameSpace.class);
    public PeerSession session = mock(PeerSession.class);
	public ReConnector reconnector = mock(ReConnector.class);
    
	ExecutorService executor = Executors.newCachedThreadPool();
	
	ConcurrentHashMap<Integer, StreamSocket> streamsockets = new ConcurrentHashMap<Integer, StreamSocket>();
	
	SocketRetStatus ret = new SocketRetStatus();

//	@After
//	public void validate() {
//	    Mockito.validateMockitoUsage();
//	}
	
	@Before
	public void setUp() throws Exception{

		Peerid peerLeif = new Peerid("ns","leif");
		
		when(nameSpace.getMainServ()).thenReturn(mock(PeerServer.class));

		when(reconnector.waitConnect()).thenReturn(true);
		
        when(nameSpace.getThisPeerId()).thenReturn(peerLeif);
        when(nameSpace.getListen()).thenReturn(mock(IListener.class));
        when(nameSpace.isRunning()).thenReturn(true);
        when(nameSpace.isConnected()).thenReturn(true);
        when(nameSpace.doSessionEncryption()).thenReturn(false);
        
        //when(peerservice.getLastretcode()).thenReturn(ReturMessageTypes.ok);
        when(nameSpace.getMainServ().getExecutor()).thenReturn(executor);
        
        when(nameSpace.getKnownPeers()).thenReturn(new HashMap<String,PeerPeerInfo>());
        when(nameSpace.getMessageSender()).thenReturn(mock(MessageSender.class));
    	when(nameSpace.getMessageSender().UDPSendEncrypt_Stream((MessageHeader)anyObject())).thenReturn(true);
    	
    	when(nameSpace.getReConnector()).thenReturn(reconnector);
    	when(nameSpace.getStreamSockets()).thenReturn(streamsockets);

    	ConcurrentHashMap<String, PeerSession> sessionlist = new ConcurrentHashMap<String, PeerSession>();
		when(nameSpace.getOpenPeerSessions()).thenReturn(sessionlist);
		
		session = spy(new PeerSession(nameSpace,new Peerid("ns","leif")));
		
		when(session.getNameSpace()).thenReturn(nameSpace);
		assertNotNull(session.getNameSpace());
		assertFalse(session.getNameSpace().doSessionEncryption());
		
    	when(session.isRunning()).thenReturn(true);
    	when(session.isConnected()).thenReturn(true);

    	ret.setPeerAgent(session);
        
	}
	
	@After
	public void tearDown() {
		executor.shutdownNow();
	}
	
	@Test
    public void test_stream_messagetype() throws MessageHeaderParsingException, MessageAttributeParsingException {
    	
    	
    	final byte[] data = new byte[100];
    	(new Random()).nextBytes(data);
    	
		MessageHeader message1 = new MessageHeader(MessageHeader.MessageHeaderType.StreamData);
		message1.setTransactionID(3);

		RawDataPacket packet1 = new RawDataPacket();
        packet1.setPort(2);
        packet1.setSeqNumber(3);
        packet1.setData(data);
        
        message1.addMessageAttribute(packet1);
        assertNotNull(message1.getMessageAttribute(MessageAttributeType.RawData));
        
        MessageHeader message2 = MessageHeader.parseHeader(message1.getBytes());
        message2.parseAttributes(message1.getBytes());
    	
        RawDataPacket packet2 = (RawDataPacket) message2.getMessageAttribute(MessageAttributeType.RawData);
        assertNotNull(packet2);
    	
    }
    
	@Test
    public void test_open_fixed_recipeint() {
    	
    	StreamSocket socket = spy(new StreamSocket(nameSpace, port));

    	assertTrue(socket.isOpen());
    	assertEquals(port,socket.getPort());
		assertEquals(nameSpace,socket.getNameSpace());
		
		
    }

	@Test
    public void test_PeerPeerAgent_socket_close() {
    	
        StreamSocket socket = spy(new StreamSocket(nameSpace, 100));
        //assertEquals(1,peeragent.getStreamSockets().size());
        assertTrue(socket.isOpen());
        socket.close();
        assertFalse(socket.isOpen());
        //assertEquals(0,peeragent.getStreamSockets().size());
        
    }     
    
	@Test
    public void test_socket_gotdata() {
    
 		StreamSocket socket = new StreamSocket(nameSpace, 10);
 		assertEquals(10, socket.getPort());
 		
    	for(int i=0;i<100;i++) {
    		
        	byte[] data = new byte[1000];
        	(new Random()).nextBytes(data);
        	
            RawDataPacket packet = new RawDataPacket();
            packet.setPort(socket.getPort());
            packet.setSeqNumber(i);
            packet.setData(data);
            
            socket.gotData(session,0,packet);
            
            StreamPacket buff = socket.getInbuff().poll();
            
            assertTrue(Arrays.equals(data, buff.getData()));
            assertEquals(packet.getSeqNumber(), buff.getSeqNumber());
    		
    	}
        assertEquals(0, socket.getInbuff().size());
        
    }    

	@Test
    public void test_socket_readdata() {

        StreamSocket socket = spy(new StreamSocket(nameSpace, 100));
        
    	List<byte[]> bytelist = new ArrayList<byte[]>();
    	
    	for(int i=0;i<100;i++) {
    		
        	byte[] data = new byte[100*i];
        	(new Random()).nextBytes(data);
        	
        	bytelist.add(data);
            
            RawDataPacket packet = new RawDataPacket();
            packet.setPort(socket.getPort());
            packet.setSeqNumber(i);
            packet.setData(data);
            
            socket.gotData(session,0,packet);
    	
    	}
    	
    	for(int i=0;i<100;i++) {
    		
            StreamPacket buff = socket.ReadBuffer(0);
            assertTrue(nameSpace.getThisPeerId().equals(buff.getRemotePeerId()));
            
            assertEquals(i,buff.getSeqNumber());
            assertTrue(Arrays.equals(bytelist.get(i), buff.getData()));
    	
    	}
        assertEquals(0, socket.getInbuff().size());
        
    }    
    
	@Test
    public void test_socket_readdata_not_open() {

        StreamSocket socket = spy(new StreamSocket(nameSpace, 100));
        doReturn(false).when(socket).isOpen();
        
      	byte[] data = new byte[100];
    	(new Random()).nextBytes(data);
    	
        RawDataPacket packet = new RawDataPacket();
        packet.setPort(socket.getPort());
        packet.setSeqNumber(0);
        packet.setData(data);
       
        socket.gotData(session,0,packet);
        assertEquals(0, socket.getInbuff().size());
        assertNull(socket.ReadBuffer(0));
        
        
    }     
        
    
	@Test
    public void test_send() {
    	
    	StreamSocket socket = new StreamSocket(nameSpace, 100);
    	byte[] data = new byte[InitVar.STREAM_MAX_DATA_LENGTH];
    	(new Random()).nextBytes(data);
        assertEquals(ReturMessageTypes.ok,socket.send(ret, 0, 0, data).getLastRetcode());
    	verify(nameSpace.getMessageSender(),times(1)).UDPSendEncrypt_Stream((MessageHeader)anyObject());
        
    }     
    
	@Test
    public void test_do_not_send_to_big_data() {
    	
    	StreamSocket socket = spy(new StreamSocket(nameSpace,100));
 
    	byte[] data = new byte[InitVar.STREAM_MAX_DATA_LENGTH+1];
    	(new Random()).nextBytes(data);

    	assertEquals(ReturMessageTypes.data_exceed_size,socket.send(ret, 0, 0, data).getLastRetcode());
        
    }     

	@Test
	public void test_do_not_send_not_running() {

    	StreamSocket socket = spy(new StreamSocket(nameSpace, 100));
    	when(nameSpace.isRunning()).thenReturn(false);
    	assertEquals(ReturMessageTypes.service_not_running,socket.send(ret, 0,0,"asdasd".getBytes()).getLastRetcode());
        
    } 	
    
	@Test
    @SuppressWarnings("rawtypes")
	public void test_socket_send_capture_message() {
    	
    	StreamSocket socket = new StreamSocket(nameSpace, 100);

    	final byte[] data = new byte[InitVar.STREAM_MAX_DATA_LENGTH];
    	(new Random()).nextBytes(data);
    	
    	final AtomicInteger num_messages=new AtomicInteger();

    	when(nameSpace.getMessageSender().UDPSendEncrypt_Stream((MessageHeader)anyObject())).thenAnswer(new Answer(){

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
		
				MessageHeader dataMH = (MessageHeader)invocation.getArguments()[0];
				assertEquals(MessageHeader.MessageHeaderType.StreamData,dataMH.getType());
				 
				RawDataPacket packet = (RawDataPacket) dataMH.getMessageAttribute(MessageAttributeType.RawData);
				assertNotNull(packet);
				
				assertTrue(Arrays.equals(data, packet.getData()));
				
				num_messages.incrementAndGet();
				
				return true;
		
		}});

        assertEquals(ReturMessageTypes.ok,socket.send(ret, 0, 0, data).getLastRetcode());
        assertEquals(1,num_messages.get());
    }     
	
	@Test
	public void test_PeerPeerAgent_openStream_gotdata_correct() {

		StreamSocket socket = spy(new StreamSocket(nameSpace, 100));
		streamsockets.put(100, socket);
		
		MessageHeader receiveMH = new MessageHeader(MessageHeaderType.StreamData);
		receiveMH.setTransactionID(socket.getId());

		RawDataPacket packet = new RawDataPacket();
		packet.setPort(socket.getPort());

		receiveMH.addMessageAttribute(packet);
		
		session.gotStreamData(receiveMH);
		
		verify(socket, times(1)).gotData((PeerSession)anyObject(), anyInt(), (RawDataPacket) anyObject());		
		verify(session, times(0)).sendMessage((MessageHeader) anyObject());

	}
	
	
	@Test
	public void test_PeerPeerAgent_openStream_gotdata_wrong_port() {

		StreamSocket socket = spy(new StreamSocket(nameSpace, 100));
		streamsockets.put(100, socket);

		MessageHeader receiveMH = new MessageHeader(MessageHeaderType.StreamData);
		receiveMH.setTransactionID(socket.getId());

		RawDataPacket packet = new RawDataPacket();
		packet.setPort(0);

		receiveMH.addMessageAttribute(packet);
		session.gotStreamData(receiveMH);
		
		verify(socket, times(0)).gotData((PeerSession)anyObject(), anyInt(), (RawDataPacket) anyObject());		
		verify(session, times(1)).sendMessage((MessageHeader) anyObject());

	}

	// LHA: new testes for sync on socket
	
	@Test
    @SuppressWarnings("rawtypes")
	public void test_socket_send_capture_message_sequence() {
    	
    	StreamSocket socket = spy(new StreamSocket(nameSpace, 100));

    	final byte[] data = new byte[InitVar.STREAM_MAX_DATA_LENGTH];
    	(new Random()).nextBytes(data);

    	final AtomicInteger num_messages=new AtomicInteger();
    	
    	when(nameSpace.getMessageSender().UDPSendEncrypt_Stream((MessageHeader)anyObject())).thenAnswer(new Answer(){

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
		
				MessageHeader dataMH = (MessageHeader)invocation.getArguments()[0];
				RawDataPacket packet = (RawDataPacket) dataMH.getMessageAttribute(MessageAttributeType.RawData);
				assertEquals(num_messages.get(),packet.getSeqNumber());
				assertEquals(0,packet.getFrequency());
				num_messages.incrementAndGet();
				
				return true;
		
		}});
    	
    	for(int i=0;i<100;i++) {
    		assertEquals(ReturMessageTypes.ok,socket.send(ret,100,0, data).getLastRetcode());
    	}

    	assertEquals(100,num_messages.get());
    	
    } 	
	    
	@Test
    public void test_socketlistener_readdata() {
    	
    	final List<byte[]> bytelist = new ArrayList<byte[]>();
    	final AtomicInteger num_messages=new AtomicInteger();
    	
    	StreamSocketListener listen = new StreamSocketListener() {

			@Override
			public void onIncomming(StreamPacket buffer) {
				assertTrue(nameSpace.getThisPeerId().equals(buffer.getRemotePeerId()));
				assertTrue(Arrays.equals(bytelist.get((int) buffer.getSeqNumber()), buffer.getData()));
				num_messages.incrementAndGet();
			}

			@Override
			public void onNoData() {}
    		
    		
    	};

    	
        StreamSocket socket = spy(new StreamSocket(nameSpace, 100));
        doReturn(true).when(socket).isOpen();
        

        // start listener 
        
        socket.setListener(listen);
        
        try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
        
        assertTrue(socket.isOpen());
        assertTrue(socket.getNameSpace().isRunning());
        
        // send data 
    	
    	for(int i=0;i<100;i++) {
    		
        	byte[] data = new byte[100*i];
        	(new Random()).nextBytes(data);
        	
        	bytelist.add(data);
            socket.gotData(session,0,new RawDataPacket(socket.getPort(),i,10*1000000,data,(short)0));
    	
    	}
    	
        try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}    	
    	assertEquals(100,num_messages.get());
    	
    }      
   	
        
}
