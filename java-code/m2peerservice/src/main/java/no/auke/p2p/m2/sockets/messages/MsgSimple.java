package no.auke.p2p.m2.sockets.messages;

import java.util.List;

import no.auke.util.ByteUtil;
import no.auke.util.StringConv;

public class MsgSimple {
	String userid = "";
	long id = 0;
	long timesent = 0;
	byte[] message = new byte[0];
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getTimesent() {
		return timesent;
	}
	public void setTimesent(long timesent) {
		this.timesent = timesent;
	}
	public byte[] getMessage() {
		return message;
	}
	public void setMessage(byte[] message) {
		this.message = message;
	}
	public MsgSimple(String userid, long id, byte[] message) {
		this.userid = userid;
		this.id = id;
		this.timesent = System.currentTimeMillis();
		this.message = message;
	}
	public MsgSimple(String userid, long id, long timesent, byte[] message) {
		this.userid = userid;
		this.id = id;
		this.timesent = timesent;
		this.message = message;
	}
	public MsgSimple(byte[] data) {
		try {
			if (data != null && data.length > 0) {
				List<byte[]> subs = ByteUtil.splitDynamicBytes(data);
				if (subs.size() > 3) {
					userid = StringConv.UTF8(subs.get(0));
					id = ByteUtil.getLong(subs.get(1));
					timesent = ByteUtil.getLong(subs.get(2));
					message = subs.get(3) == null ? new byte[0] : subs.get(3);
				}
			}
		} catch (Exception ex) {
			// some error in data sent
		}
	}
	public byte[] getBytes() {
		return ByteUtil.mergeDynamicBytesWithLength(userid.length() == 0 ? new byte[0] : StringConv.getBytes(userid), ByteUtil.getBytes(id, 8),
				ByteUtil.getBytes(timesent, 8), message == null ? new byte[0] : message);
	}
}
