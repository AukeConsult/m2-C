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

public class LocalAddress extends IpAddress {
	public LocalAddress() {
		super(MessageAttribute.MessageAttributeType.LocalAddress);
	}
	public LocalAddress(String address, int port) {
		this();
		try {
			setAddress(new Address(address));
			setPort(port);
		} catch (UtilityException e) {} catch (MessageAttributeException e) {}
	}
	public LocalAddress(NetAddress netAddress) {
		this(netAddress.getAddress(), netAddress.getPort());
	}
	public static MessageAttribute parse(byte[] data) throws MessageAttributeParsingException {
		LocalAddress sa = new LocalAddress();
		IpAddress.parse(sa, data);
		return sa;
	}
}