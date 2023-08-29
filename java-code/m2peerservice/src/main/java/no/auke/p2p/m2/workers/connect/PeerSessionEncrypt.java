package no.auke.p2p.m2.workers.connect;

import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import no.auke.encryption.KeyReader;
import no.auke.p2p.m2.InitVar;
import no.auke.p2p.m2.SocketRetStatus;
import no.auke.p2p.m2.general.ReturMessageTypes;
import no.auke.p2p.m2.message.attribute.PeerAddInfo;
import no.auke.p2p.m2.message.attribute.PublicKey;
import no.auke.p2p.m2.message.attribute.MessageAttributeInterface.MessageAttributeType;
import no.auke.p2p.m2.message.attribute.SessionKey;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.workers.PeerSession;

public class PeerSessionEncrypt {
	
	//private static final Logger logger = LoggerFactory.getLogger(PeerSessionEncrypt.class);
	
	private Cipher cipherRSA = null;
	private Cipher cipherAES_Encrypt = null;
	private Cipher cipherAES_Decrypt = null;
	
	private IvParameterSpec ivspec = new IvParameterSpec(new byte[]{ 0, 3, 6, 9, 12, 15, 18, 21, 24, 27, 30, 33, 36, 39, 42, 45 });
	
	private AtomicBoolean keyConfirmed = new AtomicBoolean();
	public boolean isKeyConfirmed() {
		if (DO_SESSION_ENCYPTION) {
			return keyConfirmed.get();
		} else {
			return true;
		}
	}
	private AtomicBoolean remoteKeyReady = new AtomicBoolean();
	public boolean isRemoteKeyReady() {
		if (DO_SESSION_ENCYPTION) {
			return remoteKeyReady.get();
		} else {
			return true;
		}
	}
	
	private SessionKey remoteSessionKey = null;
	private SessionKey sessionkey = null;
	public SessionKey getSessionkey() {return sessionkey;}
	private byte[] remoteAesKey = null;
	public byte[] getRemoteAesKey() {return remoteAesKey;}
	
	private byte[] localAesKey = null;
	private PeerSession peeragent;

	//TODO: for future use when multi key during session is implemented
	public short getKeyId() {return 100;}
	
	public PeerSession getPeerAgent() {return peeragent;}
	
	private boolean DO_SESSION_ENCYPTION = true;
	
