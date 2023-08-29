/* This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011-2021 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */

package no.auke.p2p.m2.workers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import no.auke.p2p.m2.message.attribute.MessageAttributeParsingException;
import no.auke.p2p.m2.message.attribute.PeerLocalId;
import no.auke.p2p.m2.message.attribute.MessageAttributeInterface.MessageAttributeType;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.message.header.MessageHeaderInterface.MessageHeaderType;
import no.auke.p2p.m2.message.header.MessageHeaderParsingException;
import no.auke.p2p.m2.sockets.udp.SpeedTester;
import no.auke.p2p.m2.workers.io.IPacketChannel;
import no.auke.p2p.m2.workers.io.PacketChannelUDP;
import no.auke.p2p.m2.workers.io.PriorityQueue;
import no.auke.p2p.m2.workers.io.PriorityQueue.OutPacket;
import no.auke.p2p.m2.workers.io.PriorityQueue.Priority;
import no.auke.p2p.m2.workers.keepalive.NetAddress;
import no.auke.m2.encryption.EncryptFactory;
import no.auke.p2p.m2.InitVar;
import no.auke.p2p.m2.NameSpace;
import no.auke.p2p.m2.PeerServer;
import no.auke.util.ListNetworks;

// main  loop for sending an receiving packets
//

public class ComChannel {
	
	final private int TRAFFIC_REPORT = 10000;
	
//	private NameSpace nameSpace;
//	public NameSpace getNameSpace() {
//		return nameSpace;
//	}
	
	PeerServer server;	
	public PeerServer getMainServ() {return server;}
	
	private String address;
	private int port;
	
	private Map<Integer, NameSpace> nameSpaceList = new ConcurrentHashMap<Integer, NameSpace>();	
	private Map<Integer, Integer> requestlist = new ConcurrentHashMap<Integer, Integer>();	
	
	// traffic counters
	private AtomicLong total_bytes_out = new AtomicLong();
	private AtomicLong last_bytes_out = new AtomicLong();
	private AtomicLong total_bytes_in = new AtomicLong();
	private AtomicLong last_bytes_in = new AtomicLong();
	private AtomicLong last_report = new AtomicLong();
	private AtomicLong last_reading = new AtomicLong();
	private AtomicLong last_sending = new AtomicLong();
	
	// removed, not uses since 2011
	
	private EncryptFactory encrypt = new EncryptFactory();
	public EncryptFactory getEncrypt() {return encrypt;}
	
	IPacketChannel packetChannel;
	public IPacketChannel getPacketChannel() {return packetChannel;}
	
	public synchronized void setPacketChannel(IPacketChannel packetChannel) {
		if(this.packetChannel!=null) {
			this.packetChannel.close();
		}
		this.packetChannel = packetChannel;
	}
	
	private AtomicLong numOutGoing = new AtomicLong();
	public long getNumOutGoing() {return numOutGoing.get();}
	
	private AtomicLong numAdded = new AtomicLong();
	public long getNumAdded() {return numAdded.get();}
	
	private AtomicBoolean isrunning = new AtomicBoolean();
	public boolean isRunning() {return isrunning.get();}
	
	public NetAddress getLocaladdress() {
		return getPacketChannel().getLocalAddress() != null ? getPacketChannel().getLocalAddress() : new NetAddress();
	}
	
	//
	// check if sending an receiving in last 2 minute
	//	
	public boolean isConnected() {
		// is regarded connected if received packets within last minute
		// return isrunning.get() && com_is_running.get() &&
		// (System.currentTimeMillis() - last_reading.get() < 120000);
		return isrunning.get() && (System.currentTimeMillis() - last_reading.get() < 120000);
	}
	
	private PriorityQueue priorityqueue;
	public PriorityQueue getPriorityQueue() {return priorityqueue;}
	
	public ComChannel(PeerServer server, String address, int port, int IN_QUEUE_SIZE, int OUT_QUEUE_SIZE, int PACKET_LOSS_SIMULATE) {
		
		this.address = address;
		this.port = port;
		this.server = server;
		
		// this.PACKET_LOSS_SIMULATE = PACKET_LOSS_SIMULATE;
		isrunning.set(false);
		priorityqueue = new PriorityQueue(IN_QUEUE_SIZE, OUT_QUEUE_SIZE);
		// this might be different channels
		setPacketChannel(new PacketChannelUDP(this));
	
	}	
	
