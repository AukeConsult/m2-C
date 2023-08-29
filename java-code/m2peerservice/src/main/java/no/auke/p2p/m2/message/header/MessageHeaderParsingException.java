/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.message.header;
public class MessageHeaderParsingException extends MessageHeaderException {
	private static final long serialVersionUID = 3544393617029607478L;
	public MessageHeaderParsingException(String mesg) {
		super(mesg);
	}
}