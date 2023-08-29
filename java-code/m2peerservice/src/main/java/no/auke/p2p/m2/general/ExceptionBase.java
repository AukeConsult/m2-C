/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.general;
public class ExceptionBase extends Exception {
	private static final long serialVersionUID = -7704391359025317515L;
	private ReturMessageTypes errcode;
	public ReturMessageTypes getErrcode() {
		return errcode;
	}
	private Exception exception;
	public Exception getException() {
		return exception;
	}
	public ExceptionBase(ReturMessageTypes ExceptionTypes, String msg) {
		super(msg);
		errcode = ExceptionTypes;
	}
	public ExceptionBase(ReturMessageTypes ExceptionTypes, String msg, Exception Exception) {
		super(msg + ":" + Exception.getClass().getName() + ":" + Exception.getMessage());
		errcode = ExceptionTypes;
		exception = Exception;
	}
	public ExceptionBase(ReturMessageTypes ExceptionTypes, Exception Exception) {
		super(Exception.getClass().getName() + ":" + Exception.getMessage());
		errcode = ExceptionTypes;
		exception = Exception;
	}
}
