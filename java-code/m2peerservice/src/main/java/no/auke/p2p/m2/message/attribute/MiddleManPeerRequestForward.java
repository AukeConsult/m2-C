package no.auke.p2p.m2.message.attribute;

import java.util.List;

import no.auke.p2p.m2.message.UtilityException;
import no.auke.util.ByteUtil;
import no.auke.util.StringConv;

public class MiddleManPeerRequestForward extends MessageAttribute {
	private String sessionId = "";
	private String myIdentity = "";
	private boolean isRejected = false;
	private String localAgentAddress = "";
	private int localAgentPort;
	public MiddleManPeerRequestForward() {
		super(MessageAttributeType.MiddleManPeerRequestForward);
	}
	@Override
	public byte[] getBytes() throws UtilityException {
		if (bytes == null) {
			byte[] databytes = ByteUtil.mergeDynamicBytesWithLength(StringConv.getBytes(getSessionId()), StringConv.getBytes(getMyIdentity()),
					isRejected ? ByteUtil.getBytes(1) : ByteUtil.getBytes(0), StringConv.getBytes(localAgentAddress), ByteUtil.getBytes(localAgentPort));
			bytes = ByteUtil.mergeBytes(ByteUtil.getBytes(typeToInteger(type), 2), ByteUtil.getBytes(databytes.length, 2), databytes);
		}
		return bytes;
	}
	public static MiddleManPeerRequestForward parse(byte[] data) {
		List<byte[]> parts = ByteUtil.splitDynamicBytes(data);
		MiddleManPeerRequestForward mm = new MiddleManPeerRequestForward();
		mm.setSessionId(StringConv.UTF8(parts.get(0)));
		mm.setMyIdentity(StringConv.UTF8(parts.get(1)));
		mm.setRejected(ByteUtil.getInt(parts.get(2)) == 1 ? true : false);
		mm.setLocalAgentAddress(StringConv.UTF8(parts.get(3)));
		mm.setLocalAgentPort(ByteUtil.getInt(parts.get(4)));
		return mm;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public boolean isRejected() {
		return isRejected;
	}
	public void setRejected(boolean isRejected) {
		this.isRejected = isRejected;
	}
	public String getMyIdentity() {
		return myIdentity;
	}
	public void setMyIdentity(String myIdentity) {
		this.myIdentity = myIdentity;
	}
	public String getLocalAgentAddress() {
		return localAgentAddress;
	}
	public void setLocalAgentAddress(String localAgentAddress) {
		this.localAgentAddress = localAgentAddress;
	}
	public int getLocalAgentPort() {
		return localAgentPort;
	}
	public void setLocalAgentPort(int localAgentPort) {
		this.localAgentPort = localAgentPort;
	}
}
