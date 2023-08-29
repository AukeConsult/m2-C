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

public class Dummy extends MessageAttribute {
	int lengthValue;
	public Dummy() {
		super(MessageAttributeType.Dummy);
	}
	public void setLengthValue(int length) {
		this.lengthValue = length;
	}
	public byte[] getBytes() throws UtilityException {
		byte[] result = new byte[lengthValue + 4];
		// message attribute header
		// type
		System.arraycopy(Utility.integerToTwoBytes(typeToInteger(type)), 0, result, 0, 2);
		// length
		System.arraycopy(Utility.integerToTwoBytes(lengthValue), 0, result, 2, 2);
		return result;
	}
	public static Dummy parse(byte[] data) {
		Dummy dummy = new Dummy();
		dummy.setLengthValue(data.length);
		return dummy;
	}
}
