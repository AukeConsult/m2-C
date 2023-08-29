package no.auke.encryption;

import java.util.UUID;

public class PublicKey extends KeyBase {

	public PublicKey(){
	    super();
	}

	public PublicKey(UUID guid){
	    super(guid);
	}
	
	public PublicKey(byte[] value) throws EncryptException{		
		super(value);		
	}
}

