package no.auke.http;

import java.util.List;

import no.auke.util.ByteUtil;

public class ResponseFunc {
	
	private byte[] content;
	private int returnCode;
	
	public ResponseFunc(byte[] packedData)
	{
		List<byte[]> parts = ByteUtil.splitDynamicBytes(packedData);
		byte[] code = parts.get(0);
		byte[] contentPart = parts.size() > 1 ? parts.get(1) : null;
		if(code!=null) 
		{
			returnCode = ByteUtil.getInt(code);
		}
		if(contentPart!=null)
		{
			setContent(contentPart);
		}
	}
	
	public ResponseFunc(int code, byte[] reponseContent)
	{
		this.returnCode = code;
		this.content = reponseContent;
		
	}
	
	public byte[] getBytes()
	{
		return ByteUtil.mergeDynamicBytesWithLength(ByteUtil.getBytes(this.getReturnCode()), this.getContent());
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

	/**
	 * @return the licenseCode
	 */
	public int getReturnCode() {
		return returnCode;
	}

	/**
	 * @param licenseCode the licenseCode to set
	 */
	public void setReturnCode(int code) {
		this.returnCode = code;
	}
	
	

}
