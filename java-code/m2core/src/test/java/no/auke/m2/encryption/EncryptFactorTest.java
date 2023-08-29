package no.auke.m2.encryption;

public class EncryptFactorTest extends BaseTestCase {

	String peeridsender ="sender";
	String peeridreceiver ="receiver";
	
//	private void checkSendRecieve(String message, EncryptFactory sender,EncryptFactory reciever) throws CipherExeption, UnsupportedEncodingException
//	{
//
//		Random rnd = new Random();
//		rnd.setSeed(System.currentTimeMillis());
//
//		byte[] bufferout=new byte[0];
//		
//		// send back and forth
//		for(int i=0;i<25;i++)
//		{
//			
//			byte[] add = new byte[100];
//			for(int x=1;x<add.length;x++)
//			{
//				add[x] = (byte)rnd.nextInt(255);
//			}				
//
//			bufferout = ByteUtil.mergeBytes(bufferout,add);
//
//			byte[] bufferout_copy = ByteUtil.mergeBytes(new byte[0],bufferout); 
//            assertTrue(message + " bufferout changed, failed at trial " + String.valueOf(i) + " length " + String.valueOf(bufferout.length), Arrays.equals(bufferout,bufferout_copy));      
//
//			byte[] buffer_encrypted = sender.enCrypt(bufferout, peeridreceiver);
//			
//			assertFalse(message + " DO encrypt " + String.valueOf(i), Arrays.equals(bufferout,buffer_encrypted));		
//			assertTrue(message + " bufferout changed, failed at trial " + String.valueOf(i) + " length " + String.valueOf(bufferout.length), Arrays.equals(bufferout,bufferout_copy));		
//            assertEquals(message + " correct method in sender " + String.valueOf(i), sender.getEncrypt_worker().getEncryptmethod(), sender.getMethod());       
//
//			byte[] buffer_decrypted = reciever.deCrypt(buffer_encrypted, peeridsender);
//			assertFalse(message + " DO decrypt " + String.valueOf(i), Arrays.equals(buffer_encrypted,buffer_decrypted));		
//			assertTrue(message + " encrypt / decrypt once failed, failed at trial " + String.valueOf(i), Arrays.equals(bufferout,buffer_decrypted));
//            assertEquals(message + " correct method in reciever " + String.valueOf(i), reciever.getDecrypt_worker().getEncryptmethod(), sender.getEncrypt_worker().getEncryptmethod());       
//			
//			byte[] buffer_encrypted2 = reciever.enCrypt(buffer_decrypted, peeridsender);
//			byte[] buffer_decrypted2 = sender.deCrypt(buffer_encrypted2, peeridreceiver);
//
//			assertEquals(message + " correct method in reciever, same as sender " + String.valueOf(i), sender.getDecrypt_worker().getEncryptmethod(), reciever.getEncrypt_worker().getEncryptmethod());       
//
//			assertTrue(message + " encrypt / decrypt twise failed, failed at trial " + String.valueOf(i), Arrays.equals(bufferout,buffer_decrypted));
//			assertTrue(message + " encrypt / decrypt twise failed " + String.valueOf(i), Arrays.equals(bufferout,buffer_decrypted2));
//			
//		}
//	}
//		
//	@Test
//	public void testENCRYPT_EX() throws CipherExeption, UnsupportedEncodingException {
//
//		EncryptFactory sender = new EncryptFactory(EncryptFactory.ENCRYPT_EX);
//		EncryptFactory reciever = new EncryptFactory(EncryptFactory.ENCRYPT_EX);
//		
//		checkSendRecieve("method ENCRYPT_EX", sender, reciever);
//		
//		assertFalse("sender useKeyENcryption " , sender.getDecrypt_worker().useKeyEncryption());
//		assertFalse("reciever useKeyENcryption " , reciever.getDecrypt_worker().useKeyEncryption());		
//		
//	}	
//	 	
//
//	@Test
//	public void testENCRYPT_INVERSE() throws CipherExeption, UnsupportedEncodingException {
//
//		EncryptFactory sender = new EncryptFactory(EncryptFactory.ENCRYPT_INVERSE);
//		EncryptFactory reciever = new EncryptFactory(EncryptFactory.ENCRYPT_INVERSE);
//		
//		checkSendRecieve("method ENCRYPT_INVERSE", sender, reciever);
//		
//		assertTrue("sender useKeyENcryption " , sender.getDecrypt_worker().useKeyEncryption());
//		assertTrue("reciever useKeyENcryption " , reciever.getDecrypt_worker().useKeyEncryption());
//
//		assertTrue("sender useKeyENcryption " , sender.getEncrypt_worker().useKeyEncryption());
//		assertTrue("reciever useKeyENcryption " , reciever.getEncrypt_worker().useKeyEncryption());
//		
//		assertTrue("sender RSA Key" , Arrays.equals(sender.getEncrypt_worker().getLocalPublicRSAKey(), reciever.getDecrypt_worker().getRemotePublicRSAKey()));
//		assertTrue("reciever RSA Key" , Arrays.equals(reciever.getEncrypt_worker().getLocalPublicRSAKey(), sender.getDecrypt_worker().getRemotePublicRSAKey()));
//
//		assertTrue("sender Key" , Arrays.equals(sender.getEncrypt_worker().getPublicKey(), reciever.getDecrypt_worker().getRemotePublicKey()));
//		assertTrue("reciever Key" , Arrays.equals(reciever.getEncrypt_worker().getPublicKey(), sender.getDecrypt_worker().getRemotePublicKey()));
//		
//		assertTrue("reciever do decrypt " , reciever.doDecypt());
//		assertTrue("reciever do encrypt " , reciever.doEncrypt());			
//		assertTrue("sender do decrypt " , sender.doDecypt());		
//		assertTrue("sender do encrypt " , sender.doEncrypt());
//
//	}	
//	
//	@Test
//	public void testENCRYPT_DES() throws CipherExeption, UnsupportedEncodingException {
//
//		EncryptFactory sender = new EncryptFactory(EncryptFactory.ENCRYPT_DES);
//		EncryptFactory reciever = new EncryptFactory(EncryptFactory.ENCRYPT_DES);
//		
//		checkSendRecieve("method ENCRYPT_DES", sender, reciever);
//		
//		assertTrue("sender useKeyENcryption " , sender.getDecrypt_worker().useKeyEncryption());
//		assertTrue("reciever useKeyENcryption " , reciever.getDecrypt_worker().useKeyEncryption());
//
//		assertTrue("sender useKeyENcryption " , sender.getEncrypt_worker().useKeyEncryption());
//		assertTrue("reciever useKeyENcryption " , reciever.getEncrypt_worker().useKeyEncryption());
//		
//		assertTrue("sender RSA Key" , Arrays.equals(sender.getEncrypt_worker().getLocalPublicRSAKey(), reciever.getDecrypt_worker().getRemotePublicRSAKey()));
//		assertTrue("reciever RSA Key" , Arrays.equals(reciever.getEncrypt_worker().getLocalPublicRSAKey(), sender.getDecrypt_worker().getRemotePublicRSAKey()));
//
//		assertTrue("sender Key" , Arrays.equals(sender.getEncrypt_worker().getPublicKey(), reciever.getDecrypt_worker().getRemotePublicKey()));
//		assertTrue("reciever Key" , Arrays.equals(reciever.getEncrypt_worker().getPublicKey(), sender.getDecrypt_worker().getRemotePublicKey()));
//		
//		
//		assertTrue("reciever do decrypt " , reciever.doDecypt());
//		assertTrue("reciever do encrypt " , reciever.doEncrypt());			
//		assertTrue("sender do decrypt " , sender.doDecypt());		
//		assertTrue("sender do encrypt " , sender.doEncrypt());		
//	
//	}	
//
//	@Test
//	public void testENCRYPT_AES() throws CipherExeption, UnsupportedEncodingException {
//
//		EncryptFactory sender = new EncryptFactory(EncryptFactory.ENCRYPT_AES);
//		EncryptFactory reciever = new EncryptFactory(EncryptFactory.ENCRYPT_AES);
//		
//		checkSendRecieve("method ENCRYPT_AES", sender, reciever);
//
//		assertTrue("sender useKeyENcryption " , sender.getDecrypt_worker().useKeyEncryption());
//		assertTrue("reciever useKeyENcryption " , reciever.getDecrypt_worker().useKeyEncryption());
//
//		assertTrue("sender useKeyENcryption " , sender.getEncrypt_worker().useKeyEncryption());
//		assertTrue("reciever useKeyENcryption " , reciever.getEncrypt_worker().useKeyEncryption());
//		
//		assertTrue("sender RSA Key" , Arrays.equals(sender.getEncrypt_worker().getLocalPublicRSAKey(), reciever.getDecrypt_worker().getRemotePublicRSAKey()));
//		assertTrue("reciever RSA Key" , Arrays.equals(reciever.getEncrypt_worker().getLocalPublicRSAKey(), sender.getDecrypt_worker().getRemotePublicRSAKey()));
//
//		assertTrue("sender Key" , Arrays.equals(sender.getEncrypt_worker().getPublicKey(), reciever.getDecrypt_worker().getRemotePublicKey()));
//		assertTrue("reciever Key" , Arrays.equals(reciever.getEncrypt_worker().getPublicKey(), sender.getDecrypt_worker().getRemotePublicKey()));
//
//		assertTrue("reciever do decrypt " , reciever.doDecypt());
//		assertTrue("reciever do encrypt " , reciever.doEncrypt());			
//		assertTrue("sender do decrypt " , sender.doDecypt());		
//		assertTrue("sender do encrypt " , sender.doEncrypt());
//		
//	}
	
}
