package no.auke.p2p.m2.sockets.messages;

import java.util.Arrays;
import java.util.UUID;

import no.auke.p2p.m2.sockets.messages.MsgSendFunction;
import junit.framework.TestCase;

public class MsgSendFunctionTest extends TestCase {

    
    public void testFunctionMessage(){
        
        MsgSendFunction message = new MsgSendFunction("leif",100,"per","test", "", UUID.randomUUID().toString(), true, "datasdfsdfsdf".getBytes());
        
        MsgSendFunction message2 = new MsgSendFunction(message.toBytes());
        
        assertEquals("name",message.getFromClientId(),message2.getFromClientId());
        assertEquals("port",message.getFromPort(),message2.getFromPort());        
        assertEquals("to",message.getToClientId(),message2.getToClientId());
        assertEquals("msgid",message.getMsgId(),message2.getMsgId());
        assertEquals("sessionid",message.getSessionId(),message2.getSessionId());
        assertEquals("function",message.getFunction(),message2.getFunction());
        assertEquals("async",message.isAsync(),message2.isAsync());
        assertTrue("data",Arrays.equals(message.getData(),message2.getData()));

    }

}
