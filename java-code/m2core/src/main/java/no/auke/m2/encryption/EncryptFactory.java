/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.m2.encryption;

public class EncryptFactory {

	public static final byte ENCRYPT_NONE = (byte) 255; // No encryption
	public static final byte ENCRYPT_EX = (byte) 253; // Original m2 encryption,
														// rolling byte based on
														// embedded 10 X 32 key
														// matrix
	public static final byte ENCRYPT_DES = (byte) 251; // DES is not implemented
	public static final byte ENCRYPT_AES = (byte) 250; // AES encryption, 2
														// phase, exchange of
														// public/private RSA,
														// and exchange RSA
														// encrypted AES key
	public static final byte ENCRYPT_INVERSE = (byte) 249; // simply invert
															// bytes with value
															// 128
	// set default encryption method where
	private byte method = ENCRYPT_EX;
	public byte getMethod() {return method;}
	public void setMethod(byte method) {this.method = method;}
	// LHA:
	// set encrypt and decrypt worker is only for test
	private CipherBase decrypt_worker = new CipherEx();
	public CipherBase getDecrypt_worker() {return decrypt_worker;}
	private CipherBase encrypt_worker = new CipherEx();
	public CipherBase getEncrypt_worker() {return encrypt_worker;}
	public EncryptFactory() {}
	public EncryptFactory(byte method) {this.method = method;}
	// LHA: simplified only one method
	// NOT tested and in production
	public byte[] enCrypt(final byte[] buffer) {
		try {
			return encrypt_worker.enCrypt(buffer);
		} catch (CipherExeption e) {
			return new byte[0];
		}
	}
	public byte[] deCrypt(final byte[] buffer) {
		try {
			return decrypt_worker.deCrypt(buffer);
		} catch (CipherExeption e) {
			return new byte[0];
		}
	}

}
