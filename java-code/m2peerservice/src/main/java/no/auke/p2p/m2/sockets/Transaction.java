package no.auke.p2p.m2.sockets;

import no.auke.p2p.m2.SocketRetStatus;

public abstract class Transaction {
	public boolean reply = true; // reply=false, do not call back
	public boolean local = false; // local=TRUE, do not call back
	public byte[] getRetbuffer() {
		return retbuffer;
	}
	public void setRetbuffer(byte[] retbuffer) {
		this.retbuffer = retbuffer;
	}
	private byte[] retbuffer = new byte[0];
	public Transaction() {}
	public Transaction(byte[] returnBuffer) {
		this.retbuffer = returnBuffer;
	}
	private SocketRetStatus ret;
	public SocketRetStatus getReStatus() {
		return ret;
	}
	public void setReStatus(SocketRetStatus ret) {
		this.ret = ret;
	}
	public abstract void commit();
	public abstract void rollback();
}
