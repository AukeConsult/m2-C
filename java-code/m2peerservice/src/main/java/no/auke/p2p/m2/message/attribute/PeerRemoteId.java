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

public class PeerRemoteId extends MessageAttribute {
	String id;
	public PeerRemoteId() {
		super(MessageAttribute.MessageAttributeType.Peer_Remote_Id);
	}
	public PeerRemoteId(String id) {
		super(MessageAttribute.MessageAttributeType.Peer_Remote_Id);
		setId(id);
	}
	public String getId() {
		return id.trim();
	}
	public void setId(String userid) {
		this.id = userid.trim();
	}
	public byte[] getBytes() throws UtilityException {
		if (bytes == null) {
			id = id.trim();
			int length = id.length();
			// userid header
			if ((length % 4) != 0) {
				length += 4 - (length % 4);
			}
			// message attribute header
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
	public static PeerRemoteId parse(byte[] data) {
		PeerRemoteId result = new PeerRemoteId();
		String id = StringConv.UTF8(data);
		result.setId(id);
		return result;
	}
}