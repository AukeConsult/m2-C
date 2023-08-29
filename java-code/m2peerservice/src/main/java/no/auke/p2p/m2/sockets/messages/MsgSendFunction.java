package no.auke.p2p.m2.sockets.messages;

import java.util.List;
import java.util.UUID;

import no.auke.util.ByteUtil;
import no.auke.util.StringConv;

public class MsgSendFunction {
	public static byte getType() {
		return (byte) 255;
	}
	public String getFromClientId() {
		return fromClientId;
	}
	public void setFromClientId(String from) {
		this.fromClientId = from;
	}
	public int getFromPort() {
		return fromPort;
	}
	public void setFromPort(int fromPort) {
		this.fromPort = fromPort;
	}
	public String getToClientId() {
		return toClientId;
	}
	public String getMsgId() {
		return msgId;
	}
	public String getSessionId() {
		return sessionId;
	}
	public String getFunction() {
		return function;
	}
	public void setFunction(String function) {
		this.function = function;
	}
	public byte[] getData() {
		return data;
	}
	public boolean isAsync() {
		return isAsync;
	}
	private String fromClientId = "";
	private int fromPort = 0;
	private String toClientId = "";
	private String sessionId = "";
	private String msgId = "";
	private String function = "";
	private byte[] data;
	private long sendingTime = 0;
	private boolean isAsync = false;
	public MsgSendFunction(String fromClientId, int fromPort, String toClientId, String function, String sessionId, String msgId, boolean isAsync, byte[] data) {
		this.fromClientId = fromClientId;
		this.fromPort = fromPort;
		this.toClientId = toClientId;
		this.function = function;
		this.msgId = msgId;
		this.sessionId = sessionId;
		this.data = data;
		this.sendingTime = System.currentTimeMillis();
		this.isAsync = isAsync;
	}
	public MsgSendFunction(String fromClientId, int fromPort, String toClientId, String function, String sessionId, boolean isAsync, byte[] data) {
		this.fromClientId = fromClientId;
		this.fromPort = fromPort;
		this.toClientId = toClientId;
		this.function = function;
		this.msgId = UUID.randomUUID().toString().substring(1, 5);
		this.sessionId = sessionId;
		this.data = data;
		this.sendingTime = System.currentTimeMillis();
		this.isAsync = isAsync;
	}
	public MsgSendFunction(byte[] message) {
		if (message[0] == MsgSendFunction.getType()) {
			byte[] messagedata = new byte[message.length - 1];
			System.arraycopy(message, 1, messagedata, 0, messagedata.length);
			List<byte[]> subs = ByteUtil.splitDynamicBytes(messagedata);
			fromClientId = StringConv.UTF8(subs.get(0));
			fromPort = ByteUtil.getInt(subs.get(1));
			toClientId = StringConv.UTF8(subs.get(2));
			sessionId = StringConv.UTF8(subs.get(3));
			msgId = StringConv.UTF8(subs.get(4));
			function = StringConv.UTF8(subs.get(5));
			data = subs.get(6);
			sendingTime = ByteUtil.getLong(subs.get(7));
			isAsync = subs.size() > 8 ? (ByteUtil.getInt(subs.get(8)) == 1 ? true : false) : isAsync;
		}
	}
	public byte[] toBytes() {
		return ByteUtil.mergeBytes(new byte[] { MsgSendFunction.getType() }, ByteUtil.mergeDynamicBytesWithLength(StringConv.getBytes(fromClientId),
				ByteUtil.getBytes(fromPort, 4), StringConv.getBytes(toClientId), StringConv.getBytes(sessionId), StringConv.getBytes(msgId),
				StringConv.getBytes(function), data, ByteUtil.getBytes(sendingTime, 8), isAsync ? ByteUtil.getBytes(1) : ByteUtil.getBytes(0)));
	}
	public long getSendingTime() {
		return sendingTime;
	}
	public void setSendingTime(long sendingTime) {
		this.sendingTime = sendingTime;
	}
}