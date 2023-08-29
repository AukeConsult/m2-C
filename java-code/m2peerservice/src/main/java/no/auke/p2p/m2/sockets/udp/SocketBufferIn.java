/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.sockets.udp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.p2p.m2.InitVar;
import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.SocketRetStatus;
import no.auke.p2p.m2.message.attribute.DataPacket;
import no.auke.p2p.m2.message.attribute.DataReplyPacket;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.workers.PeerSession;
import no.auke.util.ByteUtil;
import no.auke.util.StringConv;

public class SocketBufferIn extends SocketBuffer {
	
	static final Logger logger = LoggerFactory.getLogger(SocketBufferIn.class);
	
	private Map<Integer, ChunkIn> chuncks_incoming = new HashMap<Integer, ChunkIn>();
	private Map<Integer, ChunkIn> chuncks_to_check = new ConcurrentHashMap<Integer, ChunkIn>();
	private Map<Integer, ConcurrentHashMap<Integer, DataPacket>> chunk_datapackets = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, DataPacket>>();
	
	public ArrayList<ChunkIn> getChuncks_incoming() {return new ArrayList<ChunkIn>(chuncks_incoming.values());}
	
	public int getNumPackets() {
		int cnt = 0;
		for (ChunkIn chunk : chuncks_incoming.values()) {
			cnt += chunk.getDatapackets().size();
		}
		return cnt;
	}
	
	public int getLength() {
		int length = 0;
		for (ChunkIn chunk : new ArrayList<ChunkIn>(chuncks_incoming.values())) {
			for (DataPacket packet : chunk.getDatapackets().values()) {
				length += packet.getData().length;
			}
		}
		return length;
	}
	
	private AtomicBoolean aborted = new AtomicBoolean();
	@Override
	public boolean runCheck() {
		
		// loop the chunks to check is they are completely sent
		try {
			if (!closed.get() && !aborted.get() && chuncks_to_check.values().size() > 0) {
				
				for (ChunkIn chunk : new ArrayList<ChunkIn>(chuncks_to_check.values())) {
					if (!closed.get()) {
						// check abort, to long since last message
						if (System.currentTimeMillis() - last_time_packet.get() > InitVar.RECIEVE_TIMEOUT) {
							if (!closed.get() && !aborted.getAndSet(true)) {
								closed.set(true);
							}
						}
						if (!chunk.isComplete()) {
							chunk.checkWaitToLong();
							chunk.replyMissing();
						} else {
							chuncks_to_check.remove(chunk.getChunkNumber());
						}
					}
					
				}
				return true;
			}
		} catch (Exception ex) {
			//ex.printStackTrace();
		}
			
		return false;
	}
	public SocketBufferIn(final PeerSession peeragent, final SocketRetStatus ret, final Socket socket, final int msgId) {
		super("incoming", peeragent, ret, socket);
		
		this.msgId=msgId;
		this.peeragent = peeragent;
		this.port = socket.getPort();
		this.peerId = peeragent.getPeerid();
		this.closed.set(false);
		this.aborted.set(false);
		// for timing
		starttime.set(System.currentTimeMillis());
		numPackets.set(0);
		startCheckTimouts();
		// initiate last to avoid immediate clean up
		last_time_packet.set(System.currentTimeMillis());
	}
	public boolean gotDataPacket(final MessageHeader receiveMH, final DataPacket packet) {
		
		if (!closed.get()) {
			
			last_time_packet.set(System.currentTimeMillis());
			
			// find chunk
			if (!chuncks_incoming.containsKey(packet.getChunkId())) {
				
				if (!chunk_datapackets.containsKey(packet.getChunkNumber())) {
					chunk_datapackets.put(packet.getChunkNumber(), new ConcurrentHashMap<Integer, DataPacket>());
				}
				
				ChunkIn new_chunk = new ChunkIn(
						getSocket(), 
						this, 
						chunk_datapackets.get(packet.getChunkNumber()), 
						packet.getChunkNumber(),
						packet.getChunkVersion(), 
						packet.getChunkSize(), 
						receiveMH.getAddress(), 
						receiveMH.getTransactionID()
				);
				
				chuncks_incoming.put(packet.getChunkId(), new_chunk);
				chuncks_to_check.put(packet.getChunkId(), new_chunk);
			
			}
			
			// adding key for later decrypt
			setKeyId(packet.getKeyId());
			
			ChunkIn chunk = chuncks_incoming.get(packet.getChunkId());
			if (chunk != null && !aborted.get()) {
				
				if (chunk.addIncomingPacket(packet)) {numPackets.incrementAndGet();}
				
				if (numPackets.get() >= packet.getTotal()) {
				
					// got last packet, message is complete
					if (!closed.getAndSet(true)) {
						chuncks_to_check.remove(chunk.getChunkNumber());
						chunk.replyComplete();
						getSocket().offer(this);
					}
				
				} else {					
					if (chunk.isComplete()) {
						chuncks_to_check.remove(chunk.getChunkNumber());
						chunk.replyComplete();
					}
				}
				return true;
			}
			
		} else {
			
			// reply if buffer is closed
			
			last_time_packet.set(System.currentTimeMillis());
			
			MessageHeader datareply = new MessageHeader(MessageHeader.MessageHeaderType.DataReply);
			datareply.setTransactionID(transactionId);
			datareply.setAddress(receiveMH.getAddress());
			
			DataReplyPacket reply2 = new DataReplyPacket();
			reply2.setMsgId(getMsgId());			
			reply2.setPort(getSocket().getPort());
			reply2.setChunkNumber(packet.getChunkNumber());
			reply2.setChunkVersion(packet.getChunkVersion());
			datareply.addMessageAttribute(reply2);
			
			getSocket().getNameSpace().getMessageSender().UDPSendEncrypt(datareply);			
			
			
		}
		
		return false;
	}
	@Override
	public String toString() {
		return StringConv.UTF8(getBuffer());
	}

	// only access once
	public byte[] getBuffer() {
		
		if(chuncks_incoming!=null) {
			try {
				List<byte[]> list = new LinkedList<byte[]>();
				List<Integer> set = new LinkedList<Integer>(chuncks_incoming.keySet());
				Collections.sort(set);
				for (int i : set) {
					if (chuncks_incoming.containsKey(i) && chuncks_incoming.get(i).getBuffer() != null) {
						list.add(chuncks_incoming.get(i).getBuffer());
						// Thread.yield();
					}
				}
				// merge packets and
				// decrypt incoming data
				return peeragent.getSessionEncrypt().deCrypt(ByteUtil.mergeBytes(list));
			
			} catch (Exception ex) {
				logger.warn("error get socket buffer " + ex.getMessage());
				return null;
			} finally {
				chuncks_incoming=null;
				chuncks_to_check=null;
				chunk_datapackets=null;
			}			
		} else {
			return null;
		}
		
	}
	
	public void clear() {}
}
