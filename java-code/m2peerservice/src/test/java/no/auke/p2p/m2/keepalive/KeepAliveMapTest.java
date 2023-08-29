package no.auke.p2p.m2.keepalive;

import java.util.ArrayList;
import java.util.Arrays;

import no.auke.p2p.m2.workers.keepalive.KeepAliveAddressEntry;
import no.auke.p2p.m2.workers.keepalive.KeepAliveMap;
import junit.framework.TestCase;

public class KeepAliveMapTest extends TestCase {
	
	private static String userDir = System.getProperty("user.dir");
	
	public void test_add_default(){
		
		String address = "address1:1,address2:1,address3:1,address4:1";
		KeepAliveMap map = new KeepAliveMap(userDir,address);
		ArrayList<KeepAliveAddressEntry> list = map.getKeepAliveList();
		assertEquals(list.size(), 6);
		assertEquals(map.getVersion(), 1000);
		for(KeepAliveAddressEntry entry:list){
			String[] line = entry.toString().split("\\s*[#]\\s*");
			assertTrue(line[3].equals(address));
		}		
	}
	
	public void test_key_version(){
		
		KeepAliveMap map = new KeepAliveMap(userDir);
		assertEquals(map.getVersion(), 0);
		assertNotNull(map.getPublickey());
		assertTrue(map.getPublickey().getGuid().toString().equals("baa64c1e-90f8-4bcb-a34c-b861233f168d"));
		
	}	
	
	public void test_bytes_KeepAliveAddressEntry(){
		
		String address = "127.0.0.255:9000,127.100.0.1:9001,10.100.0.255:9002,100.129.255.255:9003";
		
		KeepAliveMap map = new KeepAliveMap(userDir,address);
		
		ArrayList<KeepAliveAddressEntry> list = map.getKeepAliveList();
		assertEquals(list.size(), 6);
		assertEquals(map.getVersion(), 1000);
		
		for(KeepAliveAddressEntry entry:list){
			byte[] entry_arr = entry.getBytes();
			KeepAliveAddressEntry entry_new = new KeepAliveAddressEntry(entry_arr);
			assertEquals(entry.getVersion(), entry.getVersion());
			assertTrue(Arrays.equals(entry.getMask(), entry_new.getMask()));
			String[] line = entry_new.toString().split("\\s*[#]\\s*");
			assertTrue(line[3].equals(address));
		}		
	
	}
			

}
