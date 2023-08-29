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

public class ErrorCode extends MessageAttribute {
	int responseCode;
	String reason;
	public ErrorCode() {
		super(MessageAttribute.MessageAttributeType.ErrorCode);
	}
	public ErrorCode(int responseCode) throws MessageAttributeException {
		super(MessageAttribute.MessageAttributeType.ErrorCode);
		setResponseCode(responseCode);
	}
	public void setResponseCode(int responseCode) throws MessageAttributeException {
		switch (responseCode) {
		case 400:
			reason = "Bad Request";
			break;
		case 401:
			reason = "Unauthorized";
			break;
		case 420:
			reason = "Unkown Attribute";
			break;
		case 430:
			reason = "Stale Credentials";
			break;
		case 431:
			reason = "Integrity Check Failure";
			break;
		case 432:
			reason = "Missing userName";
			break;
		case 433:
			reason = "Use TLS";
			break;
		case 500:
			reason = "Server Error";
			break;
		case 600:
			reason = "Global Failure";
			break;
		case 700:
			reason = "OK";
			break;
		case 701:
			reason = "Send user name";
			break;
		case 702:
			reason = "No peer user name";
			break;
		case 703:
			reason = "Bye bye";
			break;
		case 704:
			reason = "Userid is loggon on from another device";
			break;
		case 705:
			reason = "No port";
			break;
		case 706:
			reason = "Socket busy";
			break;
		case 707:
			reason = "No middleman avail";
			break;
		case 709:
			reason = "Timeout from party";
			break;
		case 710:
			reason = "No Connect";
			break;
		case 711:
			reason = "Session is stopped";
			break;
		case 712:
			reason = "Session not found";
			break;
		case 715:
			reason = "No stream port";
			break;
		case 716:
			reason = "Reset encryption";
			break;
		case 790:
			reason = "general error";
			break;
		default:
			throw new MessageAttributeException("Error response code is not valid");
		}
		this.responseCode = responseCode;
	}
	public int getResponseCode() {
		return responseCode;
	}
	public String getReason() {
		return reason;
	}
	public byte[] getBytes() throws UtilityException {
		if (bytes == null) {
			int length = reason.length();
			// length adjustment
			if ((length % 4) != 0) {
				length += 4 - (length % 4);
			}
			// message attribute header
			length += 6;
			byte[] result = new byte[length];
			// message attribute header
			// type
			System.arraycopy(Utility.integerToTwoBytes(typeToInteger(type)), 0, result, 0, 2);
			// length
			System.arraycopy(Utility.integerToTwoBytes(length - 4), 0, result, 2, 2);
			// error code header
			int classHeader = (int) Math.floor(((double) responseCode) / 100);
			result[4] = Utility.integerToOneByte(classHeader);
			result[5] = Utility.integerToOneByte(responseCode % 100);
			byte[] reasonArray = StringConv.getBytes(reason);
			System.arraycopy(reasonArray, 0, result, 6, reasonArray.length);
			bytes = result;
		}
		return bytes;
	}
	public static ErrorCode parse(byte[] data) throws MessageAttributeParsingException {
		try {
			if (data.length < 4) {
				throw new MessageAttributeParsingException("Data array too short");
			}
			byte classHeaderByte = data[0];
			int classHeader = Utility.oneByteToInteger(classHeaderByte);
			if ((classHeader < 1) || (classHeader > 9))
				throw new MessageAttributeParsingException("Class parsing error");
			byte numberByte = data[1];
			int number = Utility.oneByteToInteger(numberByte);
			if ((number < 0) || (number > 99))
				throw new MessageAttributeParsingException("Number parsing error");
			int responseCode = (classHeader * 100) + number;
			ErrorCode result = new ErrorCode();
			result.setResponseCode(responseCode);
			return result;
		} catch (UtilityException ue) {
			throw new MessageAttributeParsingException("Parsing error");
		} catch (MessageAttributeException mae) {
			throw new MessageAttributeParsingException("Parsing error");
		}
	}
}
