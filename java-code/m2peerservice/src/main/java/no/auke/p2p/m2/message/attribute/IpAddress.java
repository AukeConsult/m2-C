/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.message.attribute;

import no.auke.p2p.m2.message.Address;
import no.auke.p2p.m2.message.Utility;
import no.auke.p2p.m2.message.UtilityException;

public class IpAddress extends MessageAttribute {
	int port;
	Address address;
	public IpAddress() {
		super();
		try {
			port = 0;
			address = new Address("0.0.0.0");
		} catch (UtilityException ue) {
			ue.getMessage();
			ue.printStackTrace();
		}
	}
	public IpAddress(MessageAttribute.MessageAttributeType type) {
		super(type);
	}
	public int getPort() {
		return port;
	}
	public boolean isNull() {
		return address == null || address.toString().equals("0.0.0.0:0");
	}
	public Address getAddress() {
		try {
			return address == null ? new Address(0, 0, 0, 0) : address;
		} catch (UtilityException e) {
			;
		}
		return null;
	}
	public void setPort(int port) throws MessageAttributeException {
		if ((port > 65536) || (port < 0)) {
			throw new MessageAttributeException("Port value " + port + " out of range.");
		}
		this.port = port;
	}
	public void setAddress(String address, int port) {
		try {
			this.address = new Address(address);
			this.port = port;
		} catch (UtilityException e) {}
	}
	public void setAddress(Address address) {
		this.address = address;
	}
	public byte[] getBytes() throws UtilityException {
		if (bytes == null) {
			byte[] result = new byte[12];
			System.arraycopy(Utility.integerToTwoBytes(typeToInteger(type)), 0, result, 0, 2);
			System.arraycopy(Utility.integerToTwoBytes(8), 0, result, 2, 2);
			// family
			result[5] = Utility.integerToOneByte(0x01);
			// port
			System.arraycopy(Utility.integerToTwoBytes(port), 0, result, 6, 2);
			// address
			System.arraycopy(address == null ? new byte[4] : address.getBytes(), 0, result, 8, 4);
			bytes = result;
		}
		return bytes;
	}
	protected static IpAddress parse(IpAddress ma, byte[] data) throws MessageAttributeParsingException {
		try {
			if (data.length < 8) {
				throw new MessageAttributeParsingException("Data array too short");
			}
			int family = Utility.oneByteToInteger(data[1]);
			if (family != 0x01)
				throw new MessageAttributeParsingException("Family " + family + " is not supported");
			byte[] portArray = new byte[2];
			System.arraycopy(data, 2, portArray, 0, 2);
			ma.setPort(Utility.twoBytesToInteger(portArray));
			int firstOctet = Utility.oneByteToInteger(data[4]);
			int secondOctet = Utility.oneByteToInteger(data[5]);
			int thirdOctet = Utility.oneByteToInteger(data[6]);
			int fourthOctet = Utility.oneByteToInteger(data[7]);
			ma.setAddress(new Address(firstOctet, secondOctet, thirdOctet, fourthOctet));
			return ma;
		} catch (UtilityException ue) {
			throw new MessageAttributeParsingException("Parsing error");
		} catch (MessageAttributeException mae) {
			throw new MessageAttributeParsingException("Port parsing error");
		}
	}
	public String toString() {
		return address.toString() + ":" + port;
	}
}