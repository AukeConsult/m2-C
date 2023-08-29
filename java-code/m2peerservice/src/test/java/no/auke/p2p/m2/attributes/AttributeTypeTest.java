package no.auke.p2p.m2.attributes;

import junit.framework.TestCase;
import no.auke.p2p.m2.message.attribute.Deviceid;
import no.auke.p2p.m2.message.attribute.LocalAddress;
import no.auke.p2p.m2.message.attribute.MapFile;
import no.auke.p2p.m2.message.attribute.MessageAttributeInterface.MessageAttributeType;
import no.auke.p2p.m2.message.attribute.PeerRemoteId;
import no.auke.p2p.m2.message.attribute.PublicAddress;
import no.auke.p2p.m2.message.attribute.PeerLocalId;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.message.header.MessageHeaderInterface.MessageHeaderType;

public class AttributeTypeTest extends TestCase {
	
	public void testDeviceId() throws Exception
	{
		String pwd = "123456789";
		MessageHeader a = new MessageHeader(MessageHeaderType.PingAlive);
		a.addMessageAttribute(new Deviceid(pwd));
		MessageHeader b = MessageHeader.parseHeader(a.getBytes());
		b.parseAttributes(a.getBytes());
		Deviceid deviceid = (Deviceid) b.getMessageAttribute(MessageAttributeType.Deviceid);
		assertTrue(pwd.equals(deviceid.getPassword()));
		
		
	}
	
	public void testUserName() throws Exception
	{
		String name = "123456789";
		MessageHeader a = new MessageHeader(MessageHeaderType.PingAlive);
		a.addMessageAttribute(new PeerLocalId(name));
		MessageHeader b = MessageHeader.parseHeader(a.getBytes());
		b.parseAttributes(a.getBytes());
		PeerLocalId name2 = (PeerLocalId) b.getMessageAttribute(MessageAttributeType.Peer_Local_Id);
		assertTrue(name.equals(name2.getUsername()));
		
		
	}
	
	public void testPeerUserName() throws Exception
	{
		String name = "123456789";
		MessageHeader a = new MessageHeader(MessageHeaderType.PingAlive);
		a.addMessageAttribute(new PeerRemoteId(name));
		MessageHeader b = MessageHeader.parseHeader(a.getBytes());
		b.parseAttributes(a.getBytes());
		PeerRemoteId name2 = (PeerRemoteId) b.getMessageAttribute(MessageAttributeType.Peer_Remote_Id);
		assertTrue(name.equals(name2.getId()));
		
		
	}
	
	public void testPublicAddress() throws Exception
	{
		PublicAddress add = new PublicAddress();
		add.setAddress("175.34.59.202", 1000);
		MessageHeader a = new MessageHeader(MessageHeaderType.PingAlive);
		a.addMessageAttribute(add);
		MessageHeader b = MessageHeader.parseHeader(a.getBytes());
		b.parseAttributes(a.getBytes());
		PublicAddress add2 = (PublicAddress) b.getMessageAttribute(MessageAttributeType.PublicAddress);
		assertTrue(add.getAddress().equals(add2.getAddress()));
		assertTrue(1000== add2.getPort());
	}
	
	public void testLocalAddress() throws Exception
	{
		LocalAddress add = new LocalAddress();
		add.setAddress("127.0.0.1", 1000);
		MessageHeader a = new MessageHeader(MessageHeaderType.PingAlive);
		a.addMessageAttribute(add);
		MessageHeader b = MessageHeader.parseHeader(a.getBytes());
		b.parseAttributes(a.getBytes());
		LocalAddress add2 = (LocalAddress) b.getMessageAttribute(MessageAttributeType.LocalAddress);
		assertTrue(add.getAddress().equals(add2.getAddress()));
		assertTrue(1000== add2.getPort());
	}
	
	public void testMapFile() throws Exception
	{
		MapFile map = new MapFile();
		map.setFiletext("xxx");
		map.setFunction(10);
		map.setVersion(100);
		
		MessageHeader a = new MessageHeader(MessageHeaderType.PingAlive);
		a.addMessageAttribute(map);
		MessageHeader b = MessageHeader.parseHeader(a.getBytes());
		b.parseAttributes(a.getBytes());
		MapFile map2 = (MapFile) b.getMessageAttribute(MessageAttributeType.MapFile);
		assertTrue(map.getFiletext().equals(map2.getFiletext()));
		assertTrue(map.getFunction() == map2.getFunction());
		assertTrue(map.getVersion() == map2.getVersion());
		
	}
	
	
	public void testAllInOneHeader() throws Exception
	{
		MessageHeader a = new MessageHeader(MessageHeaderType.PingAlive);
		String pwd = "123456789";
		a.addMessageAttribute(new Deviceid(pwd));
		
		String name = "123456789";
		a.addMessageAttribute(new PeerLocalId(name));
		a.addMessageAttribute(new PeerRemoteId(name));
		
		PublicAddress add = new PublicAddress();
		add.setAddress("175.34.59.202", 1000);
		a.addMessageAttribute(add);
		
		LocalAddress ladd = new LocalAddress();
		ladd.setAddress("127.0.0.1", 1000);
		a.addMessageAttribute(ladd);
		

		MapFile map = new MapFile();
		map.setFiletext("xxx");
		map.setFunction(10);
		map.setVersion(100);
		a.addMessageAttribute(map);
		
		//parse
		
		MessageHeader b = MessageHeader.parseHeader(a.getBytes());
		b.parseAttributes(a.getBytes());
		Deviceid deviceid = (Deviceid) b.getMessageAttribute(MessageAttributeType.Deviceid);
		assertTrue(pwd.equals(deviceid.getPassword()));
		
		PeerRemoteId name2 = (PeerRemoteId) b.getMessageAttribute(MessageAttributeType.Peer_Remote_Id);
		assertTrue(name.equals(name2.getId()));
		
		PeerLocalId name3 = (PeerLocalId) b.getMessageAttribute(MessageAttributeType.Peer_Local_Id);
		assertTrue(name.equals(name3.getUsername()));
		
		PublicAddress add2 = (PublicAddress) b.getMessageAttribute(MessageAttributeType.PublicAddress);
		assertTrue(add.getAddress().equals(add2.getAddress()));
		assertTrue(1000== add2.getPort());
		
		LocalAddress ladd2 = (LocalAddress) b.getMessageAttribute(MessageAttributeType.LocalAddress);
		assertTrue(ladd.getAddress().equals(ladd2.getAddress()));
		assertTrue(1000== add2.getPort());
		
		
		MapFile map2 = (MapFile) b.getMessageAttribute(MessageAttributeType.MapFile);
		assertTrue(map.getFiletext().equals(map2.getFiletext()));
		assertTrue(map.getFunction() == map2.getFunction());
		assertTrue(map.getVersion() == map2.getVersion());
		
		
		
	}

}
