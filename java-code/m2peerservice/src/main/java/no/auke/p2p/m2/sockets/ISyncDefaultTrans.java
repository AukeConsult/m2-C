package no.auke.p2p.m2.sockets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.util.StringConv;

public abstract class ISyncDefaultTrans extends Transaction {
	private static final Logger logger = LoggerFactory.getLogger(ISyncDefaultTrans.class);
	public ISyncDefaultTrans(byte[] buffer, boolean local) {
		setRetbuffer(buffer);
		this.local = local;
	}
	// default
	public ISyncDefaultTrans(byte[] buffer) {
		if (logger.isTraceEnabled())
			logger.trace("ISyncDefaultTrans: set retur message " + StringConv.UTF8(buffer));
		setRetbuffer(buffer);
	}
	public ISyncDefaultTrans(String retur) {
		if (logger.isTraceEnabled())
			logger.trace("ISyncDefaultTrans: set retur message " + retur);
		setRetbuffer(StringConv.getBytes(retur));
	}
	public abstract void commit();
	public abstract void rollback();
}