	public boolean startChannel(NameSpace nameSpace) {
		
		nameSpaceList.put(nameSpace.getId(), nameSpace);		
		if (!isrunning.getAndSet(true)) {
		
			List<String> addressList = new ArrayList<String>();
			if (address.length() > 0) {
				addressList.clear();
				addressList.add(address);
			} else {
				addressList = ListNetworks.getIPv4Addresses();
			}
			
			if (addressList.size() > 0) {
				getPacketChannel().setLocalAddress(new NetAddress(addressList.get(0), port));
				if (!startSenderAndReader()) {
					closeChannel();
					this.getMainServ().getListen().fatalError("can not start com channel");
					return false;
				}				
			} else {
				getMainServ().getListen().fatalError("no addresses availiable");
				return false;
			}			
		}
		return true;
	}
	
	public void closeChannel() {
		isrunning.set(false);
		priorityqueue.closeQueues();
		getPacketChannel().close();
	}	
	
	private byte[] encrypt(byte[] buffer) {
		//return buffer;
		return getEncrypt().enCrypt(buffer);
	}
	
	//
	// Reading messages from incoming queues
	//
	// Priority queue 1 tried first
	public MessageHeader read() {
		if (isRunning()) {
			try {
				return priorityqueue.getInQueue();
			} catch (InterruptedException e) {}
		}
		return null;
	}
	
