/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.message.attribute;

import java.util.List;
import no.auke.util.ByteUtil;

public class PublicKey extends MessageAttribute {
	int keytype = 0;
	public int getKeytype() {
		return keytype;
	}
	public void setKeytype(int keytype) {
		this.keytype = keytype;
	}
	byte[] key = null;
	public byte[] getKey() {
		return key;
	}
	public void setKey(byte[] key) {
		this.key = key;
	}
	long timestamp = System.currentTimeMillis();
	public long getTimeStamp() {
		return timestamp;
	}
	public void setTimeStamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public PublicKey() {
		super(MessageAttribute.MessageAttributeType.PublicKey);
	}
	public PublicKey(byte[] key) {
		this();
		this.key = key;
	}
	// made more efficient
	public byte[] getBytes() {
		if (bytes == null) {
			byte[] databytes = ByteUtil.mergeDynamicBytesWithLength(ByteUtil.getBytes(getKeytype(), 2), getKey(), ByteUtil.getBytes(getTimeStamp(), 8));
			bytes = ByteUtil.mergeBytes(ByteUtil.getBytes(typeToInteger(type), 2), ByteUtil.getBytes(databytes.length, 2), databytes);
		}
		return bytes;
	}
	public PublicKey clone() {
		PublicKey new_packet = new PublicKey(getKey());
		new_packet.setKeytype(getKeytype());
		new_packet.setKey(getKey());
		new_packet.setTimeStamp(getTimeStamp());
		return new_packet;
	}
	public static PublicKey parse(byte[] data) {
		PublicKey result = new PublicKey();
		List<byte[]> bytes = ByteUtil.splitDynamicBytes(data);
		result.setKeytype(ByteUtil.getInt(bytes.get(0)));
		result.setKey(bytes.get(1));
		result.setTimeStamp(bytes.size() > 2 ? ByteUtil.getLong(bytes.get(2)) : 0);
		return result;
	}
}
