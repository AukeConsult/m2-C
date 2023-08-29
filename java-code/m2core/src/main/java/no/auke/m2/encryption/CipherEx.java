/*
 * This file is part of Smooby project,  
 * 
 * Copyright (c) 2011-2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 * Cipher model version 2 
 * 
 */
package no.auke.m2.encryption;

// fixed key
// no key transport
public class CipherEx extends CipherBase {
	public CipherEx() {
		encryptmethod = EncryptFactory.ENCRYPT_EX;
		this.setKeyEncryption(false);
	}
	@Override
	public byte[] enCryptbuffer(byte[] buffer) {
		return buffer;
	}
	@Override
	public byte[] deCryptbuffer(byte[] buffer) {
		return buffer;
	}
	@Override
	public void enCryptReset() throws CipherExeption {}
	@Override
	public void deCryptReset() throws CipherExeption {}
}