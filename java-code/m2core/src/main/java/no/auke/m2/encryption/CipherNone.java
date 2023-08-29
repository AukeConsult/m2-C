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


public class CipherNone extends CipherBase {
	public CipherNone() {
		encryptmethod = EncryptFactory.ENCRYPT_NONE;
	}
	@Override
	public byte[] enCryptbuffer(byte[] buffer) {
		return null;
	}
	@Override
	public byte[] deCryptbuffer(byte[] buffer) {
		return null;
	}
	@Override
	public byte[] enCrypt(byte[] data) throws CipherExeption {
		try {
			byte[] outdata = new byte[data.length + 4];
			outdata[0] = (byte) 255; // main marker = 255
			outdata[1] = (byte) 0; // second marker = 255 if new session, 254 if
									// not
			outdata[2] = getEncryptmethod(); // encryption method
			System.arraycopy(data, 0, outdata, 4, data.length);
			return outdata;
		} catch (Exception ex) {
			throw new CipherExeption("CyperNone enCrypt: error: " + ex.getMessage());
		}
	}
	@Override
	public byte[] deCrypt(byte[] data) throws CipherExeption {
		try {
			byte[] indata = new byte[data.length - 4];
			indata[0] = markerbytes_encrypt[0]; // main marker = 255
			indata[1] = markerbytes_encrypt[1]; // second marker = 255 if new
												// session, 254 if not
			indata[2] = getEncryptmethod(); // encryption method
			System.arraycopy(data, 4, indata, 0, indata.length);
			return indata;
		} catch (Exception ex) {
			throw new CipherExeption("CyperNone deCrypt: error: " + ex.getMessage());
		}
	}
	@Override
	public void enCryptReset() throws CipherExeption {}
	@Override
	public void deCryptReset() throws CipherExeption {}
}