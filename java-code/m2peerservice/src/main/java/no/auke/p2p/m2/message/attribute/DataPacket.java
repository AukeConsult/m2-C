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

public class DataPacket extends MessageAttribute {
	
	byte[] data;
	int port = 0;
	int total = 0;
	int number = 0;
	int chunknumber = 0;
	int chunkversion = 0;
	int chunksize = 0;
	int msgid=0;
	short keyid=0;

	public int getMsgId() {return msgid;}
	public void setMsgId(int msgid) {this.msgid = msgid;}	

	public short getKeyId() {return keyid;}
	public void setKeyId(short keyid) {this.keyid = keyid;}	
	
	public int getChunkSize() {return chunksize;}
	public void setChunkSize(int chunksize) {this.chunksize = chunksize;}
	public int getChunkId() {return (chunknumber * 255) + chunkversion;}
	public int getChunkVersion() {return chunkversion;}
	public void setChunkVersion(int chunkversion) {this.chunkversion = chunkversion;}
	public int getChunkNumber() {return chunknumber;}
	public void setChunkNumber(int chunknum) {this.chunknumber = chunknum;}
	public byte[] getData() {return data;}
	public void setData(byte[] data) {this.data = data;}
	public int getTotal() {return total;}
	public void setTotal(int total) {this.total = total;}
	public int getNumber() {return number;}
	public void setNumber(int number) {this.number = number;}
	public int getPort() {return port;}
	public void setPort(int port) {this.port = port;}
	public DataPacket() {super(MessageAttribute.MessageAttributeType.DataPacket);}
	public DataPacket(int port, byte[] data) throws Exception {
		super(MessageAttribute.MessageAttributeType.DataPacket);
		setData(data);
		setPort(port);
		setTotal(0);
		setNumber(0);
		setChunkNumber(0);
	}
	public void clearData() {
		setData(null);
	}
	public DataPacket(int port) {
		super(MessageAttribute.MessageAttributeType.DataPacket);
		setPort(port);
	}
	// made more efficient
	public byte[] getBytes() {
		if (bytes == null) {
			byte[] databytes = ByteUtil.mergeDynamicBytesWithLength(
					ByteUtil.getBytes(port, 2), 
					ByteUtil.getBytes(total, 4), 
					ByteUtil.getBytes(number, 4),
					data, 
					ByteUtil.getBytes(chunknumber, 4), 
					new byte[] { (byte) chunkversion }, 
					new byte[] { (byte) chunksize },
					ByteUtil.getBytes(msgid, 4),
					ByteUtil.getBytes(keyid, 2)
					);
			bytes = ByteUtil.mergeBytes(
					ByteUtil.getBytes(typeToInteger(type), 2), 
					ByteUtil.getBytes(databytes.length, 2), 
					databytes
					);
		}
		return bytes;
	}
	public DataPacket clone() {
		DataPacket new_packet = new DataPacket();
		new_packet.setPort(getPort());
		new_packet.setTotal(getTotal());
		new_packet.setNumber(getNumber());
		new_packet.setData(getData());
		new_packet.setChunkNumber(getChunkNumber());
		new_packet.setChunkVersion(getChunkVersion());
		new_packet.setChunkSize(getChunkSize());
		new_packet.setMsgId(getMsgId());
		new_packet.setKeyId(getKeyId());
		
		return new_packet;
	}
	public static DataPacket parse(byte[] data) {
		DataPacket result = new DataPacket();
		List<byte[]> bytes = ByteUtil.splitDynamicBytes(data);
		if (bytes.size() > 3) {
			result.setPort(ByteUtil.getInt(bytes.get(0)));
			result.setTotal(ByteUtil.getInt(bytes.get(1)));
			result.setNumber(ByteUtil.getInt(bytes.get(2)));
			result.setData(bytes.get(3));
			result.setChunkNumber(ByteUtil.getInt(bytes.get(4)));
			result.setChunkVersion(ByteUtil.getInt(bytes.get(5)));
			result.setChunkSize(ByteUtil.getInt(bytes.get(6)));
			result.setMsgId(ByteUtil.getInt(bytes.get(7)));
			result.setKeyId(ByteUtil.getShort(bytes.get(8)));
		}
		return result;
	}
}
