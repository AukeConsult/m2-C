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

public class MapFile extends MessageAttribute {
	private long version;
	public void setVersion(long version) {
		this.version = version;
	}
	public long getVersion() {
		return version;
	}
	private int function;
	public void setFunction(int function) {
		this.function = function;
	}
	public int getFunction() {
		return function;
	}
	private String filetext = "";
	public void setFiletext(String filetext) {
		this.filetext = filetext;
	}
	public String getFiletext() {
		return filetext;
	}
	public MapFile() {
		super(MessageAttribute.MessageAttributeType.MapFile);
	}
	// public int getLength() throws UtilityException {return
	// filetext.length()+12;}
	public byte[] getBytes() throws UtilityException {
		if (bytes == null) {
			filetext = filetext.trim();
			int length = (filetext != "" ? filetext.length() : 0);
			if ((length % 4) != 0) {
				length += 4 - (length % 4);
			}
			length += 14;
			byte[] result = new byte[length];
			System.arraycopy(Utility.integerToTwoBytes(typeToInteger(type)), 0, result, 0, 2);
			System.arraycopy(Utility.integerToTwoBytes(length - 4), 0, result, 2, 2);
			byte[] versionArray = Utility.longToEightBytes(version);
			System.arraycopy(versionArray, 0, result, 4, versionArray.length);
			System.arraycopy(Utility.integerToTwoBytes(function), 0, result, 12, 2);
			if (filetext != "") {
				byte[] fileArray = StringConv.getBytes(getFiletext());
				System.arraycopy(fileArray, 0, result, 14, fileArray.length);
			}
			bytes = result;
		}
		return bytes;
	}
	public static MapFile parse(byte[] data) throws MessageAttributeParsingException {
		MapFile result = new MapFile();
		try {
			if (data.length < 8) {
				throw new MessageAttributeParsingException("Data array too short");
			}
			result.setVersion(Utility.eightBytesToLong(data));
			{
				byte[] temp = new byte[2];
				System.arraycopy(data, 8, temp, 0, 2);
				result.setFunction(Utility.twoBytesToInteger(temp));
			}
			if (data.length > 10) {
				byte[] temp = new byte[data.length - 10];
				System.arraycopy(data, 10, temp, 0, data.length - 10);
				result.setFiletext(StringConv.UTF8(temp));
			}
			return result;
		} catch (UtilityException ue) {
			throw new MessageAttributeParsingException("Parsing error");
		} catch (MessageAttributeException mae) {
			throw new MessageAttributeParsingException("Parsing error");
		}
	}
}
