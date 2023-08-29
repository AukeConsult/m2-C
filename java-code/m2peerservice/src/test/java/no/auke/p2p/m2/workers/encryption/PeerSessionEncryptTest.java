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







import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;

import no.auke.encryption.KeyCreator;
import no.auke.encryption.KeyReader;
import no.auke.p2p.m2.InitVar;
import no.auke.p2p.m2.NameSpace;
import no.auke.p2p.m2.general.IListener;
import no.auke.p2p.m2.message.UtilityException;
import no.auke.p2p.m2.message.attribute.MessageAttributeParsingException;
import no.auke.p2p.m2.message.attribute.PublicAddress;
import no.auke.p2p.m2.message.attribute.PublicKey;
import no.auke.p2p.m2.message.attribute.MessageAttributeInterface.MessageAttributeType;
import no.auke.p2p.m2.message.attribute.SessionKey;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.message.header.MessageHeaderInterface.MessageHeaderType;
import no.auke.p2p.m2.message.header.MessageHeaderParsingException;
import no.auke.p2p.m2.workers.ComChannel;
import no.auke.p2p.m2.workers.PeerSession;
import no.auke.p2p.m2.workers.PeerPeerInfo;
import no.auke.p2p.m2.workers.connect.PeerSessionEncrypt;
import no.auke.p2p.m2.workers.connect.SessionEncrypt;
import no.auke.p2p.m2.workers.keepalive.NetAddress;
import no.auke.p2p.m2.workers.message.MessageSender;

import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PeerSessionEncrypt.class})

@PowerMockIgnore({ "javax.management.*", "javax.xml.parsers.*","com.sun.org.apache.*","ch.qos.logback.*",
  "org.slf4j.*","javax.crypto.*" })

public class PeerSessionEncryptTest extends TestCase {

	SessionEncrypt sessionEncrypt;
	PeerSession peeragent;
	NameSpace namespace;
	PeerSessionEncrypt session;
	MessageHeader pingMsgfromKA;
	KeyCreator keys=null;
	PublicKey remote_publickey;
	PeerPeerInfo peerinfo;

	public void setUp() throws Exception {
			
		namespace = mock(NameSpace.class);
		
		when(namespace.hashCode()).thenReturn(1);
		when(namespace.doSessionEncryption()).thenReturn(true);
		when(namespace.getListen()).thenReturn(mock(IListener.class));
		
				
		sessionEncrypt = new SessionEncrypt(namespace);
		when(namespace.getSessionEncrypt()).thenReturn(sessionEncrypt);
        when(namespace.getMessageSender()).thenReturn(mock(MessageSender.class));
		
		peeragent = mock(PeerSession.class);
		when(peeragent.getNameSpace()).thenReturn(namespace);
		when(peeragent.isPinged()).thenReturn(true);

		keys = new KeyCreator();

		assertNotNull(keys);
		assertNotNull(keys.getPrivateKey());
		assertNotNull(keys.getPublicKey());
		assertEquals(162,keys.getPublicKey().getEncoded().length);
				
		peerinfo = new PeerPeerInfo("ss","ss");
		when(peeragent.getPeerInfo()).thenReturn(peerinfo);
		
		session = new PeerSessionEncrypt(peeragent);
		
		pingMsgfromKA = new MessageHeader(MessageHeader.MessageHeaderType.PingAlive);
		pingMsgfromKA.setAddress(mock(NetAddress.class));
		pingMsgfromKA.addMessageAttribute(new PublicKey(keys.getPublicKey().getEncoded()));		
		pingMsgfromKA.addMessageAttribute(new PublicAddress("127.0.0.1",1));
		
		assertNotNull(pingMsgfromKA);
		
		remote_publickey = (PublicKey) pingMsgfromKA.getMessageAttribute(MessageAttributeType.PublicKey);
		assertNotNull(remote_publickey);
		assertTrue(Arrays.equals(keys.getPublicKey().getEncoded(), remote_publickey.getKey()));				

		

		SessionEncrypt sessionEncrypt = new SessionEncrypt(namespace);
		when(namespace.getSessionEncrypt()).thenReturn(sessionEncrypt);
	}
		
	public void test_attribute_publicKey() throws MessageHeaderParsingException, MessageAttributeParsingException {

		PublicKey key = new PublicKey();
		assertTrue(key.getTimeStamp()>0);
		key.setKey("safdasdasd".getBytes());

		MessageHeader a = new MessageHeader(MessageHeaderType.PingAlive);
		a.addMessageAttribute(key);
		MessageHeader b = MessageHeader.parseHeader(a.getBytes());
		b.parseAttributes(a.getBytes());

		PublicKey key2 = (PublicKey) b.getMessageAttribute(MessageAttributeType.PublicKey);
		assertEquals(key.getKeytype(),key2.getKeytype());
		assertEquals(key.getTimeStamp(),key2.getTimeStamp());
		assertTrue(Arrays.equals(key.getKey(),key2.getKey()));
			
	}
	
