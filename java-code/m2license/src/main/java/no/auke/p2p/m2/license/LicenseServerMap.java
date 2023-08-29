package no.auke.p2p.m2.license;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import no.auke.http.ClientSessionNoJetty;
import no.auke.http.RequestFunc;
import no.auke.util.BaseRunable;
import no.auke.util.ByteUtil;
import no.auke.util.StringConv;

public class LicenseServerMap extends BaseRunable {
	
	ConcurrentLinkedQueue<String> map = new ConcurrentLinkedQueue<String>(); 
	Vector<String> bootstrap = new Vector<String>();
	ClientSessionNoJetty agent;
	
	public ConcurrentLinkedQueue<String> getServers() {
		
		return map;
	}
	
	private String theBootstrap="";
	
	public LicenseServerMap(String boostrap) {
		
		setBootstrap(boostrap);
	
	}
	
	public void setBootstrap(String licenseboostrap) {
		
		this.theBootstrap = licenseboostrap;
		synchronized (map) {
			map.clear();
		}
		
		synchronized (this.bootstrap) {
			this.bootstrap.clear();
		}
		
		String[] servers = this.theBootstrap.split("\\s*[;,]\\s*");
		for(String server : servers) {
			
			map.add(server);
			this.bootstrap.add(server);
		}
	}
	
	private void fillBoostrap() {
		
		if(map.isEmpty()) {
			
			for(String server : bootstrap) {
				
				map.add(server);
			}
		}
	
	}
	
	
	public void ping(final String server) {

		byte[] content = agent.postAndGet(server, new RequestFunc(RequestAction.REQUEST_MAP, null).getBytes());

		if(content!=null && content.length >0) {
			
			List<byte[]> parts = ByteUtil.splitDynamicBytes(content);

			for(byte[] part : parts) {
				
				String url = StringConv.UTF8(part);
				
				if(!map.contains(url)) {
					map.add(url);
				}

			}

		} else {
			
			map.remove(server);
		}
	}

	
	//
	// LHA: poll every 10 minutes
	//
	
	@Override
	protected int getWaitperiod() {
	
		return 60000*10;
	
	}

	@Override
	protected void execute() {
		
		//fill bootstrap if empty
		fillBoostrap();
		
		for(String server : map) {
			
			if(server.length() > 0) {

				ping(server);
				
			}
		}
		
	}

	@Override
	protected boolean doExecute() {
		return true;
	}

	@Override
	protected void onStart() {
		agent = new ClientSessionNoJetty();
	}

	@Override
	protected void onStarted() {
	}

	@Override
	protected void onStopped() {
		if(agent!=null)
		agent.stop();
	}

	public String getTheBootstrap() {
		return theBootstrap;
	}

}
