package no.auke.m2.encryption;

import junit.framework.TestCase;

public class BaseTestCase extends TestCase {

	protected boolean runTesting=false;
	protected boolean runDev=false;
	protected boolean runTest=false;
	protected boolean runProd=false;
	
    protected void setUp(){
    	runTesting=true;
    }
    
    public void testDummy(){
    	
    	
    }

}
