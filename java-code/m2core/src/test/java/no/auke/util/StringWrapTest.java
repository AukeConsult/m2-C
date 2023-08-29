package no.auke.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;
import no.auke.util.StringUtil;

public class StringWrapTest extends TestCase {

	public void testMe()
	{
		String test = "22:00:07.656 DEBUG no.auke.m2.core.NetSpaceService - register conversation demo 1 \r\n df560d69-f686-4b04-acf0-9bd900fd4e5a";
		for(String s : StringUtil.wordWrap(test, 20))
		{
			System.out.println(s);
		}
	}
	
	public void testMe2()
	{
		long now =  System.currentTimeMillis();
		msgs.put("1", new simMsg("1", now - 30*1000)); //since 30 secs
		msgs.put("2", new simMsg("2", now- 25*1000)); //since 25 secs
		msgs.put("3", new simMsg("3", now - 20*1000)); //20
		msgs.put("4", new simMsg("4", now - 15*1000));//15
		msgs.put("5", new simMsg("5", now - 10*1000)); //10
		msgs.put("6", new simMsg("6", now - 5*1000));//5
		
		clearHistory(now - 25*1000);
		
		for(String id : msgs.keySet())
		{
			System.out.println(id);
		}
	}
	
	
	public class simMsg
	{
		public String msgId="";
		public long time=0;
		
		public simMsg(String msgid, long time)
		{
			this.msgId = msgid;
			this.time = time;
		}
	}
	
	Map<String, simMsg> msgs = new LinkedHashMap<String, simMsg>();
	
	void clearHistory(long fromTime) {
		//find the point first
		if(!msgs.keySet().iterator().hasNext()) return;
		String messageIdAtHead = msgs.keySet().iterator().next();
		if(fromTime >= msgs.get(messageIdAtHead).time)
		{
			msgs.remove(messageIdAtHead);
			clearHistory(fromTime);
		}
		else return ;		
	}
	public void testNew() {
		
		Date myDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String invoice = "NEW-" + dateFormat.format(myDate) + "-10";
		System.out.println(invoice);
		
		NumberFormat formatter = new DecimalFormat("0000");
		String number = formatter.format(1234567);
		System.out.println(number);
		
		String [] parts = invoice.split("-");
		String date = parts[1];
		String n = parts[2];
		System.out.println(date);
		System.out.println(n);
		
	}
	
	public void testMe4(){
		FileUtil.createDirectory("D:\\testMe\\please");
	}
}
