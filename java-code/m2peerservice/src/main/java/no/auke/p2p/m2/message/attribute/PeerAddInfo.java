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
import no.auke.util.StringConv;

public class PeerAddInfo extends MessageAttribute {

	short directconnect = 0;
	public short getDirectConnect() {return directconnect;}
	public void setDirectConnect(short directconnect) {this.directconnect = directconnect;}
		
	int version = 0;
	public int getVersion() {return version;}
	public void setVersion(int version) {this.version = version;}

	String info = "";
	public String getInfo() {return info;}
	public void setInfo(String info) {this.info = info;}
	
	byte[] secret = new byte[0];
	public byte[] getSecret() {return secret;}
	public void setSecret(byte[] secret) {this.secret = secret;}	
	
	public PeerAddInfo() {super(MessageAttribute.MessageAttributeType.PeerInfo);}
	public PeerAddInfo(short directconnect, int version, String info, byte[] secret) {
		this();
		this.directconnect = directconnect;
		this.version = version;
		this.info = info;
		this.secret = secret;
	}
	// made more efficient
	public byte[] getBytes() {
		if (bytes == null) {
			byte[] databytes = ByteUtil.mergeDynamicBytesWithLength(
					ByteUtil.getBytes(getDirectConnect(), 2), 
					ByteUtil.getBytes(getVersion(), 2),
					info != "" ? StringConv.getBytes(info) : new byte[0],
					secret		
				);
			bytes = ByteUtil.mergeBytes(ByteUtil.getBytes(typeToInteger(type), 2), ByteUtil.getBytes(databytes.length, 2), databytes);
		}
		return bytes;
	}
	public PeerAddInfo clone() {
		PeerAddInfo new_packet = new PeerAddInfo(getDirectConnect(), getVersion(), getInfo(), getSecret());
		return new_packet;
	}
	public static PeerAddInfo parse(byte[] data) {
		PeerAddInfo result = new PeerAddInfo();
		List<byte[]> bytes = ByteUtil.splitDynamicBytes(data);
		result.setDirectConnect(ByteUtil.getShort(bytes.get(0)));
		result.setVersion(ByteUtil.getInt(bytes.get(1)));
		result.setInfo(StringConv.UTF8(bytes.get(2)));
		result.setSecret(bytes.get(3));
		return result;
	}
}
