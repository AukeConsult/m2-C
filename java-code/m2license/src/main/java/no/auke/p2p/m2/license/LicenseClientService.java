package no.auke.p2p.m2.license;


import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.encryption.Encryption;
import no.auke.encryption.EncryptionType;
import no.auke.http.ClientSessionNoJetty;
import no.auke.http.RequestFunc;
import no.auke.http.ResponseFunc;
import no.auke.util.ByteUtil;
import no.auke.util.FileUtil;
import no.auke.util.StringConv;

//make client interface
public class LicenseClientService {

	Logger logger = LoggerFactory.getLogger(LicenseClientService.class);	 

	public boolean enableMap = false;
	public boolean NO_CONTACT_LICENSE_SERVER_WHEN_CHECK = false;
	public boolean NO_LICENSE_CHECK = false;
	
	private LicenseServerMap clientMap;
	private ClientSessionNoJetty session;

	
	private String applicationId;
	private String namespace;
	private ILicenseRegisterHandler registerHandler = null;
	
	public void setLicenseRegisterEvent(ILicenseRegisterHandler registerHandler) {
		this.registerHandler = registerHandler;
	}

	private ILicenseEvent globalEvent = null;
	public void setLicenseEvent(ILicenseEvent event) {
		this.globalEvent = event;
	}

	byte[] unlockKeyFromLicense = null;
	public byte[] getUnLockKey() {
		return unlockKeyFromLicense;
	}

	private boolean use_added_license=false;
	
	private String licensPath="";
	private String licenseKey="";

	public void setLicense(String licenseKey) {
		
		this.licenseKey = licenseKey;
	
	}	

	public String getLicensePath() {
	
		if(!use_added_license) {

			return licensPath + "/license";
			
		} else {
			
			return licensPath + (addedLicensePath.length()==0?"":"/" + addedLicensePath) +"/license";
			
		}
	}

	private String addedLicensePath="";
	private String addedLicenseKey="";

	public void setAddedLicense(String licensePath, String licenseKey) {
	
		this.addedLicenseKey = licenseKey;
		this.addedLicensePath = licensePath;
	
	}
	
	public String getLicenseKey() {
	
		if(!use_added_license) {

			return licenseKey;

		} else {

			return addedLicenseKey;
			
		}
	}
	
	public String getLicenseDescription() {
		
		if(!use_added_license) {

			return applicationId + ":" + namespace + ":" + licenseKey;

		} else {

			return applicationId + ":" + namespace + ":" + addedLicenseKey;
			
		}
	}	

	public String getAppId() {
		return applicationId;
	}

	public String getNetspaceId() {
		return namespace;
	}

	public void setApplicationId(String applicationId) {
		
		this.applicationId = applicationId; 
	}

	public void setNameSpaceId(String namespace) {
		
		this.namespace = namespace;
	}

	String licenseTrialBoostrap;
	String licenseServerBootstrap;


	public LicenseClientService(String appId, String namespace, String licenseBootstrap, String trialBootstrap, String licensPath, ILicenseRegisterHandler licenseRegisterHandler) {

		setApplicationId(appId);
		setNameSpaceId(namespace);

		this.licensPath = (licensPath.length()==0?System.getProperty("user.dir"):licensPath);

		clientMap = new LicenseServerMap(licenseBootstrap);
		this.licenseTrialBoostrap = trialBootstrap;
		this.licenseServerBootstrap = licenseBootstrap;
		
		//get one licenser server from the bootstrap
		setBootstrap(licenseBootstrap);

		setLicenseRegisterEvent(licenseRegisterHandler);
		
		logger.info("Init license service for server(s): " + this.licenseServerBootstrap + ", trial license for server(s): " + this.licenseTrialBoostrap);

		// init();

	}


	public void setBootstrap(String add) {
		
		clientMap.setBootstrap(add);
		//		currentLicenseServer = getNewServer("");
	}

	public void startPoll() {
		
		session = new ClientSessionNoJetty();
		if(session!=null & enableMap) {

			clientMap.startThread();

		}

	}

	public void stop() {
	    
	    if(session!=null){

	        clientMap.stopThread();
	        session.stop();
	        
	    }
	}

