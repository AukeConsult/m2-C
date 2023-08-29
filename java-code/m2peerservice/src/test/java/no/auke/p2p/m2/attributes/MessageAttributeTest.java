package no.auke.p2p.m2.attributes;

import java.util.Arrays;
import java.util.Random;

import no.auke.p2p.m2.message.Address;
import no.auke.p2p.m2.message.UtilityException;
import no.auke.p2p.m2.message.attribute.DataPacket;
import no.auke.p2p.m2.message.attribute.DataReplyPacket;
import no.auke.p2p.m2.message.attribute.Deviceid;
import no.auke.p2p.m2.message.attribute.MessageAttributeException;
import no.auke.p2p.m2.message.attribute.MessageAttributeInterface.MessageAttributeType;
import no.auke.p2p.m2.message.attribute.MessageAttributeParsingException;
import no.auke.p2p.m2.message.attribute.PeerRemoteId;
import no.auke.p2p.m2.message.attribute.PublicAddress;
import no.auke.p2p.m2.message.attribute.PeerLocalId;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.message.header.MessageHeaderInterface.MessageHeaderType;
import no.auke.p2p.m2.message.header.MessageHeaderParsingException;
import junit.framework.TestCase;

// TODO: make unit test's for all attribute types

public class MessageAttributeTest extends TestCase {
        
    public void test_Datapacket() throws UtilityException, MessageAttributeParsingException, MessageHeaderParsingException{

        Random rnd = new Random();
        
        for(int i=0;i<1000;i++){

            DataPacket packet_out = new DataPacket();
            packet_out.setPort(10);
            packet_out.setNumber(99);
            packet_out.setTotal(100);
            
            byte[] data = new byte[i];
            rnd.nextBytes(data);
            
            packet_out.setData(data);
            
            MessageHeader message = new MessageHeader(MessageHeaderType.Empty);
            message.addMessageAttribute(packet_out);
            
            MessageHeader readmessage = MessageHeader.parseHeader(message.getBytes());
            readmessage.parseAttributes(message.getBytes());
            DataPacket packet_in = (DataPacket) readmessage.getMessageAttribute(MessageAttributeType.DataPacket);
                    
            assertEquals("packetnum",packet_in.getNumber(),packet_out.getNumber());
            assertEquals("total",packet_in.getTotal(),packet_out.getTotal());
            assertEquals("port",packet_in.getPort(),packet_out.getPort());
            assertTrue("data",Arrays.equals(packet_in.getData(),packet_out.getData()));

        }
        
    }
    
    public void test_DataReplypacket() throws UtilityException, MessageAttributeParsingException, MessageHeaderParsingException{

        
        for(int i=0;i<1000;i++){
            
            DataReplyPacket packet_out = new DataReplyPacket();
            packet_out.setPort(10);
            packet_out.setComplete(true);
            packet_out.setPacketList(new int[]{1,2,3,4});
            
            for(int x=0;x<i;x++){
                
                packet_out.addPacketnum(x);
            
            }
            
            MessageHeader message = new MessageHeader(MessageHeaderType.Empty);
            message.addMessageAttribute(packet_out);
            
            MessageHeader readmessage = MessageHeader.parseHeader(message.getBytes());
            readmessage.parseAttributes(message.getBytes());
            DataReplyPacket packet_in = (DataReplyPacket) readmessage.getMessageAttribute(MessageAttributeType.DataReplyPacket);        
            
            assertEquals("port",packet_in.getPort(),packet_out.getPort());
            assertTrue("packetlist",Arrays.equals(packet_in.getPacketList(),packet_out.getPacketList()));
            
        }
        
    }
    
    public void test_PeerUsername() throws UtilityException, MessageAttributeParsingException, MessageHeaderParsingException{

        
        for(int i=0;i<1000;i++){
            
            PeerRemoteId packet_out = new PeerRemoteId();
            packet_out.setId("dsdfsdfsdf"+String.valueOf(i));
            
            MessageHeader message = new MessageHeader(MessageHeaderType.Empty);
            message.addMessageAttribute(packet_out);
            
            MessageHeader readmessage = MessageHeader.parseHeader(message.getBytes());
            readmessage.parseAttributes(message.getBytes());
            PeerRemoteId packet_in = (PeerRemoteId) readmessage.getMessageAttribute(MessageAttributeType.Peer_Remote_Id);        
            
            assertEquals("name",packet_in.getId(),packet_out.getId());

        }
        
    }
    
    public void test_Username() throws UtilityException, MessageAttributeParsingException, MessageHeaderParsingException{

        
        for(int i=0;i<1000;i++){
            
            PeerLocalId packet_out = new PeerLocalId();
            packet_out.setUsername("dsdfsdfsdf"+String.valueOf(i));
            
            MessageHeader message = new MessageHeader(MessageHeaderType.Empty);
            message.addMessageAttribute(packet_out);
            
            MessageHeader readmessage = MessageHeader.parseHeader(message.getBytes());
            readmessage.parseAttributes(message.getBytes());
            PeerLocalId packet_in = (PeerLocalId) readmessage.getMessageAttribute(MessageAttributeType.Peer_Local_Id);        
            
            assertEquals("name",packet_in.getUsername(),packet_out.getUsername());

        }
        
    }    
    
    public void test_Deviceid() throws UtilityException, MessageAttributeParsingException, MessageHeaderParsingException{

        
        for(int i=0;i<1000;i++){
            
            Deviceid packet_out = new Deviceid();
            packet_out.setPassword("dsdfsdfsdf"+String.valueOf(i));
            
            MessageHeader message = new MessageHeader(MessageHeaderType.Empty);
            message.addMessageAttribute(packet_out);
            
            MessageHeader readmessage = MessageHeader.parseHeader(message.getBytes());
            readmessage.parseAttributes(message.getBytes());
            Deviceid packet_in = (Deviceid) readmessage.getMessageAttribute(MessageAttributeType.Deviceid);        
            
            assertEquals("name",packet_in.getPassword(),packet_out.getPassword());
            
        }
        
    } 
    
    public void test_PublicAddress() throws UtilityException, MessageHeaderParsingException, MessageAttributeException{

        
        for(int i=1;i<100;i++){
            
            PublicAddress packet_out = new PublicAddress();
            packet_out.setAddress(new Address(10,111,222,i));
            packet_out.setPort(i);
            
            MessageHeader message = new MessageHeader(MessageHeaderType.Empty);
            message.addMessageAttribute(packet_out);
            
            MessageHeader readmessage = MessageHeader.parseHeader(message.getBytes());
            readmessage.parseAttributes(message.getBytes());
            PublicAddress packet_in = (PublicAddress) readmessage.getMessageAttribute(MessageAttributeType.PublicAddress);        
            
            assertEquals("name",packet_in.getAddress().toString(),packet_out.getAddress().toString());
        
        }
        
    }     

}
