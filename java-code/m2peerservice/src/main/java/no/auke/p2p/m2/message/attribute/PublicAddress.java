/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.message.attribute;

import no.auke.p2p.m2.message.Address;
import no.auke.p2p.m2.message.UtilityException;
import no.auke.p2p.m2.workers.keepalive.NetAddress;

public class PublicAddress extends IpAddress {
	public PublicAddress() {
		super(MessageAttribute.MessageAttributeType.PublicAddress);
	}
	public PublicAddress(String address, int port) {
		this();
		try {
			setAddress(new Address(address));
			setPort(port);
		} catch (UtilityException e) {} catch (MessageAttributeException e) {}
	}
	public PublicAddress(NetAddress netAddress) {
		this(netAddress.getAddress(), netAddress.getPort());
	}
	public static MessageAttribute parse(byte[] data) throws MessageAttributeParsingException {
		PublicAddress ra = new PublicAddress();
		IpAddress.parse(ra, data);
		return ra;
	}
}
