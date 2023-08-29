package no.auke.m2.encryption;

import java.util.Arrays;

import no.auke.m2.encryption.CipherAES;
import no.auke.m2.encryption.CipherBase;
import no.auke.m2.encryption.CipherExeption;

public class CipherAESTest extends BaseTestCase {

	public void testCipherAES() throws Exception   {

		// fixed key
		CipherBase cipher1 = new CipherAES();
        CipherBase cipher2 = new CipherAES();
        
		String test = "hurrey hurrey2 byasasd< fsdfsdfsdfsdfsdfsdf";
		for (int i = 0; i < 15; i++) {

			try {
				
				test += String.valueOf(i) + test;
				
				byte[] encrypt = test.getBytes();
				
				
				byte[] decode = cipher1.deCrypt(cipher2.enCrypt(encrypt));
				
				System.out.println(String.valueOf(encrypt.length) +  " - " + String.valueOf(decode.length));
				
				assertTrue("encypted and decrypted message not like 1 ->" + String.valueOf(i) + " len " + String.valueOf(encrypt.length) +  " - " + String.valueOf(decode.length) ,Arrays.equals(encrypt,decode));

				decode = cipher2.deCrypt(cipher1.enCrypt(encrypt));
	            
	            assertTrue("encypted and decrypted message not like 2 ->" + String.valueOf(i) + " len " + String.valueOf(encrypt.length) +  " - " + String.valueOf(decode.length),Arrays.equals(encrypt,decode));			
				
			} catch (CipherExeption e) {
		
				if(e.getErrlevel()==2){
					
					throw new Exception(e.getMessage());
					
				}
			}

		}
        
		assertTrue("do encrypt",cipher1.doDecrypt());  
        assertTrue("do encrypt",cipher2.doDecrypt());  

        CipherBase cipher3 = new CipherAES();
        CipherBase cipher4 = new CipherAES();
        
        test = "hurrey hurrey2 byasasd<<<z22399ASa fsdfsdfsdfsdfsdf";
        for (int i = 0; i < 10; i++) {

			try {
				
	        	
	            test += String.valueOf(i) + test;
	            
	            byte[] encrypt = test.getBytes();
	            
	            byte[] decode = cipher4.deCrypt(cipher3.enCrypt(encrypt));
	            
	            System.out.println(String.valueOf(encrypt.length) +  " - " + String.valueOf(decode.length));
	            
	            assertTrue("encypted and decrypted message not like 1 ->" + String.valueOf(i) + " len " + String.valueOf(encrypt.length) +  " - " + String.valueOf(decode.length) ,Arrays.equals(encrypt,decode));

	            decode = cipher3.deCrypt(cipher4.enCrypt(encrypt));
	            
	            assertTrue("encypted and decrypted message not like 2 ->" + String.valueOf(i) + " len " + String.valueOf(encrypt.length) +  " - " + String.valueOf(decode.length),Arrays.equals(encrypt,decode));           
				
			} catch (CipherExeption e) {
		
				if(e.getErrlevel()==2){
					
					throw new Exception(e.getMessage());
					
				}
			}        	
        	
        }        
        assertTrue("do encrypt",cipher1.doDecrypt());  
        assertTrue("do encrypt",cipher3.doDecrypt());  

        
	}
}
