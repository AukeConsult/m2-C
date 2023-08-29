//package no.auke.p2p.m2.test.client;
//
//
//import static org.mockito.Matchers.any;
//import static org.mockito.Matchers.anyString;
//import static org.powermock.api.mockito.PowerMockito.doReturn;
//import static org.powermock.api.mockito.PowerMockito.mock;
//import static org.powermock.api.mockito.PowerMockito.spy;
//import static org.powermock.api.mockito.PowerMockito.when;
//
//import java.io.File;
//import java.io.IOException;
//import java.lang.reflect.Constructor;
//
//import junit.framework.TestCase;
//import no.auke.http.ClientSessionNoJetty;
//import no.auke.http.ResponseFunc;
//import no.auke.p2p.m2.license.ClientAccessKey;
//import no.auke.p2p.m2.license.ClientLicense;
//import no.auke.p2p.m2.license.LicenseClientService;
//import no.auke.p2p.m2.license.LicenseCode;
//import no.auke.util.FileUtil;
//
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.junit.runner.RunWith;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//
//@RunWith(PowerMockRunner.class)
//@PrepareForTest( { System.class, LicenseClientServiceTest.class, LicenseClientService.class})
//public class LicenseClientServiceTest  extends TestCase {
//	
//	LicenseClientService client;
//	String userDir = "./test";
//	
//	public void setUp()
//	{
//		PowerMockito.suppress(PowerMockito.method(LicenseClientService.class, "init"));
//		client = spy(new LicenseClientService("", "", "", "",userDir, null));
//		
//	}
//	
//	public void tearDown()
//	{
//		try {
//			FileUtil.deleteDir(new File(userDir));
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	void simulateResponseFunc(int expectedReturnCode, byte[] expectedReturnContent )
//	{
//		try {
//			//see how i mock private method call directly with the use of PowerMockito
//			PowerMockito.doReturn(new ResponseFunc(expectedReturnCode, expectedReturnContent)).when(client, "get", anyString(), (byte[])any());
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			assertFalse(true);
//		}
//	}
	
//	public void testLocalCheckLicense()
//	{
//		long current = System.currentTimeMillis();
//		ClientLicense cL = mock(ClientLicense.class);
//		when(cL.getRemainingTime()).thenReturn((long) 0, (long) -1);
//		when(cL.getAppId()).thenReturn("");
//		when(cL.getNamespace()).thenReturn("");
//		when(cL.getActivateTimeOnServer()).thenReturn(current);
//		when(cL.getActivateTimeOnClient()).thenReturn(current);
//		//expired
//		int code = client.localCheckLicense(cL);
//		assertEquals("license should be expired", LicenseCode.EXPIRED, code);
//		//should return ok
//		when(cL.getRemainingTime()).thenReturn((long) 1000);
//		code = client.localCheckLicense(cL);
//		assertEquals("Should be OK", LicenseCode.OK, code);
//		//should return invalid if appID wrong
//		when(cL.getAppId()).thenReturn("someapp");
//		code = client.localCheckLicense(cL);
//		assertEquals("license should be invalid", LicenseCode.INVALID_APP, code);
//		//assume user changes his PC clock to the past (2 days ago)
//		PowerMockito.mockStatic(System.class);
//		when(cL.getAppId()).thenReturn("");
//		when(System.currentTimeMillis()).thenReturn((long) (current - 2*24*60*60*1000));
//		code = client.localCheckLicense(cL);
//		assertEquals("time should be invalid", LicenseCode.LOCALTIME_INVALID, code);
//	}
	
//	public void testLocalCheckKey()
//	{
//		long current = System.currentTimeMillis();
//		ClientAccessKey key = mock(ClientAccessKey.class);
//		when(key.getRemainingTime()).thenReturn((long) 0, (long) -1);
//		when(key.getStartTimeOnServer()).thenReturn(current);
//		when(key.getStartTimeOnClient()).thenReturn(current);
//		//expired
//		int code = client.localCheckAccessKey(key);
//		assertEquals("license should be expired", LicenseCode.EXPIRED, code);
//		//should return ok
//		when(key.getRemainingTime()).thenReturn((long) 1000);
//		code = client.localCheckAccessKey(key);
//		assertEquals("Should be OK", LicenseCode.OK, code);
//		//assume user changes his PC clock to the past (2 days ago)
//		PowerMockito.mockStatic(System.class);
//		when(System.currentTimeMillis()).thenReturn((long) (current - 2*24*60*60*1000));
//		code = client.localCheckAccessKey(key);
//		assertEquals("time should be invalid", LicenseCode.LOCALTIME_INVALID, code);
//	}
	
//	public void testRequestClientLicense()
//	{
//		
//		simulateResponseFunc(LicenseCode.OK, new ClientLicense().getBytes());
//		ClientLicense cl= client.requestClientLicense("someID", "appID",  null);
//		assertNotNull(cl);
//		
//		simulateResponseFunc(LicenseCode.EXPIRED, new ClientLicense().getBytes());
//		cl= client.requestClientLicense("someID", "appID", null);
//		assertEquals("Expired", true, cl.getVerifiedCode() == LicenseCode.EXPIRED);
//		
////		simulateResponseFunc(LicenseCode.OK, new ClientLicense().getBytes());
////		cl= client.requestClientLicense("company", "account", "password", "appID", null);
////		assertNotNull(cl);
//		
//	}
	
//	public void testRequestAccessKey()
//	{
//		simulateResponseFunc(LicenseCode.OK, new ClientAccessKey().getBytes());
//		//doReturn(new ClientAccessKey()).when(client).readAccesskey((byte[])any());
//		//ClientAccessKey key= client.requestAccessKey("someID", "appID", "namespace", "keyId", null);
//        ClientAccessKey key= client.requestAccessKey(123, "keyId", null);
//		assertNotNull(key);
//		
//		simulateResponseFunc(LicenseCode.EXPIRED, new ClientAccessKey().getBytes());
//		// key= client.requestAccessKey("someID", "appID", "namespace", "keyId", null);
//        key= client.requestAccessKey(123, "keyId", null);
//		assertEquals("Expired", true, key.getVerifiedCode() == LicenseCode.EXPIRED);
//	}
	
	
	

//}
