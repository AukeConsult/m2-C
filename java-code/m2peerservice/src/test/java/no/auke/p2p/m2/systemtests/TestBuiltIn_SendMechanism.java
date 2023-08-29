package no.auke.p2p.m2.systemtests;

import java.util.Arrays;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import no.auke.p2p.m2.NameSpace;
import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.Socket.Packet;
import no.auke.p2p.m2.SocketRetStatus;
import no.auke.p2p.m2.sockets.messages.MsgShort;

//A sends a message to B
//A detects if B has received any messages
class TestBuiltIn_SendMechanism implements Runnable, ITestSend {

	NameSpace clientA;
	NameSpace clientB;

	final Socket socketA;
	final Socket socketB;

	final String testmessage;
	public String getTestMessage() {return testmessage;}

	@Override
	public String getSocketStatus() {return "A " + socketA.getSocketStatus() + " B " + socketB.getSocketStatus();}

	int messagesize=0;
    public ArrayBlockingQueue<String> errors = new ArrayBlockingQueue<String>(10000);
     
    public ArrayBlockingQueue<String> getErrors() {return errors;}

	@Override
	public int getTotLen() {return totlen.get();}
	@Override
	public int getTotErrors() {return errors.size();}
	@Override
	public int getTotRecieved() {return totrecieved.get();}
	@Override
	public int getTotMissing() {return nummessages.get() - totrecieved.get();}
	@Override
	public int getTotMessages() {return nummessages.get();}		
	@Override
	public boolean getFinish() {return finish.get();}

	final AtomicInteger totlen = new AtomicInteger();
	final AtomicInteger totrecieved = new AtomicInteger();
	final AtomicInteger nummessages = new AtomicInteger();

	final AtomicBoolean finish = new AtomicBoolean();

	public Queue<MsgShort> queue  = new LinkedBlockingQueue<MsgShort>();

	public TestBuiltIn_SendMechanism(NameSpace clientA, NameSpace clientB, String testmessage, int port, int nummessages, int messagesize) {
		
		// System.out.println("start " + testmessage + " numMsgs " + String.valueOf(nummessages));

		this.clientA = clientA;
		this.clientB = clientB;
		socketA = clientA.openSocket(port);
		socketB = clientB.openSocket(port);
		this.nummessages.set(nummessages);
		this.testmessage = testmessage;
		this.messagesize=messagesize;

		finish.set(false);
				
		Random rnd = new Random();
		for (int i = 0; i < this.nummessages.get(); i++) {
			// init message
			int len = rnd.nextInt(messagesize) + 5000;
			byte[] add = new byte[len];
			rnd.nextBytes(add);
			
			totlen.addAndGet(add.length);
			MsgShort sendMessage = new MsgShort(clientA.getClientid(),clientB.getClientid(), add);
			queue.offer(sendMessage);
		}

	}

	public int getSpeedA() {return (int)socketA.getNameSpace().getMainServ().getStat(clientB.getMainServ().getLocaladdress()).getRealSpeed();}
	public int getSpeedB() {return (int)socketB.getNameSpace().getMainServ().getStat(clientA.getMainServ().getLocaladdress()).getRealSpeed();}

	@Override
	public void run() {

		finish.set(false);
		long start = System.currentTimeMillis();
		while(queue.size()>0) {
			MsgShort sendMessage = queue.peek();
			//A send
			SocketRetStatus ret = socketA.send(sendMessage.getTo(), sendMessage.getBytes());			
			if (ret.isOk()) {				
				//B receive
				MsgShort gotMessage = hasReceived(socketB, sendMessage);
				if(gotMessage!=null) {
					if(Arrays.equals(sendMessage.getData(), gotMessage.getData())) {
						totrecieved.incrementAndGet();
					}					
					queue.poll(); //ok, the other end has received the message, we can remove this message now
				}
			} else {
				//System.out.println(testmessage + " sendA failed " + ret.getLastMessage());
			}
		}

		double time = ((System.currentTimeMillis() - start) / 1000.00);

		System.out.println("Ended " + testmessage + 
				" sent: " + String.valueOf(nummessages.get()) + 
				" received: " + String.valueOf(totrecieved.get()) + 
				" MB " + String.valueOf(totlen.get()/1000000.00) + 
				" time " + String.valueOf(time) + 
				" Speed A " + getSpeedA() + 
				" Speed B " + getSpeedB());

		// System.out.println(getTestName() + " " + getSocketStatus());
		finish.set(true);

	}

	protected MsgShort hasReceived(Socket socket, MsgShort originMsg) {
		Packet buff = socket.readBuffer(1000);
		if (buff!=null && buff.getData() != null) {
			// Got a message
			MsgShort msg2 = new MsgShort(buff.getData());
			return msg2;
		}
		return null;
	}

	@Override
	public void closeSockets() {
		socketA.close();
		socketB.close();
		
	}
}	