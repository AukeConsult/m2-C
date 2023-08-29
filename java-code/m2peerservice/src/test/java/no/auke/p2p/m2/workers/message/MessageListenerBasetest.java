package no.auke.p2p.m2.workers.message;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import junit.framework.TestCase;


//import static org.mockito.Matchers.*;
//import static org.mockito.Mockito.atLeast;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.spy;
//import static org.mockito.Mockito.stub;


//import static org.junit.Assert.*;
//import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import no.auke.m2.encryption.EncryptFactory;
import no.auke.m2.task.ServiceMonitors;
import no.auke.m2.task.TaskMonitor;
import no.auke.p2p.m2.NameSpace;
import no.auke.p2p.m2.PeerServer;
import no.auke.p2p.m2.general.IListener;
import no.auke.p2p.m2.workers.ComChannel;
import no.auke.p2p.m2.workers.PeerSession;
import no.auke.p2p.m2.workers.keepalive.KeepAlivePool;
import no.auke.p2p.m2.workers.message.MessageListener;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ComChannel.class})

@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*",
    "com.sun.org.apache.xerces.internal.jaxp.*", "ch.qos.logback.*",
    "org.slf4j.*" })

public class MessageListenerBasetest extends TestCase {
	
	NameSpace namespace;
	KeepAlivePool pingAlive;
	MessageListener listner;
	ComChannel channel;
	EncryptFactory encrypt;
	
	ServiceMonitors monitors;
	
	Map<String, PeerSession> agentlist = new HashMap<String, PeerSession>();
	
	@Before
	public void setUp() {

		encrypt = mock(EncryptFactory.class);
		pingAlive = mock(KeepAlivePool.class);
		
		namespace = mock(NameSpace.class);
		when(namespace.isRunning()).thenReturn(true);
		when(namespace.getKeepAlivePool()).thenReturn(pingAlive);
		when(namespace.getMainServ()).thenReturn(mock(PeerServer.class));		
		when(namespace.getMainServ().getExecutor()).thenReturn(mock(ExecutorService.class));
		when(namespace.doSessionEncryption()).thenReturn(true);
		when(namespace.getListen()).thenReturn(mock(IListener.class));
		when(namespace.getOpenPeerSessions()).thenReturn(agentlist);
		
		channel = mock(ComChannel.class);
		//when(channel.getEncrypt()).thenReturn(encrypt);		

		when(namespace.getMainServ().getChannel()).thenReturn(channel);

		monitors = mock(ServiceMonitors.class);
		when(monitors.getConnectMonitor()).thenReturn(mock(TaskMonitor.class));
		when(namespace.getMonitors()).thenReturn(monitors);
		
		listner = spy(new MessageListener(namespace));	
	//	assertNotNull(listner.getPingAlive());

		//when(server.getChannel().UDPSendEncrypt(any(MessageHeader.class))).thenReturn(true);
		//doNothing().when(encrypt).resetEncryption(anyString());
	}
	
}
