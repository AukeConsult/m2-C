package no.auke.encryption;

import java.util.UUID;

public class PrivateKey extends KeyBase {
	public PrivateKey(){
		super();
	}	
	public PrivateKey(UUID guid, String password) throws EncryptException {
		super(guid);
        	setPassword(password);	
	}

	public PrivateKey(byte[] value) throws EncryptException {
		super(value);
	}
}
