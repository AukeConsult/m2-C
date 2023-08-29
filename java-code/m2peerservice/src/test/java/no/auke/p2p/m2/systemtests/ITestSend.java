package no.auke.p2p.m2.systemtests;

import java.util.concurrent.ArrayBlockingQueue;

interface ITestSend {

	 public ArrayBlockingQueue<String> getErrors();
	 
	String getSocketStatus();
	int getTotLen();
	int getTotErrors();
	int getTotRecieved();
	int getTotMissing();
	int getTotMessages();
	boolean getFinish();
	void closeSockets();
	String getTestMessage();

}