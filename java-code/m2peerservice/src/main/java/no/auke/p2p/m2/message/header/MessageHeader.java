/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.message.header;

import java.util.Iterator;
import java.util.TreeMap;

import no.auke.p2p.m2.message.Utility;
import no.auke.p2p.m2.message.UtilityException;
import no.auke.p2p.m2.message.attribute.*;
import no.auke.p2p.m2.workers.keepalive.NetAddress;

public class MessageHeader implements MessageHeaderInterface {
	private MessageHeaderType type = MessageHeaderType.Empty;
	private byte[] id = new byte[4];
	private TreeMap<MessageAttribute.MessageAttributeType, MessageAttribute> ma = new TreeMap<MessageAttribute.MessageAttributeType, MessageAttribute>();
	public MessageHeader() {super();}
	public MessageHeader(MessageHeaderType type) {
		super();
		setType(type);
	}
	
	public void setType(MessageHeaderType type) {this.type = type;}
	public MessageHeaderType getType() {return type;}
	private NetAddress address;
	public NetAddress getAddress() {return address;}
	public void setAddress(NetAddress address) {this.address = address;}
	
	public static int typeToInteger(MessageHeaderType type) {
		if (type == MessageHeaderType.MapResponse) {
			return MAPRESPONSE;
		} else if (type == MessageHeaderType.MapRequest) {
			return MAPREQUEST;
		} else if (type == MessageHeaderType.PingClose) {
			return PINGCLOSE;
		} else if (type == MessageHeaderType.PingAlive) {
			return PINGALIVE;
		} else if (type == MessageHeaderType.PingService) {
			return PINGSERVICE;
		} else if (type == MessageHeaderType.PingPeer) {
			return PINGPEER;
		} else if (type == MessageHeaderType.PingPeerResponse) {
			return PINGPEERRESPONSE;
		} else if (type == MessageHeaderType.PeerRequest) {
			return PEERREQUEST;
		} else if (type == MessageHeaderType.PeerResponse) {
			return PEERRESPONSE;
		} else if (type == MessageHeaderType.Data) {
			return DATA;
		} else if (type == MessageHeaderType.DataReply) {
			return DATAREPLY;
		} else if (type == MessageHeaderType.StreamData) {
			return STREAMDATA;
		} else if (type == MessageHeaderType.Empty) {
			return EMPTY;
		} else if (type == MessageHeaderType.MiddleMan) {
			return MIDDLEMAN;
		} else if (type == MessageHeaderType.DirectPeerRequest) {
			return DIRECTPEERREQUEST;
		} else if (type == MessageHeaderType.nudge) {
			return NUDGE;
		} else if (type == MessageHeaderType.Serial) {
			return SERIAL;
		} else if (type == MessageHeaderType.SerialReply) {
			return SERIALREPLY;
		}
		return 0;
	}
	public void setTransactionID(byte[] id) {
		System.arraycopy(id, 0, this.id, 0, 4);
	}
	public void setTransactionID(int id) {
		try {
			setTransactionID(Utility.integerToFourBytes(id));
		} catch (UtilityException e) {}
	}
	public Integer getTransactionID() {
		int idCopy = 0;
		try {
			idCopy = (int) Utility.fourBytesToLong(id);
		} catch (UtilityException e) {}
		return Integer.valueOf(idCopy);
	}
	public byte[] getTransactionIDByte() {
		return id;
	}
	public boolean equalTransactionID(MessageHeader header) {
		return equalTransactionID(header.getTransactionIDByte());
	}
	public boolean equalTransactionID(byte[] idHeader) {
		if (idHeader == null || idHeader.length != 4)
			return false;
		if ((idHeader[0] == id[0]) && (idHeader[1] == id[1]) && (idHeader[2] == id[2]) && (idHeader[3] == id[3]))
			return true;
		else
			return false;
	}
	public void addMessageAttribute(MessageAttribute attri) {
		ma.put(attri.getType(), attri);
	}
	public MessageAttribute getMessageAttribute(MessageAttribute.MessageAttributeType type) {
		return ma.get(type);
	}
	private byte[] bytes = new byte[0];
	public byte[] getBytes() {
		if (bytes.length == 0) {
			try {
				int length = 8;
				Iterator<MessageAttribute.MessageAttributeType> it = ma.keySet().iterator();
				while (it.hasNext()) {
					MessageAttribute attri = ma.get(it.next());
					length += attri.getLength();
				}
				// add attribute size + attributes.getSize();
				byte[] result = new byte[length];
				System.arraycopy(Utility.integerToTwoBytes(typeToInteger(type)), 0, result, 0, 2);
				System.arraycopy(Utility.integerToTwoBytes(length - 8), 0, result, 2, 2);
				System.arraycopy(id, 0, result, 4, 4);
				// Arraycopy of attributes
				int offset = 8;
				it = ma.keySet().iterator();
				while (it.hasNext()) {
					MessageAttribute attri = ma.get(it.next());
					System.arraycopy(attri.getBytes(), 0, result, offset, attri.getLength());
					offset += attri.getLength();
				}
				bytes = result;
			} catch (UtilityException e) {
				e.printStackTrace();
			}
		}
		return bytes;
	}
	public int getLength() throws UtilityException {
		return getBytes().length;
	}
	public void parseAttributes(byte[] data) throws MessageAttributeParsingException {
		try {
			byte[] lengthArray = new byte[2];
			System.arraycopy(data, 2, lengthArray, 0, 2);
			int length = Utility.twoBytesToInteger(lengthArray);
			System.arraycopy(data, 4, id, 0, 4);
			byte[] cuttedData;
			int offset = 8;
			while (length > 0) {
				cuttedData = new byte[length];
				System.arraycopy(data, offset, cuttedData, 0, length);
				MessageAttribute ma = MessageAttribute.parseCommonHeader(cuttedData);
				addMessageAttribute(ma);
				length -= ma.getLength();
				offset += ma.getLength();
			}
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new MessageAttributeParsingException("Parsing error");
		} catch (UtilityException ue) {
			throw new MessageAttributeParsingException("Parsing error");
		}
	}
	public static MessageHeader parseHeader(byte[] data) throws MessageHeaderParsingException {
		try {
			MessageHeader mh = new MessageHeader();
			byte[] typeArray = new byte[2];
			System.arraycopy(data, 0, typeArray, 0, 2);
			int type = Utility.twoBytesToInteger(typeArray);
			switch (type) {
			case SERIAL:
				mh.setType(MessageHeaderType.Serial);
				break;
			case SERIALREPLY:
				mh.setType(MessageHeaderType.SerialReply);
				break;
			case DATA:
				mh.setType(MessageHeaderType.Data);
				break;
			case DATAREPLY:
				mh.setType(MessageHeaderType.DataReply);
				break;
			case STREAMDATA:
				mh.setType(MessageHeaderType.StreamData);
				break;
			case MAPRESPONSE:
				mh.setType(MessageHeaderType.MapResponse);
				break;
			case MAPREQUEST:
				mh.setType(MessageHeaderType.MapRequest);
				break;
			case PINGCLOSE:
				mh.setType(MessageHeaderType.PingClose);
				break;
			case PINGALIVE:
				mh.setType(MessageHeaderType.PingAlive);
				break;
			case PINGSERVICE:
				mh.setType(MessageHeaderType.PingService);
				break;
			case PINGPEER:
				mh.setType(MessageHeaderType.PingPeer);
				break;
			case PINGPEERRESPONSE:
				mh.setType(MessageHeaderType.PingPeerResponse);
				break;
			case PEERREQUEST:
				mh.setType(MessageHeaderType.PeerRequest);
				break;
			case PEERRESPONSE:
				mh.setType(MessageHeaderType.PeerResponse);
				break;
			case EMPTY:
				mh.setType(MessageHeaderType.Empty);
				break;
			case MIDDLEMAN:
				mh.setType(MessageHeaderType.MiddleMan);
				break;
			case DIRECTPEERREQUEST:
				mh.setType(MessageHeaderType.DirectPeerRequest);
				break;
			case NUDGE:
				mh.setType(MessageHeaderType.nudge);
				break;
			default:
				throw new MessageHeaderParsingException("Message type " + type + " is not supported");
			}
			return mh;
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new MessageHeaderParsingException("Parsing error");
		} catch (UtilityException ue) {
			throw new MessageHeaderParsingException("Parsing error");
		}
	}
}