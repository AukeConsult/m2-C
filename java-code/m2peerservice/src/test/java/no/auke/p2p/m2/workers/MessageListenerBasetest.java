package no.auke.p2p.m2.workers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import no.auke.m2.encryption.EncryptFactory;
import no.auke.m2.task.ServiceMonitors;
import no.auke.m2.task.TaskMonitor;
import no.auke.p2p.m2.NameSpace;
import no.auke.p2p.m2.general.IListener;
import no.auke.p2p.m2.workers.PeerSession;
import no.auke.p2p.m2.workers.keepalive.KeepAlivePool;
import no.auke.p2p.m2.workers.message.MessageListener;


public class MessageListenerBasetest {
	
	NameSpace nameSpace;
	KeepAlivePool pingAlive;
	MessageListener listner;
	ComChannel channel;
	EncryptFactory encrypt;
	ServiceMonitors monitors;
	
	public void init() {

		encrypt = mock(EncryptFactory.class);
		pingAlive = mock(KeepAlivePool.class);
		
		nameSpace = mock(NameSpace.class);
		when(nameSpace.isRunning()).thenReturn(true);
		when(nameSpace.getKeepAlivePool()).thenReturn(pingAlive);
		when(nameSpace.getMainServ().getExecutor()).thenReturn(mock(ExecutorService.class));
		when(nameSpace.getMainServ().getExecutor()).thenReturn(mock(ExecutorService.class));
		
		monitors = mock(ServiceMonitors.class);
		when(monitors.getConnectMonitor()).thenReturn(mock(TaskMonitor.class));
		when(nameSpace.getMonitors()).thenReturn(monitors);
		
		
		when(nameSpace.getListen()).thenReturn(mock(IListener.class));
		
		channel = mock(ComChannel.class);
		//when(channel.getEncrypt()).thenReturn(encrypt);		

		when(nameSpace.getMainServ().getChannel()).thenReturn(channel);

		listner = spy(new MessageListener(nameSpace));	
	//	assertNotNull(listner.getPingAlive());

		Map<String, PeerSession> sessions = new HashMap<String, PeerSession>();
		
		when(nameSpace.getOpenPeerSessions()).thenReturn(sessions);			
		
				
		//when(server.getChannel().UDPSendEncrypt(any(MessageHeader.class))).thenReturn(true);
		//doNothing().when(encrypt).resetEncryption(anyString());
	}
	
}