	// sender thread
	private boolean startSenderAndReader() {
		
		try {
		
			priorityqueue.initQueues();
			final AtomicBoolean issenderready = new AtomicBoolean();
			issenderready.set(false);
			final AtomicBoolean isreaderready = new AtomicBoolean();
			isreaderready.set(false);
			final AtomicInteger failes = new AtomicInteger();
			failes.set(0);
			
			final Object read_lock = new Object();
			
			// reader
			getMainServ().getExecutor().execute(new Runnable() {
				
				@Override
				public void run() {
					last_reading.set(System.currentTimeMillis());
					isreaderready.set(true);
					// reader loop
					while (isrunning.get()) {
						try {
							
							if (last_report.get() < (System.currentTimeMillis() - TRAFFIC_REPORT)) {
								long seconds = (System.currentTimeMillis() - last_report.get()) / 1000;
								if (seconds > 1) {
									float bits_in_sec = (last_bytes_in.get() / 1024 * 8) / seconds;
									float bits_out_sec = (last_bytes_out.get() / 1024 * 8) / seconds;
									total_bytes_in.getAndAdd(last_bytes_in.get());
									total_bytes_out.getAndAdd(last_bytes_out.get());
									getMainServ().getListen().onTraffic(bits_in_sec, bits_out_sec, total_bytes_in.get(), total_bytes_out.get());
									last_bytes_in.set(0);
									last_bytes_out.set(0);
									last_report.set(System.currentTimeMillis());
								}
							}
							
							byte[] data = getPacketChannel().receive();
							if (data.length > 1) {
								last_bytes_in.addAndGet(data.length);
								
								// removed, not used sinze 2011
								byte[] packetdata = getEncrypt().deCrypt(data);
								if (packetdata != null && packetdata.length > 1) {
									// reset fail trials
									failes.set(0);
									last_reading.set(System.currentTimeMillis());
									
									MessageHeader receiveMH = MessageHeader.parseHeader(packetdata);
									receiveMH.parseAttributes(packetdata);
									receiveMH.setAddress(new NetAddress(getPacketChannel().getHostAddress(), getPacketChannel().getPort()));
									
//									if(receiveMH.getType()!=MessageHeaderType.Data 
//											&& receiveMH.getType()!=MessageHeaderType.DataReply
//											&& receiveMH.getType()!=MessageHeaderType.PingClose
//											)
//										System.out.println(receiveMH.getType() + " " + receiveMH.getAddress().getAddressPort());
									
									// only put to buffer if 
									if (isrunning.get()) {
										if (receiveMH.getType() == MessageHeaderType.Data) {
											priorityqueue.addInQueue(receiveMH, Priority.medium);
										} else if (receiveMH.getType() == MessageHeaderType.StreamData) {
											priorityqueue.addInQueue(receiveMH, Priority.low);
										} else {
											priorityqueue.addInQueue(receiveMH, Priority.high);
										}
									} else {
										getMainServ().getListen().error("got data while not running");
									}
								} else {
									if (packetdata == null || packetdata.length == 0) {
										getMainServ().getListen().error("should not get here, data empty, could be decrypt trouble");
									}
								}
							} else {
								getMainServ().getListen().message("reader error " + String.valueOf(getPacketChannel().getError()));
								synchronized(read_lock) {
									read_lock.wait(5000);
								}
							}
						} catch (MessageHeaderParsingException ex) {
							getMainServ().getListen().error("com reader: MessageHeader: " + ex.getMessage());
						} catch (MessageAttributeParsingException e) {
							getMainServ().getListen().error("com reader: messageAttribute: " + e.getMessage());
						} catch (Exception e) {
							getMainServ().getListen().error("com reader: Exception: " + e.getMessage());
							e.printStackTrace();
						}
					}
				}
			});
			
			//sender
			getMainServ().getExecutor().execute(new Runnable() {
				@Override
				public void run() {
					last_sending.set(System.currentTimeMillis());
					// sender loop
					issenderready.set(true);
					while (isrunning.get()) {
						try {
							
							OutPacket packet = priorityqueue.getOutQueue();
							
//							if(packet.messageType!=MessageHeaderType.Data 
//									&& packet.messageType!=MessageHeaderType.DataReply
//									&& packet.messageType!=MessageHeaderType.PingClose
//									)
//								System.out.println(packet.messageType + " " + packet.address.getAddressPort());
							
							if (packet != null) {
								
								//System.out.println("xx s " + packet.address.getAddressPort());								
								if (getPacketChannel().send(packet.buffer, packet.address)) {
									
									
									// reset fail trials
									failes.set(0);
									last_sending.set(System.currentTimeMillis());
									last_bytes_out.addAndGet(packet.buffer.length);
									numOutGoing.incrementAndGet();
									
									synchronized(read_lock) {
										read_lock.notify();
									}
									
								} else {

									System.out.println("fail");

									if (failes.incrementAndGet() > 10) {
										getMainServ().getListen().message("sender error " + getPacketChannel().getError());
										failes.set(0);
									}
								}
							}
						} catch (Exception e) {
							getMainServ().getListen().fatalError("com sender: Exception : " + e.getMessage() + " "
									+ getPacketChannel().getLocalAddress().getAddressPort());
							e.printStackTrace();						
						}
					}
					
					synchronized(read_lock) {
						read_lock.notify();
					}
				}
			});
			
			// message reader
			getMainServ().getExecutor().execute(new Runnable() {
				@Override
				public void run() {
					
					while (isrunning.get()) {
						
						try {
							
							MessageHeader receiveMH = read();
							if(receiveMH!=null) {
								
//								if(receiveMH.getType()!=MessageHeaderType.Data 
//										&& receiveMH.getType()!=MessageHeaderType.DataReply
//										&& receiveMH.getType()!=MessageHeaderType.PingClose
//										)
//									System.out.println("got" + receiveMH.getType() + " " + receiveMH.getAddress().getAddressPort());
								

								// special handling for connect from other peer
								if(receiveMH.getType() == MessageHeaderType.PeerRequest) {
									
									PeerLocalId peer_local_id = (PeerLocalId) receiveMH.getMessageAttribute(MessageAttributeType.Peer_Local_Id);
									if(peer_local_id!=null) {
										boolean found=false;
										for(NameSpace namespace:nameSpaceList.values()) {
											if(namespace.getThisPeerId().getPeerhexid().equals(peer_local_id.getUsername())) {
												requestlist.put(receiveMH.getTransactionID(),namespace.getId());
												namespace.getMessageListener().executeMessage(receiveMH);
												found=true;
											}
										}										
										if(!found) {
											// TODO: if not found to found this user pass call in in a multi cast to known IP addresses
											forwardMulticast(receiveMH);
											System.out.println("local peer id not found");

										}
										
									} else {
										
										System.out.println("local peer id missing");
										
									}
									
								} else {
									
									if(requestlist.containsKey(receiveMH.getTransactionID())) {
										int nsId = requestlist.get(receiveMH.getTransactionID());
										nameSpaceList.get(nsId).getMessageListener().executeMessage(receiveMH);
									} else {
										System.out.println("XX recived unknown requestid from " + receiveMH.getAddress().getAddressPort());
									}
								}
							}
							

						} catch(Exception e) {
							getMainServ().getListen().fatalError("com message reader: Exception : " + e.getMessage());
							e.printStackTrace();						
						}
							
					}
						
				}

				
			});			
			
			int wait = 0;
			while (wait < 5000 && (!isreaderready.get() || !issenderready.get())) {
				try {
					Thread.sleep(50);
					wait += 50;
				} catch (InterruptedException e) {}
			}
			if (!isreaderready.get() || !issenderready.get()) {
				return false;
			}
			
			getMainServ().getListen().debug("com sender and reader running ");
			
			return true;
			
		} catch (Exception e) {
			getMainServ().getListen().fatalError("com sender and reader: Exception : " + e.getMessage());
			e.printStackTrace();						
			return false;
		}
	}

