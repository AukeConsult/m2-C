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

import java.util.UUID;

public class CipherInverse extends CipherBase {
	byte[] privatekey = null;
	public CipherInverse() {
		encryptmethod = EncryptFactory.ENCRYPT_INVERSE;
		setKeyEncryption(true);
		privatekey = UUID.randomUUID().toString().getBytes();
		byte[] pubkey = new byte[privatekey.length];
		for (int i = 0; i < pubkey.length; i++) {
			byte x = privatekey[i];
			x += 128;
			pubkey[i] = (byte) x;
		}
		setPublicKey(pubkey);
	}
	@Override
	public byte[] enCryptbuffer(byte[] buffer) {
		byte[] outbuffer = new byte[buffer.length];
		int s = 0;
		for (int i = 0; i < buffer.length; i++) {
			outbuffer[i] = (byte) (buffer[i] + privatekey[s]);
			s = (s == privatekey.length - 1 ? 0 : s++);
		}
		return outbuffer;
	}
	@Override
	public byte[] deCryptbuffer(byte[] buffer) {
		byte[] key = getRemotePublicKey();
		byte[] outbuffer = new byte[buffer.length];
		int s = 0;
		for (int i = 0; i < buffer.length; i++) {
			outbuffer[i] = buffer[i];
			outbuffer[i] -= (key[s] - 128);
			s = (s == key.length - 1 ? 0 : s++);
		}
		return outbuffer;
	}
	@Override
	public void enCryptReset() throws CipherExeption {}
	@Override
	public void deCryptReset() throws CipherExeption {}
}