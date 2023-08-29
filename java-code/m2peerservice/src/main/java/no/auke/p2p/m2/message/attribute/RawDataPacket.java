/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.message.attribute;

import java.util.List;
import no.auke.util.ByteUtil;

public class RawDataPacket extends MessageAttribute {
	
	byte[] data;
	int port = 0;
	long seqnumber = 0;
	long frequency = 0;
	short keyid=0;
	
	public short getKeyId() {return keyid;}
	public void setKeyId(short keyid) {this.keyid = keyid;}
	
	public byte[] getData() {return data;}
	public void setData(byte[] data) {this.data = data;}
	public long getSeqNumber() {return seqnumber;}
	public void setSeqNumber(long seqnumber) {this.seqnumber = seqnumber;}
	public int getPort() {return port;}
	public void setPort(int port) {this.port = port;}
	public long getFrequency() {return frequency;}
	public void setFrequency(long frequency) {this.frequency = frequency;}
	public RawDataPacket() {super(MessageAttribute.MessageAttributeType.RawData);}
	public RawDataPacket(int port, long seqnumber, int frequency, byte[] data, short keyid) {
		super(MessageAttribute.MessageAttributeType.RawData);
		setPort(port);
		setSeqNumber(seqnumber++);
		setData(data);
		setFrequency(frequency);
		setKeyId(keyid);
	}
	// made more efficient
	public byte[] getBytes() {
		if (bytes == null) {
			byte[] databytes = ByteUtil.mergeDynamicBytesWithLength(
					ByteUtil.getBytes(port, 2), 
					ByteUtil.getBytes(seqnumber, 8),
					ByteUtil.getBytes(frequency, 8), 
					data,
					ByteUtil.getBytes(keyid, 2) 
					);
			bytes = ByteUtil.mergeBytes(ByteUtil.getBytes(typeToInteger(type), 2), ByteUtil.getBytes(databytes.length, 2), databytes);
		}
		return bytes;
	}
	public RawDataPacket clone() {
		RawDataPacket new_packet = new RawDataPacket();
		new_packet.setPort(getPort());
		new_packet.setSeqNumber(getSeqNumber());
		new_packet.setFrequency(getFrequency());
		new_packet.setData(getData());
		return new_packet;
	}
	public static RawDataPacket parse(byte[] data) {
		RawDataPacket result = new RawDataPacket();
		List<byte[]> bytes = ByteUtil.splitDynamicBytes(data);
		result.setPort(ByteUtil.getInt(bytes.get(0)));
		result.setSeqNumber(ByteUtil.getLong(bytes.get(1)));
		result.setFrequency(ByteUtil.getLong(bytes.get(2)));
		result.setData(bytes.get(3));
		result.setKeyId(ByteUtil.getShort(bytes.get(4)));
		return result;
	}
}
