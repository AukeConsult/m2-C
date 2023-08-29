/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.message.header;
public interface MessageHeaderInterface {
	public enum MessageHeaderType {
		PingClose, PingAlive, PingService, PeerRequest, PeerResponse, Data, StreamData, PingPeer, PingPeerResponse, MapResponse, MapRequest, DataReply, Empty, MiddleMan, DirectPeerRequest, nudge, Serial, SerialReply
	};
	final static int MAPRESPONSE = 0x11;
	final static int MAPREQUEST = 0x12;
	final static int PINGCLOSE = 0x13;
	final static int PINGALIVE = 0x14;
	final static int PEERREQUEST = 0x15;
	final static int PEERRESPONSE = 0x16;
	final static int DATA = 0x17;
	final static int PINGPEER = 0x18;
	final static int PINGSERVICE = 0x19;
	final static int STREAMDATA = 0x20;
	final static int PINGPEERRESPONSE = 0x21;
	final static int DATAREPLY = 0x1A;
	final static int EMPTY = 0x1B;
	final static int MIDDLEMAN = 0x1C;
	final static int DIRECTPEERREQUEST = 0x1D;
	final static int NUDGE = 0x22;
	final static int SERIAL = 0x30;
	final static int SERIALREPLY = 0x31;
}