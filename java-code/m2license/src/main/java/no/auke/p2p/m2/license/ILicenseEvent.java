package no.auke.p2p.m2.license;


public interface ILicenseEvent {
	
	void onRequestNewKey();
	void onGotNewKey(ClientAccessKey key);
	void onRequestLicense();
	void onGotLicense(ClientLicense license);
	void onLicenseError(int code);
	void onKeyError(int code);
	void onOkLicence(ClientLicense license);
    void onOkKey(ClientAccessKey key);

}
