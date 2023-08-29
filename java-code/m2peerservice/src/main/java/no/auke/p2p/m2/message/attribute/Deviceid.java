/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.message.attribute;

import no.auke.p2p.m2.message.Utility;
import no.auke.p2p.m2.message.UtilityException;
import no.auke.util.StringConv;

public class Deviceid extends MessageAttribute {
	String deviceid;
	public Deviceid() {
		super(MessageAttribute.MessageAttributeType.Deviceid);
	}
	public Deviceid(String password) {
		super(MessageAttribute.MessageAttributeType.Deviceid);
		setPassword(password);
	}
	public String getPassword() {
		return deviceid;
	}
	public void setPassword(String deviceid) {
		this.deviceid = deviceid;
	}
	// TODO: Check out getLength
	// public int getLength() throws UtilityException {return
	// password.length()+4;}
	public byte[] getBytes() throws UtilityException {
		if (bytes == null) {
			deviceid = deviceid.trim();
			int length = deviceid.length();
			if ((length % 4) != 0) {
				length += 4 - (length % 4);
			}
			length += 4;
			byte[] result = new byte[length];
			// message attribute header
			// type
			System.arraycopy(Utility.integerToTwoBytes(typeToInteger(type)), 0, result, 0, 2);
			// length
			System.arraycopy(Utility.integerToTwoBytes(length - 4), 0, result, 2, 2);
			// password header
			byte[] temp = StringConv.getBytes(deviceid);
			System.arraycopy(temp, 0, result, 4, temp.length);
			bytes = result;
		}
		return bytes;
	}
	public static Deviceid parse(byte[] data) {
		Deviceid result = new Deviceid();
		String password = StringConv.UTF8(data);
		result.setPassword(password);
		return result;
	}
}