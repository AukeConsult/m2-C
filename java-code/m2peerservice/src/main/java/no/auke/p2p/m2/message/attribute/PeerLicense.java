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

public class PeerLicense extends MessageAttribute {
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public String getNameSpace() {
		return nameSpace;
	}
	public void setNameSpace(String nameSpace) {
		this.nameSpace = nameSpace;
	}
	public byte[] getMacAddress() {
		return macAddress;
	}
	public void setMacAddress(byte[] macAddress) {
		this.macAddress = macAddress;
	}
	String application = "";
	String nameSpace = "";
	byte[] macAddress = new byte[0];
	public PeerLicense(String application, String nameSpace, byte[] macAddress) {
		this();
		this.application = application;
		this.nameSpace = nameSpace;
		this.macAddress = macAddress;
	}
	public PeerLicense() {
		super(MessageAttribute.MessageAttributeType.PeerLicence);
	}
	// made more efficient
	public byte[] getBytes() {
		if (bytes == null) {
			byte[] databytes = ByteUtil
					.mergeDynamicBytesWithLength(StringConv.getBytes(getApplication()), StringConv.getBytes(getNameSpace()), getMacAddress());
			bytes = ByteUtil.mergeBytes(ByteUtil.getBytes(typeToInteger(type), 2), ByteUtil.getBytes(databytes.length, 2), databytes);
		}
		return bytes;
	}
	public PeerLicense clone() {
		PeerLicense new_packet = new PeerLicense(getApplication(), getNameSpace(), getMacAddress());
		return new_packet;
	}
	public static PeerLicense parse(byte[] data) {
		PeerLicense result = new PeerLicense();
		List<byte[]> bytes = ByteUtil.splitDynamicBytes(data);
		result.setApplication(StringConv.UTF8(bytes.get(0)));
		result.setNameSpace(StringConv.UTF8(bytes.get(1)));
		result.setMacAddress(bytes.get(2));
		return result;
	}
}
