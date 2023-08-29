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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.message.attribute.DataPacket;
import no.auke.p2p.m2.message.attribute.DataReplyPacket;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.workers.keepalive.NetAddress;
import no.auke.p2p.m2.workers.message.MessageSender;
import no.auke.p2p.m2.InitVar;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class ChunkOut {
	
	//private static final Logger logger = LoggerFactory.getLogger(ChunkOutgoing2.class);
	private Socket socket;
	private MessageSender channel;
	
	private SocketBufferOut socketbuffer;
	private SendStatistics stat;

	private Map<Integer, DataPacket> datapackets = new ConcurrentHashMap<Integer, DataPacket>();
	public int getNumDataPackets() {return datapackets.size();}
	public List<DataPacket> getDatapackets() {
		List<DataPacket> list = new LinkedList<DataPacket>();
		List<Integer> set = new LinkedList<Integer>(datapackets.keySet());
		Collections.sort(set);
		for (int i : set) {
			list.add(datapackets.get(i));
		}
		return list;
	}

	private int firstPacketNum = 0;
	public int getFirstPacketNum() {return firstPacketNum;}
	
	private int chunkversion = 0;
	public int getChunkVersion() {return chunkversion;}
	
	private int chunknum = 0;
	public int getChunkNumber() {return chunknum;}

	//public int getChunkId() {return (chunknum * 255) + chunkversion;}
	
	private int chunksize = 0;
	public int getChunkSize() {return chunksize;}

	private AtomicLong startSendout = new AtomicLong();
	private AtomicLong lastSendout = new AtomicLong();
	private AtomicLong abortTime = new AtomicLong();
	
	private AtomicBoolean resend_packets = new AtomicBoolean();
	public void setAbortTime(long time) {abortTime.set(time);}
	
	private AtomicBoolean complete = new AtomicBoolean();
	public boolean isComplete() {return complete.get();}
	
	private AtomicBoolean sent = new AtomicBoolean();
	private AtomicBoolean replied = new AtomicBoolean();
	public boolean isReplied() {return replied.get();}
	
	private int sentbytes = 0;
	public int getSentBytes() {return sentbytes;}
	
	// send chunk after a while (timeout) typically 1000 ms
	public boolean doResend(long timeout) {
		
		if (resend_packets.getAndSet(false)) {
			return !complete.get();
		} else if (sent.get() && !replied.get() && !complete.get()) {
			
			long resend_time = System.currentTimeMillis() - lastSendout.get();
			if (resend_time > timeout) {
				// update sent statistic with 0 bytes reply
				if (!complete.get())
					stat.getSpeedCalc().replyChunk(startSendout.get(), lastSendout.get(), sentbytes, 0, false);
				return !complete.get();
			}
			
		}
		return false;
	}
	public boolean doAbort(long abort_timeout, int num_trials) {
		
		if (sent.get() && !replied.get() && !complete.get() && abortTime.get() > 0) {

			// LHA: will possibly never happend
			long abort_time = (System.currentTimeMillis() - abortTime.get());
			if (abort_time > abort_timeout) {
				if (!complete.get())
					stat.getSpeedCalc().replyChunk(startSendout.get(), lastSendout.get(), sentbytes, 0, false);
				return !complete.get();
			} else if (getChunkVersion() > num_trials) {
				// likely to about because of re-trials
				// update sent statistic with 0 bytes reply
				if (!complete.get())
					stat.getSpeedCalc().replyChunk(startSendout.get(), lastSendout.get(), sentbytes, 0, false);
				return !complete.get();
			}
		}
		return false;
	}
	public ChunkOut(Socket socket, SocketBufferOut socketbuffer) {
		
		this.socket = socket;
		this.channel = socket.getNameSpace().getMessageSender();
		this.socketbuffer = socketbuffer;
		
		startSendout.set(0);
		lastSendout.set(0);
		// firstsendout.set(0);
		sent.set(false);
		replied.set(false);
		complete.set(false);
		resend_packets.set(false);
		this.chunksize = InitVar.CHUNK_SIZE;
		stat = socketbuffer.getStat();
	}
	
	// public ChunkOutgoing2(Socket socket, SocketBufferOutgoing2 socketbuffer,
	// int chunkversion, int chunksize, long firstsendout, boolean complete) {
	public ChunkOut(Socket socket, SocketBufferOut socketbuffer, int chunkversion, int chunksize, boolean complete) {
		this(socket, socketbuffer);
		this.chunkversion = chunkversion;
		this.chunksize = chunksize;
		// this.firstsendout.set(firstsendout);
		this.complete.set(complete);
	}
	
	public boolean addOutgoingPacket(DataPacket packet) {
		if (packet.getNumber() > 0 && !datapackets.containsKey(packet.getNumber())) {
			if (firstPacketNum == 0) {
				firstPacketNum = packet.getNumber();
			}
			chunknum = ((packet.getNumber() - 1) / chunksize) + 1;
			packet.setChunkNumber(chunknum);
			packet.setChunkVersion(chunkversion);
			packet.setChunkSize(chunksize);
			datapackets.put(packet.getNumber(), packet);
			return true;
		}
		return false;
	}
	public ChunkOut cloneNewVersion() {
		// ChunkOutgoing2 new_version = new ChunkOutgoing2(this.socket,
		// this.socketbuffer, chunkversion + 1, chunksize, lastSendout.get(),
		// complete.get());
		ChunkOut new_version = new ChunkOut(this.socket, this.socketbuffer, chunkversion + 1, chunksize, complete.get());
		for (DataPacket packet : datapackets.values()) {
			DataPacket packet_new = packet.clone();
			new_version.addOutgoingPacket(packet_new);
		}
		return new_version;
	}
	public boolean send(int tranactionId, NetAddress address) {
		
		try {
		
			stat.sendingChunk(getChunkVersion() > 0);
			stat.getDelaySpeed().incrClient();
			stat.getSpeedCalc().calculate();
			startSendout.set(System.currentTimeMillis());
			int numsent = 0;
			
			for (DataPacket packet : getDatapackets()) {
				
				if(packet!=null) {
					// send data
					MessageHeader dataMH = new MessageHeader(MessageHeader.MessageHeaderType.Data);
					dataMH.setTransactionID(tranactionId);
					dataMH.addMessageAttribute(packet);
					dataMH.setAddress(address);
					if (!complete.get()) {

						if (channel.UDPSendEncrypt_Data(dataMH)) {
							numsent++;
							lastSendout.set(System.currentTimeMillis());
							stat.sentPacket(dataMH.getBytes().length, getChunkVersion() > 0);
							sentbytes += dataMH.getBytes().length;
							if (getNumDataPackets() > numsent)
								stat.getDelaySpeed().doDelayPacketSend(); // wait to throttle speed
						} else {
							return false;
						}
					} else {
						break;
					}					
				}
			}
			
			sent.set(true);
			return true;
			
		} catch (Exception ex) {
			ex.printStackTrace();	
			return false;
		} finally {
			stat.getSpeedCalc().sendChunk(sentbytes);
			stat.getDelaySpeed().decrClient();
			stat.getSpeedCalc().calculate();
		}
	}
	//
	// got reply from data sending
	// also used to measure the timing
	//
	public boolean gotReply(final MessageHeader receiveMH, DataReplyPacket packet) {
		
		replied.set(true); // <- flag got reply
		if (!complete.get()) {
		
			// check reply
			// got packet complete from other side
			// chunk is completed
			
			if (packet.isComplete()) {
				// set the chunk completed
				// a packet with completed is returned
				stat.replySending(lastSendout.get());
				if (!complete.getAndSet(true)) {
					// all sent, clear list and data buffers
					datapackets.clear();
				}
				stat.getSpeedCalc().replyChunk(startSendout.get(), lastSendout.get(), sentbytes, sentbytes, true);
			
			} else {
				// not complete reply
				// Resend missing packets and update send statistics
				int resendbytes = 0;
				Map<Integer, DataPacket> datapackets_resent = new ConcurrentHashMap<Integer, DataPacket>();
				for (Integer packetnum : packet.getPacketList()) {
					if (datapackets.containsKey(packetnum)) {
						DataPacket packet_out = datapackets.get(packetnum);
						datapackets_resent.put(packet_out.getNumber(), packet_out);
						resendbytes += packet_out.getBytes().length;
					}
				}
				// when resend, reduce the reported number of bytes send, by
				// the length of the number need to resend
				// set new send bytes = bytes resent to get correct
				// statistics when complete
				if (datapackets_resent.size() > 0) {
					// in case some mixup, and no packet for re-send,
					// keep packet list for next chunk re-send
					datapackets = datapackets_resent;
				}
				stat.getSpeedCalc().replyChunk(startSendout.get(), lastSendout.get(), sentbytes, sentbytes - resendbytes, true);
				resend_packets.set(true); // <- flag for re-send
				replied.set(false);
			}
			stat.getSpeedCalc().calculate();
			return true;
		}
		return false;
	}
}
