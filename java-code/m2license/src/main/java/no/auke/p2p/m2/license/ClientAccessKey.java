package no.auke.p2p.m2.license;

import java.util.List;

import no.auke.util.ByteUtil;
import no.auke.util.StringConv;

public class ClientAccessKey {
	private String keyId="";
	private int licenseId =0;
	private long startTimeOnServer;
	private long expiry;
	private long remainingTime;
	//private String clientBoot="";
	private String clientParametter="";
	private long startTimeOnClient;
	private int verifiedCode; //for server only, not use for client
	private byte[] encryptKey; //for server only, not use for client
	
	/**
	 * @return the keyId
	 */
	public String getKeyId() {
		return keyId;
	}
	/**
	 * @param keyId the keyId to set
	 */
	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}
	
	public long getStartTimeOnServer() {
		return startTimeOnServer;
	}
	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTimeOnServer(long startTime) {
		this.startTimeOnServer = startTime;
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
	 * @return the clientBoot
	 */
//	public String getClientBoot() {
//		return clientBoot;
//	}
//	/**
//	 * @param clientBoot the clientBoot to set
//	 */
//	public void setClientBoot(String clientBoot) {
//		this.clientBoot = clientBoot;
//	}
	/**
	 * @return the clientParametter
	 */
	public String getClientParametter() {
		return clientParametter;
	}
	/**
	 * @param clientParametter the clientParametter to set
	 */
	public void setClientParametter(String clientParametter) {
		this.clientParametter = clientParametter;
	}
	
	public ClientAccessKey(){}
	public ClientAccessKey(byte[] data)
	{
		List<byte[]> parts = ByteUtil.splitDynamicBytes(data);
		keyId = StringConv.UTF8(parts.get(0));
		licenseId =ByteUtil.getInt(parts.get(1));
		startTimeOnServer = ByteUtil.getLong(parts.get(2));
		expiry = ByteUtil.getLong(parts.get(3));
		remainingTime = ByteUtil.getLong(parts.get(4));
		//clientBoot = StringConv.UTF8(parts.get(5));
		clientParametter = StringConv.UTF8(parts.get(5));
		startTimeOnClient = ByteUtil.getLong(parts.get(6));
	}
	
	public byte[] getBytes()
	{
		return ByteUtil.mergeDynamicBytesWithLength(
		StringConv.getBytes(keyId),
		ByteUtil.getBytes(licenseId),
		ByteUtil.getBytes(startTimeOnServer),
		ByteUtil.getBytes(expiry),
		ByteUtil.getBytes(remainingTime),
		//StringConv.getBytes(clientBoot),
		StringConv.getBytes(clientParametter),
		ByteUtil.getBytes(startTimeOnClient));
	}
	/**
	 * @return the remainingTime
	 */
	public long getRemainingTime() {
		return remainingTime;
	}
	/**
	 * @param remainingTime the remainingTime to set
	 */
	public void setRemainingTime(long remainingTime) {
		this.remainingTime = remainingTime;
	}
	/**
	 * @return the startTimeOnClient
	 */
	public long getStartTimeOnClient() {
		return startTimeOnClient;
	}
	/**
	 * @param startTimeOnClient the startTimeOnClient to set
	 */
	public void setStartTimeOnClient(long startTimeOnClient) {
		this.startTimeOnClient = startTimeOnClient;
	}
	public int getVerifiedCode() {
		return verifiedCode;
	}
	public void setVerifiedCode(int verifiedCode) {
		this.verifiedCode = verifiedCode;
	}
	public byte[] getEncryptKey() {
		return encryptKey;
	}
	public void setEncryptKey(byte[] key) {
		this.encryptKey = key;
	}
	public int getLicenseId() {
		return licenseId;
	}
	public void setLicenseId(int licenseId) {
		this.licenseId = licenseId;
	}
	

}
