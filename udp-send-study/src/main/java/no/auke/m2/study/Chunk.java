package no.auke.m2.study;

public class Chunk {

    public int msgId;
    public int chunkNum;
    public int totalNum;

    public Chunk(int msgId, int chunkNum, int totalNum) {
        this.msgId=msgId;
        this.chunkNum=chunkNum;
        this.totalNum=totalNum;
    }
}
