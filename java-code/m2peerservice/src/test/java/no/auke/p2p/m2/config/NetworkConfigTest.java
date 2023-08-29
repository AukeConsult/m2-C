package no.auke.p2p.m2.config;

import java.util.List;

import junit.framework.TestCase;
import no.auke.util.ListNetworks;

import org.junit.Test;

public class NetworkConfigTest extends TestCase {

    @Test
    public void testIPLookup() {

        String testCategory = System.getProperty("TEST_CATEGORY");

        if (testCategory != null && testCategory.equalsIgnoreCase("SYSTEM")) {

            List<String> ips = ListNetworks.getIPv4Addresses();

            boolean ok = false;

            for (Object ip : ips) {
                System.out.println("IP: " + ip.toString());
                if (!ip.toString().equals("127.0.0.1")) {
                    ok = true;
                }

            }
            assertTrue("not get any IP list ", ips != null);
            assertTrue("return only local host ", ok);

        }
    }
}
