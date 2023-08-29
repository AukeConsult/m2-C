package no.auke.m2.encryption;
public class CipherExeption extends Exception {
	private static final long serialVersionUID = -319754395927340098L;
	private int errlevel = 2;
	public int getErrlevel() {
		return errlevel;
	}
	public CipherExeption(String mesg) {
		super(mesg);
	}
	public CipherExeption(int errlevel, String mesg) {
		super(mesg);
		this.errlevel = errlevel;
	}
}