	public void test_attribute_SessionKey() throws MessageHeaderParsingException, MessageAttributeParsingException {

		SessionKey key = new SessionKey();
		assertEquals(0,key.getPublicKey_TimeStamp());
		key.setKey("safdasdasd".getBytes());
		key.setPublicKey_TimeStamp(System.currentTimeMillis());

		MessageHeader a = new MessageHeader(MessageHeaderType.PingAlive);
		a.addMessageAttribute(key);
		MessageHeader b = MessageHeader.parseHeader(a.getBytes());
		b.parseAttributes(a.getBytes());

		SessionKey key2 = (SessionKey) b.getMessageAttribute(MessageAttributeType.SessionKey);
		assertEquals(key.getKeytype(),key2.getKeytype());
		assertEquals(key.getPublicKey_TimeStamp(),key2.getPublicKey_TimeStamp());
		assertTrue(Arrays.equals(key.getKey(),key2.getKey()));
			
	}	
		
	public void test_gotPublicKey() {
		
		session.gotPublicKey(pingMsgfromKA,false);
		assertNotNull(session.getSessionkey());
		assertEquals(session.getSessionkey().getPublicKey_TimeStamp(),remote_publickey.getTimeStamp());
		
	}
	
	public void test_gotPublicKey_last() {
		
		PublicKey key1 = new PublicKey(keys.getPublicKey().getEncoded());
		key1.setTimeStamp(100);
		
		MessageHeader msg1 = new MessageHeader(MessageHeader.MessageHeaderType.PingPeer);
		msg1.setAddress(mock(NetAddress.class));
		msg1.addMessageAttribute(key1);
			
		session.gotPublicKey(msg1,false);
		assertEquals(session.getSessionkey().getPublicKey_TimeStamp(),key1.getTimeStamp());
		
		PublicKey key2 = new PublicKey(keys.getPublicKey().getEncoded());
		key2.setTimeStamp(101);
		
		MessageHeader msg2 = new MessageHeader(MessageHeader.MessageHeaderType.PingPeer);
		msg2.setAddress(mock(NetAddress.class));
		msg2.addMessageAttribute(key2);
		
		session.gotPublicKey(msg2,false);
		assertEquals(session.getSessionkey().getPublicKey_TimeStamp(),key2.getTimeStamp());		

		PublicKey key3 = new PublicKey(keys.getPublicKey().getEncoded());
		key3.setTimeStamp(99);
		
		MessageHeader msg3 = new MessageHeader(MessageHeader.MessageHeaderType.PingPeer);
		msg3.setAddress(mock(NetAddress.class));
		msg3.addMessageAttribute(key3);
		
		session.gotPublicKey(msg3,false);
		
		assertEquals(session.getSessionkey().getPublicKey_TimeStamp(),key2.getTimeStamp());			
	
	}
	
	public void test_sendSessionKey_no_public_key() throws UtilityException {
		
		MessageHeader msg1 = new MessageHeader(MessageHeader.MessageHeaderType.PingPeer);
		msg1.setAddress(mock(NetAddress.class));
		
		msg1 = session.sendSessionKey(msg1,"");		
		SessionKey sessionKey = (SessionKey) msg1.getMessageAttribute(MessageAttributeType.SessionKey);
		assertNotNull(sessionKey);	
		
		// session key is to be empty
		assertNull(sessionKey.getKey());
		
		
	}
	
	public void test_sendSessionKey() {
		
		session.gotPublicKey(pingMsgfromKA,false);
		
		
		MessageHeader msg1 = new MessageHeader(MessageHeader.MessageHeaderType.PingPeer);
		msg1.setAddress(mock(NetAddress.class));
		
		// to set pin confirmed
		session.gotSessionKeyConfirmed(msg1);
		// no key set
		assertNull((SessionKey) msg1.getMessageAttribute(MessageAttributeType.SessionKey));
		
		
		msg1 = session.sendSessionKey(msg1,"");

		SessionKey sessionKey = (SessionKey) msg1.getMessageAttribute(MessageAttributeType.SessionKey);
		assertNotNull(sessionKey);
		
		assertEquals(sessionKey.getPublicKey_TimeStamp(),remote_publickey.getTimeStamp());	
		assertFalse(session.isKeyConfirmed());
		
	}
	
