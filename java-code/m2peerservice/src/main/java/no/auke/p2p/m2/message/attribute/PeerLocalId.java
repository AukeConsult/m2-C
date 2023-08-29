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

public class PeerLocalId extends MessageAttribute {
	String id;
	public PeerLocalId() {
		super(MessageAttribute.MessageAttributeType.Peer_Local_Id);
	}
	public PeerLocalId(String id) {
		super(MessageAttribute.MessageAttributeType.Peer_Local_Id);
		setUsername(id);
	}
	public String getUsername() {
		return id.trim();
	}
	public void setUsername(String username) {
		this.id = username.trim();
	}
	// TODO: Check out getLength
	// public int getLength() throws UtilityException {return
	// username.length()+4;}
	public byte[] getBytes() throws UtilityException {
		if (bytes == null) {
			id = id.trim();
			int length = id.length();
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
			// username header
			byte[] temp = StringConv.getBytes(id);
			System.arraycopy(temp, 0, result, 4, temp.length);
			bytes = result;
		}
		return bytes;
	}
	public static PeerLocalId parse(byte[] data) {
		PeerLocalId result = new PeerLocalId();
		String id = StringConv.UTF8(data);
		result.setUsername(id);
		return result;
	}
}
