package no.auke.encryption;

import java.security.InvalidKeyException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public interface IEncryptionUtil {


	public byte[] getEncryptedBytesRSA(byte[] messsageBytes, RSAPublicKey publicKey) throws Exception;

	public byte[] getEncryptedBytesPrivateRSA(byte[] messsageBytes, RSAPrivateKey privatekey) throws Exception;

	public byte[] getDecryptedBytesRSA(byte[] messsageBytes, RSAPrivateKey privateKey) throws Exception;

	public byte[] getDecryptedBytesPublicRSA(byte[] messsageBytes, RSAPublicKey publicKey) throws Exception;

	public byte[] getEncryptedBytesRSA(byte[] messsageBytes, byte[] publicKey) throws Exception;

	public byte[] getDecryptedBytesRSA(byte[] messsageBytes, byte[] privateKey) throws Exception;

	public byte[] getEncryptedBytesAES(byte[] messageBytes, byte[] keyBytes) throws Exception;

	public byte[] getDecryptedBytesAES(byte[] messageBytes, byte[] keyBytes) throws Exception;

	public byte[] getUUIDKey16();

	public byte[] getUUIDKey32();
	
	public byte[] getEncryptKey(byte[] keyBytes, byte[] key) throws InvalidKeyException, Exception;

	public byte[] getDecryptKey(byte[] keyBytes, byte[] key) throws InvalidKeyException, Exception;



}
