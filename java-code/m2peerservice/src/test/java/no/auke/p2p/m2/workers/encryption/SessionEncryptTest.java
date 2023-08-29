package no.auke.p2p.m2.workers.encryption;

import junit.framework.TestCase;

//import static org.mockito.Matchers.*;
//import static org.mockito.Mockito.atLeast;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.spy;
//import static org.mockito.Mockito.stub;


import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
//import static org.powermock.api.mockito.PowerMockito.spy;
//import static org.powermock.api.mockito.PowerMockito.doReturn;


import no.auke.p2p.m2.NameSpace;
import no.auke.p2p.m2.message.attribute.PublicAddress;
import no.auke.p2p.m2.message.attribute.PublicKey;
import no.auke.p2p.m2.message.attribute.MessageAttributeInterface.MessageAttributeType;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.message.header.MessageHeaderInterface.MessageHeaderType;
import no.auke.p2p.m2.workers.connect.PeerSessionEncrypt;
import no.auke.p2p.m2.workers.connect.SessionEncrypt;
import no.auke.p2p.m2.workers.keepalive.NetAddress;

import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PeerSessionEncrypt.class})

@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*","com.sun.org.apache.*","ch.qos.logback.*",
  "org.slf4j.*","javax.crypto.*" })

public class SessionEncryptTest extends TestCase {

	NameSpace namespace;
	SessionEncrypt session;

	public void setUp() throws Exception {
		
		namespace = mock(NameSpace.class);
		
		//when(namespace.getLocaladdress()).thenReturn(new NetAddress("127.0.0.1",200));
		when(namespace.doSessionEncryption()).thenReturn(true);
		session = new SessionEncrypt(namespace);
		assertNotNull(session.getPublickey());
		assertNotNull(session.getPrivateKey());
		
	}
	
	public void test_confirmKa() {
		
		NetAddress address = new NetAddress("127.0.0.1",100);
		
		MessageHeader msg = new MessageHeader(MessageHeaderType.PingAlive);
		msg.setAddress(address);
		msg.addMessageAttribute(session.getPublickey());
		msg.addMessageAttribute(new PublicAddress("127.0.0.1",1));
		
		session.confirmKA(msg);
		assertTrue(session.kaHasKey(address));		
		
	}
				
	public void test_confirmKa_wrong_timestamp() {
		
		NetAddress address = new NetAddress("127.0.0.1",100);
		
		PublicKey key = session.getPublickey().clone();		
		key.setTimeStamp(1000);
		
		MessageHeader msg = new MessageHeader(MessageHeaderType.PingAlive);
		msg.setAddress(address);
		msg.addMessageAttribute(key);
		msg.addMessageAttribute(new PublicAddress("127.0.0.1",1));
		
		session.confirmKA(msg);
		assertFalse(session.kaHasKey(address));		
		
	}
	
	public void test_pingKA() {
		
		NetAddress address = new NetAddress("127.0.0.1",100);
		
		// first add
		MessageHeader msg = new MessageHeader(MessageHeaderType.PingAlive);
		msg.setAddress(address);
		msg.addMessageAttribute(new PublicAddress("127.0.0.1",1));
		msg = session.pingKA(msg);
		PublicKey key = (PublicKey) msg.getMessageAttribute(MessageAttributeType.PublicKey);
		assertNotNull(key);
		
		// confirm
		
		session.confirmKA(msg);
		
		// next without key attribute
		
		MessageHeader msg2 = new MessageHeader(MessageHeaderType.PingAlive);
		msg2.setAddress(address);
		msg2.addMessageAttribute(new PublicAddress("127.0.0.1",1));
		msg2 = session.pingKA(msg2);
		PublicKey key2 = (PublicKey) msg2.getMessageAttribute(MessageAttributeType.PublicKey);
		assertNull(key2);

		
	}	
	
	
}
