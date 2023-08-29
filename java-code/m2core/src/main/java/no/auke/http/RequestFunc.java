package no.auke.http;

import java.util.List;

import no.auke.util.ByteUtil;
import no.auke.util.StringConv;

public class RequestFunc {
	
	
	
	private String function="";
	private byte[] content;
	
	public RequestFunc(byte[] data)
	{
		List<byte[]> parts = ByteUtil.splitDynamicBytes(data);
		byte[] functionPart = parts.get(0);
		byte[] contentPart = parts.size() > 1 ? parts.get(1) : null;
		if(functionPart!=null) 
		{
			setFunction(StringConv.UTF8(functionPart));
		}
		if(contentPart!=null)
		{
			setContent(contentPart);
		}
	}
	
	
	public RequestFunc(String function, byte[] data)
	{
		this.setFunction(function);
		this.setContent(data);
	}
	
	public byte[] getBytes()
	{
		return ByteUtil.mergeDynamicBytesWithLength(StringConv.getBytes(this.getFunction()), getContent());
	}


	/**
	 * @return the function
	 */
	public String getFunction() {
		return function;
	}


	/**
	 * @param function the function to set
	 */
	public void setFunction(String function) {
		this.function = function;
	}


	/**
	 * @return the content
	 */
	public byte[] getContent() {
		return content;
	}


	/**
	 * @param content the content to set
	 */
	public void setContent(byte[] content) {
		this.content = content;
	}
	
	
	

}
