package no.auke.p2p.m2.systemtests;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
//import org.junit.After;
import org.junit.Test;

//@RunWith(PowerMockRunner.class)

public class Concurrent_Test  {

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
	public void test_socket_one_package() {

		if(!isReadyToTest) return;

		System.out.println("test_socket_one_package ->>>>>>>>>>>>>>>>>>>>>> ");
		
		List<ITestSend> list = new ArrayList<ITestSend>();
		list.add(helper.startBuiltInSendTest("socket 2000", 2000, 1, 1000));
		helper.waitUntilFinishAndCheck(list);
	
	}
	
	@Test
	public void test_OneSockets_200_Package() {

		if(!isReadyToTest) return;

		System.out.println("test_OneSockets_200Package ->>>>>>>>>>>>>>>>>>>>>> ");

		List<ITestSend> list = new ArrayList<ITestSend>();
		list.add(helper.startBuiltInSendTest("socket 2000", 2000, 200, 10000));
		helper.waitUntilFinishAndCheck(list);

	}
		 
	@Test
	public void test_two_sockets_100_small_packages() {

		if (isReadyToTest) {
			
			System.out.println("test_two_sockets_10packages ->>>>>>>>>>>>>>>>>>>>>> ");
			
			List<ITestSend> list = new ArrayList<ITestSend>();
			list.add(helper.startBuiltInSendTest("socket 2000", 2000, 100, 1000));
			list.add(helper.startBuiltInSendTest("socket 2001", 2001, 100, 1000));
			
			helper.waitUntilFinishAndCheck(list);
		}
	}

	@Test
	public void test_twenty_sockets_random_packages_10_to_200() {

		if (isReadyToTest) {

			System.out.println("test_twenty_sockets_random_packages_10_to_60 ->>>>>>>>>>>>>>>>>>>>>> ");
			
			Random rnd = new Random();
			List<ITestSend> list = new ArrayList<ITestSend>();
			for (int i = 1; i <= 20; i++) {
				list.add(helper.startBuiltInSendTest("socket "
						+ String.valueOf(3000 + i), 3000 + i,
						rnd.nextInt(200) + 10, rnd.nextInt(200000) + 10000));
							
			}			
			helper.waitUntilFinishAndCheck(list);

		}
	
	}
	
	
	@Test
	public void test_twenty_sockets_random_packages_10_to_60_10_times() {

		if(!isReadyToTest) return;
		
		for(int x=0;x<10;x++){
			
			
			System.out.println(String.valueOf(x) + " test_twenty_sockets_random_packages_10_to_60 ");
			
			Random rnd = new Random();

			List<ITestSend> list = new ArrayList<ITestSend>();
			for (int i = 1; i <= 20; i++) {
				list.add(helper.startBuiltInSendTest("socket "
						+ String.valueOf(3000 + i), 3000 + i,
						rnd.nextInt(50) + 10, rnd.nextInt(200000) + 10000));
							
			}
			helper.waitUntilFinishAndCheck(list);
		
		}		
		
	}

	
}
