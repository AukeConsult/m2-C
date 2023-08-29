//
// Created by leif on 8/28/2023.
//

#ifndef TEST_MESSAGEATTRIBUTE_HPP
#define TEST_MESSAGEATTRIBUTE_HPP

#include <cstdint>
#include <cstring>
#include <vector>

typedef std::vector<unsigned char> vint;

using namespace std;

struct MsgType {

    enum AttrType {
        PublicAddress,
        LocalAddress,
        Peer_Local_Id,
        Deviceid,
        ErrorCode,
        UnknownAttribute,
        Peer_Remote_Id,
        Dummy,
        DataPacket,
        DataDestination,
        MapFile,
        DataReplyPacket,
        MiddleManRequire,
        MiddleManPeerRequest,
        MiddleManPeerRequestForward,
        MiddleManInfo,
        RawData,
        PublicKey,
        SessionKey,
        PeerInfo,
        Aeskey,
        PeerLicence,
        SerialPacket,
        SerialReply
    }

    static const messageTypes;

    static const uint16_t PUBLICADDRESS = 0x0002;
    static const uint16_t LOCALADDRESS = 0x0004;
    static const uint16_t PEER_LOCAL = 0x0006;
    static const uint16_t DEVICEID = 0x0007;
    static const uint16_t ERRORCODE = 0x0009;
    static const uint16_t UNKNOWNATTRIBUTE = 0x000a;
    static const uint16_t PEER_REMOTE = 0x000c;
    static const uint16_t DATAPACKET = 0x000d;
    static const uint16_t DATADESTINATION = 0x000f;
    static const uint16_t MAPFILE = 0x0010;
    static const uint16_t DATAREPLYPACKET = 0x0011;
    static const uint16_t MIDDLEMAN_REQUIRE = 0x0012;
    static const uint16_t MIDDLEMAN_PEERREQUEST = 0x0013;
    static const uint16_t MIDDLEMAN_PEERREQUESTFORWARD = 0x0014;
    static const uint16_t DUMMY = 0x0000;
    static const uint16_t MIDDLEMANINFO = 0x0015;
    static const uint16_t RAWDATA = 0x0016;
    static const uint16_t PUBLICKEY = 0x0017;
    static const uint16_t SESSIONKEY = 0x0018;

    static const uint16_t AESKEY = 0x0019;
    static const uint16_t PEERINFO = 0x8fff;
    static const uint16_t PEERLICENCE = 0x8eff;

    static const uint16_t SERIALPACKET = 0x011a;
    static const uint16_t SERIALREPLY = 0x011b;

    uint16_t typeToInt(const AttrType &type) {
        if (type == SerialPacket)
            return SERIALPACKET;
        if (type == SerialReply)
            return SERIALREPLY;
        if (type == DataPacket)
            return DATAPACKET;
        if (type == DataReplyPacket)
            return DATAREPLYPACKET;
        if (type == PublicAddress)
            return PUBLICADDRESS;
        if (type == LocalAddress)
            return LOCALADDRESS;
        if (type == Peer_Local_Id)
            return PEER_LOCAL;
        if (type == Peer_Remote_Id)
            return PEER_REMOTE;
        if (type == Deviceid)
            return DEVICEID;
        if (type == ErrorCode)
            return ERRORCODE;
        if (type == DataDestination)
            return DATADESTINATION;
        if (type == MapFile)
            return MAPFILE;
        if (type == Dummy)
            return DUMMY;
        if (type == MiddleManRequire)
            return MIDDLEMAN_REQUIRE;
        if (type == MiddleManPeerRequest)
            return MIDDLEMAN_PEERREQUEST;
        if (type == MiddleManPeerRequestForward)
            return MIDDLEMAN_PEERREQUESTFORWARD;
        if (type == MiddleManInfo)
            return MIDDLEMANINFO;
        if (type == RawData)
            return RAWDATA;
        if (type == PublicKey)
            return PUBLICKEY;
        if (type == SessionKey)
            return SESSIONKEY;
        if (type == PeerInfo)
            return PEERINFO;
        if (type == Aeskey)
            return AESKEY;
        if (type == UnknownAttribute)
            return UNKNOWNATTRIBUTE;
        return -1;

    };

