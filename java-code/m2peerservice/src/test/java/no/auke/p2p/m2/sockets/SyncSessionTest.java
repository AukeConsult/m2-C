package no.auke.p2p.m2.sockets;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberModifier;


import no.auke.p2p.m2.NameSpace;
import no.auke.p2p.m2.PeerServer;
import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.SocketRetStatus;
import no.auke.p2p.m2.general.ReturMessageTypes;
import no.auke.p2p.m2.sockets.messages.MsgFunctionResult;
import no.auke.p2p.m2.sockets.messages.MsgSendFunction;
import junit.framework.TestCase;

//import static org.mockito.Matchers.any;
//import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

public class SyncSessionTest extends TestCase {

	class DummyTransactionSocket implements ISocket {

		public ReturMessageTypes retcode=ReturMessageTypes.ok;
		public boolean oksend=true;
		public long waitresult=0;
		public MsgSendFunction out_message=null;
		public MsgFunctionResult retur_message=null;
		
		public int incomming=0;
		
		//SyncSession session;
		
		@Override
		public int getPort() {return 0;}

		public SocketRetStatus send(String toClient, int toPort, String sessionid, final byte[] data) {
			
			if(oksend) {
				
				incomming=0;
				out_message = new MsgSendFunction(data);
				executor.execute(new Runnable(){

					@Override
					public void run() {
						try {
							Thread.sleep(waitresult+10);
						} catch (InterruptedException e) {
						}
						retur_message = new MsgFunctionResult(out_message.getToClientId(), out_message.getSessionId(), out_message.getMsgId(), out_message.getFunction(), data);
						session.gotReply(retur_message);
						
					}});
			}
			return new SocketRetStatus(retcode);
		}


		@Override
		public void close() {}

		@Override
		public String getClientId() {return "sender";}

		@Override
		public void gotReply(MsgFunctionResult retmsg) {}

		@Override
		public boolean isClosed() {
			return false;
		}

		@Override
		public boolean isConnected() {
			return false;
		}

		@Override
		public NameSpace getNameSpace() {
			//return mock(PeerServer.class);
			NameSpace server = spy(new NameSpace(mock(PeerServer.class),"",0,null));
			return server;
		}

		@Override
		public boolean checkIncomming(String toClient) {
			incomming++;
			return incomming<5;
		}

		Socket _s;
		@Override
		public Socket getSocket() {
			return _s;
		}


		@Override
		public void setSocket(Socket socket) {
			_s = socket;
			// TODO Auto-generated method stub
			
		}


		@Override
		public int getNumTrial() {
			return 3;
		}


		@Override
		public int getTimeout() {
			return 2000;
		}


		@Override
		public void setTimeout(int timeout) {
		}


		@Override
		public void setNumTrial(int timeout) {
		}
		
		
		
	}
	
	DummyTransactionSocket transSocket;
	SyncSession session;
	Socket socket;
	ExecutorService executor = Executors.newCachedThreadPool();
	
	
	public void setUp() {

		transSocket = spy(new DummyTransactionSocket());
		session = spy(new SyncSession(transSocket, "remote_client", 2000));
		
		socket = spy(transSocket.getNameSpace().openSocket(1000));
		transSocket.setSocket(socket);
		doReturn(socket).when(transSocket).getSocket();
		
		assertTrue(session.isOpen());		
		assertEquals("remote_client",session.getToClientId());
		assertEquals(2000,session.getToPort());
		
	}

	public void test_send_out_ok() throws Exception {
		//no problem with sending
		SocketRetStatus r = new SocketRetStatus();
		r.setOk();
		doReturn(r).when(socket).send(anyString(), any(byte[].class));
		
		//assume we have another thread listening from some peer and get the reply
		Thread incommingCall = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					//allow some seconds for the other peer to response
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
				ConcurrentLinkedQueue incomming = new ConcurrentLinkedQueue<byte[]>();
				incomming.add(new byte[] {1,2,3,4}); //receive something
				try {
					MemberModifier.field(SyncSession.class, "result").set(session, incomming);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}				
			}
		});		
		incommingCall.start();
		//this should run first before incommingCall thread actually runs
		SocketRetStatus ret = session.get("test", new byte[10]);
		//should be ok
		assertTrue(ret.isOk());
		assertNull(transSocket.out_message);
		
	}
	
	public void test_send_out_error_session_timeout() {
	
		//no problem with sending
		SocketRetStatus r = new SocketRetStatus();
		r.setOk();
		doReturn(r).when(socket).send(anyString(), any(byte[].class));
	
		//no reply
		SocketRetStatus ret = session.get("test", new byte[10]);
		assertEquals(ReturMessageTypes.session_timeout, ret.getLastRetcode());
	}
	
	public void test_send_out_error_sending_problems() {
		//got send_timeout when sending
		SocketRetStatus r = new SocketRetStatus();
		r.setLastRetcode(ReturMessageTypes.send_timeout);
		
		doReturn(r).when(socket).send(anyString(), any(byte[].class));
		
		//no reply
		SocketRetStatus ret = session.get("test", new byte[10]);
		assertEquals(ReturMessageTypes.send_timeout, ret.getLastRetcode());
	}
	
	
	
	
}
