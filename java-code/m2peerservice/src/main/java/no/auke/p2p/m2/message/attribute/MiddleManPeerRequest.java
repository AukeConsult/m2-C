package no.auke.p2p.m2.message.attribute;

import java.util.List;

import no.auke.p2p.m2.message.UtilityException;
import no.auke.util.ByteUtil;
import no.auke.util.StringConv;

public class MiddleManPeerRequest extends MessageAttribute {
	private String sessionId = "";
	private String srcPublicAddress = "";
	private int srcPublicPort;
	private String srcLocalAddress = "";
	private int srcLocalPort;
	private String targetPublicAddress = "";
	private int targetPublicPort;
	private String targetLocalAddress = "";
	private int targetLocalPort;
	private String middleManIdentity = "";
	private String middleManAddressMap = "";
	public MiddleManPeerRequest() {
		super(MessageAttributeType.MiddleManPeerRequest);
	}
	@Override
	public byte[] getBytes() throws UtilityException {
		if (bytes == null) {
			byte[] databytes = ByteUtil.mergeDynamicBytesWithLength(StringConv.getBytes(getSessionId()), StringConv.getBytes(getMiddleManIdentity()),
					StringConv.getBytes(getSrcPublicAddress().toString()), ByteUtil.getBytes(srcPublicPort),
					StringConv.getBytes(getSrcLocalAddress().toString()), ByteUtil.getBytes(srcLocalPort),
					StringConv.getBytes(getTargetPublicAddress().toString()), ByteUtil.getBytes(targetPublicPort),
					StringConv.getBytes(getTargetLocalAddress().toString()), ByteUtil.getBytes(targetLocalPort), StringConv.getBytes(getMiddleManAddressMap()));
			bytes = ByteUtil.mergeBytes(ByteUtil.getBytes(typeToInteger(type), 2), ByteUtil.getBytes(databytes.length, 2), databytes);
		}
		return bytes;
	}
	public static MiddleManPeerRequest parse(byte[] data) {
		List<byte[]> parts = ByteUtil.splitDynamicBytes(data);
		MiddleManPeerRequest mm = new MiddleManPeerRequest();
		mm.setSessionId(StringConv.UTF8(parts.get(0)));
		mm.setMiddleManIdentity(StringConv.UTF8(parts.get(1)));
		mm.setSrcPublicAddress(StringConv.UTF8(parts.get(2)));
		mm.setSrcPublicPort(ByteUtil.getInt(parts.get(3)));
		mm.setSrcLocalAddress(StringConv.UTF8(parts.get(4)));
		mm.setSrcLocalPort(ByteUtil.getInt(parts.get(5)));
		mm.setTargetPublicAddress(StringConv.UTF8(parts.get(6)));
		mm.setTargetPublicPort(ByteUtil.getInt(parts.get(7)));
		mm.setTargetLocalAddress(StringConv.UTF8(parts.get(8)));
		mm.setTargetLocalPort(ByteUtil.getInt(parts.get(9)));
		mm.setMiddleManAddressMap(StringConv.UTF8(parts.get(10)));
		return mm;
	}
	public String getMiddleManAddressMap() {
		return middleManAddressMap;
	}
	public void setMiddleManAddressMap(String middleManAddressMap) {
		this.middleManAddressMap = middleManAddressMap;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getMiddleManIdentity() {
		return middleManIdentity;
	}
	public void setMiddleManIdentity(String middleManIdentity) {
		this.middleManIdentity = middleManIdentity;
	}
	public String getSrcPublicAddress() {
		return srcPublicAddress;
	}
	public void setSrcPublicAddress(String srcPublicAddress) {
		this.srcPublicAddress = srcPublicAddress;
	}
	public int getSrcPublicPort() {
		return srcPublicPort;
	}
	public void setSrcPublicPort(int srcPublicPort) {
		this.srcPublicPort = srcPublicPort;
	}
	public String getSrcLocalAddress() {
		return srcLocalAddress;
	}
	public void setSrcLocalAddress(String srcLocalAddress) {
		this.srcLocalAddress = srcLocalAddress;
	}
	public int getSrcLocalPort() {
		return srcLocalPort;
	}
	public void setSrcLocalPort(int srcLocalPort) {
		this.srcLocalPort = srcLocalPort;
	}
	public String getTargetPublicAddress() {
		return targetPublicAddress;
	}
	public void setTargetPublicAddress(String targetPublicAddress) {
		this.targetPublicAddress = targetPublicAddress;
	}
	public int getTargetPublicPort() {
		return targetPublicPort;
	}
	public void setTargetPublicPort(int targetPublicPort) {
		this.targetPublicPort = targetPublicPort;
	}
	public String getTargetLocalAddress() {
		return targetLocalAddress;
	}
	public void setTargetLocalAddress(String targetLocalAddress) {
		this.targetLocalAddress = targetLocalAddress;
	}
	public int getTargetLocalPort() {
		return targetLocalPort;
	}
	public void setTargetLocalPort(int targetLocalPort) {
		this.targetLocalPort = targetLocalPort;
	}
}
