/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.sockets.udp;

import java.util.Map;

import no.auke.p2p.m2.general.ReturMessageTypes;
import no.auke.p2p.m2.message.attribute.DataPacket;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.workers.PeerSession;

public interface ISocketBuffer {
	public abstract ReturMessageTypes getLastretcode();
	public abstract String getLastmessage();
	public abstract Map<Integer, DataPacket> getDatapackets();
	public abstract int getPort();
	public abstract int getMessageId();
	public abstract void setMessageId(int messageId);
	public abstract int length();
	public abstract String getPeerId();
	public abstract void setPeerId(String peerId);
	public abstract byte[] getBuffer();
	public abstract String toString();
	// sender side
	//
	// complete sent
	public abstract boolean isComplete();
	// error in sending
	public abstract boolean isSendError();
	// send buffer
	public abstract void send(final PeerSession peeragent);
	// wait for send to complete
	public abstract void waitForsent(final PeerSession peeragent, int timeout);
	// handle reply messages
	public abstract boolean gotReply(MessageHeader receiveMH);
	// Receiver side
	public abstract boolean gotDataPacket(final MessageHeader receiveMH, final DataPacket packet);
}