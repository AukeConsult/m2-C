package no.auke.m2.study;


public class Udp {

    enum Msgtype {
        empty,
        sendChunk,
        sendFailed,
        sendComplete,
        AskComplete
    }

    public long timeStamp;
    public long msgId;
    public Msgtype type = Msgtype.empty;
    public Chunk chunk;

    public Udp(long msgId, Chunk chunk) {
        this.timeStamp=System.currentTimeMillis();
        this.msgId=msgId;
        this.chunk=chunk;
        this.type=Msgtype.sendChunk;
    }

}