	//	String currentLicenseServer ="";
	//	Vector<String> rejectedServers = new Vector<String>(); 
	//	
	//	private String getNewServer(String old)
	//	{
	//		if(old.length() > 0) rejectedServers.add(old);
	//		for(String server : clientMap.getServers())
	//		{
	//			//if(!rejectedServers.contains(server))
	//			//{
	//				//return server;
	//			//}
	//		}
	//		return "";
	//	}

	public int localCheckLicense(ClientLicense license) {

		if(license == null) {

			return LicenseCode.INVALID_LICENSE;

		}

		
		//HUYDO: if this client requires an app-specific-license
		//we check whether the license's appID matches with current appID
		//Otherwise, this is open license. We accept all licenses
		
		if(license.getAppId().length()>0 && !license.getAppId().equals(applicationId)) {
			return LicenseCode.INVALID_APP;
		}

		//check the expired date
		//HUYDO: server calculated the remaining time and returned to client
		if(license.getRemainingTime() <= 0) {
			return LicenseCode.EXPIRED;
		}

		//Problems:
		//1. server time and client time is often different (+-<= 24 hours)
		//2. local checking depends on the client PC'clock. We cant commit client time on server and calculate, that is very ridiculous
		//3. user can change the clock intentionally or accidently, we must handle

		//SOLUTION: -> hwo to not fully-trust client
		//1. make sure if(activeDateOnServer - System.currentTimeMillis() - 24*60*60*1000 > 0) -> invalid, user changed his clock to the past -> should call server
		//2. otherwise
		//if(activeDateOnClient + RemainingTime(Calculated from Server) < System.currentTimeMillis()) -> expired, should call server now


		//user changed his clock, should check license on server and adjust the time
		//HUYDO: no need , dont check, make it simple as possible
		/*
		if(license.getActivateTimeOnServer() - System.currentTimeMillis() - 24*60*60*1000 >0)
		{
			return LicenseCode.LOCALTIME_INVALID;
		}
		else
		{
			if(license.getActivateTimeOnClient() + license.getRemainingTime() <= System.currentTimeMillis())
			{
				return LicenseCode.EXPIRED;
			}
		}*/
		
		if(license.getActivateTimeOnClient() + license.getRemainingTime() <= System.currentTimeMillis()) {
		
			return LicenseCode.EXPIRED;
		}
		
		
		
		return LicenseCode.OK;
	}

	public int localCheckAccessKey(ClientAccessKey key) {
		
		if(key.getRemainingTime() <=0) {
			return LicenseCode.EXPIRED;
		}
		
		//DONT CHECK, too complicated
		/*
		if(key.getStartTimeOnServer() - System.currentTimeMillis() - 24*60*60*1000 > 0)
		{
			return LicenseCode.LOCALTIME_INVALID;
		}
		else
		{
			if(key.getStartTimeOnClient() + key.getRemainingTime() <= System.currentTimeMillis())
			{
				return LicenseCode.EXPIRED;
			}
		}*/
		
		if(key.getStartTimeOnClient() + key.getRemainingTime() <= System.currentTimeMillis()) {
			
			return LicenseCode.EXPIRED;
		
		}
		
		return LicenseCode.OK;
	}

