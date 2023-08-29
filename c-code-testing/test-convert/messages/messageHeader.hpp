//
// Created by leif on 8/28/2023.
//

#ifndef MESSAGEHEADER_H
#define MESSAGEHEADER_H

#include <cstdint>
#include <string>
#include <vector>
#include <map>

#include "messageAttribute.hpp"



class MessageHeader {

private:

    static const uint16_t MAPRESPONSE = 0x11;
    static const uint16_t MAPREQUEST = 0x12;
    static const uint16_t PINGCLOSE = 0x13;
    static const uint16_t PINGALIVE = 0x14;
    static const uint16_t PEERREQUEST = 0x15;
    static const uint16_t PEERRESPONSE = 0x16;
    static const uint16_t DATA = 0x17;
    static const uint16_t PINGPEER = 0x18;
    static const uint16_t PINGSERVICE = 0x19;
    static const uint16_t STREAMDATA = 0x20;
    static const uint16_t PINGPEERRESPONSE = 0x21;
    static const uint16_t DATAREPLY = 0x1A;
    static const uint16_t EMPTY = 0x1B;
    static const uint16_t MIDDLEMAN = 0x1C;
    static const uint16_t DIRECTPEERREQUEST = 0x1D;
    static const uint16_t NUDGE = 0x22;
    static const uint16_t SERIAL = 0x30;
    static const uint16_t SERIALREPLY = 0x31;

    enum headerType {
        PingClose,
        PingAlive,
        PingService,
        PeerRequest,
        PeerResponse,
        Data,
        StreamData,
        PingPeer,
        PingPeerResponse,
        MapResponse,
        MapRequest,
        DataReply,
        Empty,
        MiddleMan,
        DirectPeerRequest,
        nudge,
        Serial,
        SerialReply
    };

    uint16_t typeToInteger(headerType type) {
        if (type == MapResponse) {
            return MAPRESPONSE;
        } else if (type == MapRequest) {
            return MAPREQUEST;
        } else if (type == PingClose) {
            return PINGCLOSE;
        } else if (type == PingAlive) {
            return PINGALIVE;
        } else if (type == PingService) {
            return PINGSERVICE;
        } else if (type == PingPeer) {
            return PINGPEER;
        } else if (type == PingPeerResponse) {
            return PINGPEERRESPONSE;
        } else if (type == PeerRequest) {
            return PEERREQUEST;
        } else if (type == PeerResponse) {
            return PEERRESPONSE;
        } else if (type == Data) {
            return DATA;
        } else if (type == DataReply) {
            return DATAREPLY;
        } else if (type == StreamData) {
            return STREAMDATA;
        } else if (type == Empty) {
            return EMPTY;
        } else if (type == MiddleMan) {
            return MIDDLEMAN;
        } else if (type == DirectPeerRequest) {
            return DIRECTPEERREQUEST;
        } else if (type == nudge) {
            return NUDGE;
        } else if (type == Serial) {
            return SERIAL;
        } else if (type == SerialReply) {
            return SERIALREPLY;
        }
        return 0;
    }

    headerType _type = MessageHeader::headerType::Empty;
    vint _id = {0, 0, 0, 0};
    std::map<MessageAttribute::AttrType, MessageAttribute> * ma = new std::map<MessageAttribute::AttrType, MessageAttribute>();

public:

    MessageHeader() = default;
    ~MessageHeader() {
        delete ma;
    };

    explicit MessageHeader(headerType type) : MessageHeader() {
        _type = type;
    }

    headerType getType() { return _type; }

    void setTransactionID(vint &id) {
        memcpy(_id.data(), id.data(), 4);
    }

    void setTransactionID(int32_t &id) {
        memcpy(_id.data(), &id, 4);
    }

    vint getTransactionIDByte() {
        return _id;
    }

    int32_t getTransactionID() {
        int32_t id = 0;
        memcpy(&id, _id.data(), 4);
        return id;
    }

    bool equalTransactionID(MessageHeader &header) {
        return getTransactionID() == header.getTransactionID();
    }

