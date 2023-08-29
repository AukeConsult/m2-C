package no.auke.p2p.m2.systemtests;

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import no.auke.p2p.m2.SendTask;
import no.auke.p2p.m2.NameSpace;
import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.Socket.Packet;
import no.auke.p2p.m2.SocketRetStatus;
import no.auke.p2p.m2.sockets.messages.MsgShort;

//A sends a message to B with the help of ISendTask
//A detects if B has received any messages
class TestSendWith_ISendTask implements Runnable, ITestSend {

	NameSpace clientA;
	NameSpace clientB;

	final Socket socketA;
	final Socket socketB;
	final AtomicInteger totlen = new AtomicInteger();
	final AtomicInteger nummessages = new AtomicInteger();

	final String testmessage;
	boolean isrunning=true;
	int messagesize=0;
	final public ArrayBlockingQueue<String> errors = new ArrayBlockingQueue<String>(10000);

	public int getTotLen() {return totlen.get();}

	public ArrayBlockingQueue<String> getErrors() {return errors;}
	public int getTotRecieved() {return totrecieved.get();}

	public int getTotMissing() {return nummessages.get() - totrecieved.get();}
	public int getTotMessages() {return nummessages.get();}	
	final AtomicInteger totrecieved = new AtomicInteger();
	final AtomicBoolean finish = new AtomicBoolean();
	public boolean getFinish() {return finish.get();}

	public TestSendWith_ISendTask(NameSpace clientA, NameSpace clientB, String testmessage, int port, int nummessages, int messagesize) {
		this.clientA = clientA;
		this.clientB = clientB;
		socketA = clientA.openSocket(port);
		socketB = clientB.openSocket(port);
		this.nummessages.set(nummessages);
		this.testmessage = testmessage;
		this.messagesize=messagesize;
		finish.set(false);
	}

	class sendTask extends SendTask {
		MsgShort thismessage;
		public sendTask(MsgShort thismessage) {
			this.thismessage=thismessage;
		}

		@Override
		public void onError(SocketRetStatus ret) {}			

		@Override
		public void onSentComplete(SocketRetStatus ret) {
			if (ret.isOk()) {
				System.out.println("> " + testmessage + " sent " + String.valueOf(thismessage.getBytes().length));
				// 2. Client B waiting for answer
				MsgShort gotMessage = hasReceived(socketB, thismessage.getMessageId());
				if (gotMessage != null) {
					totrecieved.incrementAndGet();
				} 

			} else {
				errors.add(testmessage + " sendA failed " + ret.getLastMessage());
			}

		}

		private MsgShort hasReceived(Socket socket, String msgId) {

			Packet buff = socket.readBuffer(500);
			if (buff != null) {
				// Got a message
				MsgShort msg = new MsgShort(buff.getData());
				return msg;
			}
			return null;
		}
	}

	public String getSocketStatus() {return "A " + socketA.getSocketStatus() + " B " + socketB.getSocketStatus();}

	@Override
	public void run() {

		long start = System.currentTimeMillis();
		System.out.println("start " + testmessage + " number of messages " + String.valueOf(nummessages));

		Queue<MsgShort> queue = new LinkedBlockingQueue<MsgShort>();
		Random rnd = new Random();
		for (int i = 0; i < nummessages.get(); i++) {

			int len = rnd.nextInt(messagesize) + 5000;
			byte[] add = new byte[len];
			rnd.nextBytes(add);
			totlen.addAndGet(add.length);
			MsgShort sendMessage = new MsgShort(clientA.getClientid(),clientB.getClientid(), add);
			queue.offer(sendMessage);
		}


		while(queue.size()>0) {

			MsgShort sendMessage = queue.peek();
			SocketRetStatus ret = socketA.send(sendMessage.getTo(), sendMessage.getBytes(), new sendTask(sendMessage));
			if (ret.isOk()) {
				queue.poll();
			} else {
				errors.add(testmessage + " sendA failed " + ret.getLastMessage() + " message ");
			}
		}

		double time = ((System.currentTimeMillis() - start) / 1000.00);
		System.out.println("end " + testmessage + " num of message: " + 
				String.valueOf(nummessages) + 
				" total MB " + String.valueOf(totlen.get()/1000000.00) + 
				" time " + String.valueOf(time));

		isrunning=false;
		finish.set(true);

	}

	@Override
	public int getTotErrors() {return errors.size();}

	@Override
	public void closeSockets() {
		socketA.close();
		socketB.close();

	}

	@Override
	public String getTestMessage() {return testmessage;}


}	
