/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.message.attribute;

import java.util.List;

import no.auke.p2p.m2.message.UtilityException;
import no.auke.util.ByteUtil;
import no.auke.util.StringConv;

public class MapFile2 extends MessageAttribute {
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
	// Verification
	private String certificateid = "";
	public String getCertificateid() {
		return certificateid;
	}
	public void setCertificateid(String certificateid) {
		this.certificateid = certificateid;
	}
	private String token = "";
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public MapFile2() {
		super(MessageAttribute.MessageAttributeType.MapFile);
	}
	public byte[] getBytes() throws UtilityException {
		if (bytes == null) {
			byte[] main = ByteUtil.mergeDynamicBytesWithLength(ByteUtil.getBytes(version, 4), ByteUtil.getBytes(function, 2),
					(filetext != "" ? StringConv.getBytes(filetext) : new byte[0]), (certificateid != "" ? StringConv.getBytes(certificateid) : new byte[0]),
					(token != "" ? StringConv.getBytes(token) : new byte[0]));
			main = ByteUtil.mergeBytes(ByteUtil.getBytes(main.length, 2), main); // add
																					// length
			bytes = ByteUtil.mergeBytes(ByteUtil.getBytes(typeToInteger(type), 2), main);
		}
		return bytes;
	}
	public static MapFile2 parse(byte[] data) // throws
												// MessageAttributeParsingException
												// {
	{
		MapFile2 result = new MapFile2();
		List<byte[]> bytes = ByteUtil.splitDynamicBytes(data);
		result.setVersion(ByteUtil.getInt(bytes.get(0)));
		result.setFunction(ByteUtil.getInt(bytes.get(1)));
		result.setFiletext(bytes.size() > 2 ? StringConv.UTF8(bytes.get(2)) : "");
		result.setCertificateid(bytes.size() > 3 ? StringConv.UTF8(bytes.get(3)) : "");
		result.setToken(bytes.size() > 4 ? StringConv.UTF8(bytes.get(4)) : "");
		return result;
	}
}
