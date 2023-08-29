package no.auke.p2p.m2.sockets.udp;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import no.auke.p2p.m2.InitVar;

public class SpeedTester {

	int PERIOD = 250;
	AtomicInteger totalBytes = new AtomicInteger();
	AtomicLong lastCheck = new AtomicLong();
	public SpeedTester() {}
	public boolean trowAway(int lenBytes) {
		if (lastCheck.get() == 0) {
			lastCheck.set(System.currentTimeMillis());
		}
		totalBytes.addAndGet(lenBytes);
		//System.out.println("bytes " + lenBytes + " total " + totalBytes.get() + " limit " + (InitVar.TEST_SPEED_DELAY * 1024 / 1000) * PERIOD);
		if (System.currentTimeMillis() - lastCheck.get() < PERIOD && totalBytes.get() > (InitVar.TEST_SPEED_DELAY * 1024 / 1000) * PERIOD) {
			return true;
		} else if (System.currentTimeMillis() - lastCheck.get() > PERIOD) {
			totalBytes.set(0);
			lastCheck.set(System.currentTimeMillis());
			return false;
		} else {
			return false;
		}
	}
}