	//client uses it license to verify itself on the server to get access key
	// public ClientAccessKey requestAccessKey(String licenseID, String appID, String namespace, String keyId, ILicenseEvent event)
	public ClientAccessKey requestAccessKey(int licenseID, String keyId, ILicenseEvent event) {

		// 
		// LHA: why a keyid, app and namespace on this?
		// its a key for a license
		// 

		// byte[] data = ByteUtil.mergeDynamicBytesWithLength(StringConv.getBytes(licenseID), StringConv.getBytes(appID),  StringConv.getBytes(namespace), StringConv.getBytes(keyId));
		byte[] data = ByteUtil.mergeDynamicBytesWithLength(ByteUtil.getBytes(licenseID), StringConv.getBytes(keyId));

		if(event !=null) {
			event.onRequestNewKey();
		}

		if(globalEvent!=null) {
			globalEvent.onRequestNewKey();
		}

		logger.info("Request new key " + keyId);

		ResponseFunc func = get(RequestAction.REQUEST_KEY, data);

		if(func!=null) {
			
			if(func.getReturnCode()==LicenseCode.OK) {
				
				try {

					byte[] keyContent = Encryption.getEncryption(EncryptionType.standard).getDecryptedBytesRSA(func.getContent(), getUnLockKey());
					ClientAccessKey key = new ClientAccessKey(keyContent);
					key.setStartTimeOnClient(System.currentTimeMillis());
					key.setVerifiedCode(LicenseCode.OK);
				
					//save key file
					FileUtil.createDirectory(getLicensePath());
					
					//remove any key existing
					for(File f :FileUtil.listFiles(getLicensePath(), new String[]{".key"})) {
						
						f.delete();
					}
					
					//save new key
					FileUtil.saveData(ByteUtil.mergeDynamicBytesWithLength(ByteUtil.getBytes(key.getStartTimeOnClient()), func.getContent()), getLicensePath(), ".key");
					
					if(event!=null) {

						event.onGotNewKey(key);

					}

					if(globalEvent!=null) {

						globalEvent.onGotNewKey(key);

					}

					logger.info("Received new key -Id: " + key.getKeyId() + "-Expiry: " + key.getExpiry() + "milisecs" + "-Remaining time: " + key.getRemainingTime() );
					return key;

				} catch (Exception e) {

					ClientAccessKey key = new ClientAccessKey();
					key.setVerifiedCode(LicenseCode.ENCRYPTION_ERROR);

					if(event !=null) {

						event.onKeyError(LicenseCode.ENCRYPTION_ERROR);
					}

					if(globalEvent!=null) {

						globalEvent.onKeyError(LicenseCode.ENCRYPTION_ERROR);
					}

					logger.error("Error:requestAccessKey:" + e.getMessage());
					return null;
				}

			} else {

				ClientAccessKey key = new ClientAccessKey();
				key.setVerifiedCode(func.getReturnCode());

				//notify the message
				if(event !=null) {

					event.onKeyError(func.getReturnCode());

				}

				if(globalEvent!=null) {

					globalEvent.onKeyError(func.getReturnCode());
				}

				logger.warn(LicenseCode.getMsg(func.getReturnCode()));
				return key;
			}
		}
		return null;
	}

	public ClientLicense requestClientLicense(String licenseKey, String appID, String nameSpace, ILicenseEvent event) {
		
		if(event !=null) {

			event.onRequestLicense();
		}

		if(globalEvent!=null) {

			globalEvent.onRequestLicense();

		}

		//HUYDO: we commit all licenseId, appId, namespaceId to server

		// 1. server check for a license with a spesific licenseid, if not found (or licenceid is blank)
		// 2. server check for a license with a spesific appID and namespace, if not found
		// 3. server check for a license with a spesific appid and blank namespaceid, if not found
		// 4. server check for a licsese with blank appid and a neamespaceid, if not found
		// 5. server check for a license with blank appid and blank namespaceid = overall general m2peer license

		if(licenseKey ==null) {
			
			licenseKey = "";
		}
		
		if(appID ==null) {
			appID ="";
		}
		
		if(nameSpace == null) {
			nameSpace = ""; 
		}
		
		byte[] data = ByteUtil.mergeDynamicBytesWithLength(StringConv.getBytes(licenseKey), StringConv.getBytes(appID), StringConv.getBytes(nameSpace));
		if(licenseKey.length()==0) {
			logger.debug("verifying the license - appid: " + getAppId() + " namespace: " + getNetspaceId());
		} else {
			logger.debug("verifying the license Id: " + licenseKey);
		}
		return getLicense(RequestAction.REQUEST_LICENSE, data, event);		
		

	}

