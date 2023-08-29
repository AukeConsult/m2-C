/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.sockets.udp;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.message.attribute.DataPacket;
import no.auke.p2p.m2.message.attribute.DataReplyPacket;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.workers.keepalive.NetAddress;
import no.auke.util.ByteUtil;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class ChunkIn {
	
	//private static final Logger logger = LoggerFactory.getLogger(ChunkIncoming.class);
	
	private Socket socket;
	private SocketBuffer buffer;
	private NetAddress replyAddress;
	
	public NetAddress getReplyAddress() {return replyAddress;}
	private Map<Integer, DataPacket> datapackets;
	public Map<Integer, DataPacket> getDatapackets() {return datapackets;}
	
	public boolean isComplete() {return datapackets.size() == (lastpacket.get() - firstpacket.get()) + 1;}

	private AtomicInteger lastpacket = new AtomicInteger();
	private AtomicInteger firstpacket = new AtomicInteger();
	private AtomicInteger totalpacket = new AtomicInteger();
	private AtomicBoolean ismissing = new AtomicBoolean();
	
	private int transactionId = 0;
	public int getTransactionId() {return transactionId;}
	
	private int chunkNum = 0;
	public int getChunkNumber() {return chunkNum;}
	private int chunkversion = 0;
	public int getChunkVersion() {return chunkversion;}
	private AtomicBoolean isReplied = new AtomicBoolean();;
	public boolean isReplied() {return isReplied.get();}
	public void setMissing() {ismissing.set(true);}
	public boolean isMissing() {return ismissing.get();}
	public int getChunkSize() {return ((lastpacket.get() - firstpacket.get()) + 1);}
	
	public ChunkIn(
			
			Socket socket, 
			SocketBuffer buffer, 
			Map<Integer, DataPacket> datapackets, 
			final int chunkNum, 
			int version, 
			int size,
			NetAddress replyAddress, 
			int transactionId
		
			) {
		
		this.datapackets = datapackets;
		this.chunkNum = chunkNum;
		this.chunkversion = version;
		this.totalpacket.set(0);
		
		this.lastpacket.set(chunkNum * size);
		this.firstpacket.set(((chunkNum - 1) * size) + 1);
		
		this.socket = socket;
		this.buffer = buffer;
		this.replyAddress = replyAddress;
		this.transactionId = transactionId;
		
		ismissing.set(false);
		isReplied.set(false);
		isWaitMissing.set(false);
	
	}
	
	private AtomicBoolean isWaitMissing = new AtomicBoolean();
	public boolean addIncomingPacket(DataPacket packet) {
		
		if (!datapackets.containsKey(packet.getNumber()) && packet.getNumber() >= firstpacket.get() && packet.getNumber() <= lastpacket.get()) {
		
			datapackets.put(packet.getNumber(), packet);
			totalpacket.set(packet.getTotal());
			
			if (lastpacket.get() > totalpacket.get()) {
				lastpacket.set(totalpacket.get());
			}
			// got last packet, but not complete
			if (packet.getNumber() == lastpacket.get() && datapackets.size() < getChunkSize()) {
				ismissing.set(true);
				replyMissing();
			}
			return true;
		}
		return false;
	}
	
	public void checkWaitToLong() {}
		
	public void replyMissing() {
		
		if (!isReplied.get() && ismissing.getAndSet(false) && !isWaitMissing.get()) {
						
			DataReplyPacket reply2 = new DataReplyPacket();
			for (int packetnum = firstpacket.get(); packetnum <= lastpacket.get(); packetnum++) {
				if (!datapackets.containsKey(packetnum)) {
					reply2.addPacketnum(packetnum);
				}
			}
			
			if (reply2.getPacketList().length > 0) {
				
				reply2.setMsgId(buffer.getMsgId());			
				reply2.setPort(socket.getPort());
				reply2.setChunkNumber(chunkNum);
				reply2.setChunkVersion(chunkversion);
			
				MessageHeader datareply = new MessageHeader(MessageHeader.MessageHeaderType.DataReply);
				datareply.setTransactionID(transactionId);
				datareply.setAddress(replyAddress);
				datareply.addMessageAttribute(reply2);
				
				// send immediately, but check for last time if necessary
				// all packets not awail
				if (datapackets.size() < (lastpacket.get() - firstpacket.get()) + 1) {
					isWaitMissing.set(true);
					socket.getNameSpace().getMessageSender().UDPSendEncrypt(datareply);
				}
				
			}
		}
	}
	public void replyComplete() {
		
		MessageHeader datareply = new MessageHeader(MessageHeader.MessageHeaderType.DataReply);
		datareply.setTransactionID(transactionId);
		datareply.setAddress(replyAddress);
		
		DataReplyPacket reply2 = new DataReplyPacket();
		reply2.setMsgId(buffer.getMsgId());			
		reply2.setPort(socket.getPort());
		reply2.setChunkNumber(chunkNum);
		reply2.setChunkVersion(chunkversion);
		datareply.addMessageAttribute(reply2);
		
		socket.getNameSpace().getMessageSender().UDPSendEncrypt(datareply);
		
		isReplied.set(true);
					
	}
	
	byte[] result = null;
	public byte[] getBuffer() {
		
		if (result == null) {
			
			if (isReplied.get()) {
			
				// fill buffer with packets received
				List<byte[]> list = new LinkedList<byte[]>();
				List<Integer> set = new LinkedList<Integer>(datapackets.keySet());
				Collections.sort(set);
				for (int i : set) {
					list.add(datapackets.get(i).getData());
				}
				result = ByteUtil.mergeBytes(list);
				datapackets.clear();
			}
		}
		
		return result;
	}
}
