package no.auke.encryption;


public class Encryption {

	static IEncryptionUtil encryptionUtil = new EncryptionStandard();
	
	public static IEncryptionUtil getEncryption(EncryptionType type) {
	
		// TODO: only standard is supported
		
		return encryptionUtil;
	
	}

}
