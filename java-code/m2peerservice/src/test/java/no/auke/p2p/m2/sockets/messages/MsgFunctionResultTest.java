package no.auke.p2p.m2.sockets.messages;

import java.util.Arrays;
import java.util.UUID;

import no.auke.p2p.m2.sockets.messages.MsgFunctionResult;
import junit.framework.TestCase;

public class MsgFunctionResultTest extends TestCase {
	
    public void testResultMessage(){
        
        MsgFunctionResult message = new MsgFunctionResult("leif", "", UUID.randomUUID().toString(), "datasdfsdfsdf".getBytes());
        MsgFunctionResult message2 = new MsgFunctionResult(message.toBytes());
        assertEquals("from",message.getFrom(),message2.getFrom());              
        assertEquals("msgid",message.getMsgId(),message2.getMsgId());              
        assertEquals("sessionid",message.getSessionId(),message2.getSessionId());   
        assertTrue("data",Arrays.equals(message.getData(),message2.getData()));

    }
}
