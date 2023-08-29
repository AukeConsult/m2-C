/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.workers.keepalive;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import no.auke.util.StringConv;

public class Util {
	// contain list of server ping cluster entries
	public static String convertToHex(byte[] data) {
		if (data != null && data.length > 0) {
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < data.length; i++) {
				int halfbyte = (data[i] >>> 4) & 0x0F;
				int two_halfs = 0;
				do {
					if ((0 <= halfbyte) && (halfbyte <= 9))
						buf.append((char) ('0' + halfbyte));
					else
						buf.append((char) ('a' + (halfbyte - 10)));
					halfbyte = data[i] & 0x0F;
				} while (two_halfs++ < 1);
			}
			return buf.toString();
		} else {
			return "";
		}
	}
	private static ConcurrentHashMap<String, byte[]> hashlist = new ConcurrentHashMap<String, byte[]>();
	public static byte[] getHash(String userId) {
		if (userId != null && !userId.equals("")) {
			if (!hashlist.containsKey(userId)) {
				try {
					MessageDigest md = MessageDigest.getInstance("SHA1");
					byte[] xx = StringConv.getBytes(userId);
					byte[] yy = new byte[xx.length * 2];
					for (int i = 0; i < xx.length; i++) {
						yy[i * 2] = xx[i];
						yy[(i * 2) + 1] = (byte) i;
					}
					md.update(yy, 0, yy.length);
					hashlist.put(userId, md.digest());
				} catch (NoSuchAlgorithmException e) {
					return null;
				}
			}
			return hashlist.get(userId);
		}
		return new byte[0];
	}
}