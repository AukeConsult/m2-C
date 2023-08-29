package no.auke.p2p.m2;

import static org.mockito.Matchers.*;
//import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
//import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import no.auke.p2p.m2.NameSpace;
import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.general.IListener;
import no.auke.p2p.m2.general.ReturMessageTypes;
import no.auke.p2p.m2.message.attribute.DataPacket;
import no.auke.p2p.m2.message.attribute.DataReplyPacket;
import no.auke.p2p.m2.message.header.MessageHeader;
import no.auke.m2.task.TaskMonitor;
import no.auke.p2p.m2.InitVar;
import no.auke.p2p.m2.sockets.udp.SocketBufferOut;
import no.auke.p2p.m2.workers.ComChannel;
import no.auke.p2p.m2.workers.PeerSession;
import no.auke.p2p.m2.workers.connect.PeerSessionEncrypt;
import no.auke.p2p.m2.workers.keepalive.NetAddress;
import no.auke.util.ByteUtil;
import junit.framework.TestCase;

//TODO: LHA: make tests 
//TODO: check this again, this never works
public class SocketTest extends TestCase {
	public void test_do_nothing_please_check_this_socket_test_again() {}
	/*
	 * Random rnd = new Random();
	 * 
	 * int port=10;
	 * 
	 * PeerServer peerservice = mock(PeerServer.class); PeerPeerAgent peeragent
	 * = mock(PeerPeerAgent.class); SocketRetStatus ret = spy(new
	 * SocketRetStatus());
	 * 
	 * 
	 * ComChannel4 channel = mock(ComChannel4.class); IListener listen =
	 * mock(IListener.class);
	 * 
	 * Socket socket=null;
	 * 
	 * ExecutorService executor = Executors.newCachedThreadPool(); TaskMonitor
	 * monitor = new TaskMonitor(executor,10,"");
	 * 
	 * NetAddress address;
	 * 
	 * public void setUp() {
	 * 
	 * when(peerservice.getChannel()).thenReturn(channel);
	 * when(peerservice.getListen()).thenReturn(listen);
	 * when(peerservice.isRunning()).thenReturn(true);
	 * when(peerservice.isConnected()).thenReturn(true);
	 * 
	 * when(peeragent.getSessionEncrypt()).thenReturn(spy(new
	 * PeerSessionEncrypt(peeragent)));
	 * when(peeragent.getMainserv()).thenReturn(peerservice);
	 * 
	 * when(ret.getLastRetcode()).thenReturn(ReturMessageTypes.ok);
	 * ret.setPeerAgent(peeragent);
	 * 
	 * when(peerservice.openPeer((String)any(),anyBoolean())).thenReturn(ret);
	 * when(peerservice.getExecutor(anyString())).thenReturn(executor);
	 * when(peerservice.getMonitors().getConnectMonitor()).thenReturn(monitor);
	 * 
	 * when(ret.getPeerAgent().findAndConnect()).thenReturn(true);
	 * 
	 * address = new NetAddress("127.0.0.1",200);
	 * when(ret.getPeerAgent().getPeerAddress()).thenReturn(address);
	 * 
	 * SocketBufferOutgoing2 outbuffer = mock(SocketBufferOutgoing2.class);
	 * when(outbuffer.send()).thenReturn(true);
	 * 
	 * socket = spy(new Socket(port,peerservice, outbuffer)); try {
	 * doReturn(outbuffer
	 * ).when(socket).getOutputBuffer((SocketRetStatus)anyObject(),
	 * (PeerPeerAgent)anyObject(), anyInt(), (byte[])anyObject()); } catch
	 * (Exception e) { // TODO Auto-generated catch block e.printStackTrace(); }
	 * 
	 * 
	 * }
	 * 
	 * public void test_open() {
	 * 
	 * assertEquals(port,socket.getPort());
	 * assertEquals(peerservice,socket.getService());
	 * assertFalse("busy",socket.isBusy());
	 * 
	 * }
	 * 
	 * public void test_send_closed() {
	 * 
	 * Socket socket = new Socket(port,peerservice); socket.close();
	 * assertFalse(socket.send("xxxx", port, new byte[0]).isOk());
	 * 
	 * }
	 * 
	 * public void test_send_diconnected() {
	 * 
	 * when(peerservice.isConnected()).thenReturn(false);
	 * assertFalse(socket.send("xxxx", port, new byte[0]).isOk());
	 * 
	 * }
	 * 
	 * public void test_send() {
	 * 
	 * byte[] data = new byte[10000]; rnd.nextBytes(data);
	 * assertTrue(socket.send("xxxx", port, data).isOk());
	 * 
	 * }
	 * 
	 * public void test_send_no_data() {
	 * 
	 * byte[] data = new byte[0];
	 * 
	 * assertFalse(socket.send("xxxx", port, data).isOk());
	 * assertTrue(socket.send("xxxx", port,
	 * data).getLastRetcode()==ReturMessageTypes.empty_data);
	 * 
	 * }
	 * 
	 * private void fill_socket_incoming(Socket socket, int messagenum, int
	 * datasize){
	 * 
	 * NetAddress address = mock(NetAddress.class);
	 * 
	 * byte[] data = new byte[datasize+1]; rnd.nextBytes(data);
	 * 
	 * List<byte[]> splits =
	 * ByteUtil.splitBytesWithFixedLength(data,InitVar.PACKET_SIZE-46);
	 * 
	 * // add new packets Map<String,DataPacket> packlist = new
	 * HashMap<String,DataPacket>();
	 * 
	 * for (int index = 0; index < splits.size(); index++) {
	 * 
	 * DataPacket packet = new DataPacket(); packet.setPort(port);
	 * packet.setNumber(index + 1); packet.setTotal(splits.size());
	 * packet.setData(splits.get(index));
	 * 
	 * packet.setChunkSize(8); packet.setChunkNumber(((packet.getNumber() - 1) /
	 * 8) + 1);
	 * 
	 * packlist.put(UUID.randomUUID().toString(), packet); }
	 * 
	 * int cnt=0; int length=0;
	 * 
	 * // sort to send in random order List<String> set = new
	 * LinkedList<String>(packlist.keySet()); Collections.sort(set); for (String
	 * key : set) {
	 * 
	 * cnt++;
	 * 
	 * DataPacket packet = packlist.get(key);
	 * 
	 * length += packet.getData().length;
	 * 
	 * // send data MessageHeader receiveMH = new
	 * MessageHeader(MessageHeader.MessageHeaderType.Data);
	 * receiveMH.setTransactionID(messagenum);
	 * receiveMH.addMessageAttribute(packet); receiveMH.setAddress(address);
	 * 
	 * socket.gotData(ret.getPeerAgent(), receiveMH, packet);
	 * 
	 * assertNotNull("message exists " + String.valueOf(messagenum),
	 * socket.getIncomingBuffer(messagenum));
	 * 
	 * 
	 * }
	 * 
	 * assertEquals("same number of packets1 " + String.valueOf(messagenum),
	 * packlist.size(), socket.getIncomingBuffer(messagenum).getNumPackets());
	 * assertEquals("same number of packets2 " + String.valueOf(messagenum),
	 * cnt, socket.getIncomingBuffer(messagenum).getNumPackets());
	 * assertEquals("same lenght of packets  " + String.valueOf(messagenum),
	 * length, socket.getIncomingBuffer(messagenum).getLength());
	 * assertEquals("same lenght of buffer   " + String.valueOf(messagenum),
	 * length, socket.getIncomingBuffer(messagenum).getBuffer().length);
	 * 
	 * assertEquals("same message " + String.valueOf(messagenum),
	 * socket.getIncomingBuffer(messagenum).getTranactionId(),messagenum);
	 * assertTrue("is closed " +
	 * String.valueOf(messagenum),socket.getIncomingBuffer
	 * (messagenum).isClosed()); assertTrue("same data " +
	 * String.valueOf(messagenum),Arrays.equals(data,
	 * socket.getIncomingBuffer(messagenum).getBuffer()));
	 * 
	 * 
	 * }
	 * 
	 * public void test_getData() {
	 * 
	 * Socket socket = new Socket(port,peerservice); for(int i=0;i<200;i++){
	 * 
	 * fill_socket_incoming(socket,i,rnd.nextInt(100000));
	 * assertNotNull("is in incoming buffer",socket.getIncomingBuffer(i));
	 * assertTrue("is offered",socket.getInbuff().size()==i+1);
	 * 
	 * }
	 * 
	 * }
	 * 
	 * 
	 * public void test_getData_increase() {
	 * 
	 * Socket socket = new Socket(port,peerservice); for(int
	 * i=3000;i<10000;i+=10){
	 * 
	 * fill_socket_incoming(socket,i,i);
	 * assertNotNull("is in incoming buffer",socket.getIncomingBuffer(i)); }
	 * 
	 * }
	 * 
	 * public void test_getData_message_sizes() {
	 * 
	 * for(int x=2000;x>0;x--){
	 * 
	 * Socket socket = new Socket(port,peerservice);
	 * fill_socket_incoming(socket,123223,x);
	 * assertNotNull("is in incoming buffer",socket.getIncomingBuffer(123223));
	 * 
	 * }
	 * 
	 * }
	 * 
	 * public void test_getData_message_big() {
	 * 
	 * for(int x=20;x>0;x--){
	 * 
	 * int port = 2000; Socket socket = new Socket(port,peerservice);
	 * fill_socket_incoming(socket,port,100000+rnd.nextInt(100000));
	 * assertNotNull("is in incoming buffer",socket.getIncomingBuffer(port));
	 * 
	 * }
	 * 
	 * }
	 * 
	 * 
	 * public void test_getData_max_incoming_buffer() {
	 * 
	 * Socket socket = new Socket(port,peerservice); for(int
	 * i=0;i<socket.getMaxIncomming()*i;i++){
	 * 
	 * fill_socket_incoming(socket,100,100); if(i<socket.getMaxIncomming()) {
	 * 
	 * assertFalse("is not busy",socket.isBusy());
	 * 
	 * } else {
	 * 
	 * assertTrue("is busy",socket.isBusy());
	 * 
	 * }
	 * 
	 * }
	 * 
	 * }
	 * 
	 * public void test_gotDataReply() throws Exception {
	 * 
	 * int port=10;
	 * 
	 * Socket socket = new Socket(port,peerservice, 2);
	 * 
	 * for(int i=0;i<3;i++){
	 * 
	 * byte[] data = new byte[rnd.nextInt(10000)]; rnd.nextBytes(data);
	 * 
	 * SocketBufferOutgoing2 buffer = spy(new
	 * SocketBufferOutgoing2(ret.getPeerAgent(), ret, socket, port, data));
	 * 
	 * socket.addOutBuffer(buffer.getTranactionId(), buffer);
	 * 
	 * for(DataPacket packet:buffer.getDatapackets()){
	 * 
	 * MessageHeader datareply = new
	 * MessageHeader(MessageHeader.MessageHeaderType.DataReply);
	 * datareply.setTransactionID(buffer.getTranactionId());
	 * datareply.setAddress(address);
	 * 
	 * // TODO: LHA: implement datareplypacket // if empty data reply = complete
	 * 
	 * DataReplyPacket reply2 = new DataReplyPacket();
	 * reply2.setPort(socket.getPort());
	 * reply2.setChunkNumber(packet.getChunkNumber());
	 * 
	 * datareply.addMessageAttribute(reply2);
	 * 
	 * //assertTrue("find packet ", socket.gotDataReply(datareply, reply2));
	 * 
	 * fail("must be fixed");
	 * 
	 * }
	 * 
	 * // remove message socket.deleteOutBuffer(buffer.getTranactionId());
	 * 
	 * for(DataPacket packet:buffer.getDatapackets()){
	 * 
	 * MessageHeader datareply = new
	 * MessageHeader(MessageHeader.MessageHeaderType.DataReply);
	 * datareply.setTransactionID(buffer.getTranactionId());
	 * datareply.setAddress(address);
	 * 
	 * // TODO: LHA: implement datareplypacket // if empty data reply = complete
	 * 
	 * DataReplyPacket reply2 = new DataReplyPacket();
	 * reply2.setPort(socket.getPort());
	 * reply2.setChunkNumber(packet.getChunkNumber());
	 * 
	 * datareply.addMessageAttribute(reply2);
	 * 
	 * // assertFalse("find packet ", socket.gotDataReply(datareply, reply2));
	 * 
	 * fail("must be fixed");
	 * 
	 * 
	 * }
	 * 
	 * }
	 * 
	 * }
	 */
}