	private void forwardMulticast(MessageHeader receiveMH) {

		// 
		// TODO implement forwarding
		// store list for incoming requests for later forward 
		// make timer, so latest are used
		// also add to list each forward so the list build up
		// make a special ping, so the forwards get refreshed with some lazy ping
		
	}

	public boolean UDPSendEncrypt(MessageHeader message, int nsId) {
		
		try {
			
			if (isRunning()) {
				requestlist.put(message.getTransactionID(),nsId);	
				byte[] encryptedBuffer = encrypt(message.getBytes()); // final message encryption
				if (priorityqueue.addOutQueue(priorityqueue.new OutPacket(message.getType(), encryptedBuffer, message.getAddress()), Priority.high)) {
					numAdded.incrementAndGet();
				}
				return true;				
			} else {
				System.out.println("XX send when not running");
			}
			
		} catch (InterruptedException e) {}
		return false;		
	
	}
		
	public boolean UDPSendEncrypt_Data(MessageHeader message, int nsId) {
		
		try {
		
			if (isRunning()) {
				requestlist.put(message.getTransactionID(),nsId);
				byte[] encryptedBuffer = encrypt(message.getBytes());
				if (InitVar.TEST_SPEED_DELAY > 0 && getSpeedTester().trowAway(encryptedBuffer.length)) {
					return true;
				}
				if (!priorityqueue.addOutQueue(priorityqueue.new OutPacket(message.getType(), encryptedBuffer, message.getAddress()), Priority.medium)) {
					return false;
				}
				numAdded.incrementAndGet();
				return true;
			}
			
		} catch (InterruptedException e) {}
		return false;
	}	
	
	// this is send stream packets
	// packet are places in medium priority queue
	public boolean UDPSendEncrypt_Stream(MessageHeader message, int nsId) {
		try {
			if (isRunning()) {
				requestlist.put(message.getTransactionID(),nsId);
				byte[] encryptedBuffer = encrypt(message.getBytes());
				if (!priorityqueue.addOutQueue(priorityqueue.new OutPacket(message.getType(), encryptedBuffer, message.getAddress()), Priority.low)) {
					return false;
				}
				numAdded.incrementAndGet();
				return true;
			}
		} catch (InterruptedException e) {}
		return false;
	}
	
	// this is send data packets
	// packet are places in low priority queue
	public boolean UDPSendEncrypt_Reply_Data(MessageHeader message, int nsId) {
		try {
			// skip packets (Test)
			// if (lostPacket()) {
			// return true;
			// }
			if (isRunning()) {
				requestlist.put(message.getTransactionID(),nsId);
				byte[] encryptedBuffer = encrypt(message.getBytes());
				if (!priorityqueue.addOutQueue(priorityqueue.new OutPacket(message.getType(), encryptedBuffer, message.getAddress()), Priority.high)) {
					return false;
				}
				numAdded.incrementAndGet();
				return true;
			}
		} catch (InterruptedException e) {}
		return false;
	}
	
	// this is send data packets
	// packet are places in low priority queue
	private SpeedTester speedTester = new SpeedTester();
	public SpeedTester getSpeedTester() {
		return speedTester;
	}

	// check if pri 1 queue is empty (mean all system packets are out)
	public boolean isPacketsSent() {
		if (isRunning())
			return priorityqueue.isPacketsSent();
		// if not open, then no packets are send for real, report as all sent
		return true;
	}
	public long getTotal_bytes_out() {
		return total_bytes_out.get();
	}
	public long getLast_bytes_out() {
		return last_bytes_out.get();
	}
	public long getTotal_bytes_in() {
		return total_bytes_in.get();
	}
	public long getLast_bytes_in() {
		return last_bytes_in.get();
	}
	public long getLast_report() {
		return last_report.get();
	}
}
