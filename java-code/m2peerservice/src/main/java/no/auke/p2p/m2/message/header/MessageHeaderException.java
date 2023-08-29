/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.message.header;
public class MessageHeaderException extends Exception {
	private static final long serialVersionUID = 3689066248944103737L;
	public MessageHeaderException(String mesg) {
		super(mesg);
	}
}