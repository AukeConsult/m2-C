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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CipherAES extends CipherBase {
	private static final Logger logger = LoggerFactory.getLogger(CipherAES.class);
	private Cipher decipher = null;
	private Cipher encipher = null;
	public CipherAES() {
		encryptmethod = EncryptFactory.ENCRYPT_AES;
		setKeyEncryption(true);
		byte[] uuidbytes = UUID.randomUUID().toString().getBytes();
		byte[] AESkey = new byte[16];
		for (int i = 0; i < 16; i++) {
			AESkey[i] = uuidbytes[i];
		}
		setPublicKey(AESkey);
	}
	private byte[] encryptAES(Cipher cipher, byte[] dataBytes) throws CipherExeption {
		try {
			if (logger.isTraceEnabled())
				logger.trace("encryptAES: size " + String.valueOf(dataBytes.length));
			ByteArrayInputStream bIn = new ByteArrayInputStream(dataBytes);
			CipherInputStream cIn = new CipherInputStream(bIn, cipher);
			ByteArrayOutputStream bOut = new ByteArrayOutputStream();
			int nRead;
			byte[] data = new byte[16384];
			while ((nRead = cIn.read(data, 0, data.length)) != -1) {
				bOut.write(data, 0, nRead);
			}
			bOut.flush();
			cIn.close();
			return bOut.toByteArray();
		} catch (IOException e) {
			throw new CipherExeption("CipherAES: encryptAES: error " + e.getMessage());
		}
	}
	@Override
	public byte[] enCryptbuffer(byte[] buffer) throws CipherExeption {
		if (encipher == null) {
			try {
				SecretKeySpec key = new SecretKeySpec(getPublicKey(), "AES");
				encipher = Cipher.getInstance("AES/ECB/ISO10126Padding");
				encipher.init(Cipher.ENCRYPT_MODE, key);
			} catch (InvalidKeyException e) {
				throw new CipherExeption("CipherAES: enCryptbuffer: InvalidKeyException: error " + e.getMessage());
			} catch (NoSuchAlgorithmException e) {
				throw new CipherExeption("CipherAES: enCryptbuffer: NoSuchAlgorithmException: error " + e.getMessage());
			} catch (NoSuchPaddingException e) {
				throw new CipherExeption("CipherAES: enCryptbuffer: NoSuchPaddingException: error " + e.getMessage());
			}
		}
		return encryptAES(encipher, buffer);
	}
	@Override
	public byte[] deCryptbuffer(byte[] buffer) throws CipherExeption {
		if (decipher == null) {
			try {
				SecretKeySpec key = new SecretKeySpec(getRemotePublicKey(), "AES");
				decipher = Cipher.getInstance("AES/ECB/ISO10126Padding");
				decipher.init(Cipher.DECRYPT_MODE, key);
				return encryptAES(decipher, buffer);
			} catch (InvalidKeyException e) {
				throw new CipherExeption("CipherAES: deCryptbuffer: InvalidKeyException: error " + e.getMessage());
			} catch (NoSuchAlgorithmException e) {
				throw new CipherExeption("CipherAES: deCryptbuffer: NoSuchAlgorithmException: error " + e.getMessage());
			} catch (NoSuchPaddingException e) {
				throw new CipherExeption("CipherAES: deCryptbuffer: NoSuchPaddingException: error " + e.getMessage());
			} catch (IllegalArgumentException e) {
				throw new CipherExeption("CipherAES: deCryptbuffer: IllegalArgumentException: error " + e.getMessage());
			}
		}
		return encryptAES(decipher, buffer);
	}
	@Override
	public void enCryptReset() throws CipherExeption {
		encipher = null;
	}
	@Override
	public void deCryptReset() throws CipherExeption {
		decipher = null;
	}
}