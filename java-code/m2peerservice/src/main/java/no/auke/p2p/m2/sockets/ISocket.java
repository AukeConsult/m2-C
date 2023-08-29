package no.auke.p2p.m2.sockets;

import no.auke.p2p.m2.NameSpace;
import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.sockets.messages.MsgFunctionResult;

public interface ISocket {
	int getPort();
	public Socket getSocket();
	public void setSocket(Socket socket);
	// SocketRetStatus findUser(String toClient);
	// SocketRetStatus send(String toClient, int toPort, String sessionid,
	// byte[] data);
	// boolean isRunning();
	void close();
	public String getClientId();
	public void gotReply(MsgFunctionResult retmsg);
	public boolean isClosed();
	public boolean isConnected();
	public NameSpace getNameSpace();
	public boolean checkIncomming(String toClient);
	public int getNumTrial();
	public int getTimeout();
	public void setTimeout(int timeout);
	public void setNumTrial(int timeout);
}
