package no.auke.p2p.m2.workers;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;



import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import no.auke.m2.encryption.EncryptFactory;
import no.auke.p2p.m2.InitVar;
import no.auke.p2p.m2.NameSpace;
import no.auke.p2p.m2.PeerServer;
import no.auke.p2p.m2.general.IListener;
import no.auke.p2p.m2.message.attribute.DataPacket;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.workers.io.PacketChannel;
import no.auke.p2p.m2.workers.io.PacketChannelUDP;
import no.auke.p2p.m2.workers.keepalive.NetAddress;
import no.auke.util.Lock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ComChannel.class})

@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*",
    "com.sun.org.apache.xerces.internal.jaxp.*", "ch.qos.logback.*",
    "org.slf4j.*" })

public class ComChannelTest {

	PeerServer server;
	NameSpace nameSpace;
	ComChannel channel;
	EncryptFactory encrypter = new EncryptFactory();
	ExecutorService executor = Executors.newCachedThreadPool();
	
    // TODO: LHA
    // upgrade tests later
    
	@Before
	public void setUp() throws IOException {

        nameSpace = mock(NameSpace.class);

        server = mock(PeerServer.class);
        when(server.getListen()).thenReturn(mock(IListener.class));
        
        
        when(nameSpace.getMainServ()).thenReturn(server);
        when(nameSpace.getMainServ().getExecutor()).thenReturn(executor);

        //when(nameSpace.getMainServ().getEncrypt()).thenReturn(encrypter);
        //assertEquals(nameSpace.getMainServ().getEncrypt(),encrypter);
        
        when(nameSpace.getMainServ().getLocaladdress()).thenReturn(new NetAddress("127.0.0.1",1));
        
        when(nameSpace.getListen()).thenReturn(mock(IListener.class));
        when(nameSpace.isConnected()).thenReturn(true);
        when(nameSpace.isRunning()).thenReturn(true);
        
        channel = spy(new ComChannel(server, "127.0.0.1", 8434, 100,100,0));
        channel.startChannel(nameSpace);
        assertTrue(channel.isRunning());
    	

	}
	
	@After
	public void end() throws IOException {
    	channel.closeChannel();
    	assertFalse(channel.isRunning());
	}
	
	private MessageHeader getMessage() {
		
		DataPacket packet = new DataPacket();
		packet.setPort(0);
		packet.setNumber(1);
		packet.setTotal(1);
		packet.setData(new byte[512]);
		
		MessageHeader dataMH = new MessageHeader(MessageHeader.MessageHeaderType.Data);
		dataMH.setTransactionID(22);
		dataMH.addMessageAttribute(packet);
		dataMH.setAddress(new NetAddress("127.0.0.1",22));
		
		return dataMH;
	}

    @Test
    public void test_send_stream_packets() throws Exception {
    	for(int i=0;i<200;i++){
    		assertTrue(channel.UDPSendEncrypt_Stream(getMessage(),0));
    	}
    	Thread.sleep(2000);
    	assertEquals(200,channel.getNumAdded());
    	
    }
    
    @Test
    public void test_send_data_packets() throws Exception {
    	for(int i=0;i<200;i++){
    		assertTrue(channel.UDPSendEncrypt_Data(getMessage(),0));
    	}
    	Thread.sleep(2000);
    	assertEquals(200,channel.getNumOutGoing());
    }


	@Test
    public void test_send_ping_packets() throws Exception {

        when(nameSpace.isRunning()).thenReturn(true);
    	for(int i=0;i<200;i++){
    		channel.UDPSendEncrypt(getMessage(),0);
    	}
    	Thread.sleep(2000);
    	assertEquals(200,channel.getNumOutGoing());
    	
    }
	
	@Test
    public void test_send_all_packets() throws Exception {
		Thread.sleep(100);
    	for(int i=0;i<1000;i++){    		
    		assertTrue(channel.UDPSendEncrypt_Stream(getMessage(),0));
    	}
    	assertEquals(1000,channel.getNumAdded());
    	Thread.sleep(1000);
    	assertEquals(1000,channel.getNumOutGoing());

    }	
	
    @Test
    public void test_read_wait() throws Exception {
    	Thread.sleep(2000);
    	assertEquals(0,channel.getPriorityQueue().getInBuffSize());
    }	
    
    @Test
    public void test_start_stop() throws Exception {
    	
    	final Random rnd = new Random();
    	final AtomicInteger numrun = new AtomicInteger();
    	
    	for(int i=1;i<10;i++) {
    		Thread.sleep(rnd.nextInt(10));
    		executor.execute(new Runnable() {
    			int cnt=0;
				@Override
				public void run() {
					numrun.incrementAndGet();
					while(cnt<5) {
						doRestart();
						cnt++;
						try {
							Thread.sleep(rnd.nextInt(10));
						} catch (InterruptedException e) {
						}
					}
					numrun.decrementAndGet();
				}
				});
    	}

    	while(numrun.get()>0) {
        	Thread.sleep(500);
    	}
    	assertTrue(channel.isRunning());
    	
    	
    }    
    
    //safe threading
    Lock lock = new Lock();
    private void doRestart() {
    	try {
    		lock.lock();
    		channel.closeChannel();
    		channel.startChannel(nameSpace);
    	} catch(Exception ex) {
    		
    	} finally {
    		lock.unlock();
    	}
    }
    
    @Test
    public void test_recieve_restart_on_falure() throws Exception {
    	
    	
    	//TODO: must finishup the changes in comchannel get more adresses in packetchannel, start top on fail etc.
    	
    	final Random rnd = new Random();
    	final AtomicInteger numrun = new AtomicInteger();
    	numrun.set(50);
    	
    	PacketChannel pc = spy(new PacketChannel(channel){
    		boolean open=false;
    		@Override
			public boolean send(byte[] buffer, NetAddress address) {return true;}
			@Override
			public void close() {open=false;}
			@Override
			public byte[] receive() {
				long wait = rnd.nextInt(50);
				try {
					Thread.sleep(wait);
				} catch (InterruptedException e) {
				}
				numrun.decrementAndGet();
				return new byte[0];
			}

			@Override
			public String getHostAddress() {return "";}
			@Override
			public int getPort() {return 0;}
			@Override
			public String getAddressPort() {return "";}
			@Override
			public void wakeUp() {
			}});
		
		channel.setPacketChannel(pc);	
    	assertTrue(channel.startChannel(nameSpace));

    	while(numrun.get()>0) {
        	Thread.sleep(50);
    	}
    	verify(pc,atLeast(50)).receive();
    	channel.closeChannel();
    	
    }     
	
    
}