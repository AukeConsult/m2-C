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

public class SerialPacket extends MessageAttribute {
	byte[] data;
	int port = 0;
	int chunkSize = 0;
	int seqNumber = 0;
	byte func = 0;
	public byte getFunc() {
		return func;
	}
	public void setFunc(byte func) {
		this.func = func;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public int getChunckSize() {
		return chunkSize;
	}
	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}
	public int getSeqNumber() {
		return seqNumber;
	}
	public void setSeqNumber(int seqNumber) {
		this.seqNumber = seqNumber;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public SerialPacket() {
		super(MessageAttribute.MessageAttributeType.SerialPacket);
	}
	public SerialPacket(int port, byte[] data) throws Exception {
		super(MessageAttribute.MessageAttributeType.SerialPacket);
		setData(data);
		setPort(port);
		setChunkSize(0);
		setSeqNumber(0);
		setFunc((byte) 0);
	}
	public void clearData() {
		setData(null);
	}
	public SerialPacket(int port) {
		super(MessageAttribute.MessageAttributeType.SerialPacket);
		setPort(port);
	}
	public byte[] getDataBytes() {
		return ByteUtil.mergeDynamicBytesWithLength(ByteUtil.getBytes(port, 2), ByteUtil.getBytes(chunkSize, 4), ByteUtil.getBytes(seqNumber, 4),
				new byte[] { func }, data);
	}
	public byte[] getBytes() {
		if (bytes == null) {
			byte[] databytes = getDataBytes();
			bytes = ByteUtil.mergeBytes(ByteUtil.getBytes(typeToInteger(type), 2), ByteUtil.getBytes(databytes.length, 2), databytes);
		}
		return bytes;
	}
	public SerialPacket clone() {
		return SerialPacket.parse(getDataBytes());
	}
	public static SerialPacket parse(byte[] data) {
		SerialPacket result = new SerialPacket();
		List<byte[]> bytes = ByteUtil.splitDynamicBytes(data);
		if (bytes.size() > 3) {
			result.setPort(ByteUtil.getInt(bytes.get(0)));
			result.setChunkSize(ByteUtil.getInt(bytes.get(1)));
			result.setSeqNumber(ByteUtil.getInt(bytes.get(2)));
			result.setFunc(bytes.get(3)[0]);
			result.setData(bytes.get(4));
		}
		return result;
	}
}
