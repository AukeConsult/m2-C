/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.message.attribute;

import java.util.ArrayList;
import java.util.List;

import no.auke.p2p.m2.message.UtilityException;
import no.auke.util.ByteUtil;

// LHA: attribute is for getting replies of missing packets
// 
public class DataReplyPacket extends MessageAttribute {
	private int[] packetlist = new int[0];
	private int port = 0;
	private boolean complete = true;
	private int chunknumber = 0;
	private int chunkversion = 0;
	int msgid=0;

	public int getMsgId() {
		return msgid;
	}
	public void setMsgId(int msgid) {
		this.msgid = msgid;
	}

	public int getChunkVersion() {
		return chunkversion;
	}
	public void setChunkVersion(int chunkversion) {
		this.chunkversion = chunkversion;
	}
	public int getChunkNumber() {
		return chunknumber;
	}
	public void setChunkNumber(int number) {
		this.chunknumber = number;
	}
	public boolean isComplete() {
		return complete;
	}
	public void setComplete(boolean complete) {
		this.complete = complete;
	}
	public int[] getPacketList() {
		return packetlist;
	}
	public void setPacketList(int[] packetlist) {
		this.packetlist = packetlist;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public DataReplyPacket() {
		super(MessageAttribute.MessageAttributeType.DataReplyPacket);
	}
	public DataReplyPacket(int port, byte[] data) throws Exception {
		super(MessageAttribute.MessageAttributeType.DataReplyPacket);
		setPort(port);
	}
	public void addPacketnum(int packetnum) {
		int[] packetlist_new = new int[packetlist.length + 1];
		for (int i = 0; i < packetlist.length; i++) {
			packetlist_new[i] = packetlist[i];
		}
		packetlist_new[packetlist.length] = packetnum;
		packetlist = packetlist_new;
		complete = false;
	}
	public DataReplyPacket(int port) {
		super(MessageAttribute.MessageAttributeType.DataReplyPacket);
		setPort(port);
	}
	public byte[] getBytes() throws UtilityException {
		if (bytes == null) {
			List<byte[]> packetbytes = new ArrayList<byte[]>();
			for (int i : packetlist) {
				packetbytes.add(ByteUtil.getBytes(i));
			}
			byte[] packetlistbytes = ByteUtil.mergeBytes(packetbytes);
			byte[] databytes = ByteUtil.mergeDynamicBytesWithLength(
						ByteUtil.getBytes(port, 2), 
						complete ? new byte[] { 1 } : new byte[] { 0 },
						packetlistbytes, 
						ByteUtil.getBytes(chunknumber, 4), 
						new byte[] { (byte) chunkversion },
						ByteUtil.getBytes(msgid, 4)
					);
			bytes = ByteUtil.mergeBytes(ByteUtil.getBytes(typeToInteger(type), 2), ByteUtil.getBytes(databytes.length, 2), databytes);
		}
		return bytes;
	}
	public static DataReplyPacket parse(byte[] data) {
		
		DataReplyPacket result = new DataReplyPacket();
		List<byte[]> bytes = ByteUtil.splitDynamicBytes(data);
		result.setPort(ByteUtil.getInt(bytes.get(0)));
		result.setComplete(bytes.get(1)[0] == 1 ? true : false);
		
		List<byte[]> packetnumbytes = ByteUtil.splitBytesWithFixedLength(bytes.get(2), 4);
		int[] packetlist = new int[packetnumbytes.size()];
		for (int x = 0; x < packetlist.length; x++) {
			packetlist[x] = ByteUtil.getInt(packetnumbytes.get(x));
		}
		result.setPacketList(packetlist);
		result.setChunkNumber(ByteUtil.getInt(bytes.get(3)));
		result.setChunkVersion(ByteUtil.getInt(bytes.get(4)));
		result.setMsgId(ByteUtil.getInt(bytes.get(5)));
		
		return result;
	}
}
