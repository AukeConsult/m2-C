package no.auke.p2p.m2.test.client;

import static org.powermock.api.mockito.PowerMockito.mock;
import junit.framework.TestCase;
import no.auke.http.ClientSessionNoJetty;
import no.auke.p2p.m2.license.LicenseServerMap;
import no.auke.util.ByteUtil;
import no.auke.util.StringConv;

import org.junit.runner.RunWith;
import org.powermock.api.mockito.*;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

//import static org.mockito.Matchers.any;
//import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;


@RunWith(PowerMockRunner.class)
@PrepareForTest({LicenseServerMapTest.class, LicenseServerMap.class})
public class LicenseServerMapTest extends TestCase {

	LicenseServerMap server = spy(new LicenseServerMap("license2.auke.no;license3.auke.no"));
	LicenseServerMap client = spy(new LicenseServerMap("license1.auke.no"));
	
	public void setUp()
	{
			
		//simulate returned result from server
		try {
			ClientSessionNoJetty agent = mock(ClientSessionNoJetty.class);
			PowerMockito.whenNew(ClientSessionNoJetty.class).withNoArguments().thenReturn(agent);
			PowerMockito.doReturn(fromServer()).when(agent, "postAndGet", anyString(), new byte[]{anyByte()});
			Whitebox.setInternalState(client, "agent", agent);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
				 
	}
	
	private byte[] fromServer()
	{
		byte[] content =null;
		for(String s : server.getServers())
		{
			if(content==null) content = ByteUtil.mergeDynamicBytesWithLength(StringConv.getBytes(s));
			else content = ByteUtil.mergeBytes(content, ByteUtil.mergeDynamicBytesWithLength(StringConv.getBytes(s)));
		}
		return content;
	}
	
	public void testPing() throws Exception
	{
		
		//client has no server
		assertTrue("client should have 1 server", client.getServers().size() == 1);
		//send ping
		client.ping("license1.auke.no");
		//client should have map
		assertTrue("client should have 3 servers", client.getServers().size() == 3);
		
		
	}
	
	public void testSetBootStrap()
	{
		server.setBootstrap("license1.auke.no;license2.auke.no;license3.auke.no;license4.auke.no;license5.auke.no");
		assertTrue("should have 5 servers in map", server.getServers().size() == 5);
	}
	
	
	
	
	
}
