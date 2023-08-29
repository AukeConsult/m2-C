/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.message.attribute;
public class MessageAttributeException extends Exception {
	private static final long serialVersionUID = 3258131345099404850L;
	public MessageAttributeException(String mesg) {
		super(mesg);
	}
}