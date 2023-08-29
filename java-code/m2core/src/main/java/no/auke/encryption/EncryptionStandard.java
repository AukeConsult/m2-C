package no.auke.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;

// 
// using standard javax.crypto
// 

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.SecretKeySpec;

import no.auke.util.ByteUtil;

// 
// standard RSA / AES encryption interface
// 

public class EncryptionStandard implements IEncryptionUtil {

	/* (non-Javadoc)
	 * @see no.auke.encryption.IEncryptionUtil#getEncryptedBytesRSA(byte[], java.security.PublicKey)
	 */
	
	@Override
	public byte[] getEncryptedBytesRSA(byte[] messsageBytes, RSAPublicKey publicKey) throws Exception {
		
		Cipher cipherRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipherRSA.init(Cipher.ENCRYPT_MODE, publicKey);
		
		int buffersize = publicKey.getModulus().bitLength()==4096?501:(publicKey.getModulus().bitLength()==2048?245:115);
		if(messsageBytes.length <= buffersize) {
			
			return cipherRSA.doFinal(messsageBytes);
		
		} else {
			
			//split here
			List<byte[]> parts = ByteUtil.splitBytesWithFixedLength(messsageBytes, buffersize);
			List<byte[]> encrypted_parts = new ArrayList<byte[]>();
			for(byte[] part : parts) {
				encrypted_parts.add(cipherRSA.doFinal(part));
			}
			return ByteUtil.mergeBytes(encrypted_parts);
		}
		
	}
	
	@Override
	public byte[] getDecryptedBytesRSA(byte[] messsageBytes, RSAPrivateKey privateKey) throws Exception {
		
		byte[] decryptedBytes = null;
		
		Cipher cipherRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipherRSA.init(Cipher.DECRYPT_MODE, privateKey);
		
		int buffersize = privateKey.getModulus().bitLength()==4096?512:(privateKey.getModulus().bitLength()==2048?256:128);
		if(messsageBytes.length <= buffersize) {
			
			decryptedBytes = cipherRSA.doFinal(messsageBytes);
		
		} else {
			
			//split here
			List<byte[]> parts = ByteUtil.splitBytesWithFixedLength(messsageBytes, buffersize);
			for(byte[] part : parts) {
				
				byte[] decryptedPart = cipherRSA.doFinal(part);
				decryptedBytes = decryptedBytes == null ? decryptedPart : ByteUtil.mergeBytes(decryptedBytes, decryptedPart);
			}	
		}
		return decryptedBytes;
	}	

	@Override
	public byte[] getEncryptedBytesPrivateRSA(byte[] messsageBytes, RSAPrivateKey privatekey) throws Exception {
		
		//byte[] encryptedBytes = null;
		Cipher cipherRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipherRSA.init(Cipher.ENCRYPT_MODE, privatekey);
		
		int buffersize = privatekey.getModulus().bitLength()==4096?501:(privatekey.getModulus().bitLength()==2048?245:115);
		if(messsageBytes.length <= buffersize) {

			return cipherRSA.doFinal(messsageBytes);
		
		} else {
			
			//split here
			List<byte[]> parts = ByteUtil.splitBytesWithFixedLength(messsageBytes, buffersize);
			List<byte[]> encrypted_parts = new ArrayList<byte[]>();
			
			for(byte[] part : parts) { 
				encrypted_parts.add(cipherRSA.doFinal(part));
			}
			return ByteUtil.mergeBytes(encrypted_parts);
		}

	}	

	@Override
	public byte[] getDecryptedBytesPublicRSA(byte[] messsageBytes, RSAPublicKey publicKey) throws Exception {
		
		Cipher cipherRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipherRSA.init(Cipher.DECRYPT_MODE, publicKey);
		
		int buffersize = publicKey.getModulus().bitLength()==4096?512:(publicKey.getModulus().bitLength()==2048?256:128);
		if(messsageBytes.length <= buffersize) {
			
			return cipherRSA.doFinal(messsageBytes);
		
		} else {
			
			//split here
			List<byte[]> parts = ByteUtil.splitBytesWithFixedLength(messsageBytes, buffersize);
			List<byte[]> decrypted_parts = new ArrayList<byte[]>();
			
			for(byte[] part : parts) {
				decrypted_parts.add(cipherRSA.doFinal(part));
			}
			return ByteUtil.mergeBytes(decrypted_parts);
		
		}
	
	}	
	

	@Override
	public byte[] getEncryptedBytesRSA(byte[] messsageBytes, byte[] publicKey) throws Exception {
		
		return getEncryptedBytesRSA(messsageBytes,  new KeyReader().getPublicKey(publicKey));
	}
	

	@Override
	public byte[] getDecryptedBytesRSA(byte[] messsageBytes, byte[] privateKey) throws Exception {
		
		return getDecryptedBytesRSA(messsageBytes, new KeyReader().getPrivateKey(privateKey));
	}
		
	private byte[] encryptAes(Cipher cipher, byte[] dataBytes) throws Exception{
	    
		ByteArrayInputStream bIn = new ByteArrayInputStream(dataBytes);
	    CipherInputStream cIn = new CipherInputStream(bIn, cipher);
	    ByteArrayOutputStream bOut = new ByteArrayOutputStream();
	    int ch;
	    while ((ch = cIn.read()) >= 0) {
	        bOut.write(ch);
	    }
	    cIn.close();
	    return bOut.toByteArray();
	}
		
	@Override
	public byte[] getEncryptedBytesAES(byte[] messageBytes,byte[] keyBytes) throws Exception {
	    
		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");		
		Cipher cipherAES = Cipher.getInstance("AES/ECB/PKCS5Padding");

		//byte[] iv = new byte[16]; // initialization vector with all 0
	    //cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
		
	    cipherAES.init(Cipher.ENCRYPT_MODE, key);
	    return encryptAes(cipherAES, messageBytes);
	    //return cipher.doFinal(messageBytes);
	}	
	

	@Override
	public byte[] getDecryptedBytesAES(byte[] messageBytes,byte[] keyBytes) throws Exception {
	    
		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

		Cipher cipherAES = Cipher.getInstance("AES/ECB/PKCS5Padding");
		
		//byte[] iv = new byte[16]; // initialization vector with all 0
	    //cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
		
	    cipherAES.init(Cipher.DECRYPT_MODE, key);
	    return encryptAes(cipherAES, messageBytes);
	    //return cipher.doFinal(messageBytes);
	
	}
	
	@Override
	public byte[] getUUIDKey16() {
		
		byte[] key = new byte[16];
		SecureRandom rnd = new SecureRandom();
		rnd.setSeed(System.nanoTime() * this.hashCode());
		rnd.nextBytes(key);
		return key;
		
	}
	
	@Override
	public byte[] getUUIDKey32() {
		
		byte[] key = new byte[32];
		SecureRandom rnd = new SecureRandom();
		rnd.setSeed(System.nanoTime() * this.hashCode());
		rnd.nextBytes(key);
		return key;
		
	}	

	@Override
	public byte[] getDecryptKey(byte[] keyBytes, byte[] key) throws InvalidKeyException, Exception {
		
		Cipher cipherRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipherRSA.init(Cipher.DECRYPT_MODE, new KeyReader().getPrivateKey(key));		
		return cipherRSA.doFinal(keyBytes);
	}
	
	@Override
	public byte[] getEncryptKey(byte[] keyBytes, byte[] key) throws InvalidKeyException, Exception 
	{
		Cipher cipherRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipherRSA.init(Cipher.ENCRYPT_MODE, new KeyReader().getPublicKey(key));
		return cipherRSA.doFinal(keyBytes);
		
	}	
		
}