    void addMessageAttribute(MessageAttribute &attr) {
        auto ret = ma->insert(std::pair<MessageAttribute::AttrType, MessageAttribute>(attr.getType(), attr));
    }
    MessageAttribute getMessageAttribute(MessageAttribute::AttrType &type) {
        auto res = ma->find(type);
        return res->second;
    }

    vint bytes;
    vint getBytes() {
        if (bytes.empty()) {
        try {
            size_t length = 8;
            for(auto & iter : *ma) {
                MessageAttribute attri =  iter.second;
                length +=attri.getLength();
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
//    int getLength() throws UtilityException {
//        return getBytes().length;
//    }
//    void parseAttributes(byte[] data) throws MessageAttributeParsingException {
//        try {
//            byte[] lengthArray = new byte[2];
//            System.arraycopy(data, 2, lengthArray, 0, 2);
//            int length = Utility.twoBytesToInteger(lengthArray);
//            System.arraycopy(data, 4, id, 0, 4);
//            byte[] cuttedData;
//            int offset = 8;
//            while (length > 0) {
//                cuttedData = new byte[length];
//                System.arraycopy(data, offset, cuttedData, 0, length);
//                MessageAttribute ma = MessageAttribute.parseCommonHeader(cuttedData);
//                addMessageAttribute(ma);
//                length -= ma.getLength();
//                offset += ma.getLength();
//            }
//        } catch (ArrayIndexOutOfBoundsException ex) {
//            throw new MessageAttributeParsingException("Parsing error");
//        } catch (UtilityException ue) {
//            throw new MessageAttributeParsingException("Parsing error");
//        }
//    }
//    void MessageHeader parseHeader(byte[] data) throws MessageHeaderParsingException {
//        try {
//            MessageHeader mh = new MessageHeader();
//            byte[] typeArray = new byte[2];
//            System.arraycopy(data, 0, typeArray, 0, 2);
//            int type = Utility.twoBytesToInteger(typeArray);
//            switch (type) {
//                case SERIAL:
//                    mh.setType(Serial);
//                    break;
//                case SERIALREPLY:
//                    mh.setType(SerialReply);
//                    break;
//                case DATA:
//                    mh.setType(Data);
//                    break;
//                case DATAREPLY:
//                    mh.setType(DataReply);
//                    break;
//                case STREAMDATA:
//                    mh.setType(StreamData);
//                    break;
//                case MAPRESPONSE:
//                    mh.setType(MapResponse);
//                    break;
//                case MAPREQUEST:
//                    mh.setType(MapRequest);
//                    break;
//                case PINGCLOSE:
//                    mh.setType(PingClose);
//                    break;
//                case PINGALIVE:
//                    mh.setType(PingAlive);
//                    break;
//                case PINGSERVICE:
//                    mh.setType(PingService);
//                    break;
//                case PINGPEER:
//                    mh.setType(PingPeer);
//                    break;
//                case PINGPEERRESPONSE:
//                    mh.setType(PingPeerResponse);
//                    break;
//                case PEERREQUEST:
//                    mh.setType(PeerRequest);
//                    break;
//                case PEERRESPONSE:
//                    mh.setType(PeerResponse);
//                    break;
//                case EMPTY:
//                    mh.setType(Empty);
//                    break;
//                case MIDDLEMAN:
//                    mh.setType(MiddleMan);
//                    break;
//                case DIRECTPEERREQUEST:
//                    mh.setType(DirectPeerRequest);
//                    break;
//                case NUDGE:
//                    mh.setType(nudge);
//                    break;
//                default:
//                    throw new MessageHeaderParsingException("Message type " + type + " is not supported");
//            }
//            return mh;
//        } catch (ArrayIndexOutOfBoundsException ex) {
//            throw new MessageHeaderParsingException("Parsing error");
//        } catch (UtilityException ue) {
//            throw new MessageHeaderParsingException("Parsing error");
//        }
//    }
}

#endif //MESSAGEHEADER_H
