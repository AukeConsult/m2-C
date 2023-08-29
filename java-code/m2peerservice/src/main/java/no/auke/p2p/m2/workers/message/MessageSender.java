/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011-2021 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */

package no.auke.p2p.m2.workers.message;

import no.auke.p2p.m2.NameSpace;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.p2p.m2.workers.ComChannel;

public class MessageSender {

	public ComChannel getChannel() {return getNameSpace().getMainServ().getChannel();}
	private NameSpace namespace;
	public NameSpace getNameSpace() {return namespace;}

	public MessageSender(NameSpace namespace) {this.namespace = namespace;}
	
	public boolean UDPSendEncrypt(MessageHeader message) {return getChannel().UDPSendEncrypt(message,getNameSpace().getId());}		
	public boolean UDPSendEncrypt_Data(MessageHeader message) {return getChannel().UDPSendEncrypt_Data(message,getNameSpace().getId());}	
	public boolean UDPSendEncrypt_Stream(MessageHeader message) {return getChannel().UDPSendEncrypt_Stream(message,getNameSpace().getId());}
	public boolean UDPSendEncrypt_Reply_Data(MessageHeader message) {return getChannel().UDPSendEncrypt_Reply_Data(message,getNameSpace().getId());}
		
}
