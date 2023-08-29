/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.sockets.udp;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.p2p.m2.InitVar;
import no.auke.p2p.m2.workers.keepalive.NetAddress;

// 
// one speed statistic object pr. remote peer address 
//
public class SendStatistics {
	
	private static final Logger logger = LoggerFactory.getLogger(SendStatistics.class);
	
	public int SEND_RESEND_TIMEOUT = 0;
	public int SEND_ABORT_TIMEOUT = 0;
	public int MAX_SPEED = 1000;
	public int FIXED_SPEED = 0;
	public int SPEED = 0;
	
	private NetAddress address;
	
	private AtomicLong send_starttime = new AtomicLong();
	private AtomicLong send_endtime = new AtomicLong();
	private AtomicLong send_time = new AtomicLong();
	private AtomicLong send_ok = new AtomicLong();
	private AtomicLong send_failed = new AtomicLong();
	private AtomicLong datasize = new AtomicLong();
	private AtomicLong chunkSent = new AtomicLong();
	private AtomicLong chunkResent = new AtomicLong();
	private AtomicLong packetSent = new AtomicLong();
	private AtomicLong packetResent = new AtomicLong();
	private AtomicLong bytesSent = new AtomicLong();
	private AtomicLong bytesResent = new AtomicLong();
	private AtomicInteger timeReply = new AtomicInteger();
	private AtomicInteger maxtimeReply = new AtomicInteger();
	private AtomicInteger mintimeReply = new AtomicInteger();
	private AtomicLong chunkreplies = new AtomicLong();
	private ISpeedCalc speedcalc;
	public ISpeedCalc getSpeedCalc() {return speedcalc;}
	
