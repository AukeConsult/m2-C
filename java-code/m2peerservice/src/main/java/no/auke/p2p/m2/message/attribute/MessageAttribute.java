/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.message.attribute;

import no.auke.p2p.m2.message.Utility;
import no.auke.p2p.m2.message.UtilityException;

public abstract class MessageAttribute implements MessageAttributeInterface {
	protected MessageAttributeType type;
	public MessageAttribute() {}
	public MessageAttribute(MessageAttributeType type) {
		setType(type);
	}
	public void setType(MessageAttributeType type) {
		this.type = type;
	}
	public MessageAttribute.MessageAttributeType getType() {
		return type;
	}
	public static int typeToInteger(MessageAttributeType type) {
		if (type == MessageAttributeType.SerialPacket)
			return SERIALPACKET;
		if (type == MessageAttributeType.SerialReply)
			return SERIALREPLY;
		if (type == MessageAttributeType.DataPacket)
			return DATAPACKET;
		if (type == MessageAttributeType.DataReplyPacket)
			return DATAREPLYPACKET;
		if (type == MessageAttributeType.PublicAddress)
			return PUBLICADDRESS;
		if (type == MessageAttributeType.LocalAddress)
			return LOCALADDRESS;
		if (type == MessageAttributeType.Peer_Local_Id)
			return PEER_LOCAL;
		if (type == MessageAttributeType.Peer_Remote_Id)
			return PEER_REMOTE;
		if (type == MessageAttributeType.Deviceid)
			return DEVICEID;
		if (type == MessageAttributeType.ErrorCode)
			return ERRORCODE;
		if (type == MessageAttributeType.DataDestination)
			return DATADESTINATION;
		if (type == MessageAttributeType.MapFile)
			return MAPFILE;
		if (type == MessageAttributeType.Dummy)
			return DUMMY;
		if (type == MessageAttributeType.MiddleManRequire)
			return MIDDLEMAN_REQUIRE;
		if (type == MessageAttributeType.MiddleManPeerRequest)
			return MIDDLEMAN_PEERREQUEST;
		if (type == MessageAttributeType.MiddleManPeerRequestForward)
			return MIDDLEMAN_PEERREQUESTFORWARD;
		if (type == MessageAttributeType.MiddleManInfo)
			return MIDDLEMANINFO;
		if (type == MessageAttributeType.RawData)
			return RAWDATA;
		if (type == MessageAttributeType.PublicKey)
			return PUBLICKEY;
		if (type == MessageAttributeType.SessionKey)
			return SESSIONKEY;
		if (type == MessageAttributeType.PeerInfo)
			return PEERINFO;
		if (type == MessageAttributeType.Aeskey)
			return AESKEY;
		if (type == MessageAttributeType.UnknownAttribute)
			return UNKNOWNATTRIBUTE;
		return -1;
	}
	public static MessageAttributeType intToType(long type) {
		if (type == SERIALPACKET)
			return MessageAttributeType.SerialPacket;
		if (type == SERIALREPLY)
			return MessageAttributeType.SerialReply;
		if (type == DATAPACKET)
			return MessageAttributeType.DataPacket;
		if (type == DATAREPLYPACKET)
			return MessageAttributeType.DataReplyPacket;
		if (type == PUBLICADDRESS)
			return MessageAttributeType.PublicAddress;
		if (type == LOCALADDRESS)
			return MessageAttributeType.LocalAddress;
		if (type == PEER_LOCAL)
			return MessageAttributeType.Peer_Local_Id;
		if (type == DEVICEID)
			return MessageAttributeType.Deviceid;
		if (type == ERRORCODE)
			return MessageAttributeType.ErrorCode;
		if (type == UNKNOWNATTRIBUTE)
			return MessageAttributeType.UnknownAttribute;
		if (type == PEER_REMOTE)
			return MessageAttributeType.Peer_Remote_Id;
		if (type == DATADESTINATION)
			return MessageAttributeType.DataDestination;
		if (type == MAPFILE)
			return MessageAttributeType.MapFile;
		if (type == DUMMY)
			return MessageAttributeType.Dummy;
		if (type == MIDDLEMAN_REQUIRE)
			return MessageAttributeType.MiddleManRequire;
		if (type == MIDDLEMAN_PEERREQUEST)
			return MessageAttributeType.MiddleManPeerRequest;
		if (type == MIDDLEMAN_PEERREQUESTFORWARD)
			return MessageAttributeType.MiddleManPeerRequestForward;
		if (type == MIDDLEMANINFO)
			return MessageAttributeType.MiddleManInfo;
		if (type == RAWDATA)
			return MessageAttributeType.RawData;
		if (type == PUBLICKEY)
			return MessageAttributeType.PublicKey;
		if (type == SESSIONKEY)
			return MessageAttributeType.SessionKey;
		if (type == PEERINFO)
			return MessageAttributeType.PeerInfo;
		if (type == AESKEY)
			return MessageAttributeType.Aeskey;
		return null;
	}
	abstract public byte[] getBytes() throws UtilityException;
	// abstract public MessageAttribute parse(byte[] data) throws
	// MessageAttributeParsingException;
	// public int getLength() throws UtilityException {
	// int length = getBytes().length;
	// return length;
	// }
	protected byte[] bytes;
	public int getLength() throws UtilityException {
		if (bytes == null) {
			bytes = getBytes();
		}
		return bytes.length;
	}
	public static MessageAttribute parseCommonHeader(byte[] data) throws MessageAttributeParsingException {
		try {
			byte[] typeArray = new byte[2];
			System.arraycopy(data, 0, typeArray, 0, 2);
			int type = Utility.twoBytesToInteger(typeArray);
			byte[] lengthArray = new byte[2];
			System.arraycopy(data, 2, lengthArray, 0, 2);
			int lengthValue = Utility.twoBytesToInteger(lengthArray);
			byte[] valueArray = new byte[lengthValue];
			System.arraycopy(data, 4, valueArray, 0, lengthValue);
			MessageAttribute ma;
			switch (type) {
			case SERIALPACKET:
				ma = SerialPacket.parse(valueArray);
				break;
			case SERIALREPLY:
				ma = SerialReply.parse(valueArray);
				break;
			case DATAPACKET:
				ma = DataPacket.parse(valueArray);
				break;
			case DATAREPLYPACKET:
				ma = DataReplyPacket.parse(valueArray);
				break;
			case PUBLICADDRESS:
				ma = PublicAddress.parse(valueArray);
				break;
			case LOCALADDRESS:
				ma = LocalAddress.parse(valueArray);
				break;
			case PEER_LOCAL:
				ma = PeerLocalId.parse(valueArray);
				break;
			case DEVICEID:
				ma = Deviceid.parse(valueArray);
				break;
			case ERRORCODE:
				ma = ErrorCode.parse(valueArray);
				break;
			case UNKNOWNATTRIBUTE:
				ma = UnknownAttribute.parse(valueArray);
				break;
			case PEER_REMOTE:
				ma = PeerRemoteId.parse(valueArray);
				break;
			case DATADESTINATION:
				ma = DataDestination.parse(valueArray);
				break;
			case MAPFILE:
				ma = MapFile.parse(valueArray);
				break;
			case MIDDLEMAN_REQUIRE:
				ma = MiddleManRequire.parse(valueArray);
				break;
			case MIDDLEMAN_PEERREQUEST:
				ma = MiddleManPeerRequest.parse(valueArray);
				break;
			case MIDDLEMAN_PEERREQUESTFORWARD:
				ma = MiddleManPeerRequestForward.parse(valueArray);
				break;
			case MIDDLEMANINFO:
				ma = MiddleManInfo.parse(valueArray);
				break;
			case RAWDATA:
				ma = RawDataPacket.parse(valueArray);
				break;
			case PUBLICKEY:
				ma = PublicKey.parse(valueArray);
				break;
			case SESSIONKEY:
				ma = SessionKey.parse(valueArray);
				break;
			case PEERINFO:
				ma = PeerAddInfo.parse(valueArray);
				break;
			case AESKEY:
				ma = PeerAddInfo.parse(valueArray);
				break;
			default:
				// TODO: unknown message type do not throw exception anymore
				if (type <= 0x0001) {
					throw new UnknownMessageAttributeException("Unknown mandatory message attribute", intToType(type));
				} else {
					ma = Dummy.parse(valueArray);
					break;
				}
			}
			return ma;
		} catch (UtilityException ue) {
			throw new MessageAttributeParsingException("Parsing error");
		}
	}
}
