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

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import no.auke.util.ByteUtil;

public abstract class CipherBase {

	public static final byte FIXED_MARKER = (byte) 255;
	public static final byte FORCE_INITIALIZE = (byte) 255;
	public static final byte NO_FORCE_INITIALIZE = 0;
	public static final byte ENCRYPT_VALIDATOR1 = (byte) 255;
	public static final byte ENCRYPT_VALIDATOR2 = (byte) 255;
	private static final byte IS_KEY_DELIVERY = (byte) 255;
	
	// protocol format
	// data always start with 4 bytes
	// byte 0 = 255
	// byte 1 = 255 or 0
	// byte 2 = method
	// byte 3 = length for key information record
	// Key information record
	
	private int[][] fixedKey = {
			{ 195, 83, 42, 32, 47, 32, 15, 1, 223, 22, 96, 71, 28, 93, 84, 177, 43, 239, 26, 113, 21, 214, 122, 233, 42, 57, 66, 79, 83, 194, 87, 179, 63, 55,
					241, 37 },
			{ 32, 23, 32, 43, 7, 42, 5, 14, 1, 34, 32, 33, 55, 164, 73, 128, 219, 110, 223, 218, 117, 116, 75, 84, 105, 93, 182, 63, 74, 64, 42, 80, 12, 14,
					25, 16 },
			{ 15, 183, 42, 32, 247, 232, 15, 11, 23, 122, 196, 1, 228, 3, 284, 17, 42, 29, 226, 13, 221, 14, 12, 23, 242, 57, 66, 79, 83, 194, 287, 79, 6, 55,
					21, 7 },
			{ 33, 84, 242, 29, 47, 209, 15, 73, 27, 2, 16, 171, 128, 32, 4, 17, 14, 129, 2, 13, 11, 24, 212, 23, 2, 25, 136, 47, 82, 117, 7, 127, 27, 160, 1,
					200 },
			{ 0, 83, 242, 142, 46, 32, 145, 3, 7, 22, 36, 181, 170, 132, 44, 76, 15, 129, 2, 13, 26, 24, 221, 23, 3, 25, 166, 23, 222, 14, 7, 17, 62, 115, 21,
					3 },
			{ 195, 55, 42, 32, 47, 32, 15, 1, 73, 22, 96, 71, 28, 93, 84, 177, 43, 239, 26, 113, 21, 214, 122, 203, 42, 57, 66, 79, 83, 194, 87, 179, 63, 55,
					241, 37 },
			{ 55, 23, 32, 43, 7, 42, 5, 14, 1, 34, 32, 33, 55, 164, 73, 128, 219, 110, 63, 18, 117, 116, 75, 84, 105, 93, 182, 63, 74, 64, 42, 80, 12, 14, 25,
					16 },
			{ 15, 183, 42, 32, 247, 232, 15, 11, 23, 122, 196, 1, 28, 3, 284, 37, 42, 29, 226, 13, 21, 14, 12, 23, 242, 57, 66, 79, 83, 194, 287, 79, 6, 55,
					24, 7 },
			{ 4, 55, 242, 135, 48, 32, 15, 13, 27, 2, 16, 171, 128, 132, 4, 227, 13, 169, 2, 13, 218, 24, 22, 63, 2, 25, 166, 47, 82, 114, 7, 127, 62, 125, 31,
					3 },
			{ 6, 88, 242, 134, 49, 35, 155, 103, 7, 2, 56, 181, 128, 132, 4, 137, 104, 129, 203, 3, 21, 129, 9, 23, 23, 25, 167, 47, 82, 14, 7, 27, 62, 115,
					111, 40 } };
	// for check only
	private boolean dodecrypt = false;
	public boolean doDecrypt() {
		return dodecrypt;
	}
	private boolean doencrypt = false;
	public boolean doEncrypt() {
		return doencrypt;
	}
	// marker byte always starts with 255 255
	// when encryption started changed to 255 0
	protected byte[] markerbytes_encrypt = new byte[] { FIXED_MARKER, FORCE_INITIALIZE };
	protected byte encryptmethod = EncryptFactory.ENCRYPT_NONE;
	public byte getEncryptmethod() {
		return encryptmethod;
	}
	// other peer have got the key and confirmed
	public boolean remote_Has_My_PublicKey = false;
	// other peer have got the key encryption key and confirmed
	private boolean remote_Has_My_PublicRSAKey = false;
	// use RSA key encryption
	private boolean usekeyencryption = false;
	public boolean useKeyEncryption() {
		return usekeyencryption;
	}
	public void setKeyEncryption(boolean usekeyencryption) {
		this.usekeyencryption = usekeyencryption;
	}
	// key hide with RSA
	private PublicKey remote_PublicRSAKey = null;
	public byte[] getRemotePublicRSAKey() {
		return remote_PublicRSAKey == null ? null : remote_PublicRSAKey.getEncoded();
	}
	private PublicKey my_PublicRSAKey = null;
	public byte[] getLocalPublicRSAKey() {
		return my_PublicRSAKey == null ? null : my_PublicRSAKey.getEncoded();
	}
	private PrivateKey my_PrivateRSAKey = null;
	private byte[] enCryptKey(byte[] key) throws CipherExeption {
		final int key_encrypt_blocksize = 53;
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, my_PrivateRSAKey);
			ArrayList<byte[]> blocks = new ArrayList<byte[]>();
			int i = 0;
			int totlen = 0;
			while (i < key.length) {
				int len = i + key_encrypt_blocksize > key.length ? key.length - i : key_encrypt_blocksize;
				byte[] temp = new byte[len];
				System.arraycopy(key, i, temp, 0, temp.length);
				temp = cipher.doFinal(temp);
				totlen += temp.length;
				blocks.add(temp);
				i += key_encrypt_blocksize;
			}
			byte[] outbuff = new byte[totlen];
			int x = 0;
			for (byte[] temp : blocks) {
				System.arraycopy(temp, 0, outbuff, x, temp.length);
				x += temp.length;
			}
			return outbuff;
		} catch (NoSuchAlgorithmException e) {
			throw new CipherExeption("CipherBase: enCryptKey: NoSuchAlgorithmException:" + e.getMessage());
		} catch (IllegalBlockSizeException e) {
			throw new CipherExeption("CipherBase: enCryptKey: IllegalBlockSizeException:" + e.getMessage());
		} catch (BadPaddingException e) {
			throw new CipherExeption("CipherBase: enCryptKey: BadPaddingException:" + e.getMessage());
		} catch (InvalidKeyException e) {
			throw new CipherExeption("CipherBase: enCryptKey: InvalidKeyException:" + e.getMessage());
		} catch (NoSuchPaddingException e) {
			throw new CipherExeption("CipherBase: enCryptKey: NoSuchPaddingException:" + e.getMessage());
		}
	}
	private byte[] deCryptKey(byte[] key) throws CipherExeption {
		final int key_decrypt_blocksize = 64;
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, remote_PublicRSAKey);
			ArrayList<byte[]> blocks = new ArrayList<byte[]>();
			int i = 0;
			int totlen = 0;
			while (i < key.length) {
				int len = i + key_decrypt_blocksize > key.length ? key.length - i : key_decrypt_blocksize;
				byte[] temp = new byte[len];
				System.arraycopy(key, i, temp, 0, temp.length);
				temp = cipher.doFinal(temp);
				totlen += temp.length;
				blocks.add(temp);
				i += key_decrypt_blocksize;
			}
			byte[] outbuff = new byte[totlen];
			int x = 0;
			for (byte[] temp : blocks) {
				System.arraycopy(temp, 0, outbuff, x, temp.length);
				x += temp.length;
			}
			return outbuff;
		} catch (NoSuchAlgorithmException e) {
			throw new CipherExeption("CipherBase: deCryptKey: NoSuchAlgorithmException:" + e.getMessage());
		} catch (IllegalBlockSizeException e) {
			throw new CipherExeption("CipherBase: deCryptKey: IllegalBlockSizeException:" + e.getMessage());
		} catch (BadPaddingException e) {
			throw new CipherExeption("CipherBase: deCryptKey: BadPaddingException:" + e.getMessage());
		} catch (InvalidKeyException e) {
			throw new CipherExeption("CipherBase: deCryptKey: InvalidKeyException:" + e.getMessage());
		} catch (NoSuchPaddingException e) {
			throw new CipherExeption("CipherBase: deCryptKey: NoSuchPaddingException:" + e.getMessage());
		}
	}
	private byte[] remote_PublicKey = null;
	public byte[] getRemotePublicKey() {
		return remote_PublicKey;
	}
	private byte[] my_publicKey = null;
	public byte[] getPublicKey() {
		return my_publicKey;
	}
	protected void setPublicKey(byte[] publicKey) {
		this.my_publicKey = publicKey;
	}
	protected byte[] enCryptSimple(byte[] data, int start) {
		if (data[2] != EncryptFactory.ENCRYPT_NONE) {
			byte[] outdata = new byte[data.length];
			for (int i = 0; i < start; i++) {
				outdata[i] = data[i];
			}
			int keynum = (new java.util.Random()).nextInt(9);
			outdata[3] += (byte) keynum;
			int[] work_key = fixedKey[keynum];
			for (int i = start, z = 10, s = 0; i < data.length; i++, z++) {
				int x = data[i];
				x += work_key[z];
				if (x > 255) {
					x -= 256;
				}
				outdata[i] = (byte) x;
				if (z > 31) {
					z = s;
					s++;
					if (s > 30) {
						s = 0;
					}
				}
			}
			return outdata;
		} else {
			return data;
		}
	}
	protected byte[] deCryptSimple(byte[] data, int start) {
		if (data[2] != EncryptFactory.ENCRYPT_NONE) {
			int[] work_key = fixedKey[(int) (data[3] & 0x0F)];
			byte[] indata = new byte[data.length - start];
			for (int i = start, d = 0, z = 10, s = 0; i < data.length; i++, z++, d++) {
				int x = data[i];
				x -= work_key[z];
				if (x < 0) {
					x += 256;
				}
				indata[d] = (byte) x;
				if (z > 31) {
					z = s;
					s++;
					if (s > 30) {
						s = 0;
					}
				}
			}
			return indata;
		} else {
			byte[] indata = new byte[data.length - start];
			System.arraycopy(data, start, indata, 0, indata.length);
			return indata;
		}
	}
	private byte[] add_encrypted_markers(byte[] data) {
		// mark encrypted data with double 255 255 bytes
		byte[] markeddata = new byte[data.length + 2];
		System.arraycopy(new byte[] { ENCRYPT_VALIDATOR1, ENCRYPT_VALIDATOR2 }, 0, markeddata, 0, 2);
		if (data.length > 0) {
			System.arraycopy(data, 0, markeddata, 2, data.length);
		}
		return markeddata;
	}
	private byte[] check_encrypted_marker(byte[] data) throws CipherExeption {
		// check data for marker to ensure encryption is valid
		// first 2 bytes must encrypt to double value 255 255
		// and strip off the marker bytes
		// this is extra protection and strip off non encrypted data from
		// further processing
		// TODO: maybe avoid this extra buffer copy some how.
		if (data.length > 1) {
			if (data[0] == ENCRYPT_VALIDATOR1 && data[1] == ENCRYPT_VALIDATOR2) {
				byte[] ret_data = new byte[data.length - 2];
				System.arraycopy(data, 2, ret_data, 0, ret_data.length);
				return ret_data;
			} else {
				throw new CipherExeption("CipherBase: check_indata: wrong header on encrypted data (must start with 255 255)");
			}
		}
		return new byte[0];
	}
	// sending data
	private byte[] getOutbuffer(byte[] data) throws CipherExeption {
		try {
			byte[] headerdata = null;
			byte[] outdata = null;
			// Key initialization
			// starts here
			int startdata = 4;
			if (markerbytes_encrypt[1] == NO_FORCE_INITIALIZE 
					&& !remote_Has_My_PublicKey // remote key not present, start // initialize
					&& my_publicKey != null) { // have public keys defined
				// starts here, sending a RSA key to hide real Key
				if (useKeyEncryption() && !remote_Has_My_PublicRSAKey) {
					if (my_PublicRSAKey == null) {
						try {
							KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
							kpg.initialize(512);
							KeyPair kp = kpg.genKeyPair();
							my_PrivateRSAKey = kp.getPrivate();
							my_PublicRSAKey = kp.getPublic();
						} catch (NoSuchAlgorithmException e) {
							throw new CipherExeption("CipherBase: NoSuchAlgorithmException" + e.getMessage());
						}
					}
					// send the key us remote user don't have it
					byte[] publickey = my_PublicRSAKey.getEncoded();
					startdata = publickey.length + 7;
					headerdata = new byte[startdata];
					// mark key is embedded
					headerdata[3] = (byte) (headerdata[3] | 0x80);
					// key length
					System.arraycopy(ByteUtil.getBytes(publickey.length, 2), 0, headerdata, 4, 2);
					// mark key are RSA key
					headerdata[6] = (byte) 127;
					System.arraycopy(publickey, 0, headerdata, 7, publickey.length);
				} else {
					byte[] key = my_publicKey;
					if (useKeyEncryption()) {
						// encrypt the key
						key = enCryptKey(my_publicKey);
					}
					// send the key us remote user don't have it
					startdata = key.length + 7;
					headerdata = new byte[startdata];
					// mark key is embedded
					headerdata[3] = (byte) (headerdata[3] | 0x80);
					// key length
					System.arraycopy(ByteUtil.getBytes(key.length, 2), 0, headerdata, 4, 2);
					// mark key are public key
					headerdata[6] = (byte) 255;
					System.arraycopy(key, 0, headerdata, 7, key.length);
				}
			} else {
				headerdata = new byte[startdata];
				headerdata[3] = 0;
			}
			
			headerdata[0] = markerbytes_encrypt[0]; // main marker = 255
			headerdata[1] = markerbytes_encrypt[1];
			headerdata[2] = getEncryptmethod(); // encryption method
			
			if (remote_PublicRSAKey != null) {
				// signal mark ready to receive encrypted key
				headerdata[3] = (byte) (headerdata[3] | 0x10);
			}
			if (remote_PublicKey != null) {
				// signal mark ready to receive encrypted message
				headerdata[3] = (byte) (headerdata[3] | 0x40);
			}
			
			if (remote_Has_My_PublicKey) {
			
				headerdata[3] = (byte) (headerdata[3] | 0x20);
				doencrypt = true;
				byte[] encrypted_data = enCryptbuffer(add_encrypted_markers(data));
				byte[] length = ByteUtil.getBytes(encrypted_data.length, 4);
				outdata = new byte[headerdata.length + encrypted_data.length + length.length];
				System.arraycopy(headerdata, 0, outdata, 0, headerdata.length);
				System.arraycopy(length, 0, outdata, headerdata.length, length.length);
				System.arraycopy(encrypted_data, 0, outdata, headerdata.length + length.length, encrypted_data.length);
			
			} else {
				
				doencrypt = false;
				
				enCryptReset();
				
				headerdata[3] = (byte) (headerdata[3] | 0x00);
				
				byte[] marked_data = add_encrypted_markers(data);
				
				outdata = new byte[headerdata.length + marked_data.length];
				
				System.arraycopy(headerdata, 0, outdata, 0, headerdata.length);
				System.arraycopy(marked_data, 0, outdata, headerdata.length, marked_data.length);
				
				outdata = enCryptSimple(outdata, startdata);
				
			}
			return outdata;
		} catch (CipherExeption e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new CipherExeption("CipherBase: getOutbuffer: error make output " + e.getMessage() + e.getClass().toString());
		}
	}
	// reading data
	private byte[] getIndata(byte[] data) throws CipherExeption {
		// set to normal always when get data in because then someone sent you
		// something first
		// for next pass
		// byte 2 = 0
		// Important to
		markerbytes_encrypt[1] = NO_FORCE_INITIALIZE;
		//
		if (data == null || data.length < 4) {
			throw new CipherExeption(0, "CipherBase: getIndata, empty data");
		} else if (data[0] != FIXED_MARKER) {
			throw new CipherExeption(0, "CipherBase: getIndata, unknown data markers");
		} else if (data[2] != getEncryptmethod()) {
			throw new CipherExeption(1, "CipherBase: getIndata, not same method");
		}
		// mark if same method
		try {
			int startdata = 4;
			if ((data[3] & 0x80) == 0x80 && data.length > 6) {
				// got a key
				byte[] len = new byte[2];
				System.arraycopy(data, 4, len, 0, 2);
				if (data[6] == IS_KEY_DELIVERY) {
					byte[] key = new byte[ByteUtil.getInt(len)];
					System.arraycopy(data, 7, key, 0, key.length);
					if (key.length > 0) {
						if (remote_PublicRSAKey != null) {
							if (useKeyEncryption()) {
								remote_PublicKey = deCryptKey(key);
							} else {
								remote_PublicKey = key;
							}
						} else {
							throw new CipherExeption(1, "CipherBase: getIndata: remotePublicRSAKey missing");
						}
					}
					startdata = ByteUtil.getInt(len) + 7;
				} else if (data[6] == (byte) 127) {
					// public RSA
					byte[] remoteRSAKey = new byte[ByteUtil.getInt(len)];
					System.arraycopy(data, 7, remoteRSAKey, 0, remoteRSAKey.length);
					try {
						KeyFactory keyFactory = KeyFactory.getInstance("RSA");
						EncodedKeySpec KeySpec = new X509EncodedKeySpec(remoteRSAKey);
						remote_PublicRSAKey = keyFactory.generatePublic(KeySpec);
					} catch (InvalidKeySpecException e) {
						throw new CipherExeption("CipherBase: getIndata: InvalidKeySpecException" + e.getMessage());
					} catch (NoSuchAlgorithmException e) {
						throw new CipherExeption("CipherBase: getIndata: NoSuchAlgorithmException" + e.getMessage());
					}
					startdata = ByteUtil.getInt(len) + 7;
				}
			}
			if ((data[3] & 0x40) == 0x40) {
				if (my_publicKey != null) { // I must have a public key before
											// remove kan have it
					// remote report ready for receiving encrypted messages
					remote_Has_My_PublicKey = true;
				}
			}
			if ((data[3] & 0x10) == 0x10) {
				if (my_PublicRSAKey != null) { // I must have a localRSAKey key
												// before remote can have it
					// remote report ready for receiving encrypted keys
					remote_Has_My_PublicRSAKey = true;
				}
			}
			if ((data[3] & 0x20) == 0x20) {
				if (remote_PublicKey == null) {
					throw new CipherExeption(1, "CipherBase: getIndata: encrypted input, but don't have remote key");
				} else {
					// get encrypted data
					// just get raw data if encrypted
					// remove last 12345 added when simulate encryption
					byte[] len_arr = new byte[4];
					System.arraycopy(data, startdata, len_arr, 0, len_arr.length);
					int lenght = ByteUtil.getInt(len_arr);
					if (lenght > 0 && lenght < (1024 * 1024 * 16)) {
						byte[] indata_encrypted = new byte[lenght];
						System.arraycopy(data, startdata + 4, indata_encrypted, 0, lenght);
						dodecrypt = true;
						return check_encrypted_marker(deCryptbuffer(indata_encrypted));
					} else {
						throw new CipherExeption("CipherBase: getIndata: encrypt data bigger than 16MB not allowed");
					}
				}
			} else {
				deCryptReset();
				// it is simple
				dodecrypt = false;
				return check_encrypted_marker(deCryptSimple(data, startdata));
			}
		} catch (CipherExeption e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new CipherExeption("CipherBase: getIndata: Error when decrypt input " + e.getMessage() + e.getClass().toString());
		}
	}
	public byte[] enCrypt(byte[] data) throws CipherExeption {
		return getOutbuffer(data);
	}
	public byte[] deCrypt(byte[] data) throws CipherExeption {
		return getIndata(data);
	}
	public abstract byte[] enCryptbuffer(byte[] buffer) throws CipherExeption;
	public abstract byte[] deCryptbuffer(byte[] buffer) throws CipherExeption;
	public abstract void enCryptReset() throws CipherExeption;
	public abstract void deCryptReset() throws CipherExeption;
}