	public PeerSessionEncrypt(PeerSession peeragent) {
		this.peeragent = peeragent;

		DO_SESSION_ENCYPTION = getPeerAgent().getNameSpace().doSessionEncryption();
		if (DO_SESSION_ENCYPTION) {
		
			keyConfirmed.set(false);
			remoteKeyReady.set(false);
			try {
				
				cipherRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				cipherAES_Encrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
				cipherAES_Decrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
				
				// AES 256
				localAesKey = getPeerAgent().getPeerInfo().getLocalAesKey();
				if (localAesKey == null) {					
					
					localAesKey = getPeerAgent().getNameSpace().getSessionEncrypt().getRandom();
					byte[] keyseed = getPeerAgent().getNameSpace().getSessionEncrypt().getRandom();
					String key = new String(localAesKey);
			        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			        KeySpec spec = new PBEKeySpec(key.toCharArray(), keyseed, 65536, 256);
			        localAesKey=factory.generateSecret(spec).getEncoded();
			        
					getPeerAgent().getPeerInfo().setLocalAesKey(localAesKey);
				
				}				
				cipherAES_Encrypt.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(localAesKey, "AES"),ivspec);
				
			} catch (Exception e) {
				getPeerAgent().getNameSpace().getListen().fatalError("Can not initiate session encryption: " + e.getMessage());
			}
		}
	}
	
	// got the public key of other party
	ReentrantLock gotpublickey_synckey = new ReentrantLock();
	ReentrantLock sendsessionkey_synckey = new ReentrantLock();
	ReentrantLock gotsessionkey_synckey = new ReentrantLock();
	ReentrantLock synckey = new ReentrantLock();
	
	public boolean gotPublicKey(MessageHeader msg, boolean fromPing) {
		
		// create a new session key (only once)
		if (DO_SESSION_ENCYPTION) {
			PublicKey remote_publickey = (PublicKey) msg.getMessageAttribute(MessageAttributeType.PublicKey);
			if (remote_publickey != null && remote_publickey.getKey().length > 0) {
				gotpublickey_synckey.lock();
				try {
					
					// check if same key
					// is any is in message

					if ((
					// update if key not exists locally
							sessionkey == null)
							|| (
							// update if key happens to be empty
							// (should not happen)
							sessionkey.getKey() == null || sessionkey.getKey().length == 0) || (
							// update if existing key is older than current key
							// can happend if keysare send to KA and KA session
							// is old
							// and have not recieved a new key from client
							// (rearly happening)
							sessionkey.getPublicKey_TimeStamp() < remote_publickey.getTimeStamp()) || (
							// mean getting key from ping
							// i.e when sending new key directly
							// update i
							fromPing && sessionkey.getPublicKey_TimeStamp() != remote_publickey.getTimeStamp())) {

						try {
							// encrypt the AES certificate with the public key
							cipherRSA.init(Cipher.ENCRYPT_MODE, new KeyReader().getPublicKey(remote_publickey.getKey()));
							sessionkey = new SessionKey(cipherRSA.doFinal(localAesKey));
							sessionkey.setPublicKey_TimeStamp(remote_publickey.getTimeStamp());
							// store the key
							getPeerAgent().getPeerInfo().setSessionKey(sessionkey);
							// important, so my key also get resent to the other
							// party
							keyConfirmed.set(false);
							return true;
						} catch (InvalidKeyException e) {
							getPeerAgent().getNameSpace().getListen().fatalError("InvalidKeyException " + e.getMessage());
						} catch (Exception e) {
							getPeerAgent().getNameSpace().getListen().fatalError("Exception " + e.getMessage());
						}
					}
				} catch (Exception ex) {
					getPeerAgent().getNameSpace().getListen().fatalError("error got public key " + getPeerAgent().getPeerid().getUserid() + ex.getMessage());
					sessionkey = null;
				} finally {
					gotpublickey_synckey.unlock();
				}
			}
		}
		return false;
	}
	//
	// called from send ping
	// add session key to ping message if key is not confirmed
	//
	public MessageHeader sendSessionKey(MessageHeader msg, String fromMessage) {
		
		if (DO_SESSION_ENCYPTION) {
			
			if (getPeerAgent().isPinged() && !isKeyConfirmed() && msg != null && msg.getAddress() != null) {
				
				sendsessionkey_synckey.lock();
				try {
					
					if (!isKeyConfirmed() && sessionkey != null) {
						msg.addMessageAttribute(sessionkey);
					
					} else if (!isKeyConfirmed() && sessionkey == null) {
						// NO session key, means don't have public key present
						// send and empty session key will trigger public key
						// from other side
						msg.addMessageAttribute(new SessionKey());
					}
					msg.addMessageAttribute(new PeerAddInfo((short)(InitVar.PEER_DO_DIRECT_CONNECT ? 1 : 0), 0,"",null));
					
				} catch (Exception ex) {
					getPeerAgent().getNameSpace().getListen().fatalError("can not send sessionkey " + ex.getMessage());
				} finally {
					sendsessionkey_synckey.unlock();
				}
			}
		} else {
			// When no encryption
			// always send session key because this signal
			// that ping have arrived and
			// trigger connect on the other side
			// Its a bit inefficient, but only solution right now
			if (getPeerAgent().isPinged()) {
				msg.addMessageAttribute(new SessionKey());
			}
		}
		return msg;
	}
	// got a encrypted session key from other party
	public void gotSessionKey(MessageHeader msg, MessageHeader outMsg) {
		
		if (DO_SESSION_ENCYPTION) {
		
			// is any in message
			SessionKey remote_sessionkey_incoming = (SessionKey) msg.getMessageAttribute(MessageAttributeType.SessionKey);
			if (remote_sessionkey_incoming != null) {
				try {
					gotsessionkey_synckey.lock();
					// only if real session key
					// key is correct (same time stamp)
					if (remote_sessionkey_incoming.getPublicKey_TimeStamp() == getPeerAgent().getNameSpace().getSessionEncrypt().getPublickey().getTimeStamp()) {
						// only decrypt if not already received
						if (!remoteKeyReady.get() || remoteSessionKey == null
								|| remoteSessionKey.getPublicKey_TimeStamp() != remote_sessionkey_incoming.getPublicKey_TimeStamp()) {

							// decrypt other side session key with your private
							// RSA key
							cipherRSA.init(Cipher.DECRYPT_MODE, getPeerAgent().getNameSpace().getSessionEncrypt().getPrivateKey());
							
							byte[] remoteAesKey = cipherRSA.doFinal(remote_sessionkey_incoming.getKey());
							// init AES encryption with this remore party AES
							// key

							this.remoteAesKey = remoteAesKey;
							cipherAES_Decrypt.init(Cipher.DECRYPT_MODE, new SecretKeySpec(remoteAesKey, "AES"),ivspec);
							
							// mark if other side support direct connect
							PeerAddInfo peeriaddinfo = (PeerAddInfo) msg.getMessageAttribute(MessageAttributeType.PeerInfo);
							
							if (peeriaddinfo != null) {
								getPeerAgent().getPeerInfo().setDirectConnect(peeriaddinfo.getDirectConnect() > 0 ? true : false);
							} else {
								getPeerAgent().getPeerInfo().setDirectConnect(false);
							}
							
							remoteSessionKey = remote_sessionkey_incoming;
							remoteKeyReady.set(true);
						}
						if (remoteKeyReady.get()) {
							// this is where session keys is confirmed
							// add key to out message to send back for
							// confirmation, send only time stamp
							// NB on key attribute with time stamp is sent, no
							// key
							//
							SessionKey return_remotesessionkey_confirm = new SessionKey();
							return_remotesessionkey_confirm.setKeytype(remote_sessionkey_incoming.getKeytype());
							return_remotesessionkey_confirm.setPublicKey_TimeStamp(remote_sessionkey_incoming.getPublicKey_TimeStamp());
							outMsg.addMessageAttribute(return_remotesessionkey_confirm);

						}
					} else if (remote_sessionkey_incoming.getPublicKey_TimeStamp() == 0) {
						remoteKeyReady.set(false);
						remoteSessionKey = null;
						// send correct key to peer and re-ping to KA server to
						// make sure correct key
						getPeerAgent().getNameSpace().getSessionEncrypt().sendPubKey(getPeerAgent().getRequestId(), outMsg);

					} else {
						remoteKeyReady.set(false);
						remoteSessionKey = null;
						getPeerAgent()
								.getNameSpace()
								.getListen()
								.error("WRONG sessionkey from " + msg.getAddress().getAddressPort() + " " + getPeerAgent().getPeerid().getUserid()
										+ " incoming key " + String.valueOf(remote_sessionkey_incoming.getPublicKey_TimeStamp()));
						// send correct key to peer and re-ping to KA server to
						// make sure correct key
						getPeerAgent().getNameSpace().getSessionEncrypt().sendPubKey(getPeerAgent().getRequestId(), outMsg);
					}
				} catch (Exception e) {
					remoteKeyReady.set(false);
					remoteSessionKey = null;
					// also send new if error
					getPeerAgent().getNameSpace().getSessionEncrypt().sendPubKey(getPeerAgent().getRequestId(), outMsg);
					getPeerAgent().getNameSpace().getListen()
							.fatalError("Error when decrypting sessionkey from " + msg.getAddress().getAddressPort() + " error " + e.getMessage());
				} finally {
					gotsessionkey_synckey.unlock();
				}
			}
		}
	}
	// got confirmed the return of the session key
	public void gotSessionKeyConfirmed(MessageHeader msg) {
		if (DO_SESSION_ENCYPTION) {
			if (!isKeyConfirmed()) {
				SessionKey key = (SessionKey) msg.getMessageAttribute(MessageAttributeType.SessionKey);
				if (key != null && sessionkey != null && sessionkey.getPublicKey_TimeStamp() == key.getPublicKey_TimeStamp()) {
					// got my session key confirmed by the other party, can no
					// start to encrypt
					keyConfirmed.set(true);
				} else if (key != null) {
					getPeerAgent()
							.getNameSpace()
							.getListen()
							.error("GOT WRONG CONFIRMED sessionkey " + msg.getAddress().getAddressPort() 
									+ " my timestamp " + String.valueOf(key.getPublicKey_TimeStamp()) + " recieved timestamp "
									+ String.valueOf(key.getPublicKey_TimeStamp()));
				}
			}
		}
	}
	//
	// de crypt the data with my AES key
	//
	public byte[] deCrypt(byte[] datain) throws Exception {
		if (DO_SESSION_ENCYPTION) {
			try {
				if (datain != null && datain.length > 0 && isRemoteKeyReady()) {
					return cipherAES_Decrypt.doFinal(datain);
				} else {
					return null;
				}
			} catch (IllegalBlockSizeException e) {
				getPeerAgent().getSessionEncrypt().resetEncryption();
				throw new Exception("Can not decrypt session, IllegalBlockSizeException " + e.getMessage());
			} catch (BadPaddingException e) {
				getPeerAgent()
						.getNameSpace()
						.getListen()
						.fatalError(
								"Can not decrypt session from " + getPeerAgent().getPeerAddress().getAddressPort() + " BadPaddingException " + e.getMessage());
				getPeerAgent().getSessionEncrypt().resetEncryption();
				throw new Exception("Can not decrypt session, BadPaddingException " + e.getMessage());

			} catch (Exception e) {
				getPeerAgent().getSessionEncrypt().resetEncryption();
				throw new Exception("Can not decrypt session " + e.getMessage());
			}
		}
		return datain;
	}
	//
	// encrypt the data with my AES key
	//
	public byte[] enCrypt(byte[] datain) throws Exception {
		if (DO_SESSION_ENCYPTION) {
			synckey.lock();
			try {
				if (isKeyConfirmed()) {
					try {
						getPeerAgent().getNameSpace().getListen().trace("encrypt message " + datain.length);
						return cipherAES_Encrypt.doFinal(datain);
					} catch (Exception e) {
						getPeerAgent().getNameSpace().getListen().debug("Can not Encypt " + e.getMessage());
						throw new Exception("Can not Encypt, Exception " + e.getMessage());
					}
				} else {
					getPeerAgent().getNameSpace().getListen().trace(getPeerAgent().getNameSpace().getClientid() + " not encrypt message" + datain.length);
				}
				return datain;
			} finally {
				synckey.unlock();
			}
		} else {
			return datain;
		}
	}
	//
	// waiting for session to get encrypted
	// call basically from socket send
	//
	public SocketRetStatus waitForEncryption(SocketRetStatus ret) {
		
		if (DO_SESSION_ENCYPTION) {
		
			if (getPeerAgent().isRunning() && getPeerAgent().isConnected()) {
			
				if (!isKeyConfirmed()) {
					
					//peeragent.getNameSpace().getListen().debug("waitForEncryption, client: " + getPeerAgent().getPeerid().getUserid());
					getPeerAgent().runConnectPingTask();
					
					// TODO: wait for encryption also implement in task
					// wait for encryption to enable
					// LHA: this is important
					int waitEncrypt = 0;
					while (getPeerAgent().isRunning() && !isKeyConfirmed() && getPeerAgent().isConnected()
							&& waitEncrypt < InitVar.INIT_PEER_ENCRYPTION_TIMEOUT) {
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {}
						waitEncrypt += 50;
					}
					if (getPeerAgent().isRunning() && !isKeyConfirmed()) {
						if (ret != null && !isKeyConfirmed()) {
							ret.setLastRetcode(ReturMessageTypes.no_session_encryption);
							ret.setLastMessage("Session encryption is not initialized");
						}
						getPeerAgent()
								.getNameSpace()
								.getListen()
								.debug(getPeerAgent().getNameSpace().getClientid() + " waitForEncryption, no encryption ready "
										+ getPeerAgent().getPeerid().getUserid());
					}

				}
			}
		}
		return ret;
	}
	public void resetEncryption() {
		if (DO_SESSION_ENCYPTION) {
			getPeerAgent().getPeerInfo().setRemoteAesKey(getRemoteAesKey());
			getPeerAgent().getPeerInfo().setSessionKey(sessionkey);
			getPeerAgent().killSession(getPeerAgent().getNameSpace().getClientid() + " reset encryption");
		}
	}

}
