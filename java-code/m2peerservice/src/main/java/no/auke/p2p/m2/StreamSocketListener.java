package no.auke.p2p.m2;

import no.auke.p2p.m2.StreamSocket.StreamPacket;

public abstract class StreamSocketListener {
	// 10 millisecond tolerance in delay
	protected final long DELAY_TOOLERANCE = 3;
	public abstract void onIncomming(StreamPacket buffer);
	public abstract void onNoData();
}
