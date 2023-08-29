package no.auke.m2.encryption;

import java.util.UUID;

import no.auke.m2.encryption.CipherBase;
import no.auke.m2.encryption.CipherExeption;
import no.auke.util.StringConv;

import org.junit.Test;

public class CipherKeyExchangeTest extends BaseTestCase {

	class CipherTest extends CipherBase
	{

		public CipherTest()
		{
			super();
			setKeyEncryption(true);
			setPublicKey(UUID.randomUUID().toString().getBytes());
		}
		
		@Override
		public byte[] enCryptbuffer(byte[] buffer) throws CipherExeption {
			return buffer;
		}

		@Override
		public byte[] deCryptbuffer(byte[] buffer) throws CipherExeption {
			return buffer;
		}

        @Override
        public void enCryptReset() throws CipherExeption {
        }

        @Override
        public void deCryptReset() throws CipherExeption {
        }
		
	}
	
	CipherBase cipher1 = null;
	CipherBase cipher2 = null;
	
	String test = "hellokeydfgdfg dfg d fg dfg d f gdfgdfgdfg dfgdfg dfg";	
	
	public void init() throws CipherExeption {

		cipher1 = new CipherTest();
		cipher2 = new CipherTest();
				
	}

	@Test
	public void test_init_RSA_key() throws CipherExeption {

		init();		
		
		// first exchange
		cipher1.deCrypt(cipher2.enCrypt(test.getBytes()));
		
		assertNull("RSA key set first time",cipher1.getRemotePublicRSAKey());
		assertNull("no public key first time",cipher1.getRemotePublicKey());		
				
	}
	
	@Test
	public void test_init_key() throws CipherExeption {

		init();		
		
		// first exchange send RSA
		cipher1.deCrypt(cipher2.enCrypt(test.getBytes()));
		cipher2.deCrypt(cipher1.enCrypt(test.getBytes()));
		
        assertNull("public local key first time",cipher2.getLocalPublicRSAKey());        
        assertNull("public local key first time",cipher1.getRemotePublicRSAKey()); 
		assertFalse("not confirmed that remote have key, not ready to encrypt",cipher2.remote_Has_My_PublicKey);
		
		cipher1.deCrypt(cipher2.enCrypt(test.getBytes()));
		cipher2.deCrypt(cipher1.enCrypt(test.getBytes()));

        assertNotNull("public local key first time",cipher2.getLocalPublicRSAKey());        
        assertNotNull("public local key first time",cipher1.getRemotePublicRSAKey()); 

        assertTrue("RSA key not ok second time",StringConv.UTF8(cipher2.getLocalPublicRSAKey()).equals(StringConv.UTF8(cipher1.getRemotePublicRSAKey())));          
        		        
		// second response confirm KEY
		cipher2.deCrypt(cipher1.enCrypt(test.getBytes()));
		cipher1.deCrypt(cipher2.enCrypt(test.getBytes()));
		cipher2.deCrypt(cipher1.enCrypt(test.getBytes()));

		assertTrue("confirmed that remote have key, ready to encrypt",cipher2.remote_Has_My_PublicKey);
		        
        assertTrue("key not ok",StringConv.UTF8(cipher1.getPublicKey()).equals(StringConv.UTF8(cipher2.getRemotePublicKey())));  
        assertTrue("key not ok",StringConv.UTF8(cipher2.getPublicKey()).equals(StringConv.UTF8(cipher1.getRemotePublicKey())));  

        assertTrue("RSA key not ok",StringConv.UTF8(cipher2.getLocalPublicRSAKey()).equals(StringConv.UTF8(cipher1.getRemotePublicRSAKey())));  
        assertTrue("RSA key not ok",StringConv.UTF8(cipher1.getLocalPublicRSAKey()).equals(StringConv.UTF8(cipher2.getRemotePublicRSAKey())));  
        
        
	}	

	@Test
	public void test_first_message_no_ecryption() throws CipherExeption {

		init();		

		// first exchange
		String receive = StringConv.UTF8(cipher1.deCrypt(cipher2.enCrypt(test.getBytes())));
		assertTrue("cipher 1 decrypted message not like without key " + receive, test.equals(receive));		
	}
	
	@Test
	public void test_encrypt_with_key() throws CipherExeption {

		init();		

        assertFalse("cipher 1 wrong remote public key ", cipher1.remote_Has_My_PublicKey);             
        assertFalse("cipher 2 wrong remote public key ", cipher2.remote_Has_My_PublicKey);             
		
		// both send each RSA
		cipher2.deCrypt(cipher1.enCrypt(test.getBytes()));
		cipher1.deCrypt(cipher2.enCrypt(test.getBytes()));

        assertFalse("cipher 1 wrong remote public key ", cipher1.remote_Has_My_PublicKey);             
        assertFalse("cipher 2 wrong remote public key ", cipher2.remote_Has_My_PublicKey);             		
		
		// send second time confirm RSA
		cipher1.deCrypt(cipher2.enCrypt(test.getBytes()));
		cipher2.deCrypt(cipher1.enCrypt(test.getBytes()));

        assertFalse("cipher 1 wrong remote public key ", cipher1.remote_Has_My_PublicKey);             
        assertFalse("cipher 2 wrong remote public key ", cipher2.remote_Has_My_PublicKey);                     
				
		// send third time send key
		cipher2.deCrypt(cipher1.enCrypt(test.getBytes()));
		cipher1.deCrypt(cipher2.enCrypt(test.getBytes()));

		// send forth time confirm KEY
		cipher1.deCrypt(cipher2.enCrypt(test.getBytes()));
		cipher2.deCrypt(cipher1.enCrypt(test.getBytes()));
		
		// send fift time both should encrypt
		cipher1.deCrypt(cipher2.enCrypt(test.getBytes()));
		cipher2.deCrypt(cipher1.enCrypt(test.getBytes()));
		
        assertTrue("cipher 1 wrong remote public key ", cipher1.remote_Has_My_PublicKey);             
        assertTrue("cipher 2 wrong remote public key ", cipher2.remote_Has_My_PublicKey);             
				
		assertTrue("cipher 1 wrong remote public key ", StringConv.UTF8(cipher2.getPublicKey()).equals(StringConv.UTF8(cipher1.getRemotePublicKey())));				
		assertTrue("cipher 2 wrong remote public key ", StringConv.UTF8(cipher1.getPublicKey()).equals(StringConv.UTF8(cipher2.getRemotePublicKey())));				

		assertTrue("cipher 1 do not perform encryption " , cipher1.doEncrypt());	
		assertTrue("cipher 1 do not perform decryption " , cipher1.doDecrypt());	

		assertTrue("cipher 2 do not perform encryption " , cipher2.doEncrypt());	
		assertTrue("cipher 2 do not perform decryption " , cipher2.doDecrypt());	
		
	}
		
}
