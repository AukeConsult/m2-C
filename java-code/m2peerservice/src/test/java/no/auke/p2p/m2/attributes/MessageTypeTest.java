package no.auke.p2p.m2.attributes;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import no.auke.p2p.m2.message.attribute.DataPacket;
import no.auke.p2p.m2.message.attribute.MapFile;
import no.auke.p2p.m2.message.attribute.MessageAttributeParsingException;
import no.auke.p2p.m2.message.attribute.PeerLocalId;
import no.auke.p2p.m2.message.attribute.MessageAttributeInterface.MessageAttributeType;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.message.header.MessageHeaderParsingException;
import no.auke.p2p.m2.message.header.MessageHeaderInterface.MessageHeaderType;
import no.auke.p2p.m2.InitVar;
import no.auke.util.ByteUtil;

public class MessageTypeTest extends TestCase {

	public void test_Map_request() {

        // TODO: messageTypeTest must test more message and attribute types
		// must test all message types and attributes in the system
		// test message types

		for (int i = 1; i < 100; i++) {
		    
			Long value = 3242342399L * i;
			MessageHeader message = new MessageHeader(MessageHeaderType.MapRequest);

			MapFile map = new MapFile();
			map.setVersion(value);
			map.setFiletext(String.valueOf(value) + "abcdefgh:" + String.valueOf(i));

			PeerLocalId user = new PeerLocalId();
			user.setUsername("Leif:" + String.valueOf(value) + "abcdefgh:" + String.valueOf(i));

			message.addMessageAttribute(map);
			message.addMessageAttribute(user);

			byte[] packetdata = message.getBytes();
			try {

				MessageHeader readmessage = MessageHeader.parseHeader(packetdata);
				readmessage.parseAttributes(packetdata);

				MapFile readmap = (MapFile) readmessage.getMessageAttribute(MessageAttributeType.MapFile);
				PeerLocalId readuser = (PeerLocalId) readmessage.getMessageAttribute(MessageAttributeType.Peer_Local_Id);

				assertTrue("map version",map.getVersion() == readmap.getVersion());
				assertTrue("map text",map.getFiletext().equals(readmap.getFiletext()));
				assertTrue("user attribute : ",user.getUsername().equals(readuser.getUsername()));

			} catch (MessageHeaderParsingException e) {
				fail(e.getMessage());
			} catch (MessageAttributeParsingException e) {
				fail(e.getMessage());
			}
		}
	}

    public byte[] getdata(int length)
    {
        byte[] data = new byte[length];
        
        int x=0;
        for(int i=0;i<length;i++){          
            data[i] = (byte)x;
            x++;
            if(x>255){
                x=0;
            }           
        }           
        return data;
    }	
	
    public void test_Data_message() {

        // TODO: messageTypeTest must test more message and attribute types
        // must test all message types and attributes in the system
        // test message types
        
        for (int i = 1; i < 100; i++) {
            
            byte[] data = getdata(i*8832);

            List<byte[]> splits = ByteUtil.splitBytesWithFixedLength(data,InitVar.PACKET_SIZE-42);

            for (int index = 0; index < splits.size(); index++) {
                
                DataPacket packet_out = new DataPacket();
                packet_out.setPort(10);
                packet_out.setNumber(index + 1);
                packet_out.setTotal(splits.size());
                packet_out.setData(splits.get(index));
                packet_out.setChunkNumber(100);
                packet_out.setChunkVersion(100);
                
                
                try {

                    MessageHeader message = new MessageHeader(MessageHeaderType.Data);
                    
                    message.setTransactionID(0);
                    message.addMessageAttribute(packet_out);
                    
                    byte[] packetdata = message.getBytes();
                    
                    MessageHeader readmessage = MessageHeader.parseHeader(packetdata);
                    readmessage.parseAttributes(packetdata);
                    DataPacket packet_in = (DataPacket) readmessage.getMessageAttribute(MessageAttributeType.DataPacket);

                    assertEquals("packetnum",packet_in.getNumber(),packet_out.getNumber());
                    assertEquals("total",packet_in.getTotal(),packet_out.getTotal());
                    assertEquals("port",packet_in.getPort(),packet_out.getPort());
                    assertEquals("chunknum",packet_in.getChunkNumber(),packet_out.getChunkNumber());
                    assertEquals("chunkversion",packet_in.getChunkVersion(),packet_out.getChunkVersion());
                    assertEquals("chunkId",packet_in.getChunkId(),packet_out.getChunkId());
                    assertTrue("data",Arrays.equals(packet_in.getData(),packet_out.getData()));
                    
                    

                } catch (MessageHeaderParsingException e) {
                    fail(e.getMessage());
                } catch (MessageAttributeParsingException e) {
                    fail(e.getMessage());
                }
                
            }            

        }
    }	
}