package no.auke.encryption;

public class EncryptException extends Exception {
	private static final long serialVersionUID = 1793945684038863518L;

	private EncryptExceptionTypes exceptiontype=EncryptExceptionTypes.general;
	
	public EncryptExceptionTypes getExceptionType() {
		return exceptiontype;
	}

	public EncryptException(String mesg) {
        super(mesg);
    }

	public EncryptException(EncryptExceptionTypes exceptiontype, String mesg) {
        super(mesg);
        this.exceptiontype=exceptiontype;
	}	
}