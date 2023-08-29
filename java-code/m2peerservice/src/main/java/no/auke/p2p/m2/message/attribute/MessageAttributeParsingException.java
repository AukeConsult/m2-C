/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.message.attribute;
public class MessageAttributeParsingException extends MessageAttributeException {
	private static final long serialVersionUID = 3258409534426263605L;
	public MessageAttributeParsingException(String mesg) {
		super(mesg);
	}
}