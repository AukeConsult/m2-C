package no.auke.p2p.m2.workers.keepalive;

import java.util.Arrays;

// TDO: LHA: change peer id to byte value at some point
// I.e dont use hex

public class Peerid {
	private String userid = "";
	public String namespace;
	private String peerhexid = "";
	public byte[] peerid = new byte[0];
	public String getUserid() {return userid;}
	public void setUserid(String userid) {this.userid = userid;}
	public String getPeerhexid() {return peerhexid;}
	public void setPeerhexid(String peerhexid) {this.peerhexid = peerhexid;}
	public Peerid() {}
	public Peerid(String peerhexid) {this.peerhexid = peerhexid;}
	public Peerid(String namespace, String userid) {
		this.userid = userid;
		this.namespace = namespace;
		this.peerid = Util.getHash(namespace + userid);
		this.peerhexid = Util.convertToHex(this.peerid);
	}
	public boolean equals(Peerid e) {
		return Arrays.equals(peerid, e.peerid) || this.peerhexid.equals(e.peerhexid);
	}
	public boolean isEmpty() {return peerid.length == 0;}
}