	public void test_gotSessionKeyConfirmed() {
		
		session.gotPublicKey(pingMsgfromKA,false);
		
		MessageHeader msg1 = new MessageHeader(MessageHeader.MessageHeaderType.PingPeer);
		msg1.setAddress(mock(NetAddress.class));
		msg1.addMessageAttribute(session.getSessionkey());
		
		session.gotSessionKeyConfirmed(msg1);
		assertTrue(session.isKeyConfirmed());
		
	}	
	
	public void test_gotSessionKeyConfirmed_wrongkey() {
		
		session.gotPublicKey(pingMsgfromKA,false);
		
		MessageHeader msg1 = new MessageHeader(MessageHeader.MessageHeaderType.PingPeer);
		msg1.setAddress(mock(NetAddress.class));
		
		SessionKey sessionkey = session.getSessionkey().clone();
		sessionkey.setPublicKey_TimeStamp(1000);		
		
		msg1.addMessageAttribute(sessionkey);
		
		session.gotSessionKeyConfirmed(msg1);
		
		assertFalse(session.isKeyConfirmed());
		
	}	
	
	private SessionKey newSessionKey() throws InvalidKeyException, Exception {
	
		// simulate key
		
		Cipher cipherRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipherRSA.init(Cipher.ENCRYPT_MODE, new KeyReader().getPublicKey(peeragent.getNameSpace().getSessionEncrypt().getPublickey().getKey()));
		byte[] aesKey = new byte[16];
		SecureRandom rnd = new SecureRandom();
		rnd.nextBytes(aesKey);
		SessionKey sessionKey = new SessionKey(cipherRSA.doFinal(aesKey));
		sessionKey.setPublicKey_TimeStamp(peeragent.getNameSpace().getSessionEncrypt().getPublickey().getTimeStamp());
	
		return sessionKey;

	}
	
	public void test_gotSessionKey() throws InvalidKeyException, Exception {

		SessionKey sessionKey = newSessionKey();
				
		MessageHeader msg = new MessageHeader(MessageHeader.MessageHeaderType.PingPeer);
		NetAddress address = new NetAddress();
		address.setPort(10);
		msg.setAddress(address);
		msg.addMessageAttribute(sessionKey);

		MessageHeader msgout = new MessageHeader(MessageHeader.MessageHeaderType.PeerResponse);
		msgout.setAddress(mock(NetAddress.class));

		session.gotSessionKey(msg,msgout);
		
		SessionKey sessionKeyOut = (SessionKey) msgout.getMessageAttribute(MessageAttributeType.SessionKey);
		assertNotNull(sessionKeyOut);
		assertTrue(session.isRemoteKeyReady());	
		
		assertEquals(sessionKeyOut.getPublicKey_TimeStamp(),sessionKey.getPublicKey_TimeStamp());
		
	}
	
	public void test_gotSessionKey_wrong_timestamp() throws InvalidKeyException, Exception {

		SessionKey sessionKey = newSessionKey();
		
		sessionKey.setPublicKey_TimeStamp(100);
				
		MessageHeader msg = new MessageHeader(MessageHeader.MessageHeaderType.PingPeer);
		msg.setAddress(new NetAddress("1.1.1.1",10));
		assertNotNull(msg.getAddress().getAddressPort());
		
		msg.addMessageAttribute(sessionKey);

		MessageHeader msgout = new MessageHeader(MessageHeader.MessageHeaderType.PeerResponse);
		msgout.setAddress(new NetAddress("2.2.2.2",10));
		assertNotNull(msgout.getAddress().getAddressPort());

		session.gotSessionKey(msg,msgout);
		
		SessionKey sessionKeyOut = (SessionKey) msgout.getMessageAttribute(MessageAttributeType.SessionKey);
		assertNull(sessionKeyOut);
		assertFalse(session.isRemoteKeyReady());	
		
	}	

	// LHA: direct key exchange
	
	public void test_gotSessionKey_setPeer_info() throws InvalidKeyException, Exception {

		SessionKey sessionKey = newSessionKey();
				
		MessageHeader msg = new MessageHeader(MessageHeader.MessageHeaderType.PingPeer);
		msg.setAddress(mock(NetAddress.class));
		msg.addMessageAttribute(sessionKey);

		session.gotPublicKey(msg, false);
		
		//assertEquals(peerinfo.getSessionKey().getPublicKey_TimeStamp(),sessionKey.getPublicKey_TimeStamp());
		//assertTrue(Arrays.equals(peerinfo.getSessionKey().getKey(),session.getRemoteAesKey()));
		
		
	}	
	
	
	
}
