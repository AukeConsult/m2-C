package no.auke.p2p.m2.message.attribute;

import no.auke.p2p.m2.message.UtilityException;
import no.auke.util.ByteUtil;

public class MiddleManRequire extends MessageAttribute {
	private int numberOfMM = 1;
	public MiddleManRequire() {
		super(MessageAttributeType.MiddleManRequire);
	}
	public MiddleManRequire(int numberOfRequire) {
		this();
		this.numberOfMM = numberOfRequire;
	}
	@Override
	public byte[] getBytes() throws UtilityException {
		if (bytes == null) {
			byte[] databytes = ByteUtil.getBytes(getNumberOfMiddleman(), 2);
			bytes = ByteUtil.mergeBytes(ByteUtil.getBytes(typeToInteger(type), 2), ByteUtil.getBytes(databytes.length, 2), databytes);
		}
		return bytes;
	}
	public static MiddleManRequire parse(byte[] data) {
		MiddleManRequire mm = new MiddleManRequire();
		mm.setNumberOfMiddleman(ByteUtil.getInt(data));
		return mm;
	}
	public int getNumberOfMiddleman() {
		return numberOfMM;
	}
	public void setNumberOfMiddleman(int numberOfMM) {
		this.numberOfMM = numberOfMM;
	}
}
