package no.auke.p2p.m2;

import java.util.List;

import no.auke.p2p.m2.workers.keepalive.Peerid;
import no.auke.util.ByteUtil;
import no.auke.util.StringConv;

public class SocketHandlerMsg {
	public String getFunction() {
		return function;
	}
	public short getStatus() {
		return status;
	}
	public byte[] getData() {
		return data;
	}
	public boolean isRetMessage() {
		return ret != 0;
	}
	public void setStatus(short status) {
		this.status = status;
	}
	private String function;
	private short status;
	private byte ret;
	private byte[] data;
	
	// local data
	
	private long transactionId;
	public long getTransactionId() {return transactionId;}
	public void setTransactionId(long transactionId) {this.transactionId = transactionId;}
	
	private Peerid peerId;
	public Peerid getPeerId() {return peerId;}
	public void setPeerId(Peerid peerId) {this.peerId = peerId;}
	
	public SocketHandlerMsg(String function, int status, byte[] data) {
		this.function = function;
		this.status = (short) status;
		this.ret = 0;
		this.data = data;
	}
	public SocketHandlerMsg(byte[] indata) {
		List<byte[]> subs = ByteUtil.splitDynamicBytes(indata);
		function = StringConv.UTF8(subs.get(0));
		status = ByteUtil.getShort(subs.get(1));
		ret = subs.get(2)[0];
		data = subs.get(3);
	}
	public byte[] toBytes() {
		return ByteUtil.mergeDynamicBytesWithLength(StringConv.getBytes(function), ByteUtil.getBytes(status, 2), new byte[] { ret }, data);
	}
	public void setRetur() {
		ret = 1;
	}
	private SocketRetStatus retStatus;
	public synchronized SocketRetStatus getRetStatus() {
		return retStatus;
	}
	public synchronized void setRetStatus(SocketRetStatus retStatus) {
		this.retStatus = retStatus;
	}
}
