package no.auke.encryption;
public class KeyPair
	{
		public PublicKey publicKey;
		public PrivateKey privateKey;
		
		public KeyPair(){}
		public KeyPair(PublicKey publicKey, PrivateKey privateKey)
		{
			this.publicKey = publicKey;
			this.privateKey = privateKey;
		}
	}