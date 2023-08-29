package no.auke.encryption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

import no.auke.util.ByteUtil;
import no.auke.util.FileUtil;
import no.auke.util.StringConv;

public class KeyBase {

	private UUID guid;
	private String encryptmethod="";
	private byte[] key;
	private long created=0;
	private long expire=0;
	private boolean haspassword=false;
	private byte[] token;
	
	private byte[] password;	
	
	public String getEncryptmethod() {	
		return encryptmethod;	
	}

	public void setEncryptmethod(String encryptmethod) {	
		this.encryptmethod = encryptmethod;
	}

	public byte[] getKey() throws EncryptException {
	    
	    if(haspassword){
	        
	        try {

	            if(password!=null && password.length>0) {
	                

	                if(StringConv.UTF8(SecurityUtil.getDecryptedBytesAES(token, password)).equals(guid.toString())) {

	                    return SecurityUtil.getDecryptedBytesAES(key, password);
	                    
	                }
	            
	            }

	        } catch (Exception e) {
                
	            e.printStackTrace();
	            
                throw new EncryptException("KeyBase: getKey: " + e.getMessage());

            }

	    } else {
	        
	        if(guid==null){
	            
	            return key;
	            
	        } else if(StringConv.UTF8(token).equals(guid.toString())) {

	            return key;
	            
	        } 
	    }
	    
	    return null;
	
	}

	public void setKey(byte[] key) throws EncryptException {
	
	    if(haspassword){
	        
	           try {
	               
	               this.key = SecurityUtil.getEncryptedBytesAES(key, password);
	               this.token = SecurityUtil.getEncryptedBytesAES(StringConv.getBytes(guid.toString()), password);
	               
	            } catch (Exception e) {
	                
	                throw new EncryptException("KeyBase: setKey: " + e.getMessage());
	               
	            }
	        
	        
	    } else {
	        
	        this.key = key;
	        this.token = StringConv.getBytes(guid.toString());
	        
	    }
	    
	
	}
	
	public long getCreated() {
	
		return created;
	
	}

	public void setCreated(long created) {
	
		this.created = created;
	
	}

	public long getExpire() {
	
		return expire;
	
	}

	public void setExpire(long expire) {
		
		this.expire = expire;
	
	}	

	public KeyBase(){
		
	}	
	
	public KeyBase(UUID guid) {
		
		created = System.currentTimeMillis();
		expire = System.currentTimeMillis();
		
        if(guid!=null){

            this.guid=guid;
            this.token=StringConv.getBytes(this.guid.toString());
            
        }
		
	}

	public KeyBase(byte[] value) {
	
		toObject(value);
		
	}
	
	public byte[] getBytes() {
		    
		byte[] method_arr = StringConv.getBytes(encryptmethod);
	    	byte[] create_arr = ByteUtil.getBytes(created);
	    	byte[] expire_arr = ByteUtil.getBytes(expire);
	    	byte[] guid_arr = guid!=null?StringConv.getBytes(guid.toString()):new byte[0];
	    
		return ByteUtil.mergeDynamicBytesWithLength(
				key, 
				method_arr,
				create_arr,
				expire_arr, 
				guid_arr, 
				ByteUtil.getBytes(haspassword?1:0), 
				token!=null?token:new byte[0]);
			
	}

	private void toObject(byte[] value) {
			
		if(value!=null&&value.length>0) {	
		    
			List<byte[]> subs = ByteUtil.splitDynamicBytes(value);
		    
		    key = (subs.size() > 0 ? subs.get(0) : null);
		    encryptmethod = (StringConv.UTF8(subs.size() > 1 ? subs.get(1) : null));
		    created = (ByteUtil.getLong(subs.size() > 2 ? subs.get(2) : null));
		    expire = (ByteUtil.getLong(subs.size() > 3 ? subs.get(3) :  null));
		    guid = subs.size() > 4 && subs.get(4).length>0?UUID.fromString(StringConv.UTF8(subs.get(4))) : null;	
		    haspassword = subs.size() > 5 ? (ByteUtil.getInt(subs.get(5))==1?true:false) : false;
		    token = (subs.size() > 6 ? subs.get(6) : (subs.size() > 4 ? subs.get(4):new byte[0]));
		    
		}
	}	
	
	public void storeKey(String keyfile) throws EncryptException {
		FileUtil.deleteFile(keyfile);
		FileUtil.writeToFile(keyfile, getBytes(), false);
	}

	public void readKey(String keyfile) throws EncryptException
	{
		try {
		
			toObject(FileUtil.getByteArrayFromFile(keyfile));
		
		} catch (Exception e) {
			
			throw new EncryptException("KeyBase: readKey: " + e.getMessage());			
		
		}	
	}

	public UUID getGuid() {
		return guid;
	}

	public boolean hasPassword() {
		return haspassword;
	}
	
	public boolean isPasswordSet() {
	    return (password!=null && !(password.length==0));
	}

	public void setPassword(String password) throws EncryptException {
	       
	    if(password!=null && password.length() > 0) {
	        
	        try {
	            
	            MessageDigest md;
	            md = MessageDigest.getInstance("SHA1");

	            byte[] xx = StringConv.getBytes(password);
	            byte[] yy = new byte[xx.length * 2];
	        
	            for (int i = 0; i < xx.length; i++) {
	                yy[i * 2] = xx[i];
	                yy[(i * 2) + 1] = (byte) i;
	            }
	        
	            md.update(yy, 0, yy.length);
	            byte[] digest=md.digest();
	            byte[] pwd = new byte[16];
	            
	            for(int i=0;i<16;i++){
	                pwd[i] = digest[i];
	            }
	            
	            this.haspassword=true;
	            this.password = pwd;
	            
	        } catch (NoSuchAlgorithmException e) {
	    
	            throw new EncryptException("PrivateKey: hashPassword: " + e.getMessage());
	        }
	        
	    }
	    
	}
	
}	
