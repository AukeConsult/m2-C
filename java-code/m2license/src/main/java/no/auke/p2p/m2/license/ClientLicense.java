package no.auke.p2p.m2.license;

import java.util.List;

import no.auke.util.ByteUtil;
import no.auke.util.StringConv;

public class ClientLicense {
	
	// private static final Logger logger = LoggerFactory.getLogger(ClientLicense.class);

	
	private int licenseId;
    private String licenseKey="";
	private byte[] publicKey;
	private String appId = "";
	private String namespace = "";
	private String type = "";
	private long expiry;
	private long activateTimeOnServer;
	private long remainingTimeFromServer;
	private String licenseServerMap = "";
	private long activateTimeOnClient;
	private int verifiedCode;
	private int numberOfAccessKeys=0;
	private long checkFrequency = 0;
	private boolean isOpenLicense = false;
	
	public ClientLicense()
	{
		
	}
	
	public ClientLicense(byte[] data)
	{
		List<byte[]> parts = ByteUtil.splitDynamicBytes(data);
		licenseKey = StringConv.UTF8(parts.get(0));
		publicKey = parts.get(1);
		appId = StringConv.UTF8(parts.get(2));
		namespace = StringConv.UTF8(parts.get(3));
		type = StringConv.UTF8(parts.get(4));
		expiry = ByteUtil.getLong(parts.get(5));
		activateTimeOnServer = ByteUtil.getLong(parts.get(6));
		remainingTimeFromServer = ByteUtil.getLong(parts.get(7));
		licenseServerMap = StringConv.UTF8(parts.get(8));
		activateTimeOnClient = ByteUtil.getLong(parts.get(9));
		setLicenseId(ByteUtil.getInt(parts.get(10)));
		//new check added
		if(parts.size() > 11)
		{
			setNumberOfAccessKeys(ByteUtil.getInt(parts.get(11)));
		}
		if(parts.size() > 12)
		{
			setCheckFrequency(ByteUtil.getLong(parts.get(12)));
		}
		if(parts.size() > 13)
		{
			setOpenLicense(ByteUtil.getInt(parts.get(13))==1);
		}
	}
	
	public byte[] getBytes()
	{
		return ByteUtil.mergeDynamicBytesWithLength(
				StringConv.getBytes(licenseKey!=null?licenseKey:""),
				publicKey,
				StringConv.getBytes(appId!=null?appId:""),
				StringConv.getBytes(namespace!=null?namespace:""),
				StringConv.getBytes(type),
				ByteUtil.getBytes(expiry),
				ByteUtil.getBytes(activateTimeOnServer),
				ByteUtil.getBytes(remainingTimeFromServer),
				StringConv.getBytes(licenseServerMap),
				ByteUtil.getBytes(activateTimeOnClient),
				ByteUtil.getBytes(licenseId),
				ByteUtil.getBytes(numberOfAccessKeys),
				ByteUtil.getBytes(checkFrequency),
				ByteUtil.getBytes(isOpenLicense?1:0)
				);
				
	}
	
	
	
	
	/**
	 * @return the licenseId
	 */
	public String getLicenseKey() {
		return licenseKey;
	}
	/**
	 * @param licenseId the licenseId to set
	 */
	public void setLicenseKey(String licenseId) {
		this.licenseKey = licenseId;
	}
	/**
	 * @return the publicKey
	 */
	public byte[] getPublicKey() {
		return publicKey;
	}
	/**
	 * @param publicKey the publicKey to set
	 */
	public void setunLockKey(byte[] publicKey) {
		this.publicKey = publicKey;
	}
	/**
	 * @return the appId
	 */
	public String getAppId() {
		return appId==null?"":appId;
	}
	/**
	 * @param appId the appId to set
	 */
	public void setAppId(String appId) {
		this.appId = appId;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the expiry
	 */
	public long getExpiry() {
		return expiry;
	}
	/**
	 * @param expiry the expiry to set
	 */
	public void setExpiry(long expiry) {
		this.expiry = expiry;
	}
	/**
	 * @return the activateTime
	 */
	public long getActivateTimeOnServer() {
		return activateTimeOnServer;
	}
	/**
	 * @param activateTime the activateTime to set
	 */
	public void setActivateTimeOnServer(long activateTime) {
		this.activateTimeOnServer = activateTime;
	}
	/**
	 * @return the remainingTime
	 */
	public long getRemainingTime() {
		return remainingTimeFromServer;
	}
	/**
	 * @param remainingTime the remainingTime to set
	 */
	public void setRemainingTime(long remainingTime) {
		this.remainingTimeFromServer = remainingTime;
	}
	/**
	 * @return the licenseServerMap
	 */
	public String getLicenseServerMap() {
		return licenseServerMap;
	}
	/**
	 * @param licenseServerMap the licenseServerMap to set
	 */
	public void setLicenseServerMap(String licenseServerMap) {
		this.licenseServerMap = licenseServerMap;
	}
	/**
	 * @return the namespace
	 */
	public String getNamespace() {
		return namespace==null?"":namespace;
	}
	/**
	 * @param namespace the namespace to set
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	/**
	 * @return the activateTimeOnClient
	 */
	public long getActivateTimeOnClient() {
		return activateTimeOnClient;
	}
	/**
	 * @param activateTimeOnClient the activateTimeOnClient to set
	 */
	public void setActivateTimeOnClient(long activateTimeOnClient) {
		this.activateTimeOnClient = activateTimeOnClient;
	}
	public int getVerifiedCode() {
		return verifiedCode;
	}
	public void setVerifiedCode(int verifiedCode) {
		this.verifiedCode = verifiedCode;
	}

	public void setLicenseId(int licenseId) {
		this.licenseId = licenseId;
	}
	public int getLicenseId() {
		return licenseId;
	}

	public int getNumberOfAccessKeys() {
		return numberOfAccessKeys;
	}

	public void setNumberOfAccessKeys(int numberOfAccessKeys) {
		this.numberOfAccessKeys = numberOfAccessKeys;
	}

	public long getCheckFrequency() {
		return checkFrequency;
	}

	public void setCheckFrequency(long checkFrequency) {
		this.checkFrequency = checkFrequency;
	}

	public boolean isOpenLicense() {
		return isOpenLicense;
	}

	public void setOpenLicense(boolean isOpenLicense) {
		this.isOpenLicense = isOpenLicense;
	}

	
	
	
	
}
