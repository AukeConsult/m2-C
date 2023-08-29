package no.auke.p2p.m2.systemtests;

import org.junit.Before;
import org.junit.Test;

import no.auke.p2p.m2.SocketRetStatus;
import no.auke.p2p.m2.sockets.Transaction;
import no.auke.p2p.m2.sockets.ITransacationSession;
import no.auke.p2p.m2.sockets.TransactionSocket;
import static org.junit.Assert.*;

import org.junit.After;

public class SyncSocket_Integration_Test{

	TestServiceHelper helper = new TestServiceHelper();
	boolean isReadyToTest= false;
	
	
	@Before 
	public void setUp() {

		isReadyToTest = helper.initNewTestClients();

	}

	@After
	public void tearDown() {
		helper.closeTestClients();
	}
        
    class SyncWorkClass extends TransactionSocket {

        public String ERROR="ERROR";
        public String OK="OK";
        public String TESTOK="testok";
        public String RETUR="returndata";
        public String RETURDATA="1234567890";
        
        @Override
        public byte[] onFunctionMessage(String from, String function, byte[] data) {
            
            if(function.equals(TESTOK)){
                
                return OK.getBytes();
            
            } else if(function.equals(RETUR)){
                
                    byte[] retadd = RETURDATA.getBytes();
                    byte[] ret = new byte[data.length+retadd.length]; 
                    System.arraycopy(data, 0, ret, 0, data.length);        
                    System.arraycopy(retadd, 0, ret, data.length,retadd.length);  
                    
                    return ret;
                    
            } else {
                
                return ERROR.getBytes();
            }
        }

		@Override
		public Transaction onTransFunctionMessage(String fromClientId,String function, byte[] data) {
			return null;
		}

    }
    
    
    @Test
    public void test_Session() {
    	if(!isReadyToTest) return;
        SyncWorkClass syncA = new SyncWorkClass();
        SyncWorkClass syncB = new SyncWorkClass();
         
        try {
             
        	 syncA.open(helper.clientA,1000);
             syncB.open(helper.clientB,1000);
             
            
             
             ITransacationSession sessionA = syncA.openSession(syncB.getClientId(), syncB.getPort());
            
             SocketRetStatus a = sessionA.get(syncB.TESTOK, "some message".getBytes());
             SocketRetStatus aa = sessionA.get("somefunc", "some message".getBytes());
             
             if(a.getPeerSession().isConnected() || aa.getPeerSession().isConnected()) {

            	 byte[] resulta = a.getData();
            	 byte[] resulterra = aa.getData();
            	 if(!a.isOk()) {
            		 System.out.println("Error: " + a.getLastRetcode().toString() + "-" + a.getLastMessage());
            	 }            
            	 if(!aa.isOk()) {
            		 System.out.println("Error: " + aa.getLastRetcode().toString() + "-" + aa.getLastMessage());
            	 }
            	 assertNotNull("clientA null result",resulta);
            	 assertEquals("clientA",new String(resulta),syncA.OK);

            	 assertNotNull("clientA err null result",resulterra);
            	 assertEquals("clientA err result",new String(resulterra),syncA.ERROR);
             } else {
            	 System.out.println("Error : peer not connected " + a.getPeerSession().getPeerid().getUserid());
             }
             
             ITransacationSession sessionB = syncB.openSession(syncA.getClientId(), syncA.getPort());
             
             
             SocketRetStatus b = sessionB.get(syncA.TESTOK, "some message".getBytes());
             SocketRetStatus bb = sessionB.get("somefunc", "some message".getBytes());
             
             if(b.getPeerSession().isConnected() || bb.getPeerSession().isConnected()) {
            	 byte[] resultb = b.getData();
            	 byte[] resulterrb = bb.getData();

            	 if(!b.isOk()) {
            		 System.out.println("Error: " + b.getLastRetcode().toString() + "-" + b.getLastMessage());
            	 }            
            	 if(!bb.isOk()) {
            		 System.out.println("Error: " + bb.getLastRetcode().toString() + "-" + bb.getLastMessage());
            	 }
            	 assertNotNull("clientB null result",resultb);
            	 assertEquals("clientB result",new String(resultb),syncB.OK);  

            	 assertNotNull("clientB err null result",resulterrb);
            	 assertEquals("clientB err result",new String(resulterrb),syncB.ERROR);  
             } else {
            	 System.out.println("Error : peer not connected " + b.getPeerSession().getPeerid().getUserid());
             }
                    
             String message = "sdfsdf sd fs df sdf 3244444444444444444444444444444444444444444444444444444444444444444444444444444444444443344444444444444444444444444444";
             
             for(int x=0;x<100;x++) {
            	 
            	 long time = System.currentTimeMillis();
                 message += "sdfsdf rewrwerw  rwe we r wer w er werwer21312 31 23 12 31 23 123 34234 23423ddfsdfsdf sdfs sdsdvs ssd sd s dsd sdsdsds  4234234234234234234234234234" + String.valueOf(x);
                 message += "sdfsdf rewrwerw  rwe we r wer w er werwer21312 31 23 12 31 23 123 34234 23423ddfsdfsdf sdfs sdsdvs ssd sd s dsd sdsdsds  4234234234234234234234234234" + String.valueOf(x);
                 message += "sdfsdf rewrwerw  rwe we r wer w er werwer21312 31 23 12 31 23 123 34234 23423ddfsdfsdf sdfs sdsdvs ssd sd s dsd sdsdsds  4234234234234234234234234234" + String.valueOf(x);

                 SocketRetStatus retA = sessionA.get(syncB.RETUR, message.getBytes());
                 SocketRetStatus retB = sessionB.get(syncA.RETUR, message.getBytes());
                 byte[] resa = retA.getData();
                 byte[] resb = retB.getData();
                 
                 if(!retA.isOk()) {
                	 System.out.println("Error: " + retA.getLastRetcode().toString() + "-" + retA.getLastMessage());
                 }
                 
                 if(!retB.isOk()) {
                	 System.out.println("Error: " + retB.getLastRetcode().toString() + "-" + retB.getLastMessage());
                 }
                 assertNotNull("clientA null result",resa);
                 assertNotNull("clientB null result",resb);
                 
                 assertTrue(new String(resa).equals(message+syncB.RETURDATA));
                 assertTrue(new String(resb).equals(message+syncA.RETURDATA));
                 
                 System.out.println("send message " + String.valueOf(x) + " size " + String.valueOf(message.length()) + " time " + String.valueOf(System.currentTimeMillis() - time));
                 
             }
             
             syncA.close();
             syncB.close();
         
        } catch(Exception ex) {
        	 
        	ex.printStackTrace();
         	return;
        }
    	
    }
}

