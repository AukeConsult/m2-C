/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.sockets.udp;
public class SendCalcPolecy2 {
	// % reduse speed
	public static double DECLINE_FACTOR = 0.01;
	// % rise speed
	public static double RISE_FACTOR = 0.2;
	public static int CALC_FREQUENCE = 100;
	public static int ABORT_DELAY_FACTOR = 5;
	public static int REPLY_DELAY_FACTOR = 3;
	public static int NUM_HISTORY = 10;
}
