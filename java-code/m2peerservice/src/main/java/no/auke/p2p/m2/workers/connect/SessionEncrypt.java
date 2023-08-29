package no.auke.p2p.m2.workers.connect;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.encryption.KeyCreator;
import no.auke.p2p.m2.NameSpace;
import no.auke.p2p.m2.message.attribute.AesKey;
import no.auke.p2p.m2.message.attribute.MessageAttribute;
import no.auke.p2p.m2.message.attribute.PublicKey;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.workers.keepalive.NetAddress;

// class for encrypt the session 
public class SessionEncrypt {
	
	private static final Logger logger = LoggerFactory.getLogger(SessionEncrypt.class);
	private Map<String, Long> kalist = new ConcurrentHashMap<String, Long>();
	private boolean DO_SESSION_ENCYPTION = true;
	private NameSpace namespace;
		
	private byte[] rndseed = new byte[32];

	private void initRandom() {
		byte[] ns = getNameSpace().getNameSpaceId().getBytes();
		long t = System.nanoTime();
		rndseed = new byte[]
				{(byte)(t & 0xff),
				 (byte)(t & 0xff00),		
				 (byte)(t & 0xff0000),		
				 (byte)(t & 0xff000000),			
				 (ns.length>0?(byte)ns[0]:12),		
				 (ns.length>1?(byte)ns[1]:2),		
				 (ns.length>2?(byte)ns[2]:36),		
				 (ns.length>3?(byte)ns[3]:4),		
				 (ns.length>4?(byte)ns[4]:5),		
				 (ns.length>5?(byte)ns[5]:6),		
				 (ns.length>6?(byte)ns[6]:72),		
				 (ns.length>7?(byte)ns[7]:111),		
				 (ns.length>8?(byte)ns[8]:22),		
				 (ns.length>9?(byte)ns[9]:32),		
				 (ns.length>10?(byte)ns[10]:4),		
				 (ns.length>11?(byte)ns[11]:5),		
				 (ns.length>12?(byte)ns[12]:6),		
				 (ns.length>13?(byte)ns[13]:32),		
				 (ns.length>14?(byte)ns[14]:11),		
				 (ns.length>15?(byte)ns[15]:2),		
				 (ns.length>16?(byte)ns[16]:66)		
				};
	}
	
	public byte[] getRandom() {
		SecureRandom rnd = new SecureRandom(rndseed);
		byte[] byternd = new byte[32];
		for(int i=1;i<rnd.nextInt(100);i++) {
			rnd.nextBytes(byternd);
		}
		return byternd;
	}
	
	public NameSpace getNameSpace() {
		return namespace;
	}
	
	PublicKey publickey;
	public PublicKey getPublickey() {
		return publickey;
	}
	RSAPrivateKey privatekey;
	public RSAPrivateKey getPrivateKey() {
		return privatekey;
	}
	
	public SessionEncrypt(NameSpace namespace) {
		
		this.namespace = namespace;
		initRandom();
		
		DO_SESSION_ENCYPTION = namespace.doSessionEncryption();
		// when public / private key is set, it is sent to KA
		// when public key is stored in KA, session encryption is done
		if (DO_SESSION_ENCYPTION) {
			
			try {
			
				//TODO Check encryption
				KeyCreator keys = new KeyCreator(2048);
				
				KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		        keyGen.initialize(2048, new SecureRandom(rndseed));
				KeyPair keyPair = keyGen.generateKeyPair();
				privatekey = (RSAPrivateKey) keyPair.getPrivate();
				publickey = new PublicKey(keyPair.getPublic().getEncoded());
			
			} catch (Exception e) {
				logger.error(getNameSpace().getClientid() + " can not initiate session encryption " + e.getMessage());
			}
		}
	}
	// add public key to KA ping
	public MessageHeader pingKA(MessageHeader msg) {
		// add public key attribute to message
		if (DO_SESSION_ENCYPTION && !kalist.containsKey(msg.getAddress().getAddressPort())) {
			if (publickey != null) {
				// send real key
				msg.addMessageAttribute(publickey);
			}
		}
		return msg;
	}
	
	// confirm KA return, do not send key anymore
	public void confirmKA(MessageHeader receiveMH) {
		
		if (DO_SESSION_ENCYPTION) {
			
			PublicKey receivedKey = (PublicKey) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.PublicKey);
			
			if (receivedKey != null) {
			
				if (receivedKey.getTimeStamp() == publickey.getTimeStamp()) {
				
//					if (logger.isDebugEnabled())
//						logger.debug(getNameSpace().getClientid() + " got confirm key from KA " + receiveMH.getAddress().getAddressPort() + " timestamp "
//								+ String.valueOf(receivedKey.getTimeStamp()));
					
					// TODO: get a AES key from KA to be used in further
					// communication with KA
					
					AesKey aeskey = (AesKey) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.Aeskey);
					if (aeskey != null) {
						// initiate key for KA communication
					}
					
					kalist.put(receiveMH.getAddress().getAddressPort(), publickey.getTimeStamp());
				
				} else {
				
					if (logger.isWarnEnabled())
						logger.warn(getNameSpace().getClientid() + " got wrong key from KA " + receiveMH.getAddress().getAddressPort() + " wrong timestamp "
								+ String.valueOf(receivedKey.getTimeStamp()) + " correct timestamp " + String.valueOf(publickey.getTimeStamp()));
				}
				
			}
		}
	}
	
	public void initKA(NetAddress netAddress) {
		kalist.remove(netAddress.getAddressPort());
	}
	
	// check if KA has key
	// used to keep ping frequency
	public boolean kaHasKey(NetAddress netAddress) {
		return !DO_SESSION_ENCYPTION || kalist.containsKey(netAddress.getAddressPort());
	}
	
	// check if KA has key
	// used to keep ping frequency
	public boolean kaHasKey() {
		return !DO_SESSION_ENCYPTION || kalist.size() > 0;
	}
	
	// ping other side with new key
	// and ping KA with new key
	public void sendPubKey(int requestId, MessageHeader msg) {
		
		if (DO_SESSION_ENCYPTION) {
			
			if (!msg.getAddress().getAddressPort().equals("0.0.0.0:0")) {
				
//				if (logger.isDebugEnabled())
//					logger.debug(getNameSpace().getClientid() + " send public key to " + msg.getAddress().getAddressPort() + " timestamp "
//							+ String.valueOf(publickey.getTimeStamp()));
				
				MessageHeader pingMsg = new MessageHeader(MessageHeader.MessageHeaderType.PingPeer);
				pingMsg.setTransactionID(requestId);
				pingMsg.setAddress(msg.getAddress());
				pingMsg.addMessageAttribute(publickey);
			
				namespace.getMessageSender().UDPSendEncrypt(pingMsg);
				namespace.getMessageSender().UDPSendEncrypt(pingMsg);
			
			} else {
				
				logger.error(getNameSpace().getClientid() + " ERROR: send public key to " + msg.getAddress().getAddressPort() + " timestamp "
						+ String.valueOf(publickey.getTimeStamp()));
			}
			
		}
	}
}
