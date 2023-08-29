/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
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
import no.auke.util.ByteUtil;

// LHA: attribute is for getting replies of missing packets
// 
public class SerialReply extends MessageAttribute {
	private int[] packetlist = new int[0];
	private int port = 0;
	public int getLastnumber() {
		return lastnumber;
	}
	public void setLastnumber(int lastnumber) {
		this.lastnumber = lastnumber;
	}
	public int getOknumber() {
		return oknumber;
	}
	public void setOknumber(int oknumber) {
		this.oknumber = oknumber;
	}
	private int lastnumber = 0;
	private int oknumber = 0;
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
	int seqNumber = 0;
	public int getSeqNumber() {
		return seqNumber;
	}
	public void setSeqNumber(int seqNumber) {
		this.seqNumber = seqNumber;
	}
	public SerialReply() {
		super(MessageAttribute.MessageAttributeType.SerialReply);
	}
	public SerialReply(int port, byte[] data) throws Exception {
		super(MessageAttribute.MessageAttributeType.SerialReply);
		setPort(port);
	}
	public void addPacketnum(int packetnum) {
		int[] packetlist_new = new int[packetlist.length + 1];
		for (int i = 0; i < packetlist.length; i++) {
			packetlist_new[i] = packetlist[i];
		}
		packetlist_new[packetlist.length] = packetnum;
		packetlist = packetlist_new;
	}
	public SerialReply(int port) {
		super(MessageAttribute.MessageAttributeType.SerialReply);
		setPort(port);
	}
	public SerialReply clone() {
		return SerialReply.parse(this.getDataBytes());
	}
	public byte[] getDataBytes() {
		if (packetlist == null) {
			packetlist = new int[0];
		}
		List<byte[]> packetbytes = new ArrayList<byte[]>();
		for (int i : packetlist) {
			packetbytes.add(ByteUtil.getBytes(i));
		}
		return ByteUtil.mergeDynamicBytesWithLength(ByteUtil.getBytes(port, 2), ByteUtil.getBytes(seqNumber, 4), ByteUtil.getBytes(lastnumber, 4),
				ByteUtil.getBytes(oknumber, 4), ByteUtil.mergeBytes(packetbytes));
	}
	public byte[] getBytes() {
		if (bytes == null) {
			byte[] databytes = getDataBytes();
			bytes = ByteUtil.mergeBytes(ByteUtil.getBytes(typeToInteger(type), 2), ByteUtil.getBytes(databytes.length, 2), databytes);
		}
		return bytes;
	}
	public static SerialReply parse(byte[] data) {
		SerialReply result = new SerialReply();
		List<byte[]> bytes = ByteUtil.splitDynamicBytes(data);
		result.setPort(ByteUtil.getInt(bytes.get(0)));
		result.setSeqNumber(ByteUtil.getInt(bytes.get(1)));
		result.setLastnumber(ByteUtil.getInt(bytes.get(2)));
		result.setOknumber(ByteUtil.getInt(bytes.get(3)));
		List<byte[]> packetnumbytes = ByteUtil.splitBytesWithFixedLength(bytes.get(4), 4);
		int[] packetlist = new int[packetnumbytes.size()];
		for (int x = 0; x < packetlist.length; x++) {
			packetlist[x] = ByteUtil.getInt(packetnumbytes.get(x));
		}
		result.setPacketList(packetlist);
		return result;
	}
}