	private ClientLicense getLicense(String function, byte[] data, ILicenseEvent event) {

		ResponseFunc func = get(function, data);
		if(func!=null) {
			
			if(func.getReturnCode()==LicenseCode.OK) {
				
				ClientLicense l = new ClientLicense(func.getContent());

				l.setVerifiedCode(LicenseCode.OK);
				l.setActivateTimeOnClient(System.currentTimeMillis());
				unlockKeyFromLicense = l.getPublicKey();
				
				FileUtil.createDirectory(getLicensePath());
				FileUtil.saveData(l.getBytes(), getLicensePath(), ".license");

				if(event!=null) {
					event.onGotLicense(l);
				}

				if(globalEvent!=null) {
					globalEvent.onGotLicense(l);
				}

				logger.info("verify license - licenseId: " + l.getLicenseKey() + "-Expiry: " + l.getExpiry() + "milisecs" + "-Remaining time: " + l.getRemainingTime());
				return l;

			} else {
				
				ClientLicense l = new ClientLicense();
				l.setVerifiedCode(func.getReturnCode());

				if(event!=null) {
					event.onLicenseError(func.getReturnCode());
				}

				if(globalEvent!=null) {
					globalEvent.onLicenseError(func.getReturnCode());
				}
				logger.warn(LicenseCode.getMsg(func.getReturnCode()));
				return l;
			}
		}

		//we ignore, if there is some connection problem, check again later
		return null;
	}


	public ResponseFunc get(String function, byte[] data) {
		
		if(clientMap.getServers().isEmpty()) {
			
			return null;
		
		} else {
			
			//get server from head
			String server = clientMap.getServers().poll();
			byte[] response = session.postAndGet(server, new RequestFunc(function, data).getBytes());
			
			if(response!=null) {
				//add to tail, reuse later in turn
				clientMap.getServers().offer(server);
				ResponseFunc func = new ResponseFunc(response);
				return func;
			
			} else {
				
				//try new server possibly
				return get(function, data);
			}
		}
	}

	public class LicenseCheck implements Runnable {
		
		public ClientLicense license;
		public ILicenseEvent event;

		public LicenseCheck(ClientLicense license, ILicenseEvent event) {
			
			this.license = license;
			this.event = event;
		}

		@Override
		public void run() {

			if(license.getRemainingTime() >0) {
				
				try {

					Thread.sleep(license.getRemainingTime());
					requestClientLicense(license.getLicenseKey(), license.getAppId(), license.getNamespace(), event);

				} catch (InterruptedException e) {

				}

			}
		}
	}

	public class AccessKeyCheck implements Runnable {
		
		public ClientAccessKey key;
		public ILicenseEvent event;
		
		public AccessKeyCheck(ClientAccessKey key, ILicenseEvent event) {
			
			this.key = key;
			this.event = event;
		
		}

		@Override
		public void run() {

			if(key.getRemainingTime() >0) {
				
				try {

					Thread.sleep(key.getRemainingTime());
					//requestAccessKey(key.getLicenseId(), applicationId, key.getKeyId(), namespace, event);
					requestAccessKey(key.getLicenseId(), key.getKeyId(), event);

				} catch (InterruptedException e) {

				}
			}

		}
	}

	public ClientLicense loadClientLicenseFile() {
		
		try {
			
			File[] files = FileUtil.listFiles(getLicensePath(), new String[]{".license"});
			
			if(files.length >0) {
			
				File file = files[0];
				byte[] data = FileUtil.getByteArrayFromFile(file.getPath());
				ClientLicense cl = new ClientLicense(data);
				
				if(cl.getActivateTimeOnClient() ==0) {
					
					cl.setActivateTimeOnClient(System.currentTimeMillis());
					//remove any license existing
					for(File f :FileUtil.listFiles(getLicensePath(), new String[]{".license"})) {
						
						f.delete();
					}
					
					//save back to file
					FileUtil.saveData(cl.getBytes(), getLicensePath() , ".license");
				}
				unlockKeyFromLicense = cl.getPublicKey();
				return cl;
			
			} else {
				
				return null;
			}

		} catch (Exception e) {

			logger.warn("warn: - loadClientLicenseFile " + e.getMessage());
			return null;

		}
	}
	
	// 
	// LHA: must delete license if wrong namespace
	// 
	
	public void deleteClientLicenseFile(){
	    
	    try {
        
	        FileUtil.deleteDir(new File(getLicensePath()));
        
	    } catch (IOException e) {
        
	        logger.warn("warn: - deleteClientLicenseFile " + e.getMessage());

	    }
	    
	    
	}

