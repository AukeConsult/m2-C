package no.auke.m2.encryption;

import no.auke.m2.encryption.CipherBase;
import no.auke.m2.encryption.CipherEx;
import no.auke.m2.encryption.CipherExeption;
import no.auke.util.StringConv;

public class CipherExTest extends BaseTestCase {

	public void testCipherEx() throws CipherExeption  {

		// fixed key
		CipherBase cipher = new CipherEx();
		String test = "hurrey hurrey2 byasasd<<<z22399ASasasasdasdasdasd";
		for (int i = 0; i < 20; i++) {

			test += String.valueOf(i) + test;
			String decode = StringConv.UTF8(cipher.deCrypt(cipher.enCrypt(StringConv.getBytes(test))));
			
			assertTrue("encypted and decrypted message not like",
			        test.equals(decode));
		}
	}
}
