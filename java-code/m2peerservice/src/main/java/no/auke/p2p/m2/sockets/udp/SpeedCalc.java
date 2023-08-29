/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.sockets.udp;

import java.util.concurrent.atomic.AtomicLong;
import no.auke.p2p.m2.InitVar;

public class SpeedCalc extends ISpeedCalc {
	public SpeedCalc(SendStatistics stat) {
		super(stat);
	}
	private AtomicLong last_calculate = new AtomicLong();
	private long totBytesReplied;
	private long totBytesSentReplied;
	private long totBytesSentWaiting;
	private int num_reply;
	private int num_sent;
	private double time_reply;
	private double time_sent;
	private int success_speed = 0;
	private int fail_speed = 0;
	private boolean failed = false;
	private double success_rate = 0;
	private void reset() {
		num_reply = 0;
		time_reply = 0;
		time_sent = 0;
		totBytesReplied = 0;
		totBytesSentReplied = 0;
	}
	@Override
	public boolean isFailed() {
		return failed;
	}
	@Override
	public void startMessage() {
		failed = false;
		calculate();
	}
	@Override
	public void completMessage() {
		if (isFailed()) {
			stat.setChunkAbortTimeout(InitVar.SEND_ABORT_TIMEOUT);
			stat.setChunkResendTimeout(InitVar.SEND_RESEND_TIMEOUT);
		}
	}
	@Override
	public void replyChunk(long startSent, long lastSent, int bytesSent, int bytesReplied, boolean reply) {
		this.totBytesReplied += bytesReplied;
		this.totBytesSentReplied += bytesSent;
		if (lastSent > 0) {
			time_sent += lastSent - startSent;
		}
		if (reply) {
			num_reply++;
			time_reply += System.currentTimeMillis() - lastSent;
		}
		// count down waiting
		num_sent--;
		totBytesSentWaiting -= bytesSent;
	}
	// depreciated
	@Override
	public void sendChunk(int bytes) {
		num_sent++;
		totBytesSentWaiting += bytes;
	}
	@Override
	public void setFail() {
		failed = true;
		reset();
	}
	//
	// TODO: LHA: must be studied closer and made better
	// Calculate in regular periods
	//
	@Override
	public void calculate() {
		locking.lock();
		try {
			if (System.currentTimeMillis() - last_calculate.get() > SendCalcPolecy2.CALC_FREQUENCE) {
				if (num_sent > 5 && totBytesReplied == 0) {
					// TODO: num_send block, this is a quick fix,
					// halt because to many waiting
					// it depends on the expected reply time and send time
					// how many packets are sent before expecting reply
					// must be studied closer
					// just delay sending
					stat.getDelaySpeed().setWaitSpeed(stat.getDelaySpeed().getOriginalSpeed() / (num_sent * 10));
				} else {
					if (time_sent > 0) {
						getReplySpeedOverall(this.totBytesReplied / time_sent);
					}
					// no delay, set ordinary speed
					stat.getDelaySpeed().setWaitSpeed(0);
					last_calculate.set(System.currentTimeMillis());
					if (totBytesSentReplied > 0) {
						int current_speed = (int) ((stat.getDelaySpeed().getOriginalSpeed() + getReplySpeedOverall(-1)) / 2);
						// set time for reply
						getReplyTimeOverall(time_reply * 1.0 / num_reply);
						// adjust for success rate
						double send_Success = (totBytesReplied / totBytesSentReplied);
						if (send_Success < 0.98) {
							fail_speed = stat.getDelaySpeed().getOriginalSpeed();
							// reduse with half the success factor
							current_speed = (int) ((current_speed + (current_speed * send_Success)) / 2);
						} else {
							// TODO: make better
							// handle fail speed and current
							// raise on a factor
							// System.out.println("average speed " +
							// String.valueOf(send_speed_avg) +
							// " send speed " + String.valueOf(send_speed)
							// );
							if (success_speed > fail_speed && fail_speed > 0) {
								fail_speed = success_speed;
								int add = (int) ((InitVar.MAX_SPEED - current_speed) * SendCalcPolecy2.DECLINE_FACTOR);
								current_speed += add;
							} else {
								int add = (int) ((InitVar.MAX_SPEED - current_speed) * SendCalcPolecy2.RISE_FACTOR);
								current_speed += add;
							}
							success_speed = current_speed;
						}
						// setting the current speed to calculation method
						stat.getDelaySpeed().setNewSpeed(current_speed);
						reset();
					}
					// LHA: must be studied closer
					stat.setChunkResendTimeout((int) ((getReplyTimeOverall(-1.0)) * SendCalcPolecy2.REPLY_DELAY_FACTOR));
					if (stat.getChunkResendTimeout() < InitVar.SEND_RESEND_TIMEOUT) {
						stat.setChunkResendTimeout(InitVar.SEND_RESEND_TIMEOUT);
					} else {
						if (stat.getChunkResendTimeout() > (InitVar.SEND_RESEND_TIMEOUT * SendCalcPolecy2.REPLY_DELAY_FACTOR)) {
							stat.setChunkResendTimeout(InitVar.SEND_RESEND_TIMEOUT * SendCalcPolecy2.REPLY_DELAY_FACTOR);
						}
					}
					// abort_timeout_calc.set(InitVar.SEND_ABORT_TIMEOUT);
					stat.setChunkAbortTimeout(stat.getChunkAbortTimeout() * SendCalcPolecy2.ABORT_DELAY_FACTOR);
					if (stat.getChunkAbortTimeout() > InitVar.SEND_ABORT_TIMEOUT) {
						stat.setChunkAbortTimeout(InitVar.SEND_ABORT_TIMEOUT);
					}
				}
			}
		} finally {
			locking.unlock();
		}
	}
	@Override
	public void printStatistics() {
		/*
		 * if(logger.isDebugEnabled() | isFailed()) {
		 * 
		 * logger.debug(" CALC STATUS: " + (isFailed() ? "FAILURE" : "SUCCESS")
		 * + " Current speed: " + String.valueOf(stat.getSpeed()) +
		 * " Average speed: " + String.valueOf(getReplySpeedOverall(-1)) +
		 * " Reply timeout : " + String.valueOf(stat.getChunkResendTimeout()) +
		 * " Abort timeout : " + String.valueOf(stat.getChunkAbortTimeout()) +
		 * " Total sent : " + String.valueOf(stat.getDataSize()) + " ");
		 * 
		 * }
		 */
	}
}