    AttrType toType(uint16_t type) {
        if (type == SERIALPACKET)
            return SerialPacket;
        if (type == SERIALREPLY)
            return SerialReply;
        if (type == DATAPACKET)
            return DataPacket;
        if (type == DATAREPLYPACKET)
            return DataReplyPacket;
        if (type == PUBLICADDRESS)
            return PublicAddress;
        if (type == LOCALADDRESS)
            return LocalAddress;
        if (type == PEER_LOCAL)
            return Peer_Local_Id;
        if (type == DEVICEID)
            return Deviceid;
        if (type == ERRORCODE)
            return ErrorCode;
        if (type == UNKNOWNATTRIBUTE)
            return UnknownAttribute;
        if (type == PEER_REMOTE)
            return Peer_Remote_Id;
        if (type == DATADESTINATION)
            return DataDestination;
        if (type == MAPFILE)
            return MapFile;
        if (type == DUMMY)
            return Dummy;
        if (type == MIDDLEMAN_REQUIRE)
            return MiddleManRequire;
        if (type == MIDDLEMAN_PEERREQUEST)
            return MiddleManPeerRequest;
        if (type == MIDDLEMAN_PEERREQUESTFORWARD)
            return MiddleManPeerRequestForward;
        if (type == MIDDLEMANINFO)
            return MiddleManInfo;
        if (type == RAWDATA)
            return RawData;
        if (type == PUBLICKEY)
            return PublicKey;
        if (type == SESSIONKEY)
            return SessionKey;
        if (type == PEERINFO)
            return PeerInfo;
        if (type == AESKEY)
            return Aeskey;
        return Dummy;
    };

};

class MessageAttribute  {

private:




public:

    enum AttrType {
        PublicAddress,
        LocalAddress,
        Peer_Local_Id,
        Deviceid,
        ErrorCode,
        UnknownAttribute,
        Peer_Remote_Id,
        Dummy,
        DataPacket,
        DataDestination,
        MapFile,
        DataReplyPacket,
        MiddleManRequire,
        MiddleManPeerRequest,
        MiddleManPeerRequestForward,
        MiddleManInfo,
        RawData,
        PublicKey,
        SessionKey,
        PeerInfo,
        Aeskey,
        PeerLicence,
        SerialPacket,
        SerialReply
    };

    AttrType _type;

    MessageAttribute(): _type(Dummy) {} ;
    explicit MessageAttribute(const AttrType &type) {_type = type;}

    AttrType getType() {return _type;}
    virtual vector<uint8_t> getBytes();

    vector<uint8_t> _bytes;
    size_t getLength()  {
            if (_bytes.empty()) _bytes = getBytes();
            return _bytes.size();
    }

//    MessageAttribute parseCommonHeader(const vector<uint8_t> &bytes)  {
//
//        uint16_t typeVal;
//        memcpy(&typeVal,bytes.data(), 2);
//        uint16_t length;
//        memcpy(&length,bytes.data()+2, 2);
//        memcpy(_bytes.data(),bytes.data()+4, length);
//
//        switch (typeVal) {
//            case SERIALPACKET:
//                return SerialPacket::parse(valueArray);
//            case SERIALREPLY:
//                return SerialReply.parse(valueArray);
//            case DATAPACKET:
//                return DataPacket.parse(valueArray);
//            case DATAREPLYPACKET:
//                return DataReplyPacket.parse(valueArray);
//            case PUBLICADDRESS:
//                return PublicAddress.parse(valueArray);
//            case LOCALADDRESS:
//                return LocalAddress.parse(valueArray);
//            case PEER_LOCAL:
//                return PeerLocalId.parse(valueArray);
//            case DEVICEID:
//                return Deviceid.parse(valueArray);
//            case ERRORCODE:
//                return ErrorCode.parse(valueArray);
//            case UNKNOWNATTRIBUTE:
//                return UnknownAttribute.parse(valueArray);
//            case PEER_REMOTE:
//                return PeerRemoteId.parse(valueArray);
//            case DATADESTINATION:
//                return DataDestination.parse(valueArray);
//            case MAPFILE:
//                return MapFile.parse(valueArray);
//            case MIDDLEMAN_REQUIRE:
//                return MiddleManRequire.parse(valueArray);
//            case MIDDLEMAN_PEERREQUEST:
//                return MiddleManPeerRequest.parse(valueArray);
//            case MIDDLEMAN_PEERREQUESTFORWARD:
//                return MiddleManPeerRequestForward.parse(valueArray);
//            case MIDDLEMANINFO:
//                return MiddleManInfo.parse(valueArray);
//            case RAWDATA:
//                return RawDataPacket.parse(valueArray);
//            case PUBLICKEY:
//                return PublicKey.parse(valueArray);
//            case SESSIONKEY:
//                return SessionKey.parse(valueArray);
//            case PEERINFO:
//                return PeerAddInfo.parse(valueArray);
//            case AESKEY:
//                return PeerAddInfo.parse(valueArray);
//            default:
//                return Dummy.parse(valueArray);
//        }
//    }
};

#endif //TEST_MESSAGEATTRIBUTE_HPP
