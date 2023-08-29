package no.auke.p2p.m2.workers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.powermock.core.classloader.annotations.PowerMockIgnore;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import no.auke.p2p.m2.SendTask;
import no.auke.m2.task.ServiceMonitors;
import no.auke.m2.task.TaskMonitor;
import no.auke.p2p.m2.NameSpace;
import no.auke.p2p.m2.PeerServer;
import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.SocketRetStatus;
import no.auke.p2p.m2.general.ReturMessageTypes;
import no.auke.p2p.m2.sockets.udp.SocketBufferOut;
import no.auke.p2p.m2.workers.connect.PeerSessionEncrypt;
import no.auke.p2p.m2.workers.keepalive.Peerid;
import no.auke.p2p.m2.workers.keepalive.Util;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NameSpace.class, PeerSession.class, Util.class})

@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*",
    "com.sun.org.apache.xerces.*", "ch.qos.logback.*",
    "org.slf4j.*" })

public class SendAsync_Test extends PeerSessionTestBase {
	
    // TODO: LHA
    // upgrade tests later
    
	//TaskMonitor monitor;
	
	SendTask taskOk;
	SendTask taskNotOk;
	SocketRetStatus ret;
	Socket socket;
	ServiceMonitors monitors;
	
	@Before
	public void setUp() {
		
		super.init();
		
		session = mock(PeerSession.class);
		socket = mock(Socket.class);
		
        when(session.getNameSpace()).thenReturn(namespace);
        when(session.getMainServ()).thenReturn(server);
        
        ret = new SocketRetStatus();
		ret.setPeerAgent(session);

		// monitor = spy(new TaskMonitor(peerServer,10,""));		
        //when(peerServer.getConnectMonitor()).thenReturn(monitor);
        //when(peerServer.getExecutor(anyString())).thenReturn(executor);
		
		monitors = mock(ServiceMonitors.class);
		when(monitors.getConnectMonitor()).thenReturn(mock(TaskMonitor.class));

		when(namespace.isRunning()).thenReturn(true);
		when(namespace.getMonitors()).thenReturn(monitors);
		when(namespace.getMonitors().getPingMonitor()).thenReturn(mock(TaskMonitor.class));
        when(namespace.getMainServ().getExecutor()).thenReturn(mock(ExecutorService.class));
		                
        when(namespace.openPeer((Peerid)anyObject())).thenReturn(ret);
        
        when(session.isConnected()).thenReturn(true);
        when(session.isRunning()).thenReturn(true);
        when(session.sendKARequest(anyBoolean())).thenReturn(true);
        when(session.waitForConnect(anyLong(), anyLong())).thenReturn(true);

        PeerSessionEncrypt pse = mock(PeerSessionEncrypt.class);
        when(pse.waitForEncryption(org.mockito.Matchers.any(SocketRetStatus.class))).thenReturn(ret);
        when(session.getSessionEncrypt()).thenReturn(pse);
        
        SocketBufferOut buffer = mock(SocketBufferOut.class);
        when(buffer.send()).thenReturn(true);       
        try {
        	when(socket.getOutputBuffer((SocketRetStatus)anyObject(),(PeerSession)anyObject(), anyInt(), (byte[])anyObject())).thenReturn(buffer);
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        

// no direct or peer requests
        when(session.sendKnownRequest()).thenReturn(false);
        when(session.sendPeerRequest()).thenReturn(false);
        
        taskOk = spy(new SendTask() {
			@Override
			public void onError(SocketRetStatus ret) {}
			@Override
			public void onSentComplete(SocketRetStatus ret) {}
			});        

    	taskOk.start(mock(TaskMonitor.class)); 
    	
    	taskOk.send(socket, ret, 1, "sadasdasd".getBytes());
    	assertNotNull(taskOk.getData());
        
	}

    @Test
    public void testRun_sendTask_startAsync() throws Exception {
		verify(taskOk,never()).onStart();
    }

    @Test
    public void testRun_sendTask_stop_connected() throws Exception {
    	
    	when(session.isConnected()).thenReturn(false);   
        taskOk.onExecute();

    	when(session.isConnected()).thenReturn(true);   
        taskOk.onStop();
        
    	//verify(taskOk,times(1)).onSentComplete(ret);
    	
    	assertEquals(ReturMessageTypes.ok,ret.getLastRetcode());

    }

    @Test
    public void testRun_sendTask_stop_not_connected() throws Exception {

        when(session.isConnected()).thenReturn(false);   

       
        taskOk.onExecute();
        taskOk.onStop();
        
        //verify(peeragent,times(1)).unLockFind();
        verify(taskOk,times(1)).onError(ret);
        verify(taskOk,never()).onSentComplete(ret);

    	assertEquals(ReturMessageTypes.peer_connect_timeout,ret.getLastRetcode());

    }

    
    @Test
    public void testRun_sendTask_execute_not_running() throws Exception {

    	when(session.isRunning()).thenReturn(false);   

    	taskOk.start(mock(TaskMonitor.class));
    	taskOk.onExecute();
        
    	verify(taskOk,times(1)).stop();  	
        verify(taskOk,times(1)).onStop();  
        
        assertEquals(ReturMessageTypes.peer_is_closed,ret.getLastRetcode());
    
    }

    
    @Test
    public void testRun_sendTask_execute_finish_wait() throws Exception {

    	when(session.waitForConnect(anyLong(), anyLong())).thenReturn(false);   
        taskOk.onExecute();
        assertEquals(ReturMessageTypes.ok,ret.getLastRetcode());
        verify(taskOk,times(1)).stop();	

    }

    @Test
    public void testRun_sendTask_execute_still_wait() throws Exception {

        when(session.isConnected()).thenReturn(false); 
    	when(session.waitForConnect(anyLong(), anyLong())).thenReturn(true);    	
        taskOk.onExecute();
    	verify(taskOk,never()).stop();

    }
    
	
    @Test
    public void testRun_send_no_data() throws Exception {
    	
    	SendTask task = spy(new SendTask(){
			public void onSentComplete(SocketRetStatus ret) {}
			public void onError(SocketRetStatus ret) {}
			}
    	);
    	
    	Socket socket = new Socket(100, namespace);    	
    	assertEquals(ReturMessageTypes.empty_data, socket.send("leif", null, task).getLastRetcode());    	
    	
    	verify(task,never()).send((Socket)anyObject(),(SocketRetStatus)anyObject(), anyInt(), (byte[])anyObject());

    }
    
    @Test
    public void testRun_send_data() throws Exception {
    	
    	SendTask task = spy(new SendTask(){
			public void onSentComplete(SocketRetStatus ret) {}
			public void onError(SocketRetStatus ret) {}
			}
    	);
    	
    	Socket socket = new Socket(100, namespace);    	
    	assertEquals(ReturMessageTypes.ok, socket.send("leif", "sadasd".getBytes(), task).getLastRetcode());    	
    	verify(task,times(1)).send((Socket)anyObject(),(SocketRetStatus)anyObject(), anyInt(), (byte[])anyObject());
    	
    }
    
	
    @Test
    public void testRun_sendTask_start_not_connected() throws Exception {
    	
        when(session.isConnected()).thenReturn(false);
        
    	taskOk.onStart();
    	taskOk.onExecute();

    	//verify(peeragent,times(1)).tryLockFind();
    	verify(session,times(1)).sendKnownRequest();
    	verify(session,times(1)).sendPeerRequest();
    	verify(session,times(1)).sendKARequest((Boolean) anyBoolean());

    	verify(taskOk,never()).stop();
    	
    	taskOk.onStop();
    	
    }
    
    @Test
    public void testRun_sendTask_start_is_connected() throws Exception {
    	
        when(session.isConnected()).thenReturn(true);    	
        
    	taskOk.onStart();
        taskOk.onExecute();
        taskOk.onStop();

        //verify(peeragent,never()).tryLockFind();
    	verify(session,never()).sendKnownRequest();
    	verify(session,never()).sendPeerRequest();
    	verify(session,never()).sendKARequest((Boolean) anyBoolean());

    	verify(taskOk,times(1)).stop();
    	//verify(taskOk,times(1)).onStop();    	
    }      
    
    
}
