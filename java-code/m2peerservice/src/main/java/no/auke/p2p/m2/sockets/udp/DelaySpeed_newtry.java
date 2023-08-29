/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.sockets.udp;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.p2p.m2.InitVar;
import no.auke.util.Lock;

public class DelaySpeed_newtry {
	private static final Logger logger = LoggerFactory.getLogger(DelaySpeed_newtry.class);
	private AtomicInteger speed = new AtomicInteger();
	private AtomicInteger numCients = new AtomicInteger(0);
	public DelaySpeed_newtry() {
		setNewSpeed(InitVar.MAX_SPEED / 2);
	}
	double fractional_parts = 0;
	Lock lock = new Lock();
	public int getPacketDelay() {
		try {
			lock.lock();
			double packet_delay = numCients.get() > 0 ? ((((InitVar.PACKET_SIZE + 0.0) * 1000.0) / ((speed.get() * 1024.0))) / (numCients.get() + 0.0)) : 0;
			fractional_parts += (packet_delay % 1);
			if (fractional_parts >= 1) {
				packet_delay += (int) fractional_parts / 1;
				fractional_parts = fractional_parts % 1;
			}
			return (int) packet_delay / 1;
		} catch (Exception ex) {
			return 0;
		} finally {
			lock.unlock();
		}
	}
	AtomicInteger lastdelay = new AtomicInteger();
	public int getLastdelay() {
		return lastdelay.get();
	}
	public void setLastdelay(AtomicInteger lastdelay) {
		this.lastdelay = lastdelay;
	}
	public void doDelayPacketSend() {
		try {
			lastdelay.set(getPacketDelay());
			if (lastdelay.get() > 50) {
				logger.warn("long delay " + lastdelay.get() + "ms, speed " + this.getSpeed());
				System.out.println("WARNING: long delay " + lastdelay.get() + "ms, speed " + this.getSpeed());
			}
			if (lastdelay.get() > 0) {
				Thread.sleep(lastdelay.get());
			}
		} catch (InterruptedException e) {}
	}
	public int getSpeed() {
		return speed.get();
	}
	public int getOriginalSpeed() {
		return speed.get();
	}
	public void setWaitSpeed(int speed) {
		// do nothing. We don't wait even when the other side does not reply
		// anything in time
		// this avoid hanging from this sender side when there are too many
		// awaiting packets in the queue and when the other end is not alive
	}
	//
	// set speed from speed calculation
	//
	public void setNewSpeed(int speed) {
		if (this.speed.get() != speed) {
			if (InitVar.FIXED_SPEED > 0) {
				this.speed.set(InitVar.FIXED_SPEED);
			} else if (InitVar.MAX_SPEED < speed) {
				this.speed.set(InitVar.MAX_SPEED);
			} else {
				this.speed.set(speed == 0 ? InitVar.MIN_SPEED : speed);
			}
			if (logger.isTraceEnabled())
				logger.trace("increase speed " + speed + " set speed " + this.speed.get() + " max " + InitVar.MAX_SPEED);
		}
	}
	public void incrClient() {
		numCients.incrementAndGet();
	}
	public void decrClient() {
		numCients.decrementAndGet();
	}
}
