package no.auke.p2p.m2.message.attribute;

import java.util.List;

import no.auke.p2p.m2.message.UtilityException;
import no.auke.util.ByteUtil;
import no.auke.util.StringConv;

public class MiddleManInfo extends MessageAttribute {
	private String mmPublicAddress = "";// current PublicIP i am having from KA,
										// but might be change
	private String mmLocalAddress = "";// private IP i have now on my local
	private int mmLocalAgentPort = 0;// local agent port i am listening
	private int mmPhysicalKAPort = 0; // my public KA port
	private int mmPhysicalAgentPort = 0;// my public agent port
	private int capacity = 0;
	private int numberOfSessions = 0;
	private String deadSessionIds = "";
	public MiddleManInfo() {
		super(MessageAttributeType.MiddleManInfo);
	}
	@Override
	public byte[] getBytes() throws UtilityException {
		if (bytes == null) {
			byte[] databytes = ByteUtil.mergeDynamicBytesWithLength(StringConv.getBytes(mmPublicAddress), StringConv.getBytes(mmLocalAddress),
					ByteUtil.getBytes(mmLocalAgentPort), ByteUtil.getBytes(mmPhysicalAgentPort), ByteUtil.getBytes(mmPhysicalKAPort),
					ByteUtil.getBytes(capacity), ByteUtil.getBytes(numberOfSessions), StringConv.getBytes(deadSessionIds));
			bytes = ByteUtil.mergeBytes(ByteUtil.getBytes(typeToInteger(type), 2), ByteUtil.getBytes(databytes.length, 2), databytes);
		}
		return bytes;
	}
	public static MessageAttribute parse(byte[] valueArray) {
		List<byte[]> parts = ByteUtil.splitDynamicBytes(valueArray);
		MiddleManInfo mm = new MiddleManInfo();
		mm.setMmPublicAddress(StringConv.UTF8(parts.get(0)));
		mm.setMmLocalAddress(StringConv.UTF8(parts.get(1)));
		mm.setMmLocalAgentPort(ByteUtil.getInt(parts.get(2)));
		mm.setMmPhysicalAgentPort(ByteUtil.getInt(parts.get(3)));
		mm.setMmPhysicalKAPort(ByteUtil.getInt(parts.get(4)));
		mm.setCapacity(ByteUtil.getInt(parts.get(5)));
		mm.setNumberOfSessions(ByteUtil.getInt(parts.get(6)));
		mm.setDeadSessionIds(StringConv.UTF8(parts.get(7)));
		return mm;
	}
	public String getMmPublicAddress() {
		return mmPublicAddress;
	}
	public void setMmPublicAddress(String mmPublicAddress) {
		this.mmPublicAddress = mmPublicAddress;
	}
	public int getMmLocalAgentPort() {
		return mmLocalAgentPort;
	}
	public void setMmLocalAgentPort(int mmLocalAgentPort) {
		this.mmLocalAgentPort = mmLocalAgentPort;
	}
	public String getMmLocalAddress() {
		return mmLocalAddress;
	}
	public void setMmLocalAddress(String mmLocalAddress) {
		this.mmLocalAddress = mmLocalAddress;
	}
	public int getMmPhysicalAgentPort() {
		return mmPhysicalAgentPort;
	}
	public void setMmPhysicalAgentPort(int mmPhysicalAgentPort) {
		this.mmPhysicalAgentPort = mmPhysicalAgentPort;
	}
	public int getMmPhysicalKAPort() {
		return mmPhysicalKAPort;
	}
	public void setMmPhysicalKAPort(int mmPhysicalKAPort) {
		this.mmPhysicalKAPort = mmPhysicalKAPort;
	}
	public int getCapacity() {
		return capacity;
	}
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	public int getNumberOfSessions() {
		return numberOfSessions;
	}
	public void setNumberOfSessions(int numberOfSessions) {
		this.numberOfSessions = numberOfSessions;
	}
	public void setDeadSessionIds(String deadSessionIds) {
		this.deadSessionIds = deadSessionIds;
	}
	public String getDeadSessionIds() {
		return deadSessionIds;
	}
}
