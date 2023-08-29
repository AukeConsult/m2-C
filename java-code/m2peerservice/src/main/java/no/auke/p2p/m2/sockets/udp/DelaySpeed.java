/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.sockets.udp;

import java.util.concurrent.atomic.AtomicInteger;



import no.auke.p2p.m2.InitVar;

public class DelaySpeed {

	private AtomicInteger speed = new AtomicInteger();
	private AtomicInteger speed_wait = new AtomicInteger();
	private AtomicInteger numCients = new AtomicInteger(0);
	public DelaySpeed() {
		setNewSpeed(InitVar.MAX_SPEED / 2);
	}
	//
	// TODO: Calculate delay, check is this can be made faster
	//
	// current speed calculated
	private int numCients_current = 0;
	private int speed_current = 0;
	private int speed_current_halt = 0;
	// low delay in MS
	private int delay_low = 0;
	// high delay in MS
	private int delay_high = 0;
	// number of low waits
	private int numlow = 0;
	// number of high waits
	private int numhigh = 0;
	// initial factor of low and high waits
	private double highlowfactor = 0;
	private Object doDelay = new Object();
	//
	// Get the current delay factor
	// Wait is on hole unit of millisecond
	// waits are calculated as number of whole ms
	// for ex. wait 10.7 milliseconds is calculated as ->
	// numwaits for 10 ms + numwaits for 11 ms
	//
	public int getPacketDelay() {
		synchronized (doDelay) {
			if ((numhigh + numlow) <= 0 || speed_current != speed.get() || speed_current_halt != speed_wait.get() || numCients_current != numCients.get()) {
				// save for next try
				speed_current = speed.get();
				speed_current_halt = speed_wait.get();
				numCients_current = numCients.get();
				// use speed halt if set, otherwise use ordinary speed
				int speed_active = speed_wait.get() > 0 ? speed_wait.get() : speed.get();
				if (speed_active == 0) {
					speed_active = 1;
				}
				// set the real delay in MS between each packet
				// adjust for number of sending clients
				double packet_delay = numCients.get() > 0 ? (1000.0 / ((speed_active * 1024.0) / (InitVar.PACKET_SIZE + 0.0))) / numCients.get() : 0;
				// calculate low and high wait in MS
				delay_low = (int) (packet_delay / 1);
				delay_high = delay_low + 1;
				// System.out.println("speed " + speed_current +
				// " packet_delay " + packet_delay + " low " + delay_low +
				// " high " + delay_high + " xxx " + (packet_delay % 1));
				// calculate number of wait for high and low
				numhigh = (int) (100 * (packet_delay % 1));
				numlow = 100 - numhigh;
				// set initial factor between high and low
				// used to distribute waits between high an low
				highlowfactor = numhigh / numlow;
				// System.out.println(" num high " + numhigh + " numlow " +
				// numlow + " highlow factor " + highlowfactor);
			}
			if (numlow == 0 || (numhigh > 0 && (numhigh / numlow) > highlowfactor)) {
				numhigh--;
				return delay_high;
			} else if (numlow > 0 || numhigh == 0) {
				numlow--;
				return delay_low;
			} else {
				return 0;
			}
		}
	}
	//
	// do the actual delay
	// this function will be executed in different threads
	// on for each peer session port / each concurrent message sent
	//
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
			if (lastdelay.get() > 500) {
				System.out.println("WARNING: long delay " + lastdelay.get() + "ms, speed " + this.getSpeed());
			}
			if (lastdelay.get() > 0) {
				// TODO: Leif, please look into this problem. We should not
				// avoid delaying too long
				Thread.sleep(lastdelay.get());
			}
		} catch (InterruptedException e) {}
	}
	
	public int getOriginalSpeed() {return speed.get();}
	public int getSpeed() {return speed_wait.get() > 0 ? speed_wait.get() : speed.get();}
	
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
		}
	}
	public void setWaitSpeed(int speed) {this.speed_wait.set(speed);}
	public void incrClient() {numCients.incrementAndGet();}
	public void decrClient() {numCients.decrementAndGet();}
}
