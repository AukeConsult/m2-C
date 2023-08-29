package no.auke.p2p.m2.keepalive;

import java.io.IOException;

import junit.framework.TestCase;
import no.auke.p2p.m2.workers.keepalive.KeepAliveMap;
import no.auke.p2p.m2.workers.keepalive.NetAddress;
import no.auke.util.StringConv;

public class MappingTest extends TestCase {

    protected void setUp() {
    }

    public void testCode() throws IOException {

        // check that user mapping work, random user id get a mapping entry

        String user = "";
        KeepAliveMap map = new KeepAliveMap(System.getProperty("user.dir"));
        map.read();

        for (NetAddress p : map.getAddresses()) {
        	System.out.println(p.getAddress());
        }
        
        while (true) {
        	
            byte[] b = new byte[1];
            b[0] = (byte) ((Math.random() * 80) + 40);
            user += StringConv.UTF8(b);

            // TODO: Make this test usefull and workable
            //assertTrue("userid: " + user + " did not get mapping",ok);

            if (user.length() > 20) {
            	
            	break;
            }
            
        }

    }
}