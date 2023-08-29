//package no.auke.http;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.MalformedURLException;
//
//import org.apache.http.HttpResponse;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.InputStreamEntity;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
//import org.apache.http.params.CoreConnectionPNames;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//@SuppressWarnings("deprecation")
//public class HttpSession {
//	    
//	private static final Logger logger = LoggerFactory.getLogger(HttpSession.class);		
//	String scheme="http://";
//	
//	private int timeout = 3000;
//    private int buffersize = 8 * 1024;
//	
//	public HttpSession(){}
//    
//	public HttpSession(int timeout, int buffersize){
//        
//        this.timeout = timeout;
//        this.buffersize = buffersize;
//        
//    }
//
//    private byte[] readResponse(InputStream content) throws IOException {
//
//		// read and decode
//		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//		byte[] inbuffer = new byte[4096];
//		int bytes=0;
//		while((bytes=content.read(inbuffer,0,4096))!=-1){
//			buffer.write(inbuffer, 0,bytes);
//		}
//		return buffer.toByteArray();			
//	}	
//	
// 	public byte[] postAndGet(String url, byte[] exchangeContent)
//	{
//		return postAndGet(url,exchangeContent, 0);
//	}
// 	
//	public byte[] postAndGet(String url, byte[] exchangeContent, final int retryCount)
//	{
//		
//	    @SuppressWarnings("resource")
//		DefaultHttpClient httpClient = new DefaultHttpClient();
//		
//        httpClient.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, timeout)
//        .setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 1000)
//        .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, buffersize)
//        .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true);		
//		
//		try 
//		{
//
//			if(!url.startsWith(scheme)) {
//				url = scheme + url;
//			}
//
//			HttpPost postRequest = new HttpPost(url);
//			InputStreamEntity dataEntity = new InputStreamEntity(new ByteArrayInputStream(exchangeContent), -1);
//			postRequest.setEntity(dataEntity);
//			httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(retryCount, false));
//
//			HttpResponse response = httpClient.execute(postRequest);
//
//			if (response.getStatusLine().getStatusCode() != 200) {
//				return null;
//			}
//
//			return readResponse(response.getEntity().getContent());
//
//		} 
//		catch (MalformedURLException e) 
//		{
//			logger.error("MalformedURLException" + e.getMessage());
//
//		} catch (IOException e) 
//		{
//
//			logger.error("IOException" + e.getMessage());
//		}
//		finally
//		{
//			// httpClient.getConnectionManager().shutdown();
//		}
//		return null;
//
//	}
//
//}
