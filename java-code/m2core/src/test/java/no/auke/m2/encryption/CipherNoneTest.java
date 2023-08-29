package no.auke.m2.encryption;

import no.auke.m2.encryption.CipherBase;
import no.auke.m2.encryption.CipherExeption;
import no.auke.m2.encryption.CipherNone;
import no.auke.m2.encryption.EncryptFactory;
import no.auke.util.StringConv;

import org.junit.Test;

public class CipherNoneTest extends BaseTestCase {

	@Test
	public void testCipherNone() throws CipherExeption {

		byte[] markerbytes = new byte[] { (byte) 255, (byte) 254 };
		
		CipherBase cipher = new CipherNone();
		String test = "no key added hurrey";
		for (int i = 0; i < 10; i++) {

			test += String.valueOf(i) + test;
			byte[] data = cipher.enCrypt(test.getBytes());
			
			byte[] datanohead = new byte[data.length-4];
			
			System.arraycopy(data, 4, datanohead, 0, datanohead.length);        
			String decode = StringConv.UTF8(datanohead);

			assertFalse("useKeyENcryption " , cipher.useKeyEncryption());
			assertNull("getPublicKey " , cipher.getPublicKey());
			assertNull("getRemotePublicKey " , cipher.getRemotePublicKey());
			assertNull("getLocalPublicRSAKey " , cipher.getLocalPublicRSAKey());
			assertNull("getRemotePublicRSAKey " , cipher.getRemotePublicRSAKey());			
			
			assertTrue("message have been ancrypted ",test.equals(decode));
			assertTrue("marker 1 wrong ",data[0]==markerbytes[0]);
			assertTrue("type is wrong ",data[2]==EncryptFactory.ENCRYPT_NONE);
			
			
		}
	}
}
