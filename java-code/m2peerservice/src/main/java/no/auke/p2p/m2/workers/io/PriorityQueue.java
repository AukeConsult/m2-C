package no.auke.p2p.m2.workers.io;

import no.auke.p2p.m2.InitVar;
import no.auke.p2p.m2.general.BlockingQueue;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.message.header.MessageHeaderInterface.MessageHeaderType;
import no.auke.p2p.m2.workers.keepalive.NetAddress;

// send to priorities queue
// TODO: make finish and test it before implement into comChannel
public class PriorityQueue {

	// packet life time in milliseconds
	// from stored to read from application
	private static final long STREAM_PACKET_LIFETIME = InitVar.STREAM_PACKET_LIFETIME;
	private static final long DATA_PACKET_LIFETIME = InitVar.DATA_PACKET_LIFETIME;
	private static final long CONTROLL_PACKET_LIFETIME = InitVar.CONTROLL_PACKET_LIFETIME;
	public static long getStreamPacketLifetime() {
		return STREAM_PACKET_LIFETIME;
	}
	private final int OFFER_SLEEP = 100; // wait 1 MS
	private final int OFFER_TRIALS = 5; // try 5 times
	// private long last_update=System.nanoTime();
	// private long skipped = 0;
	public enum Priority {
		high, medium, low
	}
	public class IPacket {
		public long added = System.currentTimeMillis();;
	}
	public class OutPacket extends IPacket {
		public MessageHeaderType messageType;
		public byte[] buffer;
		public NetAddress address;
		public OutPacket(MessageHeaderType messageType, byte[] buffer, NetAddress address) {
			this.messageType = messageType;
			this.buffer = buffer;
			this.address = address;
		}
	}
	// LHA: timing packet
	public class InPacket extends IPacket {
		public MessageHeader message;
		public InPacket(MessageHeader message) {
			this.message = message;
		}
	}
	private BlockingQueue<IPacket> outpri1;
	private BlockingQueue<IPacket> outpri2;
	private BlockingQueue<IPacket> outpri3;
	private BlockingQueue<IPacket> inpri1;
	private BlockingQueue<IPacket> inpri2;
	private BlockingQueue<IPacket> inpri3;
	public PriorityQueue(int IN_QUEUE_SIZE, int OUT_QUEUE_SIZE) {
		outpri1 = new BlockingQueue<IPacket>(OUT_QUEUE_SIZE);
		outpri2 = new BlockingQueue<IPacket>(OUT_QUEUE_SIZE);
		outpri3 = new BlockingQueue<IPacket>(OUT_QUEUE_SIZE);
		inpri1 = new BlockingQueue<IPacket>(IN_QUEUE_SIZE);
		inpri2 = new BlockingQueue<IPacket>(IN_QUEUE_SIZE);
		inpri3 = new BlockingQueue<IPacket>(IN_QUEUE_SIZE);
	}
	public void initQueues() {
		inpri1.clear();
		inpri2.clear();
		inpri3.clear();
		outpri1.clear();
		outpri2.clear();
		outpri3.clear();
		inpri1.resetDone();
		outpri1.resetDone();
	}
	public void closeQueues() {
		inpri1.clear();
		inpri2.clear();
		inpri3.clear();
		outpri1.clear();
		outpri2.clear();
		outpri3.clear();
		inpri1.done();
		outpri1.done();
	}
	public void releaseQueues() {
		inpri1.release();
		outpri1.release();
	}
	public int getOutBuffSize() {
		return outpri1.size() + outpri2.size() + outpri3.size();
	}
	public int getInBuffSize() {
		return inpri1.size() + inpri2.size() + inpri3.size();
	}
	public boolean addOutQueue(OutPacket packet, Priority priority) throws InterruptedException {
		boolean ok = false;
		if (packet != null) {
			if (priority == Priority.high) {
				ok = offerToQueue(outpri1, packet, CONTROLL_PACKET_LIFETIME, true);
			} else if (priority == Priority.medium) {
				ok = offerToQueue(outpri2, packet, DATA_PACKET_LIFETIME, true);
			} else if (priority == Priority.low) {
				ok = offerToQueue(outpri3, packet, STREAM_PACKET_LIFETIME, true);
			}
			if (outpri3.size() > 0 || outpri2.size() > 0) {
				outpri1.release();
			}
			return ok;
		}
		return ok;
	}
	private boolean offerToQueue(BlockingQueue<IPacket> queue, IPacket packet, long packet_lifetime, boolean isAddOutQueue) throws InterruptedException {
		int cnt = 0;
		packet.added = System.currentTimeMillis();
		while (!queue.offer(packet) && cnt < OFFER_TRIALS) {
			// get ridd of oldest packet
			IPacket last_packet = queue.peek();
			if (last_packet != null && last_packet.added + packet_lifetime < System.currentTimeMillis()) {
				queue.poll();
				cnt = 0;
			} else {
				cnt++;
				if (cnt >= OFFER_TRIALS) {
					return false;
				}
				Thread.sleep(OFFER_SLEEP);
			}
		}
		return true;
	}
	public OutPacket getOutQueue() throws InterruptedException {
		OutPacket packet = null;
		while (packet == null && !outpri1.isDone()) {
			if (outpri3.size() == 0 && outpri2.size() == 0) {
				packet = (OutPacket) outpri1.take(); // Normally hang waiting on
														// this take
				// LHA: throw away old packages in streams
				if (packet != null && packet.added + CONTROLL_PACKET_LIFETIME < System.currentTimeMillis()) {
					//System.out.println("WARNING: packet lost - high priority packet's lifetime passed when polling from getOutQueue(): "
					//		+ (System.currentTimeMillis() - packet.added - CONTROLL_PACKET_LIFETIME));
					packet = null;
				}
			}
			if (packet == null) {
				if (outpri1.size() > 0) {
					packet = (OutPacket) outpri1.poll();
					// LHA: throw away old packages in streams
					if (packet != null && packet.added + CONTROLL_PACKET_LIFETIME < System.currentTimeMillis()) {
						//System.out.println("WARNING: packet lost - high priority packet's lifetime passed when polling from getOutQueue(): "
						//		+ (System.currentTimeMillis() - packet.added - CONTROLL_PACKET_LIFETIME));
						packet = null;
					}
				} else if (outpri2.size() > 0) {
					packet = (OutPacket) outpri2.poll();
					// LHA: throw away old packages in streams
					if (packet != null && packet.added + DATA_PACKET_LIFETIME < System.currentTimeMillis()) {
						//System.out.println("WARNING: packet lost - medium priority packet's lifetime passed when polling from getOutQueue(): "
						//		+ (System.currentTimeMillis() - packet.added - DATA_PACKET_LIFETIME));
						packet = null;
					}
				} else if (outpri3.size() > 0) {
					packet = (OutPacket) outpri3.poll();
					// LHA: throw away old packages in streams
					if (packet != null && packet.added + STREAM_PACKET_LIFETIME < System.currentTimeMillis()) {
						//System.out.println("WARNING: packet lost - low priority packet's lifetime passed when polling from getOutQueue(): "
						//		+ (System.currentTimeMillis() - packet.added - STREAM_PACKET_LIFETIME));
						packet = null;
					}
				}
			}
		}
		return packet;
	}
	public boolean addInQueue(MessageHeader message, Priority priority) throws InterruptedException {

		InPacket packet = new InPacket(message);

		boolean ok = false;
		if (priority == Priority.high) {
			ok = offerToQueue(inpri1, packet, CONTROLL_PACKET_LIFETIME, false);
		} else if (priority == Priority.medium) {
			ok = offerToQueue(inpri2, packet, DATA_PACKET_LIFETIME, false);
		} else if (priority == Priority.low) {
			ok = offerToQueue(inpri3, packet, STREAM_PACKET_LIFETIME, false);
		}
		if (inpri2.size() > 0 || inpri3.size() > 0) {
			inpri1.release();
		}
		return ok;
	}
	public MessageHeader getInQueue() throws InterruptedException {
		InPacket packet = null;
		while (packet == null && !inpri1.isDone()) {
			if (inpri3.size() == 0 && inpri2.size() == 0) {
				packet = (InPacket) inpri1.take(); // Normally hang waiting on
													// this take
				// LHA: throw away old packages in streams
				if (packet != null && packet.added + CONTROLL_PACKET_LIFETIME < System.currentTimeMillis()) {
					System.out.println("WARNING: packet lost - high priority packet's lifetime passed when polling from getOutQueue(): "
							+ (System.currentTimeMillis() - packet.added - CONTROLL_PACKET_LIFETIME));
					packet = null;
				}
			}
			if (packet == null) {
				if (inpri1.size() > 0) {
					packet = (InPacket) inpri1.poll();
					// LHA: throw away old packages in streams
					if (packet != null && packet.added + CONTROLL_PACKET_LIFETIME < System.currentTimeMillis()) {
						//System.out.println("WARNING: packet lost - high priority packet's lifetime passed when polling from getOutQueue(): "
						//		+ (System.currentTimeMillis() - packet.added - CONTROLL_PACKET_LIFETIME));
						packet = null;
					}
				} else if (inpri2.size() > 0) {
					packet = (InPacket) inpri2.poll();
					// LHA: throw away old packages in streams
					if (packet != null && packet.added + DATA_PACKET_LIFETIME < System.currentTimeMillis()) {
						//System.out.println("WARNING: packet lost - medium priority packet's lifetime passed when polling from getOutQueue(): "
						//		+ (System.currentTimeMillis() - packet.added - CONTROLL_PACKET_LIFETIME));
						packet = null;
					}
				} else if (inpri3.size() > 0) {
					packet = (InPacket) inpri3.poll();
					// LHA: throw away old packages in streams
					if (packet != null && packet.added + STREAM_PACKET_LIFETIME < System.currentTimeMillis()) {
						//System.out.println("WARNING: packet lost - low priority packet's lifetime passed when polling from getOutQueue(): "
						//		+ (System.currentTimeMillis() - packet.added - CONTROLL_PACKET_LIFETIME));
						packet = null;
					}
				}
			}
		}

		return packet == null ? null : packet.message;
	}
	public boolean isPacketsSent() {
		return outpri1.size() == 0;
	}
}