	public ClientAccessKey loadAccesskeyFile()
	{
		try {
			
			File[] files = FileUtil.listFiles(getLicensePath(), new String[]{".key"});
			
			if(files.length >0) {
				
				File file = files[0];
				byte[] data = FileUtil.getByteArrayFromFile(file.getPath());
			
				if(data!=null) {
					
					List<byte[]> parts = ByteUtil.splitDynamicBytes(data);
					if(parts.size()>1) {
						
						byte[] content = Encryption.getEncryption(EncryptionType.standard).getDecryptedBytesRSA(parts.get(1), getUnLockKey());
						ClientAccessKey key = new ClientAccessKey(content);
						long startTime = ByteUtil.getLong(parts.get(0));
						key.setStartTimeOnClient(startTime>0?startTime:System.currentTimeMillis());
						key.setVerifiedCode(LicenseCode.OK);
						
						if(startTime==0) //store key when init  
						{

							for(File f :FileUtil.listFiles(getLicensePath(), new String[]{".key"})) {
								f.delete();
							}
							//save back to file
							FileUtil.saveData(ByteUtil.mergeDynamicBytesWithLength(ByteUtil.getBytes(key.getStartTimeOnClient()), parts.get(1)),  getLicensePath() , ".key");
						}
						return key;
					} else {
						
						return null;
					}
				
				} else {
					
					return null;
				}
			
			} else {
				// user directory
				return null;

			}

		} catch (Exception e) {

			logger.error("Error: - loadAccesskeyFile" + e.getMessage());
			return null;
		}

	}

//	public ClientLicense requestTrialLicense(String appId, String netSpaceId, ILicenseEvent event)
//	{
//		// 
//		// LHA: why call this a trial license, its a license
//		// 
//
//		byte[] data = ByteUtil.mergeDynamicBytesWithLength(StringConv.getBytes(appId), StringConv.getBytes(netSpaceId));
//		return getLicense(RequestAction.REQUEST_TRIAL_LICENSE, data, event);
//	}

	ClientAccessKey validateKeysOnServer(ClientLicense li, ClientAccessKey key) {
		
		//try to get from server
		// ClientAccessKey newkey = requestAccessKey(li.getLicenseId(), li.getAppId(), li.getNamespace(), key==null? "" : key.getKeyId(), null);
		ClientAccessKey newkey = requestAccessKey(li.getLicenseId(), key==null? "" : key.getKeyId(), null);
		return newkey;

	}

	ClientAccessKey keyCheck(ClientLicense li, ClientAccessKey key) {
		

		if(key == null) {

			key = loadAccesskeyFile();
		}

		if(key == null) {
			
			return validateKeysOnServer(li, key);

		} else {

			int code = localCheckAccessKey(key);
			if(code == LicenseCode.OK) {
				
				key.setVerifiedCode(code);
				return key;
				
			} else if(code == LicenseCode.EXPIRED) {
				
				key.setVerifiedCode(code);
				return validateKeysOnServer(li, key);
			
			} else if(code == LicenseCode.LOCALTIME_INVALID) {
				
				key.setVerifiedCode(code);
				return validateKeysOnServer(li, key);
			}
		}
		return key;

	}

	ClientLicense myLicense = null;
	ClientAccessKey myKey =null;

	public boolean pingReturn() {
		
		return get(RequestAction.REQUEST_PING, null)!=null;
	}

	public boolean askNewLicenseAndKey(ILicenseEvent lEvent) {
		
		//should ask license if we define an interface to ask for license, by displaying a popup dialog
		if(registerHandler == null) {
			
			//don't allow to use peer server
			return false;
		
		} else {
			
			// check license server is alive or not
			logger.info("Ping to our license server before asking for license");

			if(!pingReturn()) {

				return true;

			}

			//if server is alive, we get new license from that server by using a dialog

			//get new license from the GUI outside
			setLicense(registerHandler.getNewLicense());
			return check("", "", lEvent);
		}

	}
	
	public boolean check(String namespace, String appId, ILicenseEvent lEvent) {
		
		this.use_added_license=false;

		// if no added key, act as normal 
		// set askforkey = true
		
		if(addedLicenseKey.length()==0) {

			return checkLicense(namespace, appId, true, lEvent);
			
		} else {

			// if added key, ask for normal key 
			// set askforkey = false
						
			if(!checkLicense(namespace, appId, false, lEvent)) {
				
				// if normal key not ok
				// use added key
				
				this.use_added_license=true;
				return checkLicense(namespace, appId, true, lEvent);
				
			} else {
				
				return true;
			}
			
		}
		
	}

