package no.auke.encryption;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class KeyCreator {
	
	private RSAPublicKey publicKey = null;
	private RSAPrivateKey privateKey = null;
	private String method = "RSA";
	
	public KeyCreator() throws Exception {
 		this(1024);
	}

	public KeyCreator(int size) throws Exception {
	      
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance(getMethod());
        keyGen.initialize(size);
		KeyPair keyPair = keyGen.generateKeyPair();
		
		publicKey = (RSAPublicKey) keyPair.getPublic();
		privateKey = (RSAPrivateKey) keyPair.getPrivate();
		
	}
	
	public RSAPublicKey getPublicKey() {
		return publicKey;
	}

	public RSAPrivateKey getPrivateKey() {
		return privateKey;
	}
	public String getMethod() {
		return method;
	}
}
