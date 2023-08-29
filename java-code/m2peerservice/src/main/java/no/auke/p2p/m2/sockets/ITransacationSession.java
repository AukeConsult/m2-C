package no.auke.p2p.m2.sockets;

import no.auke.p2p.m2.SocketRetStatus;
import no.auke.p2p.m2.sockets.messages.MsgFunctionResult;

public interface ITransacationSession {
	public ISocket getSocket();
	public String getToClientId();
	public String getSessionID();
	public void gotReply(MsgFunctionResult retmsg);
	public SocketRetStatus fire(String function, byte[] data);
	// LHA: implement find the other peer
	public SocketRetStatus find();
	public SocketRetStatus get(String function, byte[] data);
	public void close();
	public void stopSession();
	public boolean isOpen();
}