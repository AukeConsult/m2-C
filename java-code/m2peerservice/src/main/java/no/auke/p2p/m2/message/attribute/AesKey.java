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

public class AesKey extends MessageAttribute {
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
	public AesKey() {
		super(MessageAttribute.MessageAttributeType.Aeskey);
	}
	public AesKey(byte[] key) {
		this();
		this.key = key;
	}
	// made more efficient
	public byte[] getBytes() {
		if (bytes == null) {
			byte[] databytes = ByteUtil.mergeDynamicBytesWithLength(ByteUtil.getBytes(getKeytype(), 2), getKey());
			bytes = ByteUtil.mergeBytes(ByteUtil.getBytes(typeToInteger(type), 2), ByteUtil.getBytes(databytes.length, 2), databytes);
		}
		return bytes;
	}
	public AesKey clone() {
		AesKey new_packet = new AesKey(getKey());
		new_packet.setKeytype(getKeytype());
		new_packet.setKey(getKey());
		return new_packet;
	}
	public static AesKey parse(byte[] data) {
		AesKey result = new AesKey();
		List<byte[]> bytes = ByteUtil.splitDynamicBytes(data);
		result.setKeytype(ByteUtil.getInt(bytes.get(0)));
		result.setKey(bytes.get(1));
		return result;
	}
}
