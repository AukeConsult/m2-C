package no.auke.util;

import junit.framework.TestCase;

public class BitTest extends TestCase {
	
	
	public void testBit(){
		
		byte[] bitmap = new byte[4];		
		for(int i=0;i<32;i++){			
			bitmap = ByteUtil.setBit(bitmap, i);			
			assertTrue(ByteUtil.isBit(bitmap, i));
		}		
	}
}
