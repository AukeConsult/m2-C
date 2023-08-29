
package no.auke.p2p.m2.license;

public class LicenseCode
{
	public static final int OK = 100;
	public static final int INVALID_LICENSE = 99;
	public static final int EXPIRED = 98;
	public static final int KEY_REACH_LIMIT = 97;
	public static final int WRONG_LICENSE_ACCOUNT = 96;
	public static final int LICENSE_ACCOUNT_NOT_AVAILABLE = 95;
	public static final int ENCRYPTION_ERROR = 94;
	public static final int LOCALTIME_INVALID = 93;
	public static final int INVALID_APP = 92;
	public static final int CANT_CREATE_TRIAL_LICENSE = 91;
	public static final int SERVER_ERROR = 90;
	

	public static String getMsg(int code)
	{
		String msg = "UNKNOWN";
		switch(code)
		{
		case OK : 
			msg = "OK"; 
			break;
		case INVALID_LICENSE :  
			msg = "Appication specific license is required. The license is not valid"; 
			break;
		case INVALID_APP :  
			msg = "Your appication is not registered. Please request your own appId"; 
			break;
		case EXPIRED:
			msg = "Sorry, your license is now expired.";
			break;
		case KEY_REACH_LIMIT:
			msg = "Sorry, you can not request more keys for this license";
			break;
		case WRONG_LICENSE_ACCOUNT:
			msg = "Sorry, your account is not existing.";
			break;
		case LICENSE_ACCOUNT_NOT_AVAILABLE:
			msg = "There is no license suitable with your account or your all license(s) are now expired";
			break;
		case ENCRYPTION_ERROR:
			msg = "Faltal error: encryption";
			break;
		case CANT_CREATE_TRIAL_LICENSE:
			msg = "Faltal error: sorry, we cant create any trial license at the moment";
			break;
		case SERVER_ERROR:
			msg = "Faltal error: license server error";
			break;
			
		}
		return msg;
	}
}