	private boolean checkLicense(String namespace, String appId, boolean askLicense, ILicenseEvent lEvent) {
		
		if(NO_LICENSE_CHECK) {

			return true;
		}
		
		if(myLicense == null) {

			myLicense = loadClientLicenseFile();
			
//			// LHA: check if local is same as licenkey
//			if(myLicense!=null && licenseKey.length() > 0) {
//				
//				if(!myLicense.getLicenseKey().equals(licenseKey)) {
//					
//					// its not same license
//					
//					myLicense=null;
//					
//				}
//				
//				
//			}
			

			// check namespace 
			
			if(namespace != null && namespace.length() > 0 && myLicense!=null) {

			    // LHA: 
			    // local exists and check namespace
			    // if not same namespace, delete licensefile
			    
			    if(myLicense.getNamespace().length()>0 && !myLicense.getNamespace().equals(namespace)){
			        
			        deleteClientLicenseFile();
			        myLicense=null;
			        
			    }
			    
			}
            			
		}
		
		if(myLicense == null && NO_CONTACT_LICENSE_SERVER_WHEN_CHECK) {
			
		    return false;
		}
		
		if(myLicense!=null) {
			
			int code = localCheckLicense(myLicense);
			if(code == LicenseCode.OK) {
				
				return verifyAccessKeyByLicense(lEvent);
			
			} else {
				
				myLicense=null;
				
			}
			
		}
		
		// use the license if we specified
		if(myLicense==null) {
		    
			if(clientMap.getTheBootstrap().length()==0 || !this.clientMap.getTheBootstrap().equals(licenseServerBootstrap)) {
			    
				clientMap.setBootstrap(licenseServerBootstrap);
			}
			
			ClientLicense license = requestClientLicense(getLicenseKey(), getAppId(), getNetspaceId(), lEvent);

			// try auke server
			if(license==null && (clientMap.getTheBootstrap().length()==0 || !clientMap.getTheBootstrap().equals(licenseTrialBoostrap))){

				clientMap.setBootstrap(licenseTrialBoostrap);
				license = requestClientLicense(getLicenseKey(), getAppId(), getNetspaceId(), lEvent);

			}
			
			if(license==null) {
				
				// LHA: no connection etc, can't validate to server
				// return OK and check later
				
				return true;

			} else {

				//check license
				int code = localCheckLicense(license);
				
				if(code != LicenseCode.OK) {

					if(askLicense) {
					
						return askNewLicenseAndKey(lEvent);
					
					} else {
						
						return false;
					
					}
					
				} else {

					lEvent.onOkLicence(license);

					myLicense = license;
					return verifyAccessKeyByLicense(lEvent);

				}
			}
			
		} else {
			
			return verifyAccessKeyByLicense(lEvent);
			
		}

	}

	private boolean verifyAccessKeyByLicense(ILicenseEvent lEvent) {

		//process check key
		ClientAccessKey k = keyCheck(myLicense, myKey);
		if(k!=null) {
			
			if(k.getVerifiedCode() == LicenseCode.OK) {
			
				myKey = k;
				lEvent.onOkKey(k);
				return true;
			
			} else {
				
				// LHA: dont ask for license if access key check
				return false;
				// return askNewLicenseAndKey(lEvent);
			}
			
		} else {
			
			//ignore, connection problem with server
			return true;
		}
	}

	public void removeOldLicense() {
		
		FileUtil.deleteFile(getLicensePath() +"/.license");
	}



//	public String getClientBoot()
//	{
//		if(myKey!=null)
//		{
//			return myKey.getClientBoot();
//		}
//		else
//		{
//			return null;
//		}
//	}

	public String getClientParams() {
		
		if(myKey!=null) {
			
			return myKey.getClientParametter();
		
		} else {
			
			return null;
		}
	}

	//	public boolean isUsingTrialLicense()
	//	{
	//		if(myLicense == null) {
	//		
	//		    return true;
	//		}
	//		
	//		return !myLicense.getLicenseId().equals(trial_licenseId);
	//	}

	public void forceCheckAccessKey() {
		
		if(myLicense!=null &&  myKey !=null) {
			
			validateKeysOnServer(myLicense, myKey);
		}
	}
}
