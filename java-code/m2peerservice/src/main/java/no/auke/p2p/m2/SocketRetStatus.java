package no.auke.p2p.m2;

import no.auke.p2p.m2.general.ReturMessageTypes;
import no.auke.p2p.m2.workers.PeerSession;

// helper class to make return variables from
public class SocketRetStatus {
	
	private ReturMessageTypes last_retcode = ReturMessageTypes.ok;
	private String lastmessage = "";
	private PeerSession session = null;
	//private long transactionId = 0;
	//public long getTransactionId() {return transactionId;}
	//public void setTransactionId(long transactionId) {this.transactionId = transactionId;}
	public static String OK = "ok";
	
	public SocketRetStatus() {}
	
	private byte[] data;
	public byte[] getData() {return data;}
	public void setData(byte[] data) {this.data = data;}
	
	public SocketRetStatus(ReturMessageTypes last_retcode) {this.last_retcode = last_retcode;}
	public ReturMessageTypes getLastRetcode() {return last_retcode;}
	public synchronized void setLastRetcode(ReturMessageTypes last_retcode) {this.last_retcode = last_retcode;}
	public String getLastMessage() {return lastmessage;}
	public synchronized void setLastMessage(String lastmessage) {this.lastmessage = lastmessage;}
	public PeerSession getPeerSession() {return session;}
	public synchronized void setPeerAgent(PeerSession session) {this.session = session;}
	public boolean isOk() {return last_retcode == ReturMessageTypes.ok;}
	public void setOk() {
		last_retcode = ReturMessageTypes.ok;
		lastmessage = "";
	}

}
