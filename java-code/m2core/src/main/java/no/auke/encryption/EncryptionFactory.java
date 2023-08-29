package no.auke.encryption;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import no.auke.util.FileUtil;

// generate a RSA, public / private key
public class EncryptionFactory {

    public static KeyPair generateKeyPairs(UUID guid, String password) throws EncryptException{
        
        try {
            
            
            KeyCreator keys = new KeyCreator();
            
            PublicKey publickey = new PublicKey(guid);
            publickey.setEncryptmethod(keys.getMethod());
            publickey.setKey(keys.getPublicKey().getEncoded());

            PrivateKey privatekey = new PrivateKey(publickey.getGuid(), password);
            privatekey.setEncryptmethod(keys.getMethod());
            privatekey.setKey(keys.getPrivateKey().getEncoded());

            return new KeyPair(publickey, privatekey);
            
        } catch (NoSuchAlgorithmException e) {
            
            throw new EncryptException("FactoryL3: generateKeys: NoSuchAlgorithmException: " + e.getMessage());
        
        } catch (Exception e) {
        
            throw new EncryptException("FactoryL3: readPrivateKey: General: " + e.getMessage());
            
        }               
    }
    
    public static KeyPair generateKeyPairs(int size, UUID guid, String password) throws EncryptException{
        
        try {
            
            
            KeyCreator keys = new KeyCreator(size);
            
            PublicKey publickey = new PublicKey(guid);
            publickey.setEncryptmethod(keys.getMethod());
            publickey.setKey(keys.getPublicKey().getEncoded());

            PrivateKey privatekey = new PrivateKey(publickey.getGuid(), password);
            privatekey.setEncryptmethod(keys.getMethod());
            privatekey.setKey(keys.getPrivateKey().getEncoded());

            return new KeyPair(publickey, privatekey);
            
        } catch (NoSuchAlgorithmException e) {
            
            throw new EncryptException("FactoryL3: generateKeys: NoSuchAlgorithmException: " + e.getMessage());
        
        } catch (Exception e) {
        
            throw new EncryptException("FactoryL3: readPrivateKey: General: " + e.getMessage());
            
        }               
    }    

	public static KeyPair generateKeys(String fileid, String password, UUID guid) throws EncryptException{
		
		KeyPair keys = generateKeyPairs(guid, password);

		if(!fileid.equals("")) {
			
			keys.privateKey.storeKey(fileid + ".key");
			keys.privateKey.storeKey(fileid + ".pub");

		}
		return keys;
			
	}

	public static KeyPair generateKeys(int size, String fileid, String password, UUID guid) throws EncryptException{
		
		KeyPair keys = generateKeyPairs(size,guid,password);

		if(!fileid.equals("")) {
			
			keys.privateKey.storeKey(fileid + ".key");
			keys.privateKey.storeKey(fileid + ".pub");

		}
		return keys;
			
	}

    public static KeyPair generateKeys(int size) throws EncryptException{
        
        return generateKeys(size,"","",UUID.randomUUID());
    }
    
    public static KeyPair generateKeys() throws EncryptException{
        
        return generateKeys(1024);
    }

    public static KeyPair generateKeys(int size, String fileid, String password) throws EncryptException{
        return generateKeys(size,fileid,password,UUID.randomUUID());
    }    

    public static PrivateKey readPrivateKey(String file) throws EncryptException{	    
	    return readPrivateKey(file,"");
	    
	}

    public static PrivateKey readPrivateKey(String file, String password) throws EncryptException{
		
		byte[] value=null;
        
		try {
            
		    value = FileUtil.getByteArrayFromFile(file);
        
        } catch (Exception e) {
        }

		if (value == null) {
			
			throw new EncryptException("FactoryL3: readPrivateKey:" + "Can not read private key from file "+ file);
		
		}
		if (value.length < 10) {
			
			throw new EncryptException("FactoryL3: readPrivateKey:" + "Can not read private key from file, wrong file " + file);
		
		}
		
		PrivateKey key = new PrivateKey(value);
		key.setPassword(password);
		key.getKey();
		
		return key;
			
	}
		
	public static PublicKey readPublicKey(String file) throws EncryptException{		
		try {
					
			byte[] value = FileUtil.getByteArrayFromFile(file);
			if (value == null) {
				
				throw new EncryptException("readPublicKey:" + "Can not read private key from file "+ file);
			
			}
			if (value.length < 10) {
				
				throw new EncryptException("readPublicKey:" + "Can not read private key from file, wrong file " + file);
			
			}
			
			PublicKey key = new PublicKey(value);
			if (key.getKey() == null && key.getKey().length==0) {
				
				throw new EncryptException("readPublicKey:" + " error in private key file "+ file);
			
			}

			return key;
			
		} catch (NoSuchAlgorithmException e) {
			throw new EncryptException(EncryptExceptionTypes.NoSuchAlgorithmException,"readPublicKey: " + e.getMessage());
		} catch (Exception e) {
			throw new EncryptException(EncryptExceptionTypes.Exception,"readPublicKey: " + e.getMessage());
		}				
	}
}
