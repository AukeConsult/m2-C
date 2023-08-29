package no.auke.util;

import java.util.List;

import junit.framework.TestCase;
import no.auke.util.ByteUtil;
import no.auke.util.StringConv;

public class ByteUtilTest extends TestCase {

	public void testCorrectTypes() {

        assertEquals("short", ByteUtil.getShort(ByteUtil.getBytes(Short.MAX_VALUE)),Short.MAX_VALUE);		
        assertEquals("Int", ByteUtil.getInt(ByteUtil.getBytes(Integer.MAX_VALUE)),Integer.MAX_VALUE);		
        assertEquals("long", ByteUtil.getLong(ByteUtil.getBytes(Long.MAX_VALUE-1)),Long.MAX_VALUE-1);		
		
	}
	
    public void testCorrectPackAndUnpackData() {
        
    	String test1 = "test test test yes yes yes";
        int test2 = 123456;
        int test3 = 35;
        boolean test4 = true;
        long test5 = 23423423L;
        String test6 = "test test test yes yes yes | test test test yes yes yes | test test test yes yes yes";

        byte[] test1_arr = StringConv.getBytes(test1);
        byte[] test2_arr = ByteUtil.getBytes(test2, 4);
        byte[] test3_arr = ByteUtil.getBytes(test3, 1);
        byte[] test4_arr = ByteUtil.getBytes(test4 ? 1 : 0, 1);
        byte[] test5_arr = ByteUtil.getBytes(test5, 8);
        byte[] test6_arr = StringConv.getBytes(test6);

        //compare
        assertEquals(true, ByteUtil.getInt(test2_arr) == test2);
        assertEquals(true, ByteUtil.getInt(test3_arr) == test3);
        assertEquals(true, ByteUtil.getInt(test4_arr) == (test4 ? 1 : 0));
        assertEquals(true, ByteUtil.getInt(test5_arr) == test5);

        //compare
        byte[] total = ByteUtil.mergeBytes(test1_arr, test2_arr, test3_arr, test4_arr, test5_arr, test6_arr);
        List<byte[]> subs = ByteUtil.splitBytes(total, test1.length(), 4, 1, 1, 8);
        byte[] test1_arr2 = subs.get(0);
        byte[] test2_arr2 = subs.get(1);
        byte[] test3_arr2 = subs.get(2);
        byte[] test4_arr2 = subs.get(3);
        byte[] test5_arr2 = subs.get(4);
        byte[] test6_arr2 = subs.get(5);
        
        //compare
        assertEquals(true, StringConv.UTF8(test1_arr2).equals(test1));
        assertEquals(true, ByteUtil.getInt(test2_arr2) == test2);
        assertEquals(true, ByteUtil.getInt(test3_arr2) == test3);
        assertEquals(true, ByteUtil.getInt(test4_arr2) == (test4 ? 1 : 0));
        assertEquals(true, ByteUtil.getLong(test5_arr2) == test5);
        assertEquals(true, StringConv.UTF8(test6_arr2).equals(test6));

    }
    
    public void testCorrectPackAndUnpackData2()
    {
    	String test1 = "test test test";
        int test2 = 123456;
        int test3 = 35;
        String test5 = "my test";
        byte[] test1_arr = StringConv.getBytes(test1);
        byte[] test2_arr = ByteUtil.getBytes(test2, 4);
        byte[] test3_arr = ByteUtil.getBytes(test3, 1);
        byte[] test4_arr = new byte[0];
        byte[] test5_arr = StringConv.getBytes(test5);
        byte[] test6_arr = null;
        
        byte[] total = ByteUtil.mergeDynamicBytesWithLength(test1_arr, test2_arr, test3_arr, test4_arr, test5_arr, test6_arr);
        
        List<byte[]> subs = ByteUtil.splitDynamicBytes(total);
        byte[] test1_arr2 = subs.get(0);
        byte[] test2_arr2 = subs.get(1);
        byte[] test3_arr2 = subs.get(2);
        byte[] test4_arr2 = subs.get(3);
        byte[] test5_arr2 = subs.get(4);
        byte[] test6_arr2 = subs.get(5);
        
        assertEquals(true, StringConv.UTF8(test1_arr2).equals(test1));
        assertEquals(true, ByteUtil.getInt(test2_arr2) == test2);
        assertEquals(true, ByteUtil.getInt(test3_arr2) == test3);
        assertEquals(true, test4_arr2.length == 0);
        assertEquals(true, StringConv.UTF8(test5_arr2).equals(test5));
        assertEquals(true, test6_arr2.length==0);
        
    }
    
    public void testShort()
    {
    	short x =2321;
    	byte[] xx = ByteUtil.getBytes(x,2);
    	short z = ByteUtil.getShort(new byte[]{xx[0],xx[1]});
    	
    	
    	
    	assertEquals(z, x);
    	
    			
    }

}
