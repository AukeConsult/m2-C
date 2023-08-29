package no.auke.p2p.m2.systemtests;

//import org.junit.After;
import org.junit.Before;
import org.junit.Test;

//import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;

public class Concurrent_SendAsync_Test  {

	TestServiceHelper helper = new TestServiceHelper();
	boolean isReadyToTest= false;
	String root;
	
	@Before
	public void setUp() {
		isReadyToTest = helper.initNewTestClients();
	}
	
	@After
	public void tearDown() {
		helper.closeTestClients();
	}

	@Test
	public void test_socket_10_packages() {
		if(!isReadyToTest) return;
		System.out.println("test_socket_one_package ->>>>>>>>>>>>>>>>>>>>>> ");
		List<ITestSend> list = new ArrayList<ITestSend>();
		list.add(helper.startSendTestWithISendTask("socket 2000", 2000, 10, 10000));
		helper.waitUntilFinishAndCheck(list);
	}
	
}
