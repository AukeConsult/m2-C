package no.auke.encryption;

import no.auke.util.StringConv;
import junit.framework.TestCase;

// TODO: make better testing

public class Encryption_Standard_Test extends TestCase {
	
	IEncryptionUtil worker = new EncryptionStandard();

	public void test_generate_key() throws Exception{
		
		KeyPair keys = EncryptionFactory.generateKeys();
		
		assertNotNull(keys.privateKey.getGuid());
		assertNotNull(keys.publicKey.getGuid());
		assertEquals(keys.privateKey.getGuid(),keys.publicKey.getGuid());	

	}
		
	public void test_private_encrypt_RSA_1024() throws Exception {
		
		KeyPair keys = EncryptionFactory.generateKeys();
		
		String test = "sdfssdflk12l3l23k4ldfkwlrk435dsasdasdas02034";
		
		for(int i=0;i<10;i++) {
			
			test +=test;
			byte[] encrypt = worker.getEncryptedBytesPrivateRSA(StringConv.getBytes(test), new KeyReader().getPrivateKey(keys.privateKey.getKey()));
			byte[] decrypt = worker.getDecryptedBytesPublicRSA(encrypt, new KeyReader().getPublicKey(keys.publicKey.getKey()));
			
			assertTrue(StringConv.UTF8(decrypt).equals(test));			

			System.out.println("1024test " + i + " data len " + test.length() + " encypt len " + encrypt.length);
						
		}
	}

	public void test_private_encrypt_RSA_2048() throws Exception {
		
		KeyPair keys = EncryptionFactory.generateKeys(2048);
		
		String test = "sdfssdflk12l3l23k4ldfkwlrk435dsasdasdas02034";

		for(int i=0;i<10;i++) {
		
			test +=test;
			byte[] encrypt = worker.getEncryptedBytesPrivateRSA(StringConv.getBytes(test), new KeyReader().getPrivateKey(keys.privateKey.getKey()));
			byte[] decrypt = worker.getDecryptedBytesPublicRSA(encrypt, new KeyReader().getPublicKey(keys.publicKey.getKey()));
			
			assertTrue(StringConv.UTF8(decrypt).equals(test));			

			System.out.println("2048test " + i + " data len " + test.length() + " encypt len " + encrypt.length);
						
		}
	}
	
	public void test_private_encrypt_RSA_4096() throws Exception {
		
		KeyPair keys = EncryptionFactory.generateKeys(4096);
		
		String test = "sdfssdflk12l3l23k4ldfkwlrk435dsasdasdas02034";

		for(int i=0;i<10;i++) {
			
			test +=test;
			
			byte[] encrypt = worker.getEncryptedBytesPrivateRSA(StringConv.getBytes(test), new KeyReader().getPrivateKey(keys.privateKey.getKey()));
			byte[] decrypt = worker.getDecryptedBytesPublicRSA(encrypt, new KeyReader().getPublicKey(keys.publicKey.getKey()));
			
			assertTrue(StringConv.UTF8(decrypt).equals(test));	
			
			System.out.println("4096test " + i + " data len " + test.length() + " encypt len " + encrypt.length);
						
		}
	}	
	
	public void test_public_encrypt_RSA_1024() throws Exception{
		
		KeyPair keys = EncryptionFactory.generateKeys();
		
        String test = "sdfssdflk12l3l23k4ldfkwlrk435dsasdasdas02034";
		for(int i=0;i<10;i++) {
			
			test +=test;
			
			byte[] encrypt = worker.getEncryptedBytesRSA(StringConv.getBytes(test), new KeyReader().getPublicKey(keys.publicKey.getKey()));
			byte[] decrypt = worker.getDecryptedBytesRSA(encrypt, new KeyReader().getPrivateKey(keys.privateKey.getKey()));
			
			System.out.println("1024test " + i + " data len " + test.length() + " encypt len " + encrypt.length);
			assertTrue(StringConv.UTF8(decrypt).equals(test));

						
		}	
	}
	
	public void test_public_encrypt_RSA_2048() throws Exception{
		
		KeyPair keys = EncryptionFactory.generateKeys(2048);
		
        String test = "sdfssdflk12l3l23k4ldfkwlrk435dsasdasdas02034";
		for(int i=0;i<10;i++) {
			
			test +=test;
			
			byte[] encrypt = worker.getEncryptedBytesRSA(StringConv.getBytes(test), new KeyReader().getPublicKey(keys.publicKey.getKey()));
			byte[] decrypt = worker.getDecryptedBytesRSA(encrypt, new KeyReader().getPrivateKey(keys.privateKey.getKey()));
			
			System.out.println("2048test " + i + " data len " + test.length() + " encypt len " + encrypt.length);
			assertTrue(StringConv.UTF8(decrypt).equals(test));

						
		}	
	}	

	public void test_public_encrypt_RSA_4096() throws Exception{
		
		KeyPair keys = EncryptionFactory.generateKeys(4096);
		
        String test = "sdfssdflk12l3l23k4ldfkwlrk435dsasdasdas02034";
		for(int i=0;i<10;i++) {
			
			test +=test;
			
			byte[] encrypt = worker.getEncryptedBytesRSA(StringConv.getBytes(test), new KeyReader().getPublicKey(keys.publicKey.getKey()));
			byte[] decrypt = worker.getDecryptedBytesRSA(encrypt, new KeyReader().getPrivateKey(keys.privateKey.getKey()));
			
			System.out.println("4096test " + i + " data len " + test.length() + " encypt len " + encrypt.length);
			assertTrue(StringConv.UTF8(decrypt).equals(test));

						
		}	
	}		
	
	public void test_AES_128() throws Exception{
		
		byte[] keyBytes = worker.getUUIDKey16();
		String test = "sdfssdflk12l3l23k4ldfkwlrk435dsasdasdas02034";
		for(int i=0;i<10;i++)
		{
			test +=test;
			byte[] encrypt = worker.getEncryptedBytesAES(test.getBytes(), keyBytes);
			byte[] decrypt = worker.getDecryptedBytesAES(encrypt, keyBytes);
			
			assertTrue(StringConv.UTF8(decrypt).equals(test));			
			
			System.out.println("128AES " + i + " data len " + test.length() + " encypt len " + encrypt.length);
						
		}
	}

//	public void test_AES_256() throws Exception{
//		
//		byte[] keyBytes = encryptionUtil.getUUIDKey32();
//		String test = "sdfssdflk12l3l23k4ldfkwlrk435dsasdasdas02034";
//		for(int i=0;i<10;i++)
//		{
//			test +=test;
//			byte[] encrypt = encryptionUtil.getEncryptedBytesAES(test.getBytes(), keyBytes);
//			byte[] decrypt = encryptionUtil.getDecryptedBytesAES(encrypt, keyBytes);
//			
//			assertTrue(StringConv.UTF8(decrypt).equals(test));			
//			
//			System.out.println("128AES " + i + " data len " + test.length() + " encypt len " + encrypt.length);
//						
//		}
//	}	
	
}
