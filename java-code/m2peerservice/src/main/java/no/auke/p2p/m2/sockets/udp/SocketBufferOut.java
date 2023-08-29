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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.SocketRetStatus;
import no.auke.p2p.m2.general.BlockingQueue;
import no.auke.p2p.m2.general.ReturMessageTypes;
import no.auke.p2p.m2.message.attribute.DataPacket;
import no.auke.p2p.m2.message.attribute.DataReplyPacket;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.workers.PeerSession;
import no.auke.p2p.m2.InitVar;
import no.auke.util.ByteUtil;

// 
// LHA: class for async reply
// 
// 
public class SocketBufferOut extends SocketBuffer {
	
	//static final Logger logger = LoggerFactory.getLogger(SocketBufferOutgoing2.class);
	
	private Map<Integer, ChunkOut> chunks_outgoing = new ConcurrentHashMap<Integer, ChunkOut>();
	public ArrayList<ChunkOut> getChunks_outgoing() {
		ArrayList<ChunkOut> list = new ArrayList<ChunkOut>();
		if(chunks_outgoing!=null) {
			List<Integer> set = new LinkedList<Integer>(chunks_outgoing.keySet());
			Collections.sort(set);
			for (int i : set) {
				list.add(chunks_outgoing.get(i));
			}
		}
		return list;
	}
	
	public ArrayList<DataPacket> getDatapackets() {
		ArrayList<DataPacket> packets = new ArrayList<DataPacket>();
		for (ChunkOut chunk : getChunks_outgoing()) {
			for (DataPacket packet : chunk.getDatapackets()) {
				packets.add(packet);
			}
		}
		return packets;
	}
	
	private Map<Integer, ChunkOut> chunks_not_replied = new ConcurrentHashMap<Integer, ChunkOut>();
	public List<ChunkOut> getChuncksNotReplied() {return chunks_not_replied!=null?new ArrayList<ChunkOut>(chunks_not_replied.values()):new ArrayList<ChunkOut>();}
	
	public int getNumChunkNotReplied() {return chunks_not_replied!=null ? chunks_not_replied.size() : 0;}
	public int getNumResendQueue() {return resend_queue!=null ? resend_queue.size() : 0;}
	
	private BlockingQueue<ChunkOut> send_queue = null;
	private BlockingQueue<ChunkOut> resend_queue = null;
	public BlockingQueue<ChunkOut> getSendQueue() {return send_queue;}
	public BlockingQueue<ChunkOut> getResendQueue() {return resend_queue;}
	
	final private AtomicBoolean isSending = new AtomicBoolean();
	public boolean isSending() {return isSending.get();}
	public void stopSending() {isSending.set(false);}
	private AtomicBoolean sendfailed = new AtomicBoolean();
	public boolean isFailed() {return sendfailed.get();}
	
	// check if any answer from remote, if not connection is not enabled
	private AtomicBoolean anyReply = new AtomicBoolean();
	// last data reply got from other part
	private AtomicLong lastReply = new AtomicLong();
	public long getTimeSinceReply() {return System.currentTimeMillis() - lastReply.get();}
	
	// LHA: implement more later
	SendStatistics stat = null;
	public SendStatistics getStat() {return stat;}
	
