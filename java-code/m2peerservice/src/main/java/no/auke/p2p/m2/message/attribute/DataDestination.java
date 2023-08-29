/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.message.attribute;

import no.auke.p2p.m2.message.UtilityException;

public class DataDestination extends MessageAttribute {
	@Override
	public byte[] getBytes() throws UtilityException {
		return null;
	}
	public DataDestination() {
		super(MessageAttribute.MessageAttributeType.DataDestination);
	}
	public static DataDestination parse(byte[] data) throws MessageAttributeParsingException {
		DataDestination result = new DataDestination();
		try {
			if (data.length < 4) {
				throw new MessageAttributeParsingException("Data array too short");
			}
			/*
			 * result.setPort(Utility.twoBytesToInteger(data));
			 * result.setTotal(Utility.oneByteToInteger(data[2]));
			 * result.setNumber(Utility.oneByteToInteger(data[3]));
			 * 
			 * byte[] temp = new byte[data.length-4]; System.arraycopy(data, 4,
			 * temp, 0, data.length-4); result.setData(temp);
			 */
		} catch (MessageAttributeException mae) {
			throw new MessageAttributeParsingException("Parsing error");
		}
		return result;
	}
}
