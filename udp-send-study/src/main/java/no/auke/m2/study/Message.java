package no.auke.m2.study;

import java.util.ArrayList;
import java.util.List;

public class Message {

    public Peer peer;
    public int size;
    public long msgId = App.newId();

    public List<Chunk> chunks = new ArrayList<>();
    public List<Chunk> sent_chunks = new ArrayList<>();
    public List<Chunk> confirm_chunks = new ArrayList<>();

    public Message(Peer peer, int size) {
        this.peer=peer;
        this.size=size;
    }

    public void sendAll() {
        chunks.forEach(c ->
                peer.outgoing.add(new Udp(msgId,c))
        );
    }

}
