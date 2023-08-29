package no.auke.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.conn.ClientConnectionManager;
//import org.apache.http.conn.scheme.Scheme;
//import org.apache.http.conn.scheme.SchemeRegistry;
//import org.apache.http.conn.ssl.SSLSocketFactory;
//import org.apache.http.conn.ssl.X509HostnameVerifier;
//import org.apache.http.entity.InputStreamEntity;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
//import org.apache.http.params.CoreConnectionPNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: LHA: make a clientsession without jetty
// because jetty is to big for a general lib like aukelib

// will make level0 significantly smaller

// move all jetty related stuff into m2licenseserver

@SuppressWarnings("deprecation")
public class ClientSessionNoJetty {
	    
//	DefaultHttpClient httpClient;	    
//	public DefaultHttpClient wrapClient(HttpClient base) {
//        
//        try {
//            
//            SSLContext ctx = SSLContext.getInstance("TLS");
//            X509TrustManager tm = new X509TrustManager() {
// 
//                public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
//                }
// 
//                public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
//                }
// 
//                public X509Certificate[] getAcceptedIssuers() {
//                    return null;
//                }
//            };
//            X509HostnameVerifier verifier = new X509HostnameVerifier() {
//
//				@Override
//				public boolean verify(String hostname, SSLSession session) {
//					return true;
//				}
//
//				@Override
//				public void verify(String arg0, SSLSocket arg1)
//						throws IOException {
//				}
//
//				@Override
//				public void verify(String arg0, X509Certificate arg1)
//						throws SSLException {
//				}
//
//				@Override
//				public void verify(String arg0, String[] arg1, String[] arg2)
//						throws SSLException {
//				}
// 
//            };
//            ctx.init(null, new TrustManager[]{tm}, null);
//            SSLSocketFactory ssf = new SSLSocketFactory(ctx);
//            ssf.setHostnameVerifier(verifier);
//            ClientConnectionManager ccm = base.getConnectionManager();
//            SchemeRegistry sr = ccm.getSchemeRegistry();
//            sr.register(new Scheme("https", ssf, 443));
//            
//            return new DefaultHttpClient(ccm, base.getParams());
//        
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return null;
//        }
//    }
    	
	private static final Logger logger = LoggerFactory.getLogger(ClientSessionNoJetty.class);		
	String scheme="https://";
	
	private int timeout = 10000;
	
	public ClientSessionNoJetty()
	{
		start();
	}
	
	public void setTimeOut(int timeout)
	{
		this.timeout = timeout;
		stop();
		start();
	}
	

	public void start()
	{
//        if(httpClient==null){
//
//            httpClient = wrapClient(new DefaultHttpClient());
//            
//        }
//
//        if(httpClient!=null){
//
//		    httpClient.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, timeout)
//            .setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 1000)
//            .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
//            .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true);
//		    
//		}
		
	}
	

	public void stop()
	{
//		if(httpClient!=null) {
//		    
//		    httpClient.getConnectionManager().shutdown();
//		    httpClient=null;
//		
//		}

	}
	
	private byte[] readResponse(InputStream content) throws IOException {

		// read and decode
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] inbuffer = new byte[4096];
		int bytes=0;
		while((bytes=content.read(inbuffer,0,4096))!=-1){
			buffer.write(inbuffer, 0,bytes);
		}
		return buffer.toByteArray();			
	}	
	
 	public byte[] postAndGet(String url, byte[] exchangeContent)
	{
		return postAndGet(url,exchangeContent, 0);
	}
 	

	public byte[] postAndGet(String url, byte[] exchangeContent, final int retryCount)
	{
	    
//	    // TODO: LHA: must be fixed.. this is not good
//	    // create a new client for each request ?
//        httpClient = wrapClient(new DefaultHttpClient());
//	    
//	    //start();
//		
//        if(httpClient!=null){
//        	HttpResponse response = null ;
//        	
//            try 
//            {
//
//                if(!url.startsWith(scheme)) {
//                    url = scheme + url;
//                }
//
//                HttpPost postRequest = new HttpPost(url);
//                InputStreamEntity dataEntity = new InputStreamEntity(new ByteArrayInputStream(exchangeContent), -1);
//                postRequest.setEntity(dataEntity);
//                httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(retryCount, false));
//                
//
//                 response = httpClient.execute(postRequest);
//
//                if (response.getStatusLine().getStatusCode() != 200) {
//                    return null;
//                }
//                
//
//                return readResponse(response.getEntity().getContent());
//
//
//            } catch (MalformedURLException e) {
//                logger.error("MalformedURLException" + e.getMessage());
//
//            } catch (IOException e) {
//
//                logger.error("IOException" + e.getMessage());
//                
//            
//            } finally {
//            
//                //httpClient.getConnectionManager().shutdown();
//            	
//            }
//            
//        }
		return null;

	}

}