	public SocketBufferOut(final PeerSession peeragent, final SocketRetStatus ret, final Socket socket, final int port, byte[] data) throws Exception {
		super("outgoing", peeragent, ret, socket);
	
		stat = getSocket().getNameSpace().getMainServ().getStat(peeragent.getPeerAddress());
		
		data = peeragent.getSessionEncrypt().enCrypt(data);
		
		List<byte[]> splits = ByteUtil.splitBytesWithFixedLength(data, InitVar.PACKET_SIZE - 52);
		
		ArrayList<ChunkOut> new_chunks = new ArrayList<ChunkOut>();
		length = data.length;
		anyReply.set(false);
		lastReply.set(System.currentTimeMillis());
		
		ChunkOut chunk = null;
		for (int index = 0; index < splits.size(); index++) {
			
			if (splits.get(index).length > 0) {
				
				DataPacket packet = new DataPacket();
				packet.setKeyId(keyId);
				packet.setMsgId(msgId);
				packet.setPort(port);
				packet.setNumber(index + 1);
				packet.setTotal(splits.size());
				packet.setData(splits.get(index));
				
				if (index % InitVar.CHUNK_SIZE == 0) {
					chunk = new ChunkOut(socket, this, 0, InitVar.CHUNK_SIZE, false);
					new_chunks.add(chunk);
				}
				chunk.addOutgoingPacket(packet);
			} else {
				break;
			}
		}
		
		// all packets
		// offer queue
		
		boolean iniOk = true;
		send_queue = new BlockingQueue<ChunkOut>(new_chunks.size());
		resend_queue = new BlockingQueue<ChunkOut>(new_chunks.size());
		
		for (ChunkOut new_chunk : new_chunks) {
			if (send_queue.offer(new_chunk)) {
				chunks_outgoing.put(new_chunk.getChunkNumber(), new_chunk);
			} else {
				iniOk = false;
			}
		}
		
		if (iniOk) {
			sendfailed.set(false);
			isSending.set(false);
			numPackets.set(send_queue.size());
			socket.addOutBuffer(this);
			// to avoid clean up at once
			last_time_packet.set(System.currentTimeMillis());
		} else {
			send_queue.clear();
			chunks_outgoing.clear();
		}
	}
	//
	// Sending buffer
	//
	// for testing purpose
	public void doCheckTimeout() {}
	public boolean runCheck() {
		
		try {
			
			if (!closed.get() && chunks_not_replied.size()>0) {

				if (numPackets.get() > 0 && !sendfailed.get() && !closed.get()) {
					
					doCheckTimeout();
					
					if (chunks_not_replied.size() > 0) {

						if (anyReply.get()) {
							// some chunks are replied
							// not all chunks are replied complete, its is then
							// timeout
							ret.setLastRetcode(ReturMessageTypes.send_timeout);
							ret.setLastMessage("not completed: after: "
									+ String.valueOf(System.currentTimeMillis() - starttime.get()) + " ms");
						} else {
							// no chunks are replied, indicating other isde is
							// dead
							//
							ret.setLastRetcode(ReturMessageTypes.send_no_session);
							ret.setLastMessage("no reply: after "
									+ String.valueOf(System.currentTimeMillis() - starttime.get())  + " ms");
						}

						int timeout = getStat().getChunkAbortTimeout();
						
						// must change strategy, use total time since last reply
						// from other side
						if (getTimeSinceReply() > timeout) {
							
							System.out.println("timeout factor " + getTimeSinceReply() + " " + timeout);
						
							if (!sendfailed.getAndSet(true)) {
								failed();
							}
						
						} else {

							for (ChunkOut chunk : getChuncksNotReplied()) {
								
								Thread.yield();
								
								if (!chunk.isComplete()
										&& chunk.doResend(getStat().getChunkResendTimeout())) 
								{
									// clone and replace outgoing chunk with new
									// one
									// exec_lock.lock();
									try {
										
										ChunkOut chunk_resend = chunk.cloneNewVersion();
										// data packets might be deleted while
										// clone
										
										if (!chunk.isComplete() && chunk_resend.getNumDataPackets() > 0) {
											chunks_outgoing.put(chunk_resend.getChunkNumber(), chunk_resend);
											chunks_not_replied.put(chunk_resend.getChunkNumber(), chunk_resend);
											try {
												resend_queue.put(chunk_resend);
												send_queue.release();
											} catch (Exception e) {}
										}
										
									} finally {
										// exec_lock.unlock();
									}
								
								} else {
									// calculate abort time
									// LHA: remove this calculation
									//
									chunk.setAbortTime(System.currentTimeMillis() + getNumResendQueue() * chunk.getNumDataPackets()
											* stat.getDelaySpeed().getLastdelay());
								}
								
								if (!closed.get() && chunk.isComplete()) {
									chunks_not_replied.remove(chunk.getChunkNumber());
								}
								// Thread.yield();
								if (send_queue.size() == 0 && resend_queue.size() > 0) {
									send_queue.release();
								}
							}
						}
					}
					
					if (sendfailed.get()) {
						// LHA : very important or it might hang
						send_queue.release();
						getStat().getSpeedCalc().setFail();
					} else if (numPackets.get() == 0) {
						// LHA : very important or it might hang
						send_queue.release();
					} else if (send_queue.size() == 0 && resend_queue.size() > 0) {
						send_queue.release();
					} else if (numPackets.get() > 0 && chunks_not_replied.size() + send_queue.size() + resend_queue.size() == 0) {
						// LHA : very important or it might hang
						send_queue.release();
					}
					
				} else {
					// Huy:no, it should be released. If it is done, we can
					// offer more items to the list (when initializing). This
					// threat can run before the initialisation process in this
					// constructor because it is called from the super class
					// send_queue.done();
					send_queue.release();
					if (closed.get()) {
						send_queue.done();
					}
					return false;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return true;
	}
	
	private AtomicInteger cnt = new AtomicInteger();
	public int getCnt() {return cnt.get();}
	
	public boolean send() {
		
		if (send_queue.size() > 0 && !isSending.getAndSet(true)) {
		
			// for timing
			starttime.set(System.currentTimeMillis());
			stat.startSend(length);
			
			try {
				
				int sendFails = 0;
				startCheckTimouts(); // start and check outgoing chunks
				
				ChunkOut chunk = null;
				while (getPeerAgent().isRunning() 
						&& numPackets.get() > 0 
						&& !sendfailed.get() 
						&& isSending.get() 
						&& !closed.get()) {
					
					try {
						
						chunk = resend_queue.poll();
						if (chunk == null) {
							chunk = send_queue.take();
							if (chunk == null) {
								chunk = resend_queue.poll();
							}
						}
						
						if (chunk != null) {
							
							if (numPackets.get() > 0 && !chunk.isComplete()) {
								
								last_time_packet.set(System.currentTimeMillis());
								
								if (!chunk.send(transactionId, peeragent.getPeerAddress())) {
									sendFails++;
									if (sendFails > 3) {
										ret.setLastRetcode(ReturMessageTypes.channel_io);
										ret.setLastMessage("can not send chunk");
										sendfailed.set(true);
										getStat().getSpeedCalc().setFail();
									} else {
										resend_queue.add(chunk);
									}
								} else {
									cnt.addAndGet(chunk.getNumDataPackets());
								}
								
								// startCheckTimouts(); // start and check
								// outgoing chunks
							}
						
							chunks_not_replied.put(chunk.getChunkNumber(), chunk);
						
						} else {
							// Do some extra check to see if finish
							startCheckTimouts(); // start and check outgoing chunks
						}
						
					} catch (InterruptedException e) {}
				}
				
				if (numPackets.get() > 0) {
					
					if (anyReply.get()) {
						// some chunks are replied
						// not all chunks are replied complete, its is then
						// timeout
						ret.setLastRetcode(ReturMessageTypes.send_timeout);
						ret.setLastMessage("not completed after "
								+ String.valueOf(System.currentTimeMillis() - starttime.get()) +  " ms");
					} else {
						// no chunks are replied, indicating other isde is dead
						ret.setLastRetcode(ReturMessageTypes.send_no_session);
						ret.setLastMessage("no reply after "
								+ String.valueOf(System.currentTimeMillis() - starttime.get()) +  " ms");
					}
					sendfailed.set(true);
				}
				return !sendfailed.get();
				
			} catch (Exception ex) {
			
				ex.printStackTrace();
				sendfailed.set(true);
				return !sendfailed.get();
			
			} finally {
				
				closed.set(true);
				last_time_packet.set(System.currentTimeMillis());
				
				if (isFailed()) {
					stat.getSpeedCalc().setFail();
				} else {
					getSocket().getNameSpace().getListen().onMessageConfirmed(peeragent.getPeerAddress(), msgId);
				}
				
				// stat.printMessageStatistics();
				// stat.printStatistics();
				
				stat.completMessage();
				getSocket().deleteOutgoingBuffer(this);
				
				clear();
				
			}
		} else {
			ret.setLastRetcode(ReturMessageTypes.sending_error);
			ret.setLastMessage("can not send bugger");
			return false;
		}
	}
	
	// handle reply messages
	// from other peer
	public void gotDataReply(MessageHeader receiveMH, DataReplyPacket packet) {
		
		if (chunks_outgoing!=null && packet.getChunkNumber() > 0) {

			// set true because some packet arrived from remote peer,
			// connection is not completely lost
			anyReply.set(true);
			// time for last reply to use in abort timeout
			lastReply.set(System.currentTimeMillis());
			
			ChunkOut chunk = chunks_outgoing.get(packet.getChunkNumber());
			if (chunk != null) {
				if (chunk.gotReply(receiveMH, packet)) {
					if (chunk.isComplete()) {
						// remove from outgoing
						chunks_outgoing.remove(chunk.getChunkNumber());
						if (numPackets.decrementAndGet() == 0) {
							send_queue.release();
							send_queue.done();
						}
					}
				}
			}
			startCheckTimouts(); // start and check outgoing chunks
		}
	}
	
	public void failed() {sendfailed.set(true);}
	public void clear() {}
	
}
