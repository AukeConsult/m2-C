package no.auke.p2p.m2.sockets.messages;

import java.util.List;

import no.auke.util.ByteUtil;
import no.auke.util.StringConv;

public class MsgFunctionResult {
	public static byte getType() {
		return (byte) 254;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getMsgId() {
		return msgId;
	}
	public String getSessionId() {
		return sessionId;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	private String function = "";
	private String from;
	private String sessionId;
	private String msgId;
	private byte[] data;
	public MsgFunctionResult(byte[] message) {
		if (message[0] == MsgFunctionResult.getType()) {
			byte[] messagedata = new byte[message.length - 1];
			System.arraycopy(message, 1, messagedata, 0, messagedata.length);
			List<byte[]> subs = ByteUtil.splitDynamicBytes(messagedata);
			from = subs.size() > 0 ? StringConv.UTF8(subs.get(0)) : "";
			sessionId = subs.size() > 1 ? StringConv.UTF8(subs.get(1)) : "";
			msgId = subs.size() > 2 ? StringConv.UTF8(subs.get(2)) : "";
			data = subs.size() > 3 ? subs.get(3) : new byte[0];
			setFunction(subs.size() > 4 ? StringConv.UTF8(subs.get(4)) : "");
		}
	}
	public byte[] toBytes() {
		return ByteUtil.mergeBytes(
				new byte[] { MsgFunctionResult.getType() },
				ByteUtil.mergeDynamicBytesWithLength(StringConv.getBytes(from), StringConv.getBytes(sessionId), StringConv.getBytes(msgId), data,
						StringConv.getBytes(getFunction())));
	}
	public MsgFunctionResult(String data) {
		this.data = StringConv.getBytes(data);
	}
	public MsgFunctionResult(String from, String sessionId, String msgId, String function, byte[] data) {
		this.from = from;
		this.sessionId = sessionId;
		this.msgId = msgId;
		this.data = data;
		this.setFunction(function);
	}
	public MsgFunctionResult(String from, String sessionId, String msgId, byte[] data) {
		this.from = from;
		this.sessionId = sessionId;
		this.msgId = msgId;
		this.data = data;
		this.setFunction("");
	}
	public String getFunction() {
		return function;
	}
	public void setFunction(String function) {
		this.function = function;
	}
}