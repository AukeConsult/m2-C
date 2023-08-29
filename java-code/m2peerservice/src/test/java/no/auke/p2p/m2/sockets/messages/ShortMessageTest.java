package no.auke.p2p.m2.sockets.messages;

import static org.junit.Assert.*;

import no.auke.p2p.m2.sockets.messages.MsgShort;

import org.junit.Test;

public class ShortMessageTest {

	@Test
	public void test_new_message() {
		
		MsgShort message = new MsgShort("from","to",new byte[0]);
		
		assertEquals("to",message.getTo());
		assertEquals("from",message.getFrom());
		assertEquals(true,message.isMessage());
		
	}
	
	@Test
	public void test_toByte_fromByte() {
		
		String data = "asasd.asd.asd.as.dasdasduausduasuuweuqwueqweuquweqwe";
		
		MsgShort message = new MsgShort("from","to",data.getBytes());
		
		MsgShort message2 = new MsgShort(message.getBytes());
		
		assertEquals(message.getTo(),message2.getTo());
		assertEquals(message.getFrom(),message2.getFrom());
		assertEquals(message.getMessageId(),message2.getMessageId());
		assertEquals(message.getMessage(),message2.getMessage());
		assertEquals(true,message2.isMessage());
		
	}
	
	@Test
	public void test_toReply() {
		
		String data = "asasd.asd.asd.as.dasdasduausduasuuweuqwueqweuquweqwe";
		
		MsgShort message = new MsgShort("from","to",data.getBytes());
		
		MsgShort message2 = new MsgShort(message.toReply());
		
		assertEquals(message.getTo(),message2.getFrom());
		assertEquals(message.getFrom(),message2.getTo());
		assertEquals(message.getMessageId(),message2.getReplyId());
		assertEquals(false,message2.isMessage());
		
	}	

}
