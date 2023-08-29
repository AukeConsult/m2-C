package no.auke.p2p.m2.sockets.udp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ISpeedCalc {
	protected SendStatistics stat;
	protected List<Float> reply_success;
	protected List<Double> reply_time;
	protected List<Double> speed_reply;
	public double getReplySpeedOverall(double speed) {
		synchronized (speed_reply) {
			if (speed >= 0) {
				speed_reply.add(speed);
				if (speed_reply.size() > SendCalcPolecy2.NUM_HISTORY) {
					speed_reply.remove(0);
				}
			}
			int size = speed_reply.size();
			if (size > 0) {
				double total = 0;
				for (double value : speed_reply) {
					total += value;
				}
				return total / size;
			} else {
				return 0;
			}
		}
	}
	public double getReplyTimeOverall(Double time) {
		synchronized (reply_time) {
			if (time >= 0) {
				reply_time.add(time);
				if (reply_time.size() > SendCalcPolecy2.NUM_HISTORY) {
					reply_time.remove(0);
				}
			}
			int size = reply_time.size();
			if (size > 0) {
				double total = 0.0;
				for (Double value : reply_time) {
					total += value;
				}
				return total / size;
			} else {
				return 0;
			}
		}
	}
	public ISpeedCalc(SendStatistics stat) {
		this.stat = stat;
		reply_success = Collections.synchronizedList(new ArrayList<Float>());
		reply_time = Collections.synchronizedList(new ArrayList<Double>());
		speed_reply = Collections.synchronizedList(new ArrayList<Double>());
	}
	protected ReentrantLock locking = new ReentrantLock();
	public abstract void startMessage();
	public abstract void completMessage();
	public abstract void replyChunk(long sendTime, long replyTime, int bytesSent, int bytesReplied, boolean complete);
	public abstract void sendChunk(int bytes);
	// try calculate in regular periods
	public abstract void calculate();
	public abstract void setFail();
	public abstract boolean isFailed();
	public abstract void printStatistics();
}
