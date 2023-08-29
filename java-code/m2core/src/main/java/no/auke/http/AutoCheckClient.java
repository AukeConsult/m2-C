package no.auke.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.http.ClientSessionNoJetty;
import no.auke.http.RequestFunc;
import no.auke.http.ResponseFunc;
import no.auke.util.ByteUtil;
import no.auke.util.StringConv;

public class AutoCheckClient {
	
	Logger logger = LoggerFactory.getLogger(AutoCheckClient.class);
	
	private ClientSessionNoJetty session ;
	private String url="";
	
	public AutoCheckClient(String url, int timeout) {
		this.url = url;
		session = new ClientSessionNoJetty();
		session.setTimeOut(timeout);
		
	}
	
	ResponseFunc get(String function, byte[] data)
	{
		byte[] response = session.postAndGet(url, new RequestFunc(function, data).getBytes());
		if(response != null)
		{
			ResponseFunc func = new ResponseFunc(response);
			return func;
		}
		return null;
	}
	
	public byte[] getNewVersion(String currentVersion, String appId) {
		byte[] data = ByteUtil.mergeDynamicBytesWithLength(StringConv.getBytes(currentVersion), StringConv.getBytes(appId));
		logger.info("request new version for " +  appId + " - current version: " + currentVersion);
		return getNewVersion("VERSION", data);
	}

	private byte[] getNewVersion(String function, byte[] data) {
		ResponseFunc func = get(function, data);
		if(func!=null) {
			return func.getContent();
		}
		//we ignore, if there is some connection problem, check again later
		return null;
	}

	public void stop() {
		session.stop();
	}
}