	public int getChunkResendTimeout() {return resend_timeout_calc.get();}
	public void setChunkResendTimeout(int resend_timeout_calc) {this.resend_timeout_calc.set(resend_timeout_calc);}
	public int getChunkAbortTimeout() {return abort_timeout_calc.get();}
	public void setChunkAbortTimeout(int abort_timeout_calc) {this.abort_timeout_calc.set(abort_timeout_calc);}
	private AtomicInteger resend_timeout_calc = new AtomicInteger();
	private AtomicInteger abort_timeout_calc = new AtomicInteger();
	private DelaySpeed delaySpeed;
	public DelaySpeed getDelaySpeed() {return delaySpeed;}
	public SendStatistics(NetAddress address) {
		this.address = address;
		send_starttime.set(0);
		mintimeReply.set(Integer.MAX_VALUE);
		this.FIXED_SPEED = InitVar.FIXED_SPEED;
		if (this.SPEED == 0) {
			this.SPEED = this.MAX_SPEED / 2;
		}
		this.SEND_ABORT_TIMEOUT = InitVar.SEND_ABORT_TIMEOUT;
		this.SEND_RESEND_TIMEOUT = InitVar.SEND_RESEND_TIMEOUT;
		this.MAX_SPEED = InitVar.MAX_SPEED;
		// this.speedcalc = new SpeedCalc2(this);
		this.speedcalc = new SpeedCalc(this);
		resend_timeout_calc.set(InitVar.SEND_RESEND_TIMEOUT);
		abort_timeout_calc.set(InitVar.SEND_ABORT_TIMEOUT);
		delaySpeed = new DelaySpeed();
	}
	public void completMessage() {
		if (speedcalc.isFailed()) {
			send_failed.incrementAndGet();
		} else {
			send_ok.incrementAndGet();
		}
		send_time.addAndGet((int) (send_endtime.get() - send_starttime.get()));
		send_starttime.set(0);
		send_endtime.set(0);
		speedcalc.completMessage();
	}
	public long getSendOk() {return send_ok.get();}
	public long getSendFailed() {return send_failed.get();}
	public long getDataSize() {return datasize.get();}
	public NetAddress getAddress() {return address;}
	public long getChunkSent() {return chunkSent.get();}
	public long getChunkResent() {return chunkResent.get();}
	public long getPacketSent() {return packetSent.get();}
	public long getPacketResent() {return packetResent.get();}
	public long getBytesSent() {return bytesSent.get();}
	public long getBytesResent() {return bytesResent.get();}
	public int getMaxtimeReply() {return maxtimeReply.get();}
	public int getMintimeReply() {return mintimeReply.get();}
	public long getChunkReplies() {return chunkreplies.get();}
	public void startSend(int size) {
		datasize.addAndGet(size);
		speedcalc.startMessage();
	}
	public long getBytesSentTotal() {
		return (getBytesResent() + getBytesSent());
	}
	public void sentPacket(int bytes, boolean resend) {
		if (!resend) {
			packetSent.incrementAndGet();
			bytesSent.addAndGet(bytes);
		} else {
			packetResent.incrementAndGet();
			bytesResent.addAndGet(bytes);
		}
		send_endtime.set(System.currentTimeMillis());
		if (send_starttime.get() == 0) {
			// start sending
			send_starttime.set(send_endtime.get());
		}
	}
	public void sendingChunk(boolean resend) {
		if (!resend) {
			chunkSent.incrementAndGet();
		} else {
			chunkResent.incrementAndGet();
		}
	}
	public void replySending(long replytime) {
		if (replytime > 0) {
			int time = (int) (System.currentTimeMillis() - replytime);
			if (maxtimeReply.get() < time) {
				maxtimeReply.set(time);
			}
			if (mintimeReply.get() > time) {
				mintimeReply.set(time);
			}
			timeReply.addAndGet(time);
			chunkreplies.incrementAndGet();
		}
	}
	// TODO: check this out
	public double getAverageReplyTime() {
		if (timeReply.get() > 0) {
			return ((double) Math.round(((timeReply.get() * 1.0) / (chunkreplies.get() * 1.0)) * 1000) / 1000);
		} else {
			return 0;
		}
	}
	public long getSendtime() {return send_time.get();}
	public double getRealSpeed() {
		if (getSendtime() > 0) {
			return (double) Math.round(((((bytesSent.get() + bytesResent.get()) / 1024) / (getSendtime() / 1000.00))));
		} else {
			return 0;
		}
	}
	public double getMessageSpeed() {
		if (getSendtime() > 0) {
			return (double) Math.round(((getDataSize() / 1024) / (getSendtime() / 1000.00)));
		} else {
			return 0;
		}
	}
	public double getReplyfactor() {
		return (double) Math.round((packetResent.get() * 1.0) / ((packetResent.get() * 1.0) + packetSent.get() * 1.0) * 1000) / 1000;
	}
	public void printStatistics() {
	
		
		logger.info("------------------------------");
		logger.info("Statisitic for address: " + address.getAddressPort());
		logger.info(" Time used (s):  " + String.valueOf(Math.round(getSendtime() / 10) / 100.00) + " Message sent: " + String.valueOf(getSendOk())
				+ " Message failed : " + String.valueOf(getSendFailed()) + " Message size (mb): "
				+ String.valueOf(Math.round(getDataSize() / 10240.0) / 100.00));
		logger.info(" Real speed (kb/s): " + String.valueOf(Math.round(getRealSpeed())) + " Message speed (kb/s): "
				+ String.valueOf(Math.round(getMessageSpeed())));
		logger.info(" Resent %: " + String.valueOf(Math.round(getReplyfactor() * 100 * 1000) / 1000.00) + " Avg reply time (ms):  "
				+ String.valueOf(Math.round(getAverageReplyTime() * 100) / 100.00) + " Min reply time (ms):  " + String.valueOf(getMintimeReply())
				+ " Max reply time (ms):  " + String.valueOf(getMaxtimeReply()));
		logger.info(" chunks sent: " + String.valueOf(getChunkSent()) + " chunks replied: " + String.valueOf(getChunkReplies()) + " chunks resent: "
				+ String.valueOf(getChunkResent()) + " Packets sent: " + String.valueOf(getPacketSent()) + " Packets re sent: "
				+ String.valueOf(getPacketResent()) + " Sent (bytes): " + String.valueOf(getBytesSent()) + " Re sent (bytes): "
				+ String.valueOf(getBytesResent()));
		logger.info("------------------------------");
	
	}
	public int getSpeed() {return delaySpeed.getSpeed();}
	public void printMessageStatistics() {speedcalc.printStatistics();}
}
