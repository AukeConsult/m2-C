/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.message.attribute;
public interface MessageAttributeInterface {
	public enum MessageAttributeType {
		PublicAddress, LocalAddress, Peer_Local_Id, Deviceid, ErrorCode, UnknownAttribute, Peer_Remote_Id, Dummy, DataPacket, DataDestination, MapFile, DataReplyPacket, MiddleManRequire, MiddleManPeerRequest, MiddleManPeerRequestForward, MiddleManInfo, RawData, PublicKey, SessionKey, PeerInfo, Aeskey, PeerLicence, SerialPacket, SerialReply
	}
	final static int PUBLICADDRESS = 0x0002;
	final static int LOCALADDRESS = 0x0004;
	final static int PEER_LOCAL = 0x0006;
	final static int DEVICEID = 0x0007;
	final static int ERRORCODE = 0x0009;
	final static int UNKNOWNATTRIBUTE = 0x000a;
	final static int PEER_REMOTE = 0x000c;
	final static int DATAPACKET = 0x000d;
	final static int DATADESTINATION = 0x000f;
	final static int MAPFILE = 0x0010;
	final static int DATAREPLYPACKET = 0x0011;
	final static int MIDDLEMAN_REQUIRE = 0x0012;
	final static int MIDDLEMAN_PEERREQUEST = 0x0013;
	final static int MIDDLEMAN_PEERREQUESTFORWARD = 0x0014;
	final static int DUMMY = 0x0000;
	final static int MIDDLEMANINFO = 0x0015;
	final static int RAWDATA = 0x0016;
	final static int PUBLICKEY = 0x0017;
	final static int SESSIONKEY = 0x0018;
	final static int AESKEY = 0x0019;
	// TODO: unknown ID < 0x7fff throw exception
	// this do not
	// remove this exception in attribute interface
	final static int PEERINFO = 0x8fff;
	final static int PEERLICENCE = 0x8eff;
	// new attributes for new serial protocol
	final static int SERIALPACKET = 0x011a;
	final static int SERIALREPLY = 0x011b;
}