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

public class SessionKey extends MessageAttribute {
	
	int keytype = 0;
	public int getKeytype() {return keytype;}
	public void setKeytype(int keytype) {this.keytype = keytype;}
	byte[] key = null;
	public byte[] getKey() {return key;}
	public void setKey(byte[] key) {this.key = key;}
	
	long publickey_timestamp = 0;
	public long getPublicKey_TimeStamp() {return publickey_timestamp;}
	public void setPublicKey_TimeStamp(long timestamp) {this.publickey_timestamp = timestamp;}
	
	short keyid=0;
	public short getKeyId() {return keyid;}
	public void setKeyId(short keyid) {this.keyid = keyid;}	

	public SessionKey() {super(MessageAttribute.MessageAttributeType.SessionKey);}
	
	public SessionKey(byte[] key) {
		this();
		this.key = key;
	}
	// made more efficient
	public byte[] getBytes() {
		if (bytes == null) {
			byte[] databytes = ByteUtil.mergeDynamicBytesWithLength(
					ByteUtil.getBytes(getKeytype(), 2), 
					getKey(),
					ByteUtil.getBytes(getPublicKey_TimeStamp(), 8),
					ByteUtil.getBytes(keyid, 2)
					);
			bytes = ByteUtil.mergeBytes(ByteUtil.getBytes(typeToInteger(type), 2), ByteUtil.getBytes(databytes.length, 2), databytes);
		}
		return bytes;
	}
	public SessionKey clone() {
		SessionKey new_packet = new SessionKey(getKey());
		new_packet.setKeytype(getKeytype());
		new_packet.setPublicKey_TimeStamp(getPublicKey_TimeStamp());
		return new_packet;
	}
	public static SessionKey parse(byte[] data) {
		
		SessionKey result = new SessionKey();
		
		List<byte[]> bytes = ByteUtil.splitDynamicBytes(data);
		result.setKeytype(ByteUtil.getInt(bytes.get(0)));
		result.setKey(bytes.get(1));
		result.setPublicKey_TimeStamp(ByteUtil.getLong(bytes.get(2)));
		result.setKeytype(ByteUtil.getShort(bytes.get(3)));
		
		// result.setDirectConnect(bytes.size()>3?ByteUtil.getInt(bytes.get(3)):0);
		return result;
	}
}
