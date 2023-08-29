package no.auke.encryption;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

public class SecurityUtil {

	static EncryptionStandard worker = new EncryptionStandard();
	
	public static byte[] getEncryptedBytesRSA(byte[] messsageBytes, RSAPublicKey publicKey) throws Exception {
		return worker.getEncryptedBytesRSA(messsageBytes, publicKey);
	}

	public static byte[] getDecryptedBytesRSA(byte[] messsageBytes, RSAPrivateKey privateKey) throws Exception {
		return worker.getDecryptedBytesRSA(messsageBytes, privateKey);
	}
	
	public static byte[] getEncryptedBytesRSA(byte[] messsageBytes, byte[] publicKey) throws Exception {
		return worker.getEncryptedBytesRSA(messsageBytes, new KeyReader().getPublicKey(publicKey));
	}
	
	
	public static byte[] getDecryptedBytesRSA(byte[] messsageBytes, byte[] privateKey) throws Exception {
		return worker.getDecryptedBytesRSA(messsageBytes, new KeyReader().getPrivateKey(privateKey));
	}

	public static byte[] getEncryptedBytesAES(byte[] messageBytes,byte[] keyBytes) throws Exception {
	    return worker.getEncryptedBytesAES(messageBytes,keyBytes);
	}	
	
	public static byte[] getDecryptedBytesAES(byte[] messageBytes,byte[] keyBytes) throws Exception   {
	    return worker.getDecryptedBytesAES(messageBytes,keyBytes);
	}
	
	public static byte[] getUUIDKey() {
		byte[] uuidbytes = UUID.randomUUID().toString().getBytes();
		byte[] key = new byte[16];
		for(int i=0;i<16;i++)
		{
			key[i] = uuidbytes[i];			
		}
		return key;
		
	}
	
}
