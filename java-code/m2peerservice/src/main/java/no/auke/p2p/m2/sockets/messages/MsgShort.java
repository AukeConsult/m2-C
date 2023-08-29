package no.auke.p2p.m2.sockets.messages;

import java.util.List;
import java.util.UUID;

import no.auke.util.ByteUtil;
import no.auke.util.StringConv;

public class MsgShort {
	public boolean isIncoming() {
		return incoming;
	}
	public void setIncoming(boolean incoming) {
		this.incoming = incoming;
	}
	public String getFrom() {
		return (from == null ? "" : from);
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return (to == null ? "" : to);
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getMessageId() {
		return (messageId == null ? "" : messageId);
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public String getReplyId() {
		return (replyId == null ? "" : replyId);
	}
	public void setReplyId(String replyId) {
		this.replyId = replyId;
	}
	public String getConversationId() {
		return (conversationId == null ? "" : conversationId);
	}
	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
	}
	public String getMessage() {
		return (message == null ? "" : StringConv.UTF8(message));
	}
	public void setMessage(String message) {
		this.message = StringConv.getBytes(message);
	}
	public byte[] getImage() {
		return image;
	}
	public void setImage(byte[] image) {
		this.image = image;
	}
	public String getDataName() {
		return dataName;
	}
	public void setDataName(String dataname) {
		this.dataName = dataname;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String datatype) {
		this.dataType = datatype;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public long getCreated() {
		return created;
	}
	public void setCreated(long created) {
		this.created = created;
	}
	public boolean IsRecieved() {
		return recieved;
	}
	public void setRecieved(boolean recieved) {
		this.recieved = recieved;
	}
	public void setIsmessage(boolean ismessage) {
		this.isMessage = ismessage;
	}
	public boolean isChecked() {
		return checked;
	}
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	private String from = "";
	private String to = "";
	private String messageId = UUID.randomUUID().toString().substring(0, 5);
	private String replyId = "";
	private String conversationId = "";
	private byte[] message = new byte[0];
	private byte[] image = new byte[0];
	private String dataType = "";
	private String dataName = "";
	private byte[] data = new byte[0];
	private long created = 0;
	private boolean recieved = false;
	private boolean incoming = false;
	private boolean checked = false;
	private boolean isMessage = true;
	public boolean isMessage() {
		return isMessage;
	}
	public int Tries = 0;
	public MsgShort(String from, String to, byte[] message) {
		this.from = from;
		this.to = to;
		this.message = message;
		created = System.currentTimeMillis();
	}
	public MsgShort() {}
	public MsgShort(byte[] data) {
		if (data != null) {
			List<byte[]> subs = ByteUtil.splitDynamicBytes(data);
			if (subs.size() > 11) {
				from = StringConv.UTF8(subs.get(0));
				to = StringConv.UTF8(subs.get(1));
				messageId = StringConv.UTF8(subs.get(2));
				replyId = StringConv.UTF8(subs.get(3));
				conversationId = StringConv.UTF8(subs.get(4));
				message = subs.get(5);
				image = subs.get(6);
				dataType = StringConv.UTF8(subs.get(7));
				dataName = StringConv.UTF8(subs.get(8));
				data = subs.get(9);
				created = ByteUtil.getLong(subs.get(10));
				isMessage = ByteUtil.getInt(subs.get(11)) == 1 ? true : false;
			}
		}
	}
	public byte[] getBytes() {
		return ByteUtil.mergeDynamicBytesWithLength(StringConv.getBytes(from), StringConv.getBytes(to), StringConv.getBytes(messageId),
				StringConv.getBytes(replyId), StringConv.getBytes(conversationId), message, image, StringConv.getBytes(dataType),
				StringConv.getBytes(dataName), data, ByteUtil.getBytes(created, 8), new byte[] { isMessage ? (byte) 1 : (byte) 0 });
	}
	public byte[] toReply() {
		MsgShort message = new MsgShort();
		message.setIsmessage(false);
		message.setReplyId(messageId);
		message.setTo(from);
		message.setFrom(to);
		return message.getBytes();
	}
}