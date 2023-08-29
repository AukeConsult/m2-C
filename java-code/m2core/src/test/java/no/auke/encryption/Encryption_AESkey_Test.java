package no.auke.encryption;

import java.security.InvalidKeyException;
import java.util.UUID;

import junit.framework.TestCase;
import no.auke.encryption.EncryptException;
import no.auke.encryption.Encryption;
import no.auke.encryption.EncryptionFactory;
import no.auke.encryption.EncryptionType;
import no.auke.encryption.KeyPair;


// TODO: make better testing 

public class Encryption_AESkey_Test extends TestCase {
		
	public void test_key_encryption_4096() throws InvalidKeyException, EncryptException, Exception {
		
		KeyPair keys = EncryptionFactory.generateKeyPairs(4096,UUID.randomUUID(), "");

		System.out.println("private " + String.valueOf(keys.privateKey.getKey().length) +  " public " + String.valueOf(keys.publicKey.getKey().length));
		
		for(int i=0;i<10;i++) {
		
			byte[] aeskey = Encryption.getEncryption(EncryptionType.standard).getUUIDKey16();
			byte[] aeskey_encrypted = Encryption.getEncryption(EncryptionType.standard).getEncryptKey(aeskey, keys.publicKey.getKey());
			System.out.println("AES " + String.valueOf(aeskey.length) +  " AES encrypted " + String.valueOf(aeskey_encrypted.length));
			byte[] aeskey_decrypted = Encryption.getEncryption(EncryptionType.standard).getDecryptKey(aeskey_encrypted, keys.privateKey.getKey());		

			System.out.println("AES " + String.valueOf(aeskey.length) +  " AES decrypted " + String.valueOf(aeskey_decrypted.length));
			assertEquals(new String(aeskey),new String(aeskey_decrypted));
			
		}
		
	}
	
	public void test_key_encryption_2048() throws InvalidKeyException, EncryptException, Exception {
		
		KeyPair keys = EncryptionFactory.generateKeyPairs(2048,UUID.randomUUID(), "");
		
		System.out.println("private " + String.valueOf(keys.privateKey.getKey().length) +  " public " + String.valueOf(keys.publicKey.getKey().length));
		for(int i=0;i<10;i++) {
		
			byte[] aeskey = Encryption.getEncryption(EncryptionType.standard).getUUIDKey16();
			byte[] aeskey_encrypted = Encryption.getEncryption(EncryptionType.standard).getEncryptKey(aeskey, keys.publicKey.getKey());
			System.out.println("AES " + String.valueOf(aeskey.length) +  " AES encrypted " + String.valueOf(aeskey_encrypted.length));
			byte[] aeskey_decrypted = Encryption.getEncryption(EncryptionType.standard).getDecryptKey(aeskey_encrypted, keys.privateKey.getKey());		
			
			System.out.println("AES " + String.valueOf(aeskey.length) +  " AES decrypted " + String.valueOf(aeskey_decrypted.length));
			assertEquals(new String(aeskey),new String(aeskey_decrypted));
			
		}
		
	}
	
	public void test_key_encryption_1024() throws InvalidKeyException, EncryptException, Exception {
		
		KeyPair keys = EncryptionFactory.generateKeyPairs(2048,UUID.randomUUID(), "");
		
		System.out.println("private " + String.valueOf(keys.privateKey.getKey().length) +  " public " + String.valueOf(keys.publicKey.getKey().length));
		for(int i=0;i<10;i++) {
		
			byte[] aeskey = Encryption.getEncryption(EncryptionType.standard).getUUIDKey16();
			byte[] aeskey_encrypted = Encryption.getEncryption(EncryptionType.standard).getEncryptKey(aeskey, keys.publicKey.getKey());
			System.out.println("AES " + String.valueOf(aeskey.length) +  " AES encrypted " + String.valueOf(aeskey_encrypted.length));
			byte[] aeskey_decrypted = Encryption.getEncryption(EncryptionType.standard).getDecryptKey(aeskey_encrypted, keys.privateKey.getKey());		
			System.out.println("AES " + String.valueOf(aeskey.length) +  " AES decrypted " + String.valueOf(aeskey_decrypted.length));
	
			
			assertEquals(new String(aeskey),new String(aeskey_decrypted));
			
		}
		
	}	

}
