package no.auke.encryption;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;

import java.security.spec.X509EncodedKeySpec;

public class KeyReader {
	private KeyFactory keyFactory = null;

	public KeyReader() {
		super();
		try {
			keyFactory = KeyFactory.getInstance("RSA");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private byte[] getKeyData(String fileName) throws Exception {
		FileInputStream fis = new FileInputStream(fileName);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int b;
		
		try {
			
			while ((b = fis.read()) != -1) {
				baos.write(b);
			}
			
			fis.close();
			baos.flush();
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}

	public RSAPrivateKey getPrivateKey(String filename) throws Exception {
		
		RSAPrivateKey privateKey = null;
		try {
			byte[] keydata = getKeyData(filename);
			PKCS8EncodedKeySpec encodedPrivateKey = new PKCS8EncodedKeySpec(keydata);
			privateKey = (RSAPrivateKey)keyFactory.generatePrivate(encodedPrivateKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return privateKey;
	}
	
	public RSAPrivateKey getPrivateKey(byte[] keydata) throws Exception {

		RSAPrivateKey privateKey = null;
		try {
			PKCS8EncodedKeySpec encodedPrivateKey = new PKCS8EncodedKeySpec(keydata);
			privateKey = (RSAPrivateKey)keyFactory.generatePrivate(encodedPrivateKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return privateKey;
	}

	public RSAPublicKey getPublicKey(String filename) throws Exception {

		RSAPublicKey publicKey = null;
		try {
			byte[] keydata = getKeyData(filename);
			X509EncodedKeySpec encodedPublicKey = new X509EncodedKeySpec(keydata);
			publicKey = (RSAPublicKey)keyFactory.generatePublic(encodedPublicKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return publicKey;

	}
	
	public RSAPublicKey getPublicKey(byte[] keydata) throws Exception {

		RSAPublicKey publicKey = null;
		try {
			X509EncodedKeySpec encodedPublicKey = new X509EncodedKeySpec(keydata);
			publicKey = (RSAPublicKey)keyFactory.generatePublic(encodedPublicKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return publicKey;

	}

}
