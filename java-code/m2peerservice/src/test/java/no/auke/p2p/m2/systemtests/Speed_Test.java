package no.auke.p2p.m2.systemtests;

import java.util.ArrayList;
import java.util.List;
//import java.util.Random;

import org.junit.After;
import org.junit.Before;
//import org.junit.After;
import org.junit.Test;

//import static org.junit.Assert.*;
import no.auke.p2p.m2.InitVar;

//@RunWith(PowerMockRunner.class)


public class Speed_Test {

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
	

	@Test
	public void test_socket_one_package_slow() {

		if (isReadyToTest) {
					
			System.out.println("test_socket_one_package ->>>>>>>>>>>>>>>>>>>>>> ");
			int old_speed = InitVar.MAX_SPEED; 
			InitVar.MAX_SPEED = 20;
			List<ITestSend> list = new ArrayList<ITestSend>();
			list.add(helper.startBuiltInSendTest("socket 2000", 2000, 10, 1000));
			helper.waitUntilFinishAndCheck(list);
			
			InitVar.MAX_SPEED=old_speed;
		}
	
	}	
	
	@Test
	public void test_socket_different_speed() {

		if (isReadyToTest) {

			System.out.println("test_socket_different_speed ->>>>>>>>>>>>>>>>>>>>>> ");
			int old_speed = InitVar.TEST_SPEED_DELAY; 
			List<ITestSend> list = new ArrayList<ITestSend>();
			for(int speed=10;speed<1000;speed+=(speed) + 5) {

				System.out.println("speed " + speed);

				
				InitVar.TEST_SPEED_DELAY=speed;
				
				list.add(helper.startBuiltInSendTest("socket 2000", 2000, 5, 10*speed)) ;
		
			}
			
			helper.waitUntilFinishAndCheck(list);
			InitVar.TEST_SPEED_DELAY=old_speed;
		}
	
	}	


	